package pack;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by George on 03/10/2017.
 */
public class ClientTCP {
    static Socket socket;
    static Scanner serverIn;
    static PrintWriter serverOut;
    static String username;
    static  int port = 5675;
    static InetAddress IP;

    public static Scanner serverRead(){
        return serverIn;

    }

    public static void main(String[] args)  {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter IP address: ");

            IP = InetAddress.getByName(scanner.next());

            System.out.print("Enter port number: ");
            port = Integer.parseInt(scanner.next());

        }catch(UnknownHostException e){
            e.printStackTrace();
            System.exit(1);
        }

        join();
        Thread alive = new Thread(){
            public void run(){
                try {
                    serverOut = new PrintWriter(socket.getOutputStream(), true);
                    while(true){
                        serverOut.println("ALIVE");
                        Thread.sleep(60000);}
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
        alive.start();
        MessageListener messageListener = new MessageListener();
        messageListener.start();
        sendMessage();

    }

    public static void join(){
        socket = null;

        try{
            socket = new Socket(IP, port);
            serverIn = new Scanner(socket.getInputStream());
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(),true);
            Scanner userInput = new Scanner(System.in);
            String receive = "";
            int ok = 1;

            while(ok==1){
                ok = 0;
                System.out.print("Enter username: ");
                username = userInput.next();

                if(username.length()>11)
                    ok = 1;

                for(Character character: username.toCharArray()){
                    if ((character == '_') || (character == '-') || (character > 64 && character < 91) || (character > 96 && character < 123)||(character >= 48 && character <= 57)) {

                    }
                    else ok = 1;

                }
                if(ok == 0){
                    serverOut.println("JOIN "+ username +", {"+ socket.getInetAddress()+"} {"+socket.getPort()+"} ");
                    receive = serverIn.nextLine();
                    if(receive.equalsIgnoreCase("J_ERR")) {
                        ok = 1;
                    }
                    System.out.println("Server> "+receive);

                }
                else{
                    System.out.println("<Username incorrect>");
                }

            }

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void sendMessage(){
        try{
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            Scanner userInput = new Scanner(System.in);
            String message;

            do {
                message = userInput.nextLine();

                if(!message.equalsIgnoreCase("QUIT")) {
                    serverOut.println("DATA "+username+": "+message);

                }

            } while(!message.equalsIgnoreCase("QUIT"));

        }catch (IOException e){
            e.printStackTrace();
        }
        finally{
            try{
                System.out.println("\nDisconnecting");
                serverOut.println("QUIT "+username);
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
                System.exit(1);
            }

        }

    }

}

class MessageListener extends Thread{

    public void run(){

        while(true){
            if (ClientTCP.serverRead().hasNext()) {
                System.out.println(ClientTCP.serverRead().nextLine());
            }

        }

    }

}
