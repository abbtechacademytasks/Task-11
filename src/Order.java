import java.time.LocalDateTime;
import java.util.Map;

public class Order {
    private final String id, customerId;
    private final Map<String, Integer> items; // productId -> quantity
    private OrderStatus status; // enum: PENDING, PROCESSING, COMPLETED, FAILED
    private final LocalDateTime createdAt;

    public Order(String id, String customerId, Map<String, Integer> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
