package pine.log.monitor.engine.jdbc.impl;

import lombok.Data;

@Data
public abstract class SQL {



    /**
     *  插入日志数据
     */
    public String insertSql(){
        return "insert into " + tableName() +" " +
                "(username,  oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time)" +
                " values " +
                "(  ?,         ?,         ?,         ?,          ?,         ?,        ?,         ?,          ?);";
    }


    /**
     *  计算数量
     */
    public String countSql (){
        return "select count(1) from " + tableName() + " ";
    }

    /**
     *  分页查询sql语句
     */
    public String selectSql (){
        return "select " +
                "id, username, oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time " +
                "from " + tableName() + " order by create_time desc " +
                "limit ?, ?;";
    }


    /**
     *  条件查询基础sql语句
     */
    public String searchBaseSql (){
        return "select " +
                "id, username, oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time " +
                "from " + tableName() + " ";
    }


    /**
     * 表名
     * @return
     */
    public abstract String tableName();

    /**
     * 查看所有表
     * @return
     */
    public abstract  String showTableSql();

    /**
     *  创建表的sql
     *  id自增 Innodb引擎 UTF-8编码
     */
    public abstract  String createTableSql();



}
