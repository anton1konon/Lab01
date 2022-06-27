package anton.ukma.packet;

import anton.ukma.repository.ProductGroupRepository;
import anton.ukma.repository.ProductRepository;
import lombok.NoArgsConstructor;
import org.json.Cookie;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

@NoArgsConstructor
public class Processor {


    // я зробив, щоб метод не був void, а повертав String, щоб я міг протестувати відповідь через PacketReceiver
    // (поки ми не зробили не фейкову реалізацію Sender)
    public static String process(Message message) {
        int cType = message.getcType();

        switch (cType) {
            case 1 -> {
                return getAmountOfProduct(message);
            }
            case 2 -> {
                return writeOffProducts(message);
            }
            case 3 -> {
                return writeInProducts(message);
            }
            case 4 -> {
                return addGroup(message);
            }
            case 5 -> {
                return addProductToGroup(message);
            }
            case 6 -> {
                return setPriceOnProduct(message);
            }
            default -> {
                return null;
            }
        }

    }

    private static String getAmountOfProduct(Message message) {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        int amount = ProductRepository.getAmountOfProduct(jsonObject.getLong("productId"));

        JSONObject jo2 = new JSONObject();
        jo2.put("amount", amount);
        jo2.put("response", 200);
        String answer = jo2.toString();
//        Sender.sendMessage(answer.getBytes(StandardCharsets.UTF_8));
        return answer;
    }

    private static String writeOffProducts(Message message) {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        ProductRepository.writeOffProducts(jsonObject.getLong("productId"), jsonObject.getInt("amount"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        String answer = jo2.toString();
//        Sender.sendMessage(answer.getBytes(StandardCharsets.UTF_8));
        return answer;
    }

    private static String writeInProducts(Message message) {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        ProductRepository.writeInProducts(jsonObject.getLong("productId"), jsonObject.getInt("amount"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        String answer = jo2.toString();
//        Sender.sendMessage(answer.getBytes(StandardCharsets.UTF_8));
        return answer;
    }

    private static String addGroup(Message message) {
        ProductGroupRepository.addGroup();

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        String answer = jo2.toString();
//        Sender.sendMessage(answer.getBytes(StandardCharsets.UTF_8));
        return answer;
    }

    private static String addProductToGroup(Message message) {
        JSONObject jsonObject = new JSONObject(message.getMessage_str());
        ProductGroupRepository.addProductToGroup(jsonObject.getInt("groupId"), jsonObject.getString("productName"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        String answer = jo2.toString();
//        Sender.sendMessage(answer.getBytes(StandardCharsets.UTF_8));
        return answer;
    }

    private static String setPriceOnProduct(Message message) {
        JSONObject jsonObject = Cookie.toJSONObject(message.getMessage_str());
        ProductRepository.setPriceOnProduct(jsonObject.getInt("productId"), jsonObject.getDouble("price"));

        JSONObject jo2 = new JSONObject();
        jo2.put("response", 200);
        String answer = jo2.toString();
//        Sender.sendMessage(answer.getBytes(StandardCharsets.UTF_8));
        return answer;
    }

}
