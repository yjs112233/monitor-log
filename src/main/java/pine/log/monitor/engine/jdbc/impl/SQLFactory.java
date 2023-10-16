package pine.log.monitor.engine.jdbc.impl;

import com.alibaba.druid.pool.DruidDataSource;
import pine.log.monitor.utils.SQLUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SQLFactory {

    @Autowired(required = false)
    private DruidDataSource dataSource;

    @Autowired
    private ApplicationContext context;

    public SQL get(){
        SQL sql = null;
        Map<String, SQL> beans = context.getBeansOfType(SQL.class);
        if("dm.jdbc.driver.DmDriver".equalsIgnoreCase(dataSource.getDriverClassName())){
            DMsqlService dm = (DMsqlService) beans.get("dm");
            return dm.setTable(SQLUtils.getTable(dataSource.getUrl()));
        }else {
            sql = beans.get("mysql");
        }
        return sql;
    }

}
