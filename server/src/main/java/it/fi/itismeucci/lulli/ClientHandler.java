package it.fi.itismeucci.lulli;

import java.io.*;
import java.net.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler extends Thread {
    protected Socket client;
    private DataOutputStream out = null;
    private BufferedReader in = null;
    private ObjectMapper objectMapper;
    private Msg msgSent;
    private Msg msgReceived;
    private Msg utente;
    private String nomeUtente;
    private static String list = "";

    public ClientHandler(Socket client) throws IOException{
        this.client = client;
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out = new DataOutputStream(client.getOutputStream());
        this.objectMapper = new ObjectMapper();
        this.msgSent = new Msg();
        this.msgReceived = new Msg();
    }

    @Override
    public void run() {

        try {
            login(client);
        } catch (IOException e) {
            System.out.println(e);
        }

        try {
            for (;;)
                recvMessage(in.readLine());
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String getNomeUtente() {
        return nomeUtente;
    }

    public void login(Socket socket) throws IOException {
        boolean exists;
        do {
            exists = false;
            // ricevo il messaggio e lo deserializzo
            utente = deserializza(in.readLine());
            System.out.println("Utente connesso come: " + utente.getMit());
            for (ClientHandler c : Server.clients) {
                // controllo che il nome utente non sia esistente
                if (utente.getMit().equals(c.getNomeUtente())) {
                    // var impostata su true per ripetere il ciclo
                    exists = true;
                    // invio messaggio di errore
                    msgServer("Connessione rifiutata, client gia' esistente");
                    break;
                }
            }
        } while (exists);
        // il client non è un doppioneallora lo aggiungo il client alla lista
        Server.clients.add(this);
        // imposto il nome del client
        nomeUtente = utente.getMit();
        // aggiungo il nome all'arraylist
        Server.clientsName.add(nomeUtente);
        // conferma da parte del server che il client si è connesso alla chat
        msgServer("entrato");
    }

    public void msgServer(String text) throws IOException{
        msgSent.setMit("Server");
        msgSent.setCommand("0");
        msgSent.setDest(null);
        msgSent.setText(text);
        sendMessage(msgSent);
    }

    public void msgError(String err) throws IOException{
        msgSent.setMit("Server");
        msgSent.setCommand("0");
        msgSent.setDest(null);
        msgSent.setText("Errore nel server. \n" + err);
        sendMessage(msgSent);
        System.out.println(err);
    }

    public void sendMessage(Msg msgSent) throws IOException{
        String strgSerializzata = objectMapper.writeValueAsString(msgSent);
        out.writeBytes(strgSerializzata + '\n');
    }

    public void recvMessage(String recvMsg) throws IOException{
        msgReceived = deserializza(recvMsg);
        if(msgReceived.getCommand().equals("1")){
            System.out.println(this.nomeUtente + " ha inviato a tutti: " + msgReceived.getText());
            for(ClientHandler s : Server.clients){
                try {
                    if(!s.nomeUtente.equals(this.nomeUtente))
                        s.sendMessage(msgReceived);
                    }
                    catch (IOException e) {
                    System.out.println(e);
                    msgError("Errore nell'invio del messaggio broadcast dal server");
                    }
                }
            }
            else if (msgReceived.getCommand().equals("2") || msgReceived.getCommand().equals("risposta")) {
                boolean exists = false;
                for (ClientHandler c : Server.clients) {
                    if (c.nomeUtente.equals(msgReceived.getDest().get(0))) {
                        exists = true;
                        c.sendMessage(msgReceived);
                        System.out.println(this.nomeUtente + " ha inviato a " + msgReceived.getDest() + ": "
                                + msgReceived.getText());
                        break;
                    }
                }
                if (!exists) {
                    System.out.println(this.nomeUtente + " ha inviato a " + msgReceived.getDest() + ": "
                            + msgReceived.getText() + ". Ma l'utente non esiste.");
                    msgServer("Utente non esistente");
                }
            } else if (msgReceived.getCommand().equals("-1")) {
                msgReceived.setDest(msgReceived.getDest());
                msgReceived.setMit("Server");
                msgReceived.setText(lista());
                sendMessage(msgReceived);
            }
            else if (msgReceived.getCommand().equals("4")) {
                for (ClientHandler c : Server.clients) {
                    try {
                        if (!c.nomeUtente.equals(this.nomeUtente)) {
                            msgReceived.setText(this.nomeUtente + " e' uscito dalla chat!");
                            msgReceived.setCommand("chiusura");
                            c.sendMessage(msgReceived);
                        }
                    } catch (IOException e) {
                        System.out.println(e);
                        msgError("Errore nell'invio del messaggio broadcast dal server");
                    }
                }
                Server.clients.remove(this);
                Server.clientsName.remove(nomeUtente);
                msgReceived.setDest(msgReceived.getDest());
                msgReceived.setCommand("4");
                msgReceived.setMit("Server");
                System.out.println(nomeUtente + " si è disconnesso");
                sendMessage(msgReceived);
            }
            msgReceived.setCommand(null);
            msgReceived.setText(null);
            msgReceived.setDest(null);
            msgReceived.setMit(null);
            }
    
    public String lista() throws IOException {
        list = "";
        for (ClientHandler c : Server.clients) {
            list = list + "-> " + c.nomeUtente + "\n";
        }
        return list;
    }

    public String serializza(Msg msg) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String msgSer = mapper.writeValueAsString(msg);
        return msgSer;
    }

    public Msg deserializza(String msg) throws JsonProcessingException, JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        Msg msgDes = mapper.readValue(msg, Msg.class);
        return msgDes;
    }

}


