public class OrderResult {
    private final String orderId;
    private final boolean success;
    private final String errorMessage;

    public OrderResult(String orderId, boolean success, String errorMessage) {
        this.orderId = orderId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public String getOrderId() {
        return orderId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
