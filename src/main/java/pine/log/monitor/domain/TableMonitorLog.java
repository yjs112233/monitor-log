package pine.log.monitor.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableMonitorLog {

    private int count;

    private List<MonitorLog> list;
}
