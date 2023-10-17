package pine.log.monitor.engine.jdbc.impl;

import org.springframework.stereotype.Service;

/**
 * MYSQL 数据库
 */
@Service("mysql")
public class MysqlService extends SQL{

    @Override
    public String tableName() {
        return "mxkj_monitor_log";
    }

    @Override
    public String showTableSql() {
        return "show tables";
    }

    @Override
    public String createTableSql() {
       return "                 CREATE TABLE `" + tableName() + "`  (" +
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
    }
}
