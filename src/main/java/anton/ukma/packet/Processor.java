package anton.ukma.packet;

import anton.ukma.repository.DaoService;
import anton.ukma.repository.ProductGroupRepository;
import anton.ukma.repository.ProductRepository;
import lombok.NoArgsConstructor;
import org.json.Cookie;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@NoArgsConstructor
public class Processor {

    private static final DaoService daoService = new DaoService();


    // я зробив, щоб метод не був void, а повертав String, щоб я міг протестувати відповідь через PacketReceiver
    // (поки ми не зробили не фейкову реалізацію Sender)
    public static String process(Message message) throws SQLException {
        int cType = message.getcType();

        return new JSONObject().put("response", 200).toString();

//        switch (cType) {
//            case 1 -> {
//                return getAmountOfProduct(message);
//            }
//            case 2 -> {
//                return writeOffProducts(message);
//            }
//            case 3 -> {
//                return writeInProducts(message);
//            }
//            case 4 -> {
//                return addGroup(message);
//            }
//            case 5 -> {
//                return addProductToGroup(message);
//            }
//            case 6 -> {
//                return setPriceOnProduct(message);
//            }
//            default -> {
//                return null;
//            }
//        }

    }

    private static String getAmountOfProduct(Message message) {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        int amount = daoService.getAmountOfProduct(jsonObject.getString("name"));

        JSONObject jo2 = new JSONObject();
        jo2.put("amount", amount);
        jo2.put("response", 200);
        return jo2.toString();
    }

    private static String writeOffProducts(Message message) throws SQLException {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        daoService.writeOffAmountProduct(jsonObject.getString("name"), jsonObject.getInt("amount"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        return jo2.toString();
    }

    private static String writeInProducts(Message message) throws SQLException {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        daoService.writeInAmountProduct(jsonObject.getString("name"), jsonObject.getInt("amount"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        return jo2.toString();
    }

    private static String addGroup(Message message) throws SQLException {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        daoService.createGroup(jsonObject.getString("name"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        return jo2.toString();
    }

    private static String addProductToGroup(Message message) throws SQLException {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        daoService.addProductToGroup(jsonObject.getString("name"), jsonObject.getInt("groupId"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        return jo2.toString();
    }

    private static String setPriceOnProduct(Message message) throws SQLException {
        JSONObject jsonObject = Cookie.toJSONObject(message.getMessage_str());
        daoService.setPriceOnProduct(jsonObject.getInt("productId"), jsonObject.getDouble("price"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        return jo2.toString();
    }

}
