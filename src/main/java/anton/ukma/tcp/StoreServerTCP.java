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
import java.sql.SQLException;

public class StoreServerTCP extends Thread{

    private void startServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(6666);
        while (true)
            new EchoClientHandler(serverSocket.accept()).start();
    }

    @Override
    public void run() {
        StoreServerTCP server = new StoreServerTCP();
        try {
            server.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class EchoClientHandler extends Thread {
        private final Socket clientSocket;

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try {
                InputStream in = new DataInputStream(clientSocket.getInputStream());
                OutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                baos.write(buffer, 0 , in.read(buffer));
                byte[] result = baos.toByteArray();
                PacketReceiver packet = new PacketReceiver(result);
                String response = Processor.process(packet.getMessage());
                PacketCreator pc = new PacketCreator(response, 0, 6666);
                out.write(pc.getPacketBytes());
            } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
                     InterruptedException | SQLException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
