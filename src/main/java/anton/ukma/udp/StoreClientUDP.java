package anton.ukma.udp;

import anton.ukma.packet.PacketCreator;
import anton.ukma.packet.PacketReceiver;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class StoreClientUDP {

    private static int count = 1;

    private int id;

    private int lostPackets = 0;
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf = new byte[1024];

    public StoreClientUDP() throws SocketException, UnknownHostException {
        id = count++;
        socket = new DatagramSocket();
        address = InetAddress.getByName("localhost");
    }

    public String sendMessage(String msg, int cType, int bUserId) throws IOException, InterruptedException {

        PacketCreator pc = new PacketCreator(msg, cType, bUserId);

        byte[] msgArr = pc.getPacketBytes();
        DatagramPacket packet
                = new DatagramPacket(msgArr, msgArr.length, address, 4445);
        socket.send(packet);

        packet = new DatagramPacket(buf, buf.length);
        ReceiveMessageThread receiveMessageThread = new ReceiveMessageThread(socket, packet);

        String received = null;
        receiveMessageThread.start();
        receiveMessageThread.join(2000);
        if ((received = receiveMessageThread.getReceived()) == null) {
            if (lostPackets == 3) return "Server did not respond";
            lostPackets++;
            return sendMessage(msg, cType, bUserId);
        }

        return received;
}

    public void close() {
        socket.close();
    }

private static class ReceiveMessageThread extends Thread {

    private String received = null;
    private DatagramPacket packet;
    private DatagramSocket socket;

    public ReceiveMessageThread(DatagramSocket socket, DatagramPacket packet) {
        this.packet = packet;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            PacketReceiver packetReceiver = new PacketReceiver(packet.getData());
            received = packetReceiver.getMessageStr();
        } catch (InterruptedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public String getReceived() {
        return received;
    }
}


}
