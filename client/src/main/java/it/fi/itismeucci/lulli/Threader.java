package it.fi.itismeucci.lulli;

public class Threader extends Thread{
    Client client;
    public Threader(Client client){
        this.client = client;
    }

    public void run(){
        try {
            for (;;) {
                Client.threadRecvMessage();
            }
        } catch (Exception e) {}
    }





}
