package server;

import common.Message;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerHandler extends Thread {
    private Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            Object receivedObject = ois.readObject();
            if (receivedObject instanceof Message) {
                Message message = (Message) receivedObject;
                if (Server.addMessage(message)) {
                    oos.writeObject("Message accepted");
                } else {
                    boolean routed = false;
                    for (String serverAddress : Server.getOtherServers()) {
                        String[] parts = serverAddress.split(":");
                        String host = parts[0];
                        int port = Integer.parseInt(parts[1]);

                        try (Socket serverSocket = new Socket(host, port);
                             ObjectOutputStream serverOos = new ObjectOutputStream(serverSocket.getOutputStream());
                             ObjectInputStream serverOis = new ObjectInputStream(serverSocket.getInputStream())) {

                            serverOos.writeObject(message);
                            String response = (String) serverOis.readObject();
                            if ("Message accepted".equals(response)) {
                                oos.writeObject("Message routed to another server");
                                routed = true;
                                break;
                            }
                        } catch (UnknownHostException e) {
                            System.err.println("Unknown host: " + host + ":" + port + " - " + e.getMessage());
                        } catch (IOException | ClassNotFoundException e) {
                            System.err.println("Error routing message to " + serverAddress + ": " + e.getMessage());
                        }
                    }
                    if (!routed) {
                        oos.writeObject("Recipient not found");
                    }
                }
            } else if (receivedObject instanceof String) {
                String recipient = (String) receivedObject;
                if (Server.getRecipients().contains(recipient)) {
                    oos.writeObject("Recipient found");
                } else {
                    oos.writeObject("Recipient not found");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error handling client connection: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
