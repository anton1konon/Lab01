import junit.framework.TestCase;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PacketCreatorTest extends TestCase {

    @Test
    public void testCorrectnessShortMessage()
            throws IllegalBlockSizeException, NoSuchPaddingException, IOException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        String testStr = "Hello World!";
        byte[] packetBytes = PacketCreator.createPacket(testStr, 1, 1);
        PacketReceiver.setKey(PacketCreator.getKey());
        PacketReceiver packet = new PacketReceiver(packetBytes);
        assertEquals(testStr, packet.getMessageStr());
    }

    @Test
    public void testCorrectnessJSONMessage()
            throws IllegalBlockSizeException, NoSuchPaddingException, IOException,
            BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {

        String testStr = Files.readString(Path.of("src/main/resources/file1.json"), StandardCharsets.UTF_8);
        byte[] packetBytes = PacketCreator.createPacket(testStr, 1, 1);
        PacketReceiver.setKey(PacketCreator.getKey());
        PacketReceiver packet = new PacketReceiver(packetBytes);
        assertEquals(testStr, packet.getMessageStr());
    }

    @Test
    public void testIncorrectCrc16() throws IllegalBlockSizeException, NoSuchPaddingException, IOException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String testStr = "Hello World!";
        byte[] packetBytes = PacketCreator.createPacket(testStr, 1, 1);
        PacketReceiver.setKey(PacketCreator.getKey());
        packetBytes[14] = 0x01;

        try {
            PacketReceiver packet = new PacketReceiver(packetBytes);
            fail("My method didn't throw when I expected it to");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().equals("crc16_1 not valid")) fail("Another exception");
        }
    }

    @Test
    public void testPacketWithNoBMagic() throws IllegalBlockSizeException, NoSuchPaddingException, IOException, BadPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String testStr = "Hello World!";
        byte[] packetBytes = PacketCreator.createPacket(testStr, 1, 1);
        PacketReceiver.setKey(PacketCreator.getKey());
        packetBytes[0] = 0x01;

        try {
            PacketReceiver packet = new PacketReceiver(packetBytes);
            fail("My method didn't throw when I expected it to");
        } catch (IllegalArgumentException e) {
            if (!e.getMessage().equals("first byte is not magic")) fail("Another exception");
        }
    }


}