package pine.log.monitor.domain;

public class MonitorLogWrapper {

    private MonitorLog monitorLog;

    private Long startTime;

    private Long endTime;

    public MonitorLogWrapper(MonitorLog monitorLog) {
        this.monitorLog = monitorLog;
        this.startTime = System.currentTimeMillis();
    }

    public MonitorLog getMonitorLog() {
        return monitorLog;
    }

    public void setMonitorLog(MonitorLog monitorLog) {
        this.monitorLog = monitorLog;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
