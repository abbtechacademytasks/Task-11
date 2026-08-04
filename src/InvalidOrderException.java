public class InvalidOrderException extends WarehouseException{
    public InvalidOrderException(String message, String errorCode) {
        super(message, errorCode);
    }

    public InvalidOrderException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
