package pine.log.monitor.engine.jdbc;

import pine.log.monitor.LogGlobalException;
import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.engine.jdbc.impl.SQLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TableInsert {

    private static final Logger log = LoggerFactory.getLogger(TableInsert.class);

    @Autowired
    private BaseOptional baseOptional;

    @Autowired
    private SQLFactory sqlFactory;

    /**
     *  插入数据
     * @param monitorLog
     * @throws SQLException
     */
    public void insert(MonitorLog monitorLog){
        String sql = sqlFactory.get().insertSql();
        baseOptional.execute(sql, preparedStatement -> {
            try {
                String[] params = buildParamList(monitorLog);
                for (int i = 1; i <= params.length; i++) {
                    preparedStatement.setString(i, params[i - 1]);
                }
                preparedStatement.execute();
            } catch (Exception e) {
                throw new LogGlobalException(e);
            }
        });
    }

    /**
     *  构建插入参数
     * @param monitorLog
     * @return
     */
    private String[] buildParamList(MonitorLog monitorLog) throws IllegalAccessException {
        Class clazz = MonitorLog.class;
        List<String> list = new ArrayList<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            if (!declaredField.getName().equals("id")){
                String value = (String) declaredField.get(monitorLog);
                list.add(value == null ? "" : value);
            }
        }
        return list.toArray(new String[list.size()]);
    }
}
