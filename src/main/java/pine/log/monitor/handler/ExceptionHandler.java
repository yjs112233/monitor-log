package pine.log.monitor.handler;

import com.alibaba.druid.support.json.JSONUtils;
import pine.log.monitor.LogServletUtils;
import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.MonitorLogWrapper;
import pine.log.monitor.engine.DataEngine;
import pine.log.monitor.filter.FilterCatchMap;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class ExceptionHandler implements ErrorWebExceptionHandler, Ordered {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @Autowired(required = false)
    private DataEngine dataEngine;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex)
    {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        if (exchange.getResponse().isCommitted())
        {
            return Mono.error(ex);
        }

        String msg;

        if (ex instanceof NotFoundException)
        {
            msg = "服务未找到";
        }
        else if (ex instanceof ResponseStatusException)
        {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            msg = responseStatusException.getMessage();
        }
        else
        {
            msg = "内部服务器错误";
        }
        log.error("[网关异常处理]请求路径:{},异常信息:{}", exchange.getRequest().getPath(), ex.getMessage());
        try {
            saveMonitor(msg, request);
        }catch (Exception e){
            log.error("[监控日志],异常信息", ex);
        }
        return LogServletUtils.webFluxResponseWriter(response, msg);
    }

    private void saveMonitor(String msg, ServerHttpRequest request){
        Optional<MonitorLogWrapper> optional = FilterCatchMap.consumer(request.getId());
        int code = 500;
        MonitorLog monitorLog = null;
        MonitorLogWrapper monitorLogWrapper = null;
        Map<String, Object> result = LogServletUtils.buildResult(code, msg, null);
        // 过滤器已捕获
        if (optional.isPresent()){
            monitorLogWrapper = optional.get();
            monitorLog = monitorLogWrapper.getMonitorLog();
            monitorLog.setCostTime(String.valueOf(System.currentTimeMillis() - monitorLogWrapper.getStartTime()));
        }
        // 过滤器未捕获
        else {
            monitorLog = new MonitorLog();
            monitorLogWrapper = new MonitorLogWrapper(monitorLog);
            // ip
            InetSocketAddress inetAddress = request.getRemoteAddress();
            if (inetAddress != null){
                monitorLog.setOperIp(inetAddress.getHostName());
            }else {
                monitorLog.setOperIp("未知");
            }
            // type
            if (request.getMethod() != null){
                monitorLog.setOperType(request.getMethod().toString());

                // param
                String type = request.getMethod().toString().toUpperCase();
                if (type.matches("GET")){
                    String param = request.getURI().getQuery();
                    monitorLog.setOperParam(param);
                }
                // JSON格式 APPLICATION_JSON
                if (type.matches("POST")){
                    String contentType = request.getHeaders().getFirst("Content-Type");
                    if (ContentType.APPLICATION_JSON.toString().equalsIgnoreCase(contentType)){
                        AtomicReference<String> atoBody = LogServletUtils.getRequestParams(request);
                        monitorLog.setOperParam(atoBody.get());
                    }
                }
            }
            // path
            monitorLog.setOperUrl(request.getPath().value());

            monitorLog.setCostTime(String.valueOf(0));
        }
        monitorLog.setJsonBody(JSONUtils.toJSONString(result));
        monitorLog.setResCode(String.valueOf(code));
        // 创建时间
        monitorLogWrapper.getMonitorLog().setCreateTime(LogServletUtils.formatDateNow());
        FilterCatchMap.submit(dataEngine, monitorLog);
    }

    /**
     *  设置要比原网关小，才能优先执行
     *  原Gateway网关中GatewayExceptionHandler
     * @return
     */
    @Override
    public int getOrder() {
        return -100;
    }
}
