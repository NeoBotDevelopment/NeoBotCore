package page.nafuchoco.neobot.api.datastore.exception;

public class DataStoreException extends RuntimeException {
    
    public DataStoreException() {
    }

    public DataStoreException(String message) {
        super(message);
    }

    public DataStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataStoreException(Throwable cause) {
        super(cause);
    }

    public DataStoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
