package anton.ukma;

import anton.ukma.http.MyHttpServer;
import anton.ukma.model.Product;
import anton.ukma.model.ProductGroup;
import anton.ukma.packet.PacketCreator;
import anton.ukma.packet.PacketReceiver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class App {

    static {
        try {
            new MyHttpServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final HttpClient client = HttpClient.newHttpClient();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String token;
    private JPanel Main;
    private JTable productsTable;
    private JTable groupsTable;
    private JButton addProductButton;
    private JTextField productIdField;
    private JTextField productNameField;
    private JTextField productPriceField;
    private JTextField productAmountField;
    private JTextField productGroupField;
    private JButton updateProductButton;
    private JButton searchProductByIdButton;
    private JTextField searchProductByIdField;
    private JButton deleteProductButton;
    private JButton searchProductByNameButton;
    private JTextField searchProductByNameField;
    private JButton searchGroupByIdButton;
    private JTextField searchGroupByIdField;
    private JTextField groupIdField;
    private JTextField groupNameField;
    private JButton addGroupButton;
    private JButton updateGroupButton;
    private JButton deleteGroupButton;
    private JTextField totalSumField;
    private JButton searchProductByGroupIdButton;
    private JTextField searchByGroupIdField;


    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        frame.setContentPane(new App().Main);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setSize(new Dimension(1100, 700));
        frame.setVisible(true);
    }

    private static byte[] extractMessageFromPackage(byte[] body) {
        try {
            PacketReceiver packet = new PacketReceiver(body);
            return packet.getMessageStrBytes();
        } catch (InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] extractMessageFromPackage(InputStream inputStream) {
        try {
            return extractMessageFromPackage(inputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] writeMessageIntoPacket(byte[] message) {
        PacketCreator pc = new PacketCreator(message);
        return pc.getPacketBytes();
    }

    private void updateTables() {
        List<Product> products;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8765/api/product"))
                    .headers("token", token)
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] responseStr = response.body();
            byte[] message = extractMessageFromPackage(responseStr);
            products = Arrays.asList(OBJECT_MAPPER.readValue(message, Product[].class));
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        String[] columnNames = {
                "Id",
                "Name",
                "Price",
                "Amount",
                "Group"
        };

        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        productsTable.setModel(model);

        for (Product p : products) {
            Object[] o = new Object[5];
            o[0] = p.getId();
            o[1] = p.getName();
            o[2] = p.getPrice();
            o[3] = p.getAmount();
            o[4] = p.getProductGroupId();
            model.addRow(o);
        }

        // groups
        List<ProductGroup> groups;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8765/api/group"))
                    .headers("token", token)
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] responseStr = response.body();
            byte[] message = extractMessageFromPackage(responseStr);
            groups = Arrays.asList(OBJECT_MAPPER.readValue(message, ProductGroup[].class));
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        columnNames = new String[]{
                "Id",
                "Name",
        };

        DefaultTableModel model2 = new DefaultTableModel();
        model2.setColumnIdentifiers(columnNames);
        groupsTable.setModel(model2);

        for (ProductGroup g : groups) {
            Object[] o = new Object[5];
            o[0] = g.getId();
            o[1] = g.getName();
            model2.addRow(o);
        }

        updateTotalSum();

    }


    private void updateTotalSum() {
        int rows = productsTable.getRowCount();
        double sum = 0;


        for (int i = 0; i < rows; i++) {
            sum += (Double) productsTable.getValueAt(i, 2)
                    * (Integer) productsTable.getValueAt(i, 3);
        }

        totalSumField.setText(String.valueOf(sum));

    }

    public App() {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8765/login"))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(
                            writeMessageIntoPacket(
                                    OBJECT_MAPPER.writeValueAsBytes(Map.of(
                                            "login", "user",
                                            "password", "ee11cbb19052e40b07aac0ca060c23ee")))))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] message = extractMessageFromPackage(response.body());
            JsonNode jsonNode = OBJECT_MAPPER.readTree(message);
            token = jsonNode.get("token").asText();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        updateTables();

        addProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Product product = new Product();
                product.setName(productNameField.getText());
                try {
                    product.setPrice(Double.parseDouble(productPriceField.getText()));
                    product.setAmount(Integer.parseInt(productAmountField.getText()));
                    product.setProductGroupId(Integer.parseInt(productGroupField.getText()));
                } catch (NumberFormatException exception) {
                    // TODO
                }


                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/product/"))
                            .headers("token", token)
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(
                                    writeMessageIntoPacket(
                                            OBJECT_MAPPER.writeValueAsBytes(product))))
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());

                } catch (IOException | InterruptedException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }

                updateTables();

            }
        });


        searchProductByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Product product;
                try {
                    int id = Integer.parseInt(searchProductByIdField.getText());
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/product/" + id))
                            .headers("token", token)
                            .GET()
                            .build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    byte[] responseStr = response.body();
                    byte[] message = extractMessageFromPackage(responseStr);
                    product = OBJECT_MAPPER.readValue(message, Product.class);

                    System.out.println(product);
                    if (product != null) {
                        productIdField.setText(String.valueOf(product.getId()));
                        productNameField.setText(product.getName());
                        productPriceField.setText(String.valueOf(product.getPrice()));
                        productAmountField.setText(String.valueOf(product.getAmount()));
                        productGroupField.setText(String.valueOf(product.getProductGroupId()));
                    } else {
                        productIdField.setText("");
                        productNameField.setText("");
                        productPriceField.setText("");
                        productAmountField.setText("");
                        productGroupField.setText("");
                    }

                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }


            }
        });

        updateProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Product product = new Product();
                int id = Integer.parseInt(productIdField.getText());
                product.setId(id);
                product.setName(productNameField.getText());
                try {
                    product.setPrice(Double.parseDouble(productPriceField.getText()));
                    product.setAmount(Integer.parseInt(productAmountField.getText()));
                    product.setProductGroupId(Integer.parseInt(productGroupField.getText()));
                } catch (NumberFormatException exception) {
                    // TODO
                }

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/product/" + id))
                            .headers("token", token)
                            .POST(HttpRequest.BodyPublishers.ofByteArray(
                                    writeMessageIntoPacket(
                                            OBJECT_MAPPER.writeValueAsBytes(product))))
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }

                updateTables();

            }
        });


        deleteProductButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int id = Integer.parseInt(productIdField.getText());
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/product/" + id))
                            .headers("token", token)
                            .DELETE()
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());

                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }
                updateTables();
            }
        });


        searchProductByNameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                List<Product> products;
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/product"))
                            .headers("token", token)
                            .GET()
                            .build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    byte[] responseArr = response.body();
                    byte[] message = extractMessageFromPackage(responseArr);
                    products = Arrays.asList(OBJECT_MAPPER.readValue(message, Product[].class));
                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }

                String[] columnNames = {
                        "Id",
                        "Name",
                        "Price",
                        "Amount",
                        "Group"
                };

                DefaultTableModel model = new DefaultTableModel();
                model.setColumnIdentifiers(columnNames);
                productsTable.setModel(model);

                // filtering
                String name = searchProductByNameField.getText();
                products = products.stream()
                        .filter((p) -> p.getName().toLowerCase(Locale.ROOT).contains(name.toLowerCase()))
                        .collect(Collectors.toList());

                for (Product p : products) {
                    Object[] o = new Object[5];
                    o[0] = p.getId();
                    o[1] = p.getName();
                    o[2] = p.getPrice();
                    o[3] = p.getAmount();
                    o[4] = p.getProductGroupId();
                    model.addRow(o);
                }

                updateTotalSum();

            }
        });
        searchProductByGroupIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                List<Product> products;
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/product"))
                            .headers("token", token)
                            .GET()
                            .build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    var responseStr = response.body();
                    var message = extractMessageFromPackage(responseStr);
                    products = Arrays.asList(OBJECT_MAPPER.readValue(message, Product[].class));
                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }

                String[] columnNames = {
                        "Id",
                        "Name",
                        "Price",
                        "Amount",
                        "Group"
                };

                DefaultTableModel model = new DefaultTableModel();
                model.setColumnIdentifiers(columnNames);
                productsTable.setModel(model);

                // filtering
                int id = Integer.parseInt(searchByGroupIdField.getText());
                products = products.stream().filter((p) -> p.getProductGroupId() == id).collect(Collectors.toList());

                for (Product p : products) {
                    Object[] o = new Object[5];
                    o[0] = p.getId();
                    o[1] = p.getName();
                    o[2] = p.getPrice();
                    o[3] = p.getAmount();
                    o[4] = p.getProductGroupId();
                    model.addRow(o);
                }

                updateTotalSum();

            }

        });
        searchGroupByIdButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProductGroup group;
                try {
                    int id = Integer.parseInt(searchGroupByIdField.getText());
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/group/" + id))
                            .headers("token", token)
                            .GET()
                            .build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                    var responseStr = response.body();
                    var message = extractMessageFromPackage(responseStr);
                    group = OBJECT_MAPPER.readValue(message, ProductGroup.class);

                    System.out.println(group);
                    if (group != null) {
                        groupIdField.setText(String.valueOf(group.getId()));
                        groupNameField.setText(group.getName());
                    }

                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }

            }
        });
        addGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProductGroup group = new ProductGroup();
                group.setName(groupNameField.getText());

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/group/"))
                            .headers("token", token)
                            .PUT(HttpRequest.BodyPublishers.ofByteArray(
                                    writeMessageIntoPacket(
                                            OBJECT_MAPPER.writeValueAsBytes(group))))
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }

                updateTables();
            }
        });


        updateGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProductGroup group = new ProductGroup();
                int id = Integer.parseInt(groupIdField.getText());
                group.setId(id);
                group.setName(groupNameField.getText());

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/group/" + id))
                            .headers("token", token)
                            .POST(HttpRequest.BodyPublishers.ofByteArray(
                                    writeMessageIntoPacket(
                                    OBJECT_MAPPER.writeValueAsBytes(group))))
                            .build();
                    var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }

                updateTables();
            }
        });

        deleteGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int id = Integer.parseInt(groupIdField.getText());
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(new URI("http://localhost:8765/api/group/" + id))
                            .headers("token", token)
                            .DELETE()
                            .build();
                    client.send(request, HttpResponse.BodyHandlers.ofString());
                } catch (IOException | InterruptedException | URISyntaxException exception) {
                    throw new RuntimeException(exception);
                }
                updateTables();
            }
        });
    }
}
