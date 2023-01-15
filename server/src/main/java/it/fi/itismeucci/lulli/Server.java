package it.fi.itismeucci.lulli;

import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server extends Thread{
    public ServerSocket server = null;
    
    public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    public static ArrayList<String> clientsName = new ArrayList<>();

    public void connetti() throws IOException {
        System.out.println("Server partito in esecuzione ... ");
        try (ServerSocket server = new ServerSocket(42069)) {
            for (;;) {
                Socket client;
                try {
                    client = server.accept();
                    ClientHandler newT = new ClientHandler(client);
                    newT.start();
                    System.out.println("Nuovo Thread creato");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.exit(1);
                }
            }
        }
    }
}