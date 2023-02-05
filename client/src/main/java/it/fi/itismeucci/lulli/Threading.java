package it.fi.itismeucci.lulli;

public class Threading  extends Thread{
    Client client;
    public Threading(Client client){
        this.client = client;
    }

    public void run(){
        try {
            for (;;) {
                Client.threadSendMessage();
            }
        } catch (Exception e) {}
    }
}
