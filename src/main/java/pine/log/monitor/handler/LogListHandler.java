package pine.log.monitor.handler;

import pine.log.monitor.AuthorizationException;
import pine.log.monitor.LogServletUtils;
import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.TableMonitorLog;
import pine.log.monitor.engine.DataEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.Map;

@Component
public class LogListHandler  implements HandlerFunction<ServerResponse> {

    @Autowired(required = false)
    private DataEngine dataEngine;

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        ServerHttpRequest req = request.exchange().getRequest();

        // 授权校验
        try {
            AuthHandler.checkAuth(req);
        } catch (AuthorizationException e) {
            Map<String, Object> result = LogServletUtils.buildResult(401, e.getMessage(), null);
            return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
        }

        if (dataEngine == null){
            Map<String, Object> result = LogServletUtils.buildResult(500, "监控未配置", null);
            return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
        }

        String query = req.getURI().getQuery();
        String[] params = query.split("&");

        // 默认分页参数
        int page = 0;
        int size = 10;
        Class<MonitorLog> clazz = MonitorLog.class;
        MonitorLog monitorLog = new MonitorLog();
        for (String param : params) {
            String key = param.split("=")[0];
            String value = param.split("=")[1];
            if (key.equals("page")){
                page = Integer.valueOf(value);
            }
            if (key.equals("size")){
                size = Integer.valueOf(value);
            }
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.getName().equals(key)){
                    try {
                        declaredField.setAccessible(true);
                        declaredField.set(monitorLog, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        TableMonitorLog table = dataEngine.list(monitorLog, page, size);
        Map<String, Object> result = LogServletUtils.buildResult(200, "成功", table);
        return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
    }
}
