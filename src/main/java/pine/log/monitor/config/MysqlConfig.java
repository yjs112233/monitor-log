package pine.log.monitor.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import pine.log.monitor.engine.DataEngine;
import pine.log.monitor.engine.impl.MysqlDataEngine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MysqlConfig {


    @Bean
    @ConfigurationProperties("spring.datasource.dynamic.datasource.master")
    @ConditionalOnProperty(prefix = "spring.datasource.dynamic.datasource.master", name = "enable", havingValue = "true")
    public DataSource mysqlDataSource(DruidProperties druidProperties)
    {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        return druidProperties.dataSource(dataSource);
    }

    @Bean
    @ConditionalOnBean(value = DataSource.class)
    public DataEngine getSqlDataEngine(){
        return new MysqlDataEngine();
    }


}
