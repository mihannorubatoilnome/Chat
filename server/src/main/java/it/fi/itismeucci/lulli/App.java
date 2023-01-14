package it.fi.itismeucci.lulli;

public class App 
{
    public static void main( String[] args )
    {
        Server server = new Server();
        try {
            server.connetti();
        } catch (Exception e) {
            System.out.println("ERRORE!");
            System.out.println("Connessione non riusscita!");
        }
    }
}
