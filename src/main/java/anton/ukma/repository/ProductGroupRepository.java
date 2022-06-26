package anton.ukma.repository;

import anton.ukma.model.ProductGroup;

import java.util.List;
import java.util.stream.Stream;

public class ProductGroupRepository {

    private static long id_count = 3;

    private static final List<ProductGroup> groups = Stream.of(
                    new ProductGroup(1, "1group"),
                    new ProductGroup(2, "2group"))
            .toList();


    public static void addGroup() {
        groups.add(new ProductGroup(id_count++, null));
    }

    public static void addProductToGroup(long idGroup, String productName) {
        ProductRepository
                .getProductByName(productName)
                .setProductGroup(getGroupById(idGroup));
    }

    public static ProductGroup getGroupById(long idGroup) {
        return groups.stream()
                .filter(g -> g.getId() == idGroup)
                .findFirst().orElseThrow();
    }
}
