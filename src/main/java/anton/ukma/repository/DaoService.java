package anton.ukma.repository;

import anton.ukma.http.User;
import anton.ukma.model.Product;
import anton.ukma.model.ProductGroup;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class DaoService {
    private static final Connection con;

    static {
        try {
            con = DriverManager.getConnection("jdbc:sqlite:" + "ProjectDB");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        initialization();
    }

    public static void initialization() {
        try {
            PreparedStatement st1 = con.prepareStatement("create table if not exists 'ProductGroup' (" +
                    "'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'name' text, UNIQUE(name))");
            PreparedStatement st2 = con.prepareStatement("create table if not exists 'Product' (" +
                    "'id' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "'name' text," +
                    "'price' currency," +
                    "'amount' integer," +
                    "'groupId' integer," +
                    "UNIQUE(name) ," +
                    "foreign key (groupId) references ProductGroup(id) on update cascade on delete cascade)");
            PreparedStatement st3 = con.prepareStatement("create table if not exists 'User' (" +
                    "'login' text PRIMARY KEY," +
                    "'password' text)");
            st1.executeUpdate();
            st2.executeUpdate();
            st3.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            e.printStackTrace();
        }
    }

    public boolean userExists(String login) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select * from User WHERE login=?");
            st.setString(1, login);
            ResultSet rs = st.executeQuery();
            return true;
        } catch (SQLException e) {
            System.out.println("wrong user");
            e.printStackTrace();
            return false;
        }
    }

    public boolean userIsValid(String login, String password) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select * from User WHERE login=?");
            st.setString(1, login);
            ResultSet rs = st.executeQuery();
            rs.next();
            User user = new User(rs.getString("login"), rs.getString("password"));
            return user.getPassword().equals(password);
        } catch (SQLException e) {
            System.out.println("wrong user");
            e.printStackTrace();
            return false;
        }
    }

    public Product findProductById(int id) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select * from Product WHERE id=?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            rs.next();
            Product product = new Product(rs.getInt("id"),
                    rs.getString("name"), rs.getDouble("price"),
                    rs.getInt("amount"), rs.getInt("groupId"));
            return product;
        } catch (SQLException e) {
            System.out.println("wrong sql");
            e.printStackTrace();
            return null;
        }
    }

    public Product findProductByName(String name) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select * from Product WHERE name=?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            rs.next();
            Product product = new Product(rs.getInt("id"),
                    rs.getString("name"), rs.getDouble("price"),
                    rs.getInt("amount"), rs.getInt("groupId"));
            return product;
        } catch (SQLException e) {
            System.out.println("wrong sql");
            e.printStackTrace();
            return null;
        }
    }

    public List<Product> findAllProducts() {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product");
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public int updateProduct(Product product) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set name=?, price=?, amount=?, groupId=? where id=?");
        st.setString(1, product.getName());
        st.setDouble(2, product.getPrice());
        st.setInt(3, product.getAmount());
        st.setInt(4, product.getProductGroupId());
        st.setInt(5, product.getId());
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }

    public int createProduct(Product product) throws SQLException {
        return createProduct(product.getName(), product.getPrice(), product.getAmount(), product.getProductGroupId());
    }

    public int createProduct(String name, double price, int amount, int groupId) throws SQLException {
        PreparedStatement st = con.prepareStatement("INSERT INTO Product(name, price, amount, groupId) VALUES (?, ?, ?, ?)");
        st.setString(1, name);
        st.setDouble(2, price);
        st.setInt(3, amount);
        st.setInt(4, groupId);

        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }

    public int createGroup(String name) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("INSERT INTO ProductGroup(name) VALUES (?)");
        st.setString(1, name);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }

    public int getProductIdByName(String name) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select id from Product WHERE name=?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            rs.next();
            int ans = rs.getInt("id");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public int getGroupIdByName(String name) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select id from ProductGroup WHERE name=?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            int ans = rs.getInt("id");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public int getAmountOfProduct(String name) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select amount from Product WHERE name=?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            int ans = rs.getInt("amount");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public int getAmountOfProduct(int id) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select amount from Product WHERE id=?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            int ans = rs.getInt("amount");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public double getPriceOfProduct(int id) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select price from Product WHERE id=?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            double ans = rs.getDouble("price");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public double getPriceOfProduct(String name) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select price from Product WHERE name=?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            double ans = rs.getDouble("price");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public int getGroupOfProduct(String name) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select groupId from Product WHERE name=?");
            st.setString(1, name);
            ResultSet rs = st.executeQuery();
            int ans = rs.getInt("groupId");
            rs.close();
            return ans;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public void writeInAmountProduct(int id, int amount) throws SQLException {
        int previousAmount = getAmountOfProduct(id);
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set amount=? where id=?");
        st.setInt(1, previousAmount + amount);
        st.setInt(2, id);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void writeInAmountProduct(String name, int amount) throws SQLException {
        int previousAmount = getAmountOfProduct(name);
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set amount=? where name=?");
        st.setInt(1, previousAmount + amount);
        st.setString(2, name);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void writeOffAmountProduct(int id, int amount) throws SQLException {
        int previousAmount = getAmountOfProduct(id);
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set amount=? where id=?");
        st.setInt(1, previousAmount - amount);
        st.setInt(2, id);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void writeOffAmountProduct(String name, int amount) throws SQLException {
        int previousAmount = getAmountOfProduct(name);
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set amount=? where name=?");
        st.setInt(1, previousAmount - amount);
        st.setString(2, name);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void setPriceOnProduct(String name, double newPrice) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set price=? where name=?");
        st.setDouble(1, newPrice);
        st.setString(2, name);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void setPriceOnProduct(int id, double newPrice) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set price=? where id=?");
        st.setDouble(1, newPrice);
        st.setInt(2, id);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void addProductToGroup(String productName, int groupId) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("UPDATE Product set groupId=? where name=?");
        st.setInt(1, groupId);
        st.setString(2, productName);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public int deleteProduct(String name) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("DELETE from Product where name=?");
        st.setString(1, name);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }

    public int deleteProduct(int id) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("DELETE from Product where id=?");
        st.setInt(1, id);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }

    public void deleteGroup(String name) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("DELETE from ProductGroup where name=?");
        st.setString(1, name);
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public int deleteGroup(int id) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("DELETE from Product where groupId=?");
        st.setInt(1, id);
        boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }

        st = con.prepareStatement("DELETE from ProductGroup where id=?");
        st.setInt(1, id);
        oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }

    public List<Product> productListWithSorting(String field, String sorting) {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product order by " + field + " " + sorting);
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }


    public List<Product> productListWhereGroupId(int groupId) {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product where groupId=" + groupId);
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public List<Product> productListWhereName(String name) {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product where name=" + name);
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public List<Product> productListWherePrice(double price) {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product where price=" + price);
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public List<Product> productListWherePriceIsBetween(double priceFrom, double priceTo) {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product where price> " + priceFrom + " and price <" + priceTo);
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }


    public List<Product> productListWhereAmountIsBetween(int amountFrom, int amountTo) {
        try {
            LinkedList<Product> products = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from Product where amount> " + amountFrom + " and price <" + amountTo);
            while (rs.next()) {
                Product product = new Product(rs.getInt("id"),
                        rs.getString("name"), rs.getDouble("price"),
                        rs.getInt("amount"), rs.getInt("groupId"));
                products.add(product);
            }
            st.close();
            rs.close();
            return products;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public void dropAllTables() throws SQLException {
        dropProductTable();
        dropProductGroupTable();
    }

    public void dropProductTable() throws SQLException {
        PreparedStatement st =
                con.prepareStatement("DROP TABLE Product");
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void dropProductGroupTable() throws SQLException {
        PreparedStatement st =
                con.prepareStatement("DROP TABLE ProductGroup");
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void deleteAllFromProduct() throws SQLException {
        PreparedStatement st =
                con.prepareStatement("delete from Product");
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }

    public void deleteAllFromProductGroup() throws SQLException {
        PreparedStatement st =
                con.prepareStatement("delete from ProductGroup");
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        try {
            st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
    }


    public List<ProductGroup> findAllGroups() {
        try {
            LinkedList<ProductGroup> groups = new LinkedList<>();
            Statement st =
                    con.createStatement();
            ResultSet rs = st.executeQuery("select * from ProductGroup");
            while (rs.next()) {
                ProductGroup group = new ProductGroup(rs.getInt("id"),
                        rs.getString("name"));
                groups.add(group);
            }
            st.close();
            rs.close();
            return groups;
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит");
            throw new RuntimeException(e);
        }
    }

    public ProductGroup findGroupById(int id) {
        try {
            PreparedStatement st =
                    con.prepareStatement("select * from ProductGroup WHERE id=?");
            st.setInt(1, id);
            ResultSet rs = st.executeQuery();
            rs.next();
            ProductGroup group = new ProductGroup(rs.getInt("id"), rs.getString("name"));
            return group;
        } catch (SQLException e) {
            System.out.println("wrong sql");
            e.printStackTrace();
            return null;
        }
    }

    public int updateGroup(ProductGroup group) throws SQLException {
        PreparedStatement st =
                con.prepareStatement("UPDATE ProductGroup set name=? where id=?");
        st.setString(1, group.getName());
        st.setLong(2, group.getId());
        final boolean oldAutoCommit = st.getConnection().getAutoCommit();
        st.getConnection().setAutoCommit(false);
        int ans = 0;
        try {
            ans = st.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Не вірний SQL запит або транзакція не була закінчена");
            e.printStackTrace();
            st.getConnection().rollback();
        } finally {
            st.getConnection().commit();
            st.getConnection().setAutoCommit(oldAutoCommit);
            st.close();
        }
        return ans;
    }
}
