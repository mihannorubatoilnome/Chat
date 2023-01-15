package it.fi.itismeucci.lulli;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    public static BufferedReader tastiera;
    public String ipServer = "127.0.0.1";
    public int portaServer = 42069;
    public static DataOutputStream out;
    public static BufferedReader in;
    public static Socket socket;
    public static Msg msgList = new Msg();
    public static Msg msgSent = new Msg();
    public static Msg msgReceived = new Msg();
    public static String nomeUtente;
    public static String stringaServer;
    public static String serverString;
    
    public static ArrayList<String> destinatari = new ArrayList<String>();

    private static ObjectMapper mapper = new ObjectMapper();
    
    protected Socket connetti() throws UnknownHostException, IOException{
        this.socket = new Socket(ipServer, portaServer);
        Client.tastiera = new BufferedReader(new InputStreamReader(System.in));
        Client.out = new DataOutputStream(socket.getOutputStream());
        Client.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        return socket;
    }

    public void comunica() throws IOException{
        Client c = new Client();
        Threader t1 = new Threader(c);
        Threading t2 = new Threading(c);

        t2.start();
        t1.start();
    }

    public void registra() throws IOException {
        boolean entrato = true;

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // chiusura client
                chiusuraClient();
                // invio il messaggio al server
                try {
                    sendMessage(msgSent);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        });

        do {            
            System.out.print("Inserisci il nome utente: ");
            nomeUtente = tastiera.readLine();
            msgSent.setMit(nomeUtente);
            sendMessage(msgSent);
            // leggi la risposta (deserilizzata)
            msgReceived = recvMessage();
            // controllo se sono entrato a far parte della chat
            if (msgReceived.getMit().equals("Server") && msgReceived.getText().equals("entrato"))
                entrato = false;
            else {
                System.out.println(msgReceived.getText());
                entrato = true;
            }
        } while (entrato);
        System.out.println("Entrato nella chat");
    }

    public static void threadSendMessage() throws IOException {
        msgSent.setMit(nomeUtente);
            // seleziona l'opzione del destinatario
        String opzioneDestinatario;
            do {
                // scrivo il menu
                menu();
                // l'utente scrive l'opzione
                opzioneDestinatario = tastiera.readLine();
                switch (opzioneDestinatario) {  
                    case "0":
                        // disconnessione dalla server
                        msgSent.setCommand("3");
                        sendMessage(msgSent);
                        break;  
                    case "1":
                        // scrivo a tutti
                        destinatari.add("All");
                        msgSent.setDest(destinatari);
                        msgSent.setCommand("1");
                            
                        System.out.print("Inserisci il messaggio: ");
                        // scrivo il corpo del messaggio
                        String corpo = tastiera.readLine();
                        msgSent.setText(corpo);
                        System.out.println("");
                        // invio il messaggio al server
                        sendMessage(msgSent);
                        // pulisco l'arraylist di destinatari
                        destinatari.clear();
                        break;
    
                    case "2":
                        destinatari.add("Server");
                        msgList.setDest(destinatari);
                        msgList.setCommand("-1");
                        sendMessage(msgList);
                        destinatari.clear();
                        // scrivo ad una sola persona
                        String destinatario = tastiera.readLine();
                        destinatari.add(destinatario);
                        msgSent.setDest(destinatari);
                        msgSent.setCommand("2");
    
                        System.out.print("Inserisci il messaggio: ");
                        // scrivo il corpo del messaggio
                        String tex = tastiera.readLine();
                        msgSent.setText(tex);
                        // invio il messaggio al server
                        sendMessage(msgSent);
                        // pulisco l'arraylist di destinatari
                        destinatari.clear();
                        System.out.println("");
                        break;
    
                    default:
                        System.out.println("Opzione non esistente tra le scelte");
                        break;
                }
            } while (!opzioneDestinatario.equals("0"));
    }

    public static void threadRecvMessage() throws IOException {
        Msg mexRicevuto = recvMessage();
        // riscrivo il menu perche nel caso ricevo un messaggio so cosa devo scrivere
        // messaggio dal server
        System.out.println("\n");
        if (mexRicevuto.getMit().equals("Server") && mexRicevuto.getCommand().equals("0")) {
            // messaggio di sistema
            System.out.println(mexRicevuto.getText());
        }
        // messaggio in broadcast da un altro client
        else if (mexRicevuto.getCommand().equals("1")) {
            System.out.println("Messaggio -> "
                    + mexRicevuto.getMit()  
                    + " ha scritto a tutti: " + mexRicevuto.getText());
        }
        // messaggio privato da un altro client
        else if (mexRicevuto.getCommand().equals("2")) {
            if (mexRicevuto.getMit().equals(nomeUtente)) {
                System.out.println("Hai scritto a te stesso!");
            }else{
                System.out.println("Messaggio -> " 
                        + mexRicevuto.getMit()
                        + " ti ha scritto: " + mexRicevuto.getText());
            }
        }
        // lista degli utenti connessi
        else if (mexRicevuto.getCommand().equals("-1")) {
            System.out.println("Inserisci il destinatario (Lista dei client connessi): ");
            System.out.println(mexRicevuto.getText());
        }
        // chiusura client
        else if (mexRicevuto.getCommand().equals("4")) {
            System.out.println("chiusura client");
            socket.close();
            System.exit(1);
        }
        // notifica di chiusura di un client
        else if (mexRicevuto.getCommand().equals("chiusura")) {
            System.out.println("Notifica -> "
                    + mexRicevuto.getMit() + " si e' Disconnesso.");
        }
    }

    public static void sendMessage(Msg msgSent) throws IOException{
        String strgSerializzata = mapper.writeValueAsString(msgSent);
        out.writeBytes(strgSerializzata + '\n');
    }

    public static Msg recvMessage() throws IOException {
        // leggo il messaggio ricevuto dal server
        stringaServer = in.readLine();
        // deserializzo
        Msg stringaDeserializzata = mapper.readValue(stringaServer, Msg.class);
        return stringaDeserializzata;
    }

    public static void chiusuraClient() {
        destinatari.add("Server");
        msgSent.setDest(destinatari);
        msgSent.comando("4");
    }

    public static void menu() throws IOException {
        System.out.println("-------------------Menu---------------------");
        System.out.println( " 0 -> Esci" 
                          + '\n'
                          + " 1 -> Scrivere a tutti"
                          + '\n'
                          + " 2 -> Scrivere ad una persona");
        System.out.println("--------------------------------------------");
        System.out.print("Seleziona l'opzione: ");
    }

    public String serializza(Msg msg) throws JsonProcessingException{
        String msgSer = mapper.writeValueAsString(msg);
        return msgSer;
    }

    public static Msg deserializza(String msg) throws JsonProcessingException, JsonMappingException{
        Msg msgDes = mapper.readValue(msg, Msg.class);
        return msgDes;
    }
}
