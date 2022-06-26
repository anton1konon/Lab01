package anton.ukma.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    private long id;
    private String name;
    private double price;
    private int amount;
    private ProductGroup productGroup;

}
