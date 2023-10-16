package pine.log.monitor;

public class LogGlobalException extends RuntimeException {

    public LogGlobalException(String message){
        super(message);
    }

    public LogGlobalException(Exception e){
        super(e);
    }
}
