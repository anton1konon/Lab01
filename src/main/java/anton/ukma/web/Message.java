package anton.ukma.web;

import javax.crypto.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Message {

    private static SecretKey key;
    private int cType;
    private int bUserId;
    private byte[] message;
    private byte[] decryptedMessage;
    private String message_str;

    protected Message(ByteBuffer buffer, int wLen) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);

        Thread takingParams = new Thread(() -> {
            cType = buffer.getInt(16);
            bUserId = buffer.getInt(20);
        });
        takingParams.start();

        Thread decryptMessage = new Thread(() -> {
            message = new byte[wLen - 8];
            buffer.get(24, message);
            try {
                decryptedMessage = cipher.doFinal(message);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            message_str = new String(decryptedMessage, StandardCharsets.UTF_8);
        });
        decryptMessage.start();

        takingParams.join();
        decryptMessage.join();
    }

    public int getcType() {
        return cType;
    }

    public int getbUserId() {
        return bUserId;
    }

    public String getMessage_str() {
        return message_str;
    }

    public static void setKey(SecretKey key) {
        Message.key = key;
    }
}
