public class WarehouseConnectionException extends WarehouseException {
    public WarehouseConnectionException(String message, String errorCode) {
        super(message, errorCode);
    }

    public WarehouseConnectionException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
