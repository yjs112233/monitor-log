package pine.log.monitor.engine.jdbc;

import com.alibaba.druid.util.StringUtils;
import pine.log.monitor.LogGlobalException;
import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.engine.jdbc.impl.SQLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TableSelect {

    private static final Logger log = LoggerFactory.getLogger(TableSelect.class);

    @Autowired
    private BaseOptional baseOptional;

    @Autowired
    private SQLFactory sqlFactory;

    public int count(MonitorLog monitorLog){
        String baseSql = sqlFactory.get().count_sql();

        List<String> values = new ArrayList<>();
        String sql = buildWhere(monitorLog, baseSql, values);

        AtomicInteger integer = new AtomicInteger();
        baseOptional.execute(sql, preparedStatement -> {
            try {
                for (int i = 1; i <= values.size(); i++) {
                    preparedStatement.setString(i, "%" + values.get(i - 1) + "%");
                }
                ResultSet set = preparedStatement.executeQuery();
                while (set.next()){
                    int count = set.getInt(1);
                    integer.set(count);
                }
            } catch (Exception e) {
                throw new LogGlobalException(e);
            }
        });
        return integer.get();
    }

    /**
     *  分页查询日志
     * @param page
     * @param size
     * @return
     */
    public List<MonitorLog> select(int page, int size){
        if (page < 1 || size <=0){
            throw new LogGlobalException("非法参数：page:" + page + "  size:" + size);
        }
        String sql = sqlFactory.get().select_sql();
        List<MonitorLog> list = new ArrayList<>();
        baseOptional.execute(sql, preparedStatement -> {
            try {
                preparedStatement.setInt(1, page);
                preparedStatement.setInt(2, size);
                ResultSet set = preparedStatement.executeQuery();
                mapper(set, list);
            } catch (Exception e) {
                throw new LogGlobalException(e);
            }
        });
        return list;
    }

    /**
     *  条件搜索查询
     * @param monitorLog
     * @param page
     * @param size
     * @return
     */
    public List<MonitorLog> search(MonitorLog monitorLog, int page, int size){
        String baseSql = sqlFactory.get().search_base_sql();
        List<String> values = new ArrayList<>();
        String sql = buildWhere(monitorLog, baseSql, values);
        //创建时间倒叙
        String order = " order by create_time desc ";
        sql += order;
        // 分页
        String limit = " limit ?, ?;";
        sql += limit;

        // 执行
        List<MonitorLog> list = new ArrayList<>();
        baseOptional.execute(sql, preparedStatement -> {
                    try {
                        for (int i = 1; i <= values.size(); i++) {
                            preparedStatement.setString(i, "%" + values.get(i - 1) + "%");
                        }
                        preparedStatement.setInt(values.size() + 1, page);
                        preparedStatement.setInt(values.size() +2, size);

                        ResultSet set = preparedStatement.executeQuery();
                        mapper(set, list);
                    } catch (Exception e) {
                        throw new LogGlobalException(e);
                    }
        });
        return list;
    }


    /**
     *  驼峰转下划线
     * @return
     */
    private static String toUnderlineName(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            boolean nextUpperCase = true;
            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1)) || Character.isDigit(s.charAt(i + 1));
            }
            if (Character.isUpperCase(c)) {
                boolean flag = !upperCase || !nextUpperCase;
                if (flag) {
                    if (i > 0) sb.append("_");
                }
                upperCase = true;
            } else {
                upperCase = false;
            }
            sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }

    /**
     *  Mapper数据转换
     * @param set
     * @param list
     * @throws Exception
     */
    private void mapper(ResultSet set, List<MonitorLog> list) throws Exception {
        while (set.next()){
            Class<MonitorLog> clazz = MonitorLog.class;
            MonitorLog log = clazz.newInstance();
            for (Field declaredField : clazz.getDeclaredFields()) {
                declaredField.setAccessible(true);
                declaredField.set(log, set.getString(toUnderlineName(declaredField.getName())));
            }
            list.add(log);
        }
    }

    /**
     *  构建where语句
     * @param monitorLog
     * @param baseSql
     * @return
     */
    private String buildWhere(MonitorLog monitorLog, String baseSql, List<String> values){
        String where = "where ";
        String and = "and ";
        Class<MonitorLog> clazz = MonitorLog.class;
        List<String> conditionList = new ArrayList<>();
        for (Field declaredField : clazz.getDeclaredFields()) {
            declaredField.setAccessible(true);
            String value = null;
            try {
                value = (String) declaredField.get(monitorLog);
            } catch (IllegalAccessException e) {
                throw new LogGlobalException(e);
            }
            if (!StringUtils.isEmpty(value)){
                String properties = toUnderlineName(declaredField.getName());
                conditionList.add(properties + " like ? ");
                values.add(value);
            }
        }
        // sql条件拼接
        String sql = baseSql;
        if (!conditionList.isEmpty()){
            sql = baseSql + where;
            for (int i = 0; i < conditionList.size(); i++) {
                sql += conditionList.get(i);
                if (i < conditionList.size() - 1){
                    sql += and;
                }
            }
        }
        return sql;
    }

}
