package anton.ukma.web;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

    private byte[] encryptMessage;
    private int wLen;
    private ByteBuffer byteBuffer;

    private int cType;
    private int bUserId;

    private byte[] packet;

    public PacketCreator(String str, int cType, int bUserId) {
        createPacket(str, cType, bUserId);
    }


    public static SecretKey getKey() {
        return key;
    }



    @SneakyThrows
    private void createPacket(String str, int cType, int bUserId) {

        this.cType = cType;
        this.bUserId = bUserId;


//        byte[] messageBytes = str.getBytes(StandardCharsets.UTF_8);
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//        cipher.init(Cipher.ENCRYPT_MODE, key);
//        byte[] encryptMessage = cipher.doFinal(messageBytes);

        byte[] messageBytes = str.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        encryptMessage = cipher.doFinal(messageBytes);
        wLen = encryptMessage.length + 8;
        byteBuffer = ByteBuffer.allocate(18 + wLen); // повинно бути 18 + wLen

        WriteFirstPartThread firstPart = new WriteFirstPartThread(this);
        WriteSecondPartThread secondPart = new WriteSecondPartThread(this);

        firstPart.join();
        secondPart.join();

        packet = byteBuffer.array();
    }

    public byte[] getPacketBytes() {
        return packet;
    }

    private static class WriteFirstPartThread extends Thread {

        PacketCreator pc;

        public WriteFirstPartThread(PacketCreator pc) {
            this.pc = pc;
            this.start();
        }

        @Override
        @SneakyThrows
        public void run() {
            byte bMagic = 0x13;
            pc.byteBuffer.put(0, bMagic); //1
            byte bSrc = 0x07;
            pc.byteBuffer.put(1, bSrc); //2
            pc.byteBuffer.putLong(2, id++); // 10
            pc.byteBuffer.putInt(10, pc.wLen); // 14

            byte[] firstPart = new byte[14];
            pc.byteBuffer.get(0, firstPart);
            short wCrc16_1 = CRC16.makeCrc(firstPart);

            pc.byteBuffer.putShort(14, wCrc16_1); // 16
        }
    }

    private static class WriteSecondPartThread extends Thread {

        PacketCreator pc;

        public WriteSecondPartThread(PacketCreator pc) {
            this.pc = pc;
            this.start();
        }

        @Override
        @SneakyThrows
        public void run() {
            pc.byteBuffer.putInt(16, pc.cType);
            pc.byteBuffer.putInt(20, pc.bUserId);
            pc.byteBuffer.put(24, pc.encryptMessage); // 16 + wLen
            byte[] secondPart = new byte[pc.wLen];
            pc.byteBuffer.get(16, secondPart);
            short wCrc16_2 = CRC16.makeCrc(secondPart);
            pc.byteBuffer.putShort(16 + pc.wLen, wCrc16_2); // 18 + wLen
        }
    }

}
