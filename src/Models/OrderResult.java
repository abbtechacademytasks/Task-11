package Models;

public record OrderResult(String orderId, boolean success, String errorMessage) {
}
