class Product {
    private final String id, name;
    private int stock;
    private final double price;

    public Product(String id, String name, int stock, double price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getName() {
        return name;
    }

    public void addStock(int stock) {
        this.stock += stock;
    }

    public void reduceStock(int quantity) {
        if (quantity > stock) {
            return;
        }
        this.stock -= quantity;
    }
}