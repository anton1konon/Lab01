import javax.crypto.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PacketCreator {

    static {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        SecureRandom secureRandom = new SecureRandom();
        int keyBitSize = 256;

        keyGenerator.init(keyBitSize, secureRandom);
        key = keyGenerator.generateKey();

    }

    private static final SecretKey key;
    private static long id;

    public static SecretKey getKey() {
        return key;
    }

    public static byte[] createPacket(String str, int cType, int bUserId)
            throws IOException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException {

        byte bMagic = 0x13;
        byte bSrc = 0x07;

        byte[] messageBytes = str.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptMessage = cipher.doFinal(messageBytes);


        int wLen = encryptMessage.length + 8;

        ByteBuffer byteBuffer = ByteBuffer.allocate(18 + wLen); // повинно бути 18 + wLen
        byteBuffer.put(bMagic); //1
        byteBuffer.put(bSrc); //2
        byteBuffer.putLong(id++); // 10
        byteBuffer.putInt(wLen); // 14

        byte[] firstPart = new byte[14];
        byteBuffer.get(0, firstPart);
        short wCrc16_1 = CRC16.makeCrc(firstPart);

        byteBuffer.putShort(wCrc16_1); // 16
        byteBuffer.putInt(cType);
        byteBuffer.putInt(bUserId);
        byteBuffer.put(encryptMessage); // 16 + wLen

        byte[] secondPart = new byte[wLen];
        byteBuffer.get(16, secondPart);
        short wCrc16_2 = CRC16.makeCrc(secondPart);
        byteBuffer.putShort(wCrc16_2); // 18 + wLen


        return byteBuffer.array();
    }
}
