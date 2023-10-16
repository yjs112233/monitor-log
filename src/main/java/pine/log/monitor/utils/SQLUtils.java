package pine.log.monitor.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SQLUtils {

    /**
     * 根据数据库链接地址获取表名
     * @param sqlUrl 数据库连接地址
     * @return 表名
     */
    public static String getTable(String sqlUrl){
        String regex = "/([^/]+)\\?";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sqlUrl);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

}
