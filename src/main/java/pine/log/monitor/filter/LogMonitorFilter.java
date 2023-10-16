package pine.log.monitor.filter;

import pine.log.monitor.LogServletUtils;
import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.MonitorLogWrapper;
import pine.log.monitor.engine.DataEngine;
import pine.log.monitor.utils.IPUtis;
import org.apache.http.entity.ContentType;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

@Component
public class LogMonitorFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LogMonitorFilter.class);

    @Autowired(required = false)
    private DataEngine dataEngine;

    @Override
    public int getOrder() {
        return -2000;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        MonitorLog monitorLog = new MonitorLog();
        MonitorLogWrapper monitorLogWrapper = new MonitorLogWrapper(monitorLog);
        try {

            // 请求拦截
            saveRequest(request, monitorLogWrapper);
            // 响应拦截
            ServerHttpResponse response = buildResponse(exchange.getResponse(), result -> {
                saveResponse(exchange.getResponse(), result, monitorLogWrapper);
                FilterCatchMap.consumer(request.getId());
            });

            if (request.getMethod() == null ){
                return chain.filter(exchange.mutate().response(response).build());
            }
            // 拦截参数GET方式
            if (request.getMethod().matches(HttpMethod.GET.toString())){
                String param = request.getURI().getQuery();
                monitorLog.setOperParam(param);
            }

            // 非POST方式
            if (!request.getMethod().matches(HttpMethod.POST.toString())){
                return chain.filter(exchange.mutate().response(response).build());
            }

            String contentType = request.getHeaders().getFirst("Content-Type");
            String contentLength = request.getHeaders().getFirst("Content-Length");
            if (Objects.isNull(contentType) || Objects.isNull(contentLength)){
                return chain.filter(exchange.mutate().response(response).build());
            }

            // 非JSON格式或者contentLength <= 0
            if (!contentType.contains(ContentType.APPLICATION_JSON.getMimeType()) || Long.valueOf(contentLength) <= 0){
                return chain.filter(exchange.mutate().response(response).build());
            }

            // POST方式 & JSON格式
            return DataBufferUtils.join(request.getBody()).flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                    DataBuffer buffer = response.bufferFactory().wrap(bytes);
                    return Mono.just(buffer);
                });
                ServerHttpRequest mutateReq = new ServerHttpRequestDecorator(request) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return cachedFlux;
                    }
                };
                String bodyStr = new String(bytes, StandardCharsets.UTF_8);
                monitorLog.setOperParam(bodyStr);
                return chain.filter(exchange.mutate().request(mutateReq).response(response).build());
            });
        }catch (Exception e){
            log.error("【日志监控】异常", e);
            return chain.filter(exchange.mutate().build());
        }
    }

    /**
     *  构建响应体
     * @param response
     * @return
     */
    private ServerHttpResponseDecorator buildResponse(ServerHttpResponse response, Consumer<String> jsonBodyConsumer){
        DataBufferFactory bufferFactory = response.bufferFactory();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(response) {

            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                Flux<? extends DataBuffer> fluxBody = null;
                if (body instanceof Mono){
                    Mono<? extends DataBuffer> mono = (Mono) body;
                    fluxBody = mono.flux();
                }
                if (body instanceof Flux) {
                   fluxBody = (Flux<? extends DataBuffer>) body;
                }
                Mono<Void> mono = super.writeWith(fluxBody.buffer().map(dataBuffer -> {
                    DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                    DataBuffer join = dataBufferFactory.join(dataBuffer);
                    byte[] content = new byte[join.readableByteCount()];
                    join.read(content);
                    DataBufferUtils.release(join);
                    String s = new String(content, StandardCharsets.UTF_8);

                    // gzip编码数据
                    List<String> strings = response.getHeaders().get(HttpHeaders.CONTENT_ENCODING);
                    if (!CollectionUtils.isEmpty(strings) && strings.contains("gzip")) {
                        GZIPInputStream gzipInputStream = null;
                        try {
                            gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(content), content.length);
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte[] buffer = new byte[256];
                            int n;
                            while ((n = gzipInputStream.read(buffer)) >= 0) {
                                out.write(buffer, 0, n);
                            }
                            s = new String(out.toByteArray(), "UTF-8");
                        } catch (IOException e) {

                        } finally {
                            if (gzipInputStream != null) {
                                try {
                                    gzipInputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        s = new String(content, StandardCharsets.UTF_8);
                    }
                    try {
                        jsonBodyConsumer.accept(s);
                    }catch (Exception e){
                        log.error("保存响应对象失败", e);
                    }
                    return bufferFactory.wrap(content);
                }));
                return mono;
            }
        };
        return decoratedResponse;
    }

    /**
     *  保存请求对象
     * @param request
     * @param monitorLogWrapper
     */
    private void saveRequest(ServerHttpRequest request, MonitorLogWrapper monitorLogWrapper){
//        InetSocketAddress inetAddress = request.getRemoteAddress();
        MonitorLog monitorLog = monitorLogWrapper.getMonitorLog();
//        // ip
//        if (inetAddress != null){
//            monitorLog.setOperIp(inetAddress.getHostName());
//        }else {
//            monitorLog.setOperIp("未知");
//        }
        monitorLog.setOperIp(IPUtis.getIpAddr(request));
        // type
        monitorLog.setOperType(request.getMethod().toString());
        // path
        monitorLog.setOperUrl(request.getPath().value());
        // username
        String username = "";
        monitorLog.setUsername(username);
        FilterCatchMap.put(request.getId(), monitorLogWrapper);
    }

    /**
     *  保存响应对象
     * @param monitorLogWrapper
     */
    private void saveResponse(ServerHttpResponse response, String result, MonitorLogWrapper monitorLogWrapper){
        monitorLogWrapper.setEndTime(System.currentTimeMillis());
        MonitorLog monitorLog = monitorLogWrapper.getMonitorLog();
        // 响应码
        if (response.getRawStatusCode() != null){
            monitorLog.setResCode(String.valueOf(response.getRawStatusCode()));
        }
        // 耗时
        long costTime = monitorLogWrapper.getEndTime() - monitorLogWrapper.getStartTime();
        monitorLog.setCostTime(String.valueOf(costTime));
        // 响应值
        monitorLog.setJsonBody(result == null ? "" : result);
        // 创建时间
        monitorLogWrapper.getMonitorLog().setCreateTime(LogServletUtils.formatDateNow());
        FilterCatchMap.submit(dataEngine, monitorLog);
    }

}
