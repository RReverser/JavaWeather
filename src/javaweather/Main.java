package javaweather;

import java.net.*;
import java.io.*;
import java.util.HashMap;
import java.net.URLDecoder;

class ClientWorker implements Runnable {

    private Socket client;

    public ClientWorker(Socket client) {
        this.client = client;
    }

    public void run() {
        try {
            System.out.println(client.getInetAddress().getHostAddress() + " connected.");

            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintStream out = new PrintStream(client.getOutputStream(), true);

            String query = "";
            try {
                query = in.readLine().split(" ")[1];
                query = URLDecoder.decode(query, "UTF-8");
            } catch (Exception e) {}

            String[] parts = query.replaceAll("(^/)|(/$)", "").split("/");
            
            int paramCount = parts.length / 2;
            HashMap<String, String> params = new HashMap<String, String>(paramCount);
            for (int i = 0; i < 2 * paramCount; i += 2) {
                params.put(parts[i], parts[i + 1]);
            }

            String server = params.get("server");
            
            AbstractWeatherReader weatherReader = null;

            if(server != null && params.get("location") != null) {
                if (server.equals("yahoo")) {
                    weatherReader = new YahooWeatherReader(params);
                } else if (server.equals("wunderground")) {
                    weatherReader = new WUndergroundWeatherReader(params);
                } else if(server.equals("google")) {
                    weatherReader = new GoogleWeatherReader(params);
                }
            }

            int httpCode;
            String contentType = null;
            int cacheTime;
            String content = null;

            if(weatherReader != null) {
                try { weatherReader.process(); } catch (Exception e) {}

                System.out.println(params);

                httpCode = 200;
                contentType = "text/javascript; charset=UTF-8";
                cacheTime = 3600;
                content = String.valueOf(weatherReader.getWeatherModel());
            } else {
                httpCode = 404;
                contentType = "text/plain; charset=UTF-8";
                cacheTime = 86400;
                content = "Page not found.";
            }

            out.print("HTTP/1.1 " + httpCode + " OK\r\n"
                    + "Content-Type: " + contentType + "\r\n"
                    + "Content-Length: " + (content.length() * (Character.SIZE / 8)) + "\r\n"
                    + "Cache-Control: max-age=" + cacheTime + ", must-revalidate\r\n"
                    + "\r\n");

            out.print(content);
        } catch (Exception e) {
        } finally {
            try { client.close(); } catch (Exception e) {}
        }
    }
}

/**
 * Main weather server class
 * @author I&K
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            int serverPort = args.length >= 1 ? Integer.valueOf(args[0]) : 7777;
            ServerSocket serverSocket = new ServerSocket(serverPort);

            System.out.println("Waiting for clients on " + InetAddress.getLocalHost().getHostAddress() + ":" + serverPort + "...\n");

            while (!Thread.interrupted()) {
                try {
                    new Thread(new ClientWorker(serverSocket.accept())).start();
                } catch (IOException e) {
                    System.out.println("Accept failed");
                }
            }
        } catch (Exception e) {
            System.out.println("Server work was interrupted. Reason: " + e);
        }
    }
}
