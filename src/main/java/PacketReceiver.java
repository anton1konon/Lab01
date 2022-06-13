import javax.crypto.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PacketReceiver {

    private byte bMagic = 0x13;
    private byte bSrc;
    private long bPktId;
    private int wLen;
    private short wCrc16_1;
    private Message message;
    private short wCrc16_2;

    public PacketReceiver(byte[] bytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        if (bytes[0] != bMagic) throw new IllegalArgumentException("first byte is not magic");
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        bSrc = buffer.get(1);
        bPktId = buffer.getLong(2);
        wLen = buffer.getInt(10);

        wCrc16_1 = buffer.getShort(14);
        byte[] bytesTo1Crc = new byte[14];
        buffer.get(0, bytesTo1Crc);
        if (wCrc16_1 != CRC16.makeCrc(bytesTo1Crc)) throw new IllegalArgumentException("crc16_1 not valid");

        wCrc16_2 = buffer.getShort(16 + wLen);
        byte[] bytesTo2Crc = new byte[wLen];
        buffer.get(16, bytesTo2Crc);
        if (wCrc16_2 != CRC16.makeCrc(bytesTo2Crc)) throw new IllegalArgumentException("crc16_2 not valid");

        message = new Message(buffer, wLen);
    }

    public static void setKey(SecretKey key) {
        Message.key = key;
    }

    public String getMessageStr() {
        return message.message_str;
    }

    private class Message {

        private static SecretKey key;
        private int cType;
        private int bUserId;
        private byte[] message;
        private byte[] decryptedMessage;
        private String message_str;

        private Message(ByteBuffer buffer, int wLen) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
            cType = buffer.getInt(16);
            bUserId = buffer.getInt(20);
            message = new byte[wLen - 8];
            buffer.get(24, message);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedMessage = cipher.doFinal(message);
            message_str = new String(decryptedMessage, StandardCharsets.UTF_8);
        }

    }


}
