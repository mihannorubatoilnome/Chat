package it.fi.itismeucci.lulli;

public class App 
{
    public static void main( String[] args )
    {
        try {
            Client client = new Client();
            client.connetti();
            client.registra();
            client.comunica();
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }
}
