package pine.log.monitor.engine.jdbc;

import pine.log.monitor.LogGlobalException;
import pine.log.monitor.engine.jdbc.impl.SQLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TableCreate {

    private static final Logger log = LoggerFactory.getLogger(TableCreate.class);

    @Autowired
    private BaseOptional baseOptional;

    @Autowired
    private SQLFactory sqlFactory;

    /**
     *  如果表不存在，则创建表
     * @throws SQLException
     */
    public void createTableIfPresent() {
        if (!isTableExist(sqlFactory.get().table_name())){
            String sql = sqlFactory.get().create_table_sql();
            baseOptional.execute(sql, preparedStatement -> {
                try {
                    preparedStatement.execute();
                } catch (SQLException e) {
                    throw new LogGlobalException(e);
                }
            });
        }
    }

    /**
     *  检查表是否存在
     * @param tableName
     * @return
     */
    private boolean isTableExist(String tableName){
        String sql = sqlFactory.get().show_table_sql();
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        baseOptional.execute(sql, preparedStatement -> {
            try {
                ResultSet set = preparedStatement.executeQuery(sql);
                while (set.next()){
                    String name = set.getString(1);
                    if (tableName.equalsIgnoreCase(name)){
                        atomicBoolean.set(true);
                        break;
                    }
                }
            }catch (SQLException e){
                throw new LogGlobalException(e);
            }
        });
        return atomicBoolean.get();
    }
}
