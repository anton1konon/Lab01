package anton.ukma;

import anton.ukma.http.MyHttpServer;

import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) throws IOException {
        MyHttpServer httpServer = new MyHttpServer();
        httpServer.start();
    }


}
