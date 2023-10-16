package pine.log.monitor.engine.jdbc.impl;

import lombok.Data;

@Data
public abstract class SQL {



    /**
     *  插入日志数据
     */
    public String insert_sql(){
        return "insert into " + table_name() +" " +
                "(username,  oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time)" +
                " values " +
                "(  ?,         ?,         ?,         ?,          ?,         ?,        ?,         ?,          ?);";
    }


    /**
     *  计算数量
     */
    public String count_sql (){
        return "select count(1) from " + table_name() + " ";
    }

    /**
     *  分页查询sql语句
     */
    public String select_sql (){
        return "select " +
                "id, username, oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time " +
                "from " + table_name() + " order by create_time desc " +
                "limit ?, ?;";
    }


    /**
     *  条件查询基础sql语句
     */
    public String search_base_sql (){
        return "select " +
                "id, username, oper_ip, oper_url, oper_type, oper_param, json_body, res_code, cost_time, create_time " +
                "from " + table_name() + " ";
    }


    /**
     * 表名
     * @return
     */
    public abstract String table_name();

    /**
     * 查看所有表
     * @return
     */
    public abstract  String show_table_sql();

    /**
     *  创建表的sql
     *  id自增 Innodb引擎 UTF-8编码
     */
    public abstract  String create_table_sql();



}
