import Exceptions.*;
import Interfaces.WarehouseOperation;
import Models.Order;
import Models.OrderResult;
import Models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Warehouse warehouse = new Warehouse();
        createTestProducts(warehouse);
        List<Order> orders = createTestOrders(); // 6-7 fərqli ssenari
        List<OrderResult> results = new ArrayList<>();

        try (WarehouseConnection connection = new WarehouseConnection(true)) {
            connection.executeQuery("test query");
        } catch (WarehouseConnectionException e) {
            System.out.println("Connection test: " + e.getMessage());
        }

        for (Order order : orders) {
            warehouse.addOrder(order);
        }

        while (!warehouse.isOrderQueueEmpty()) {
            Order order = warehouse.getNextOrder();

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

        WarehouseOperation warehouseOperation =
                () -> warehouse.writeLogsToFile("warehouse_log.txt");

        try {
            warehouseOperation.execute();
        } catch (WarehouseException e) {
            System.out.println("Log yazma xətası: " + e.getMessage());
        }

        warehouse.printLowestStock();
    }

    static void createTestProducts(Warehouse warehouse) {
        warehouse.addProduct(new Product("P001", "Product 1", 10, 100.0));
        warehouse.addProduct(new Product("P002", "Product 2", 0, 200.0));
        warehouse.addProduct(new Product("P003", "Product 3", 5, 150.0));
        warehouse.addProduct(new Product("P004", "Product 4", 20, 50.0));
        warehouse.addProduct(new Product("P005", "Product 5", 15, 75.0));
        warehouse.addProduct(new Product("P006", "Product 6", 8, 120.0));
        warehouse.addProduct(new Product("P007", "Product 7", 12, 90.0));
        warehouse.addProduct(new Product("P008", "Product 8", 0, 60.0));
    }

    static List<Order> createTestOrders() {
        List<Order> orders = new ArrayList<>();
        orders.add(new Order("O001", "C001",
                Map.of("P001", 2, "P003", 1))); // Valid

        orders.add(new Order("O002", "C002",
                Map.of("P004", 1))); // Valid

        orders.add(new Order("O003", "C003",
                Map.of("P002", 1))); // Out of stock

        orders.add(new Order("O004", "C003",
                Map.of("P008", 1))); // Out of stock

        orders.add(new Order("O005", "C003",
                Map.of("P005", 20))); // Out of stock + Exceptions.CriticalSystemFailureException

        orders.add(new Order("O006", "C007",
                Map.of("P009", 1))); // Invalid product

        return orders;
    }
}
