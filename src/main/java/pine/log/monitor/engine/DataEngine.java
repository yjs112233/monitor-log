package pine.log.monitor.engine;

import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.TableMonitorLog;

public interface DataEngine {

    String getName();

    void initCreate() throws Exception;

    void insert(MonitorLog monitorLog);

    TableMonitorLog list(MonitorLog monitorLog, int page, int size);

}
