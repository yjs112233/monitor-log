package pine.log.monitor.filter;

import pine.log.monitor.domain.MonitorLog;
import pine.log.monitor.domain.MonitorLogWrapper;
import pine.log.monitor.engine.DataEngine;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FilterCatchMap {

    private static final ConcurrentHashMap<String, MonitorLogWrapper> map;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
            10, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

    static {
        map = new ConcurrentHashMap<>();
    }

    public static void put(String requestId, MonitorLogWrapper monitorLogWrapper){
        map.put(requestId, monitorLogWrapper);
    }

    public static Optional<MonitorLogWrapper> consumer(String requestId){
        MonitorLogWrapper monitorLogWrapper = map.get(requestId);
        if (monitorLogWrapper == null){
            return Optional.empty();
        }
        map.remove(requestId);
        return Optional.of(monitorLogWrapper);
    }

    public static void submit(DataEngine dataEngine, MonitorLog monitorLog){
        if (dataEngine != null){
            executor.submit(() -> dataEngine.insert(monitorLog));
        }
    }

}
