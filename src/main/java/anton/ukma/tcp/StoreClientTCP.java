package anton.ukma.tcp;

import anton.ukma.packet.PacketCreator;
import anton.ukma.packet.PacketReceiver;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class StoreClientTCP {
    private Socket clientSocket;
    private OutputStream out;
    private InputStream in;

    public void startConnection(int port) throws IOException {
        clientSocket = new Socket(InetAddress.getLocalHost().getHostAddress(), port);
        // sends output to the socket
        out = new DataOutputStream(clientSocket.getOutputStream());
        //takes input from socket
        in = new DataInputStream(clientSocket.getInputStream());
    }

    public String sendMessage(String msg, int cType, int bUserId) throws InterruptedException {

        PacketCreator pc = new PacketCreator(msg, cType, bUserId);

        return sendMessage(pc);
    }

    private String sendMessage(PacketCreator pc) throws InterruptedException {
        byte[] toSend = pc.getPacketBytes();

        while (true) {
            try {
                out.write(toSend);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                baos.write(buffer, 0 , in.read(buffer));
                byte result[] = baos.toByteArray();
                PacketReceiver packet = new PacketReceiver(result);

                return packet.getMessageStr();
            } catch (IOException e) {
                try {
                    Thread.sleep(2000);
                    startConnection(6666);
                    return sendMessage(pc);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    private static class ReceiveMessageThread extends Thread {
        public String getReceived() {
            return received;
        }

        private String received = null;
        private BufferedReader in;

        public ReceiveMessageThread(BufferedReader in) {
            this.in = in;
        }

        @Override
        public void run() {
            try {
                received = in.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
