import Exceptions.WarehouseConnectionException;

import java.util.Random;

class WarehouseConnection implements AutoCloseable {
    public WarehouseConnection() throws WarehouseConnectionException {
        this(false);
    }

    public WarehouseConnection(boolean forceFailure) throws WarehouseConnectionException {
        Random random = new Random();
        if (forceFailure || random.nextInt(100) < 10) {
            throw new WarehouseConnectionException("Failed to connect to warehouse.", "CONNECTION_ERROR", new RuntimeException("Simulated connection failure."));
        }
    }

    public void executeQuery(String query) {
        System.out.println("Executing query: " + query);
    }

    @Override
    public void close() {
        System.out.println("Closing warehouse connection.");
    }
}