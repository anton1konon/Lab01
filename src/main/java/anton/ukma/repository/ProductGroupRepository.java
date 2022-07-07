package anton.ukma.repository;

import anton.ukma.model.ProductGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ProductGroupRepository {

    private static long amount = 2;
    private static ArrayList<ProductGroup> groups = new ArrayList<>();;

    static {
        groups.add(new ProductGroup(1, "1group"));
        groups.add(new ProductGroup(2, "2group"));
    }


    public static void addGroup() {
        groups.add(new ProductGroup(++amount, null));
    }

    public static void addProductToGroup(long idGroup, String productName) {
//        ProductRepository
//                .getProductByName(productName)
//                .setProductGroup(getGroupById(idGroup));
    }

    public static ProductGroup getGroupById(long idGroup) {
        return groups.stream()
                .filter(g -> g.getId() == idGroup)
                .findFirst().orElseThrow();
    }

    public static long getAmount() {
        return amount;
    }
}
