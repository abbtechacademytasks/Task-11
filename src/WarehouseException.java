import java.time.LocalDateTime;

abstract class WarehouseException extends Exception {
    private final String errorCode;
    private final LocalDateTime timestamp;

    public WarehouseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    public WarehouseException(String message, String errorCode, Throwable cause) {
        super(message, cause); // exception chaining
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    public String getErrorCode() { return errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
