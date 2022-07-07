package anton.ukma.repository;

import anton.ukma.model.Product;

import java.util.List;
import java.util.stream.Stream;

public class ProductRepository {

    private static List<Product> products = Stream.of(
                    new Product(1, "product1", 25.12, 5, 1),
                    new Product(2, "product2", 12.11, 5, 1),
                    new Product(3, "product3", 10.99, 6, 1),
                    new Product(4, "product4", 5.21, 7, 2),
                    new Product(5, "product5", 3.12, 20,2))
            .toList();


    public static int getAmountOfProduct(long idProduct) {
        return products.stream()
                .filter(p -> p.getId() == idProduct)
                .findFirst()
                .orElseThrow()
                .getAmount();
    }

    public static Product getProductById(long id) {
        return products.stream()
                .filter(p -> p.getId() == id)
                .findFirst().orElseThrow();
    }

    public synchronized static void writeOffProducts(long idProduct, int writeOffAmount) {
        Product product = getProductById(idProduct);
        product.setAmount(product.getAmount() - writeOffAmount);
    }

    public synchronized static void writeInProducts(long idProduct, int writeOffAmount) {
        Product product = getProductById(idProduct);
        product.setAmount(product.getAmount() + writeOffAmount);
    }


    public static Product getProductByName(String productName) {
        return products.stream()
                .filter(p -> p.getName().equals(productName))
                .findFirst().orElseThrow();
    }

    public synchronized static void setPriceOnProduct(long id, double price) {
        Product product = getProductById(id);
        product.setPrice(price);
    }
}
