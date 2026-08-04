public class ProductOutOfStockException extends WarehouseException {
    public ProductOutOfStockException(String message, String errorCode) {
        super(message, errorCode);
    }

    public ProductOutOfStockException(String message, String errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
