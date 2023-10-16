package pine.log.monitor.engine.jdbc.impl;

public class MYSQLConstant {

    public static final String TABLE_NAME = "mxkj_monitor_log";


    public static final String SHOW_TABLE_SQL ="show tables";


    /**
     *  创建表的sql
     *  id自增 Innodb引擎 UTF-8编码
     */
    public static final String CREATE_TALBLE_SQL =
            "                 CREATE TABLE `" + TABLE_NAME + "`  (" +
            "                `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键'," +
            "                `username` varchar(255) NULL COMMENT '用户名'," +
            "                `request_id` varchar(255) NULL COMMENT '请求对象的ID'," +
            "                `oper_ip` varchar(255) NULL COMMENT '请求IP'," +
            "                `oper_url` varchar(255) NULL COMMENT '请求地址'," +
            "                `oper_type` varchar(255) NULL COMMENT '请求类型'," +
            "                `oper_param` longtext NULL COMMENT '请求参数'," +
            "                `json_body` longtext NULL COMMENT '返回结果'," +
            "                `res_code` varchar(255) NULL COMMENT '返回状态码'," +
            "                `cost_time` int(10) NULL COMMENT '耗时'," +
            "                `create_time` datetime(0) NULL COMMENT '创建时间', " +
            "                 PRIMARY KEY (`id`)   )" +
            "                 DEFAULT CHARSET=utf8" +
            "                 ENGINE=InnoDB" +
            "                 AUTO_INCREMENT=1 " +
            "                 COMMENT='请求日志收集';";


    /**
     *  插入日志数据
     */
    public static final String INSERT_SQL =
            "insert into " + TABLE_NAME +" " +
            "(username,  oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time)" +
            " values " +
            "(  ?,         ?,         ?,         ?,          ?,         ?,        ?,         ?,          ?);";

    /**
     *  计算数量
     */
    public static final String COUNT_SQL = "select count(1) from " + TABLE_NAME + " ";

    /**
     *  分页查询sql语句
     */
    public static final String SELECT_SQL =
            "select " +
            "id, username, oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time " +
            "from " + TABLE_NAME + " order by create_time desc " +
            "limit ?, ?;";

    /**
     *  条件查询基础sql语句
     */
    public static final String SEARCH_BASE_SQL =
            "select " +
            "id, username, oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time " +
            "from " + TABLE_NAME + " ";
}
