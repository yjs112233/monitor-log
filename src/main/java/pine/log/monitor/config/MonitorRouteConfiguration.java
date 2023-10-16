package pine.log.monitor.config;

import pine.log.monitor.handler.AuthHandler;
import pine.log.monitor.handler.LogListHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

@Configuration
public class MonitorRouteConfiguration implements WebFluxConfigurer {

    @Autowired
    private LogListHandler logListHandler;

    @Autowired
    private AuthHandler authHandler;

    @Bean
    public RouterFunction monitorListRouter()
    {
        return RouterFunctions.route(
                RequestPredicates.GET("/mxkj/log").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                logListHandler);
    }

    @Bean
    public RouterFunction monitorAuthRouter()
    {
        return RouterFunctions.route(
                RequestPredicates.POST("/mxkj/login").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                authHandler);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** mxkj日志监控index 地址 */
        registry.addResourceHandler("/mxkj/**","/api/mxkj/**")
                .addResourceLocations("classpath:/static/");
    }

}
