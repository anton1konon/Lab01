import anton.ukma.repository.ProductRepository;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import anton.ukma.web.PacketCreator;
import anton.ukma.web.PacketReceiver;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PacketCreatorTest {

    @Test
    public void testCorrectnessShortMessage()
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException {

        JSONObject jo = new JSONObject();
        jo.put("productId", 5);
        String testStr = jo.toString();
        PacketCreator pc = new PacketCreator(testStr, 1, 1);
        byte[] packetBytes = pc.getPacketBytes();
        PacketReceiver.setKey(PacketCreator.getKey());
        PacketReceiver packet = new PacketReceiver(packetBytes);
        Assert.assertEquals(testStr, packet.getMessageStr());
    }


    @Test
    public void testFunctions() throws InterruptedException {
        Thread first = new Thread(() -> {
            JSONObject jo = new JSONObject();
            jo.put("productId", 5);
            int cType = 1;
            String testStr = jo.toString();
            PacketCreator pc = new PacketCreator(testStr, cType, 1);
            byte[] packetBytes = pc.getPacketBytes();
            PacketReceiver.setKey(PacketCreator.getKey());
            PacketReceiver packet = null;
            try {
                packet = new PacketReceiver(packetBytes);
            } catch (InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            Assert.assertEquals(jo.toString(), packet.getMessageStr());
            Assert.assertEquals(packet.getAnswer(), "{\"amount\":20,\"response\":200}");
        });
        first.start();

        Thread second = new Thread(() -> {
            JSONObject jo = new JSONObject();
            jo.put("productId", 5L);
            jo.put("amount", 7);
            int cType = 2;
            String testStr = jo.toString();
            PacketCreator pc = new PacketCreator(testStr, 2, 1);
            byte[] packetBytes = pc.getPacketBytes();
            PacketReceiver packet = null;
            try {
                packet = new PacketReceiver(packetBytes);
            } catch (InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            Assert.assertEquals(testStr, packet.getMessageStr());
            Assert.assertEquals(packet.getAnswer(), "{\"response\":200}");
            Assert.assertEquals(ProductRepository.getProductById(5L).getAmount(), 13L); // 20 - 7 = 13
        });

        Thread third = new Thread(() -> {
            JSONObject jo = new JSONObject();
            int cType = 4;
            String testStr = jo.toString();
            PacketCreator pc = new PacketCreator(testStr, 1, 1);
            byte[] packetBytes = pc.getPacketBytes();
            PacketReceiver.setKey(PacketCreator.getKey());
            PacketReceiver packet = null;
            try {
                packet = new PacketReceiver(packetBytes);
            } catch (InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
            Assert.assertEquals(testStr, packet.getMessageStr());
            Assert.assertEquals(packet.getAnswer(), "{\"response\":200}");
        });


        second.start();
        first.join();
        second.join();

    }

    @Test
    public void testIncorrectCrc16() throws IllegalBlockSizeException, NoSuchPaddingException, IOException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String testStr = "Hello World!";
        PacketCreator pc = new PacketCreator(testStr, 1, 1);
        byte[] packetBytes = pc.getPacketBytes();
        PacketReceiver.setKey(PacketCreator.getKey());
        packetBytes[14] = 0x01;

        try {
            PacketReceiver packet = new PacketReceiver(packetBytes);
            Assert.fail("My method didn't throw when I expected it to");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().equals("crc16_1 not valid")) Assert.fail("Another exception");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testPacketWithNoBMagic() throws IllegalBlockSizeException, NoSuchPaddingException, IOException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
        String testStr = "Hello World!";
        PacketCreator pc = new PacketCreator(testStr, 1, 1);
        byte[] packetBytes = pc.getPacketBytes();
        PacketReceiver.setKey(PacketCreator.getKey());
        packetBytes[0] = 0x01;

        try {
            PacketReceiver packet = new PacketReceiver(packetBytes);
            Assert.fail("My method didn't throw when I expected it to");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().equals("first byte is not magic")) Assert.fail("Another exception");
        }
    }


}