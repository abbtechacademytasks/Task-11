import java.util.*;

public class Warehouse {
    private final Map<String, Product> products = new HashMap<>();
    private final Queue<Order> orderQueue = new ArrayDeque<>();
    private final TreeSet<Order> completedOrders = new TreeSet<>(Comparator.comparing(Order::getCreatedAt)
            .thenComparing(Order::getId));
    private final List<String> logs = new ArrayList<>();
    private final PriorityQueue<Product> productQueue = new PriorityQueue<>(Comparator.comparing(Product::getStock));
    private final Map<String, Integer> unSuccessfulOrdersCount = new HashMap<>();

    OrderResult processOrder(Order order) throws InvalidOrderException,
            ProductOutOfStockException, WarehouseConnectionException {
        order.setStatus(OrderStatus.PROCESSING);
        try (WarehouseConnection conn = new WarehouseConnection()) {
            // 1. Sifarişdəki hər productId üçün yoxlama
            for (Map.Entry<String, Integer> entry : order.getItems().entrySet()) {
                Product product = products.get(entry.getKey());
                if (product == null) {
                    throw new InvalidOrderException(
                            "Məhsul tapılmadı: " + entry.getKey(), "ERR_404");
                }
                if (product.getStock() < entry.getValue()) {
                    throw new ProductOutOfStockException(
                            "Stok kifayət etmir: " + product.getName(), "ERR_STOCK");
                }
            }
            // 2. Stoku azalt, sifarişi tamamla, log-a yaz

            for (Map.Entry<String, Integer> entry : order.getItems().entrySet()) {
                Product product = products.get(entry.getKey());
                productQueue.remove(product);
                product.reduceStock(entry.getValue());
                productQueue.add(product);
            }

            conn.executeQuery("query imitation");
            order.setStatus(OrderStatus.COMPLETED);

            return new OrderResult(order.getId(), true, null);

        } catch (ProductOutOfStockException e) {
            // Nested try-catch nümunəsi: səbəbi araşdırıb, əgər 3-cü ardıcıl xətadırsa
            // CriticalSystemFailureException kimi "wrap" et
            incrementFailureCount(order.getCustomerId());
            if (getFailureCount(order.getCustomerId()) >= 3) {
                throw new CriticalSystemFailureException(
                        "Müştəri üçün kritik xəta həddi aşıldı: " + order.getCustomerId(), e);
            }
            throw e; // yenidən at, yuxarıda tutulsun
        }
        finally {
            if (order.getStatus() == OrderStatus.COMPLETED) {
                completedOrders.add(order);
                resetFailureCount(order.getCustomerId());
                logs.add("Order completed: " + order.getId());
            } else {
                order.setStatus(OrderStatus.FAILED);
                logs.add("Order failed: " + order.getId());
            }

        }
    }

    private void incrementFailureCount(String customerId) {
        unSuccessfulOrdersCount.put(customerId, unSuccessfulOrdersCount.getOrDefault(customerId, 0) + 1);
    }

    private int getFailureCount(String customerId) {
        return unSuccessfulOrdersCount.getOrDefault(customerId, 0);
    }

    private void resetFailureCount(String customerId) {
        unSuccessfulOrdersCount.put(customerId, 0);
    }

    void addProduct(Product product) {
        products.put(product.getId(), product);
        productQueue.add(product);
    }

    void addOrder(Order order) {
        orderQueue.add(order);
    }

    Order getNextOrder() {
        return orderQueue.poll();
    }

    boolean isOrderQueueEmpty() {
        return orderQueue.isEmpty();
    }
}
