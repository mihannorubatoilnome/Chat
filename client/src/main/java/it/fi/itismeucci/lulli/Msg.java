package it.fi.itismeucci.lulli;

import java.util.ArrayList;

public class Msg {
    private String mit;
    public static ArrayList<String> destinatari = new ArrayList<String>();
    private String text;
    private String command;


    public Msg() {
    }

    public Msg(String mit, ArrayList<String> destinatari, String text, String command) {
        this.mit = mit;
        Msg.destinatari = destinatari;
        this.text = text;
        this.command = command;
    }

    public String getMit() {
        return this.mit;
    }

    public void setMit(String mit) {
        this.mit = mit;
    }

    public ArrayList<String> getDest() {
        return Msg.destinatari;
    }

    public void setDest(ArrayList<String> destinatari) {
        Msg.destinatari = destinatari;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Msg mittente(String mittente) {
        setMit(mittente);
        return this;
    }

    public Msg comando(String comando) {
        setCommand(comando);
        return this;
    }

    public Msg destinatario(ArrayList<String> destinatario) {
        setDest(destinatario);
        return this;
    }

}
