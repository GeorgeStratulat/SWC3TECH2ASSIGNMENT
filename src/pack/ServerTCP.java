package pack;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by George on 03/10/2017.
 */
public class ServerTCP {

    static ServerSocket serverSocket;
    static Socket socket;
    static final int PORT = 5675;
    static ArrayList<ClientHandler> sockets = new ArrayList();

    public static void main(String[] args) throws IOException {
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        do {
            try {
                socket = serverSocket.accept();
                ClientHandler newClient = new ClientHandler(socket, "thread");
                newClient.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }   while(true);

    }

    public static String getActiveClients(){
        String list = "\nLIST :";

        for(ClientHandler clientHandler: sockets){
            list += clientHandler.getUsername();
            list += " ";

        }
        return list;

    }

    public static void sendMessage(String message){
        for (ClientHandler clientHandler : sockets) {
            clientHandler.getPrintWriter().println( message);

        }

    }

}

class ClientHandler extends Thread
{
    private Socket socket;
    private Scanner clientIn;
    private PrintWriter clientOut;
    private String username = "";
    private boolean running;

    public ClientHandler(Socket socket, String username){
        socket = socket;
        this.username = username;
        running = true;
        try{
            clientIn = new Scanner(socket.getInputStream());
            clientOut = new PrintWriter(socket.getOutputStream(), true);

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    public void run(){

        while(running){
            long startTime = System.currentTimeMillis();
            String receive = "QUIT";

            do {

                try {
                    if(clientIn.hasNextLine()) {
                        receive = clientIn.nextLine();
                        Scanner messageScanner = new Scanner(receive);
                        String key = messageScanner.next();
                        switch (key) {
                            case "JOIN": {
                                String name = receive.substring(receive.indexOf(" ") + 1, receive.indexOf(","));
                                if (checkUsername(name)) {
                                    clientOut.println("J_OK");
                                    username = name;
                                    ServerTCP.sockets.add(this);
                                    for (ClientHandler clientHandler : ServerTCP.sockets) {
                                        clientHandler.getPrintWriter().println(ServerTCP.getActiveClients());
                                    }

                                    System.out.println(receive);
                                } else {
                                    clientOut.println("J_ERR"); //send the J_ERR to the user, invalid name
                                    System.out.println(name + " was rejected.");
                                }
                            }
                            break;
                            case "DATA": {
                                System.out.println(receive);
                                ServerTCP.sendMessage(receive);

                            }
                            break;
                            case "QUIT": {


                            }
                            break;
                            case "ALIVE":
                                startTime = System.currentTimeMillis();
                                break;
                            default: {
                                System.out.println(this.getUsername() + "send a weird message");
                                clientOut.println("J_ERR");
                                break;
                            }

                        }

                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                long currentTime = System.currentTimeMillis();

                if(currentTime-startTime>60000) {
                    receive = "QUIT";
                }

            }while(!receive.startsWith("QUIT"));

            try
            {
                if (socket!=null){
                    stopRunning();
                    int i=-1;
                    for(ClientHandler clientHandler: ServerTCP.sockets){
                        if(clientHandler.getUsername().equalsIgnoreCase(username))
                            i = ServerTCP.sockets.indexOf(clientHandler);
                    }
                    ServerTCP.sockets.remove(i);
                    for (ClientHandler clientHandler : ServerTCP.sockets) {
                        clientHandler.getPrintWriter().println(ServerTCP.getActiveClients());
                    }

                    System.out.println(username+" left the chat");

                    socket.close();
                }

            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean checkUsername(String name) {
        for (ClientHandler socketThread : ServerTCP.sockets) {
            if (socketThread.username.equalsIgnoreCase(name))
                return false;
        }
        return true;
    }

    public void stopRunning(){
        running = false;

    }

    public String getUsername() {
        return username;

    }

    public Socket getSocket() {
        return socket;

    }

    public Scanner getScanner() {
        return clientIn;

    }

    public PrintWriter getPrintWriter(){
        return clientOut;

    }

}




