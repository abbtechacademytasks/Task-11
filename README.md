# Task-11

Sifariş Emalı və Anbar Sistemi
Java tapşırığı — Exception Hierarchy, Resurs İdarəetməsi və Collection-lar (Thread-siz versiya)
Ümumi tələb
Bir e-ticarət anbarının sifarişləri ardıcıl (sequential) şəkildə emal edən sistemini yazın. Sistem çoxsəviyyəli exception hierarchy, resurs idarəetməsi, müxtəlif collection-lar üzərində qurulmalıdır.
1-ci addım: Exception Hierarchy (çoxsəviyyəli)
Abstract base exception yaradın:
abstract class Exceptions.WarehouseException extends Exception {
private final String errorCode;
private final LocalDateTime timestamp;

    public Exceptions.WarehouseException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    public Exceptions.WarehouseException(String message, String errorCode, Throwable cause) {
        super(message, cause); // exception chaining
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }
    public String getErrorCode() { return errorCode; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
Bundan miras alan checked exception-lar:
class Exceptions.ProductOutOfStockException extends Exceptions.WarehouseException { ... }
class Exceptions.InvalidOrderException extends Exceptions.WarehouseException { ... }
class Exceptions.WarehouseConnectionException extends Exceptions.WarehouseException { ... }
Bir unchecked (RuntimeException) exception:
class Exceptions.CriticalSystemFailureException extends RuntimeException {
public Exceptions.CriticalSystemFailureException(String message, Throwable cause) {
super(message, cause); // başqa exception-ı "wrap" edir
}
}
Tələb: Bir sifarişin emalı zamanı 2-dən çox exception ardıcıl baş verərsə, sonuncusu tutulub Exceptions.CriticalSystemFailureException kimi yenidən atılmalı (cause saxlanılmaqla).
2-ci addım: Resurs idarəetməsi (AutoCloseable)
class WarehouseConnection implements AutoCloseable {
public WarehouseConnection() throws Exceptions.WarehouseConnectionException {
// 10% ehtimalla qoşulma xətası simulyasiyası (Random ilə)
}
public void executeQuery(String query) { ... }
@Override
public void close() { System.out.println("Bağlantı bağlanıldı."); }
}
Bunu try-with-resources ilə istifadə edin.
3-cü addım: Model sinifləri
class Models.Product {
String id, name;
int stock;
double price;
}

class Models.Order {
String id, customerId;
Map<String, Integer> items; // productId -> miqdar
Enums.OrderStatus status; // enum: PENDING, PROCESSING, COMPLETED, FAILED
LocalDateTime createdAt;
}
4-cü addım: Collection-ları birgə istifadə edin
Warehouse sinfi daxilində:
Collection
Məqsəd
Map<String, Models.Product> (HashMap)
Məhsulları ID-yə görə saxlamaq
Queue<Models.Order> (LinkedList / ArrayDeque)
Emal gözləyən sifarişlər növbəsi (FIFO)
TreeSet<Models.Order> + Comparator
Tamamlanmış sifarişləri tarixə görə sıralı saxlamaq
List<String>
Log tarixçəsi
PriorityQueue<Models.Product>
Stoku ən az olan məhsulları prioritetləşdirmək
Map<String, Integer>
Hər müştərinin uğursuz sifariş sayını izləmək (statistika)

5-ci addım: Sifariş emalı metodu
Models.OrderResult processOrder(Models.Order order) throws Exceptions.InvalidOrderException,
Exceptions.ProductOutOfStockException, Exceptions.WarehouseConnectionException {

    try (WarehouseConnection conn = new WarehouseConnection()) {
        // 1. Sifarişdəki hər productId üçün yoxlama
        for (Map.Entry<String, Integer> entry : order.getItems().entrySet()) {
            Models.Product product = products.get(entry.getKey());
            if (product == null) {
                throw new Exceptions.InvalidOrderException(
                    "Məhsul tapılmadı: " + entry.getKey(), "ERR_404");
            }
            if (product.getStock() < entry.getValue()) {
                throw new Exceptions.ProductOutOfStockException(
                    "Stok kifayət etmir: " + product.getName(), "ERR_STOCK");
            }
        }
        // 2. Stoku azalt, sifarişi tamamla, log-a yaz
        ...
        return new Models.OrderResult(order.getId(), true, null);
 
    } catch (Exceptions.ProductOutOfStockException e) {
        // Nested try-catch nümunəsi: səbəbi araşdırıb, əgər 3-cü ardıcıl xətadırsa
        // Exceptions.CriticalSystemFailureException kimi "wrap" et
        incrementFailureCount(order.getCustomerId());
        if (getFailureCount(order.getCustomerId()) >= 3) {
            throw new Exceptions.CriticalSystemFailureException(
                "Müştəri üçün kritik xəta həddi aşıldı: " + order.getCustomerId(), e);
        }
        throw e; // yenidən at, yuxarıda tutulsun
    }
}
Tələb: processOrder() çağırılarkən həm ayrıca catch, həm də multi-catch (Exceptions.InvalidOrderException | Exceptions.WarehouseConnectionException) nümunələri göstərilməli.
6-cı addım: finally bloku
Hər sifariş emalından sonra, nəticədən asılı olmayaraq:
Log-a (List<String>) nəticə yazılmalı
Sifarişin statusu yenilənməli (COMPLETED / FAILED)
7-ci addım: main() metodunda test ssenariləri
Sifarişləri ardıcıl (for dövrü ilə) emal edən kod yazın:
List<Models.Order> orders = createTestOrders(); // 6-7 fərqli ssenari
List<Models.OrderResult> results = new ArrayList<>();

for (Models.Order order : orders) {
try {
Models.OrderResult result = warehouse.processOrder(order);
results.add(result);
} catch (Exceptions.CriticalSystemFailureException e) {
System.out.println("KRİTİK XƏTA: " + e.getMessage());
System.out.println("Əsl səbəb: " + e.getCause().getMessage());
} catch (Exceptions.InvalidOrderException | Exceptions.WarehouseConnectionException e) {
System.out.println("Xəta: " + e.getMessage());
} catch (Exceptions.ProductOutOfStockException e) {
System.out.println("Stok xətası: " + e.getMessage());
} finally {
System.out.println("Sifariş " + order.getId() + " emal edildi.");
}
}
Test ssenariləri belə seçilsin ki:
Normal sifariş → uğurla tamamlanır
Mövcud olmayan məhsul → Exceptions.InvalidOrderException
Stok kifayət etmir → Exceptions.ProductOutOfStockException
Bağlantı xətası → Exceptions.WarehouseConnectionException
Eyni müştərinin ardıcıl 3 uğursuz sifarişi → Exceptions.CriticalSystemFailureException (getCause() ilə əsl səbəb göstərilsin)
Bonus tapşırıqlar
Optional<Models.Product> istifadə edərək məhsul axtarışını null-safe edin (findProduct(String id): Optional<Models.Product>).
Comparator.comparing().thenComparing() ilə tamamlanmış sifarişləri həm statusa, həm tarixə görə sıralayın.
Bütün log-u fayla (try-with-resources + FileWriter) yazan metod əlavə edin — bu da resurs idarəetməsini gücləndirir.
PriorityQueue-dan istifadə edərək "stoku 5-dən az olan məhsullar" siyahısını çap edən metod yazın.
Öz @FunctionalInterface-inizi yaradıb, checked exception atan bir lambda ilə sınayın.
Qeyd
Bu tapşırıqda thread istifadə olunmur — bütün əməliyyatlar ardıcıl icra olunur. Diqqət mərkəzi exception hierarchy, exception chaining/wrapping, try-with-resources və müxtəlif collection növlərinin birgə istifadəsindədir.
