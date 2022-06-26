package anton.ukma.web;

import java.nio.charset.StandardCharsets;

public class Sender {

    public static void sendMessage(byte[] mess) {
        String str = new String(mess, StandardCharsets.UTF_8);
        System.out.println("final: " + str);

    }

}
