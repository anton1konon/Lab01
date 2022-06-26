package anton.ukma.web;


import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PacketReceiver {

    private final byte bMagic = 0x13;

    private byte bSrc;
    private long bPktId;
    private int wLen;
    private short wCrc16_1;

    private boolean crc1IsValid = true;
    private Message message;
    private short wCrc16_2;
    private boolean crc2IsValid = true;

    private ByteBuffer buffer;

    // just for test
    private final String answer;


    public PacketReceiver(byte[] bytes) throws InterruptedException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        if (bytes[0] != bMagic) throw new IllegalArgumentException("first byte is not magic");
        buffer = ByteBuffer.wrap(bytes);
        bSrc = buffer.get(1);
        bPktId = buffer.getLong(2);
        wLen = buffer.getInt(10);

        Thread crc1Thread = new Thread(() -> {
            wCrc16_1 = buffer.getShort(14);

            byte[] bytesTo1Crc = new byte[14];
            buffer.get(0, bytesTo1Crc);
            if (wCrc16_1 != CRC16.makeCrc(bytesTo1Crc)) crc1IsValid = false;
        });
        crc1Thread.start();

        Thread crc2Thread = new Thread(() -> {
            wCrc16_2 = buffer.getShort(16 + wLen);
            byte[] bytesTo2Crc = new byte[wLen];
            buffer.get(16, bytesTo2Crc);
            if (wCrc16_2 != CRC16.makeCrc(bytesTo2Crc)) crc2IsValid = false;
        });

        crc2Thread.start();

        crc1Thread.join();
        crc2Thread.join();

        if (!crc1IsValid) throw new IllegalArgumentException("crc16_1 not valid");
        if (!crc2IsValid) throw new IllegalArgumentException("crc16_2 not valid");

        message = new Message(buffer, wLen);

        answer = Processor.process(message);
    }

    public static void setKey(SecretKey key) {
        Message.setKey(key);
    }

    public String getMessageStr() {
        return message.getMessage_str();
    }

    public int getCType() {
        return message.getcType();
    }

    public int getBUserId() {
        return message.getbUserId();
    }

    public String getAnswer() {
        return answer;
    }

    //    private class CheckCRC1Thread extends Thread{
//        @Override
//        public void run() {
//            wCrc16_2 = buffer.getShort(16 + wLen);
//            byte[] bytesTo2Crc = new byte[wLen];
//            buffer.get(16, bytesTo2Crc);
//            if (wCrc16_2 != CRC16.makeCrc(bytesTo2Crc)) throw new IllegalArgumentException("crc16_2 not valid");
//        }
//    }


}
