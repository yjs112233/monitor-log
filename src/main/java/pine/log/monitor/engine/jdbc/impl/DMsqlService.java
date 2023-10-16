package pine.log.monitor.engine.jdbc.impl;

import org.springframework.stereotype.Service;

/**
 * DM 数据库
 */
@Service("dm")
public class DMsqlService extends SQL{

    public String table;

    public DMsqlService setTable(String table) {
        this.table = table;
        return this;
    }

    @Override
    public String table_name() {
        return table + "." +"MXKJ_MONITOR_LOG";
    }

    @Override
    public String show_table_sql() {
        return "SELECT CONCAT('"+table+".',TABLE_NAME) FROM ALL_TABLES WHERE OWNER = '"+table+"';";
    }

    @Override
    public String create_table_sql() {
        return  "CREATE TABLE "+ table_name() +"\n" +
                "(\n" +
                "\"ID\" BIGINT IDENTITY(1, 1) NOT NULL,\n" +
                "\"USERNAME\" VARCHAR(255),\n" +
                "\"REQUEST_ID\" VARCHAR(255),\n" +
                "\"OPER_IP\" VARCHAR(255),\n" +
                "\"OPER_URL\" VARCHAR(255),\n" +
                "\"OPER_TYPE\" VARCHAR(255),\n" +
                "\"OPER_PARAM\" TEXT,\n" +
                "\"JSON_BODY\" TEXT,\n" +
                "\"RES_CODE\" CHAR(255),\n" +
                "\"COST_TIME\" INT,\n" +
                "\"CREATE_TIME\" TIMESTAMP(0),\n" +
                "NOT CLUSTER PRIMARY KEY(\"ID\")) STORAGE(ON \"MAIN\", CLUSTERBTR) ;\n";
    }

    /**
     *                  "COMMENT ON COLUMN "+ table_name() +".COST_TIME IS '耗时';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".CREATE_TIME IS '创建时间';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".ID IS 'ID';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".JSON_BODY IS '返回结果';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".OPER_IP IS '请求IP';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".OPER_PARAM IS '请求参数';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".OPER_TYPE IS '请求类型';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".OPER_URL IS '请求地址';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".REQUEST_ID IS '请求对象的ID';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".RES_CODE IS '返回状态码';\n" +
     *                 "COMMENT ON COLUMN "+ table_name() +".USERNAME IS '用户名';";
     */
}
