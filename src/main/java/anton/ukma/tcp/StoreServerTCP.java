package anton.ukma.tcp;

import anton.ukma.packet.PacketCreator;
import anton.ukma.packet.PacketReceiver;
import anton.ukma.packet.Processor;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class StoreServerTCP extends Thread{

    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(6666);
        while (true)
            new EchoClientHandler(serverSocket.accept()).start();
    }

    public void close() throws IOException {
        serverSocket.close();
    }

    @Override
    public void run() {
        StoreServerTCP server = new StoreServerTCP();
        try {
            server.start(6666);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private InputStream in;
        private OutputStream out;

        private PacketReceiver packet;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                in = new DataInputStream(clientSocket.getInputStream());
                out = new DataOutputStream(clientSocket.getOutputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte buffer[] = new byte[1024];
                baos.write(buffer, 0 , in.read(buffer));
                byte result[] = baos.toByteArray();
                packet = new PacketReceiver(result);
                String response = Processor.process(packet.getMessage());
                PacketCreator pc = new PacketCreator(response, 0, 6666);
                out.write(pc.getPacketBytes());

            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                     InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
