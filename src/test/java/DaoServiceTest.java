import anton.ukma.model.Product;
import anton.ukma.repository.DaoService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class DaoServiceTest {

    private static DaoService daoService;

    @BeforeAll
    public static void deleteDataFromTables() throws SQLException {
        daoService = new DaoService();
        daoService.dropAllTables();
        DaoService.initialization("ProjectDB");
    }


    @Test
    public void testCreateAndGettingMethods() throws SQLException {

        daoService.createGroup("testGroup1");
        daoService.createGroup("testGroup2");
        daoService.createProduct("test1", 25.21, 5, 2);
        daoService.createProduct("test2", 27.21, 8, 1);
        daoService.createProduct("test3", 35.52, 9, 2);
        daoService.createProduct("test4", 35.52, 9, 2);
        daoService.createProduct("test5", 35.52, 9, 2);

        assertEquals(daoService.getAmountOfProduct("test1"), 5);
        assertEquals(daoService.getPriceOfProduct("test2"), 27.21, 0);
        assertEquals(daoService.getGroupOfProduct("test3"), 2);

    }

    @Test
    public void testUpdateMethods() throws SQLException {
        daoService.writeInAmountProduct("test2", 5);
        assertEquals(daoService.getAmountOfProduct("test2"), 13);
        daoService.writeOffAmountProduct("test1", 1);
        assertEquals(daoService.getAmountOfProduct("test1"), 4);
    }

    @Test
    public void testList() {
        List<Product> products = daoService.productListWithSorting("price", "DESC");
        double prevPrice = Double.MAX_VALUE;
        for (Product product : products) {
            assertTrue(prevPrice - product.getPrice() >= 0);
            prevPrice = product.getPrice();
        }

        products = daoService.productListWithSorting("amount", "ASC");
        int prevAmount = 0;
        for (Product product : products) {
            assertTrue(product.getAmount() - prevAmount >= 0);
            prevAmount = product.getAmount();
        }
    }

    @Test
    public void testDelete() throws SQLException {
        daoService.deleteProduct("test4");
        List<Product> products = daoService.productListWithSorting("name", "ASC");

        for (Product product : products) {
            if (product.getName().equals("test4")) {
                fail();
            }
        }
    }

    @AfterAll
    public static void dropTables() throws SQLException {
//        daoService.dropAllTables();
    }

}