package pine.log.monitor.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

public class DataEngineInitializer implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger log = LoggerFactory.getLogger(DataEngineInitializer.class);

    @Autowired(required = false)
    private DataEngine dataEngine;

    // 容器加载完成后的事件
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        try {
            if (dataEngine == null){
                log.info("日志监控初始化成功，无引擎");
                return;
            }
            dataEngine.initCreate();
            log.info("日志监控初始化成功，{}", dataEngine.getName());
        }catch (Exception e){
            log.info("日志监控异常", e);
        }
    }
}
