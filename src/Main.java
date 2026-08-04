import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Order> orders = createTestOrders(); // 6-7 fərqli ssenari
        List<OrderResult> results = new ArrayList<>();
        Warehouse warehouse = new Warehouse();

        for (Order order : orders) {
            try {
                OrderResult result = warehouse.processOrder(order);
                results.add(result);
            } catch (CriticalSystemFailureException e) {
                System.out.println("KRİTİK XƏTA: " + e.getMessage());
                System.out.println("Əsl səbəb: " + e.getCause().getMessage());
            } catch (InvalidOrderException | WarehouseConnectionException e) {
                System.out.println("Xəta: " + e.getMessage());
            } catch (ProductOutOfStockException e) {
                System.out.println("Stok xətası: " + e.getMessage());
            } finally {
                System.out.println("Sifariş " + order.getId() + " emal edildi.");
            }
        }
    }
}
