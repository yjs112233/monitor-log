package pine.log.monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mxkj.auth")
public class AuthorizationConfig {

    private String username;

    private String password;

    private int session;
}
