package pine.log.monitor.engine.impl;

import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.TableMonitorLog;
import pine.log.monitor.engine.DataEngine;
import pine.log.monitor.engine.jdbc.TableCreate;
import pine.log.monitor.engine.jdbc.TableInsert;
import pine.log.monitor.engine.jdbc.TableSelect;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class MysqlDataEngine implements DataEngine {

    @Autowired
    private TableCreate tableCreate;
    @Autowired
    private TableInsert tableInsert;
    @Autowired
    private TableSelect tableSelect;

    @Override
    public String getName() {
        return "Mysql";
    }

    @Override
    public void initCreate() {
        tableCreate.createTableIfPresent();
    }

    @Override
    public void insert(MonitorLog monitorLog) {
        tableInsert.insert(monitorLog);
    }

    @Override
    public TableMonitorLog list(MonitorLog monitorLog, int page, int size) {
        List<MonitorLog> list = null;
        if (monitorLog == null){
            list = tableSelect.select(page, size);
        }else {
            list = tableSelect.search(monitorLog, page, size);
        }
        int count = tableSelect.count(monitorLog);
        return new TableMonitorLog(count, list);
    }
}
