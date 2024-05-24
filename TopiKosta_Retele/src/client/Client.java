package client;

import common.Message;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Citire informatii de la tastatura
        System.out.print("Enter recipient: ");
        String recipient = scanner.nextLine();

        System.out.print("Enter sender: ");
        String sender = scanner.nextLine();

        System.out.print("Enter subject: ");
        String subject = scanner.nextLine();

        System.out.print("Enter message body: ");
        String body = scanner.nextLine();

        // Creare mesaj
        Message message = new Message(recipient, sender, subject, body);

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            // Trimitere mesaj catre server
            oos.writeObject(message);
            // Primire raspuns de la server
            String response = (String) ois.readObject();
            System.out.println("Server response: " + response);

        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        }
    }
}
