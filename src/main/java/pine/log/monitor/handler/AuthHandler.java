package pine.log.monitor.handler;

import pine.log.monitor.AuthorizationException;
import pine.log.monitor.LogServletUtils;
import pine.log.monitor.config.AuthorizationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthHandler implements HandlerFunction<ServerResponse>, WebFluxConfigurer {

    /**
     *  登录令牌容器，有时间限制
     */
    public static final Map<String, Long> LIMITED_TOKENS = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private AuthorizationConfig authorizationConfig;

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        ServerHttpRequest req = request.exchange().getRequest();
        String query = req.getURI().getQuery();
        String[] params = query.split("&");
        if (params.length != 2){
            Map<String, Object> result = LogServletUtils.buildResult(500, "参数不正确，例如：username=test&password=123456", null);
            return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
        }
        String username = params[0].split("=")[1];
        String password = params[1].split("=")[1];
        if (authorizationConfig.getUsername() == null || authorizationConfig.getPassword() == null){
            Map<String, Object> result = LogServletUtils.buildResult(500, "账号未配置", null);
            return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
        }
        // 匹配账号密码 默认会话过期时间30分钟
        if (authorizationConfig.getUsername().equals(username) && authorizationConfig.getPassword().equals(password)){
            String uuid = UUID.randomUUID().toString();
            int minites = authorizationConfig.getSession() == 0 ? 30 : authorizationConfig.getSession();
            LIMITED_TOKENS.put(uuid, System.currentTimeMillis() + minites * 60 * 1000);
            Map<String, Object> result = LogServletUtils.buildResult(200, "登录成功", uuid);
            return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
        }
        Map<String, Object> result = LogServletUtils.buildResult(500, "账号或者密码错误", null);
        return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(result));
    }

    public static void checkAuth(ServerHttpRequest request) throws AuthorizationException {
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null){
            throw new AuthorizationException("登录失效或已过期");
        }
        Long time = LIMITED_TOKENS.get(token);
        if (time == null || time < System.currentTimeMillis()){
            LIMITED_TOKENS.remove(token);
            throw new AuthorizationException("登录失效或已过期");
        }
    }
}
