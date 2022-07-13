package anton.ukma.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private int id;
    private String name;
    private double price;
    private int amount;
    private int productGroupId;

    public Product(String name, double price, int amount, int productGroupId) {
        this.name = name;
        this.price = price;
        this.amount = amount;
        this.productGroupId = productGroupId;
    }
}
