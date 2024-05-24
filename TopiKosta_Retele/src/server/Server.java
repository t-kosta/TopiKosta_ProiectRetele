package server;

import common.Message;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static Map<String, List<Message>> messages = new HashMap<>();
    private static List<String> recipients = new ArrayList<>(Arrays.asList("user1", "user2", "user3"));
    private static List<String> otherServers = new ArrayList<>(Arrays.asList("localhost:12346", "localhost:12347"));

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running...");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    new ServerHandler(socket).start();
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }

    public static boolean addMessage(Message message) {
        if (recipients.contains(message.getRecipient())) {
            messages.computeIfAbsent(message.getRecipient(), k -> new ArrayList<>()).add(message);
            saveMessageToFile(message);
            return true;
        }
        return false;
    }

    private static void saveMessageToFile(Message message) {
        String recipientDir = "messages/" + message.getRecipient();
        File dir = new File(recipientDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File messageFile = new File(dir, "message_" + timestamp + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(messageFile))) {
            writer.write("Sender: " + message.getSender() + "\n");
            writer.write("Subject: " + message.getSubject() + "\n");
            writer.write("Body: " + message.getBody() + "\n");
        } catch (IOException e) {
            System.err.println("Error saving message to file: " + e.getMessage());
        }
    }

    public static List<String> getOtherServers() {
        return otherServers;
    }

    public static List<String> getRecipients() {
        return recipients;
    }
}
