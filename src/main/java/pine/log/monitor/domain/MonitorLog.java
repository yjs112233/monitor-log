package pine.log.monitor.domain;

import lombok.Data;

@Data
public class MonitorLog {

    /**
     *  主键
     */
    private String id;

    /**
     *  用户名称
     */
    private String username;

    /**
     *  请求ip地址
     */
    private String operIp;

    /**
     *  请求资源地址
     */
    private String operUrl;

    /**
     *  请求方式
     */
    private String operType;

    /**
     *  请求参数
     */
    private String operParam;

    /**
     *  响应参数
     */
    private String jsonBody;

    /**
     *  操作状态
     */
    private String resCode;

    /**
     *  接口耗时
     */
    private String costTime;

    /**
     *  创建时间
     */
    private String createTime;
}
