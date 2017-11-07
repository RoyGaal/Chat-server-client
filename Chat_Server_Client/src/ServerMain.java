import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class ServerMain extends JFrame implements Runnable {
    public static ServerMain server;
    private int port;
    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream strIn = null;
    private DataOutputStream strOut = null;
    
    private DatagramSocket UDPsocket; 
    
    private static ArrayList clients;
    private static JTextPane txtArea;
    private JScrollPane scrollBar;

    public ServerMain(){
        this.port         = 4000;
        this.socket       = null;
        this.serverSocket = null;
        txtArea = new JTextPane();
        clients = new ArrayList<ServerClient>();

        txtArea.setBorder(BorderFactory.createLineBorder(Color.red, 5));
        txtArea.setFont(new Font("Dialog", Font.PLAIN, 14));
        txtArea.setEditable(false);
        txtArea.setBackground(Color.black);

        // Scroll Bar
        scrollBar = new JScrollPane(txtArea);
        scrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollBar.setBorder(null);

        add(scrollBar);
        run();
    }

    @Override
    public void run() {
        Thread thread = new Thread(new Runnable() {
            public void run(){
                try{
                    // Create New Server Socket
                    serverSocket = new ServerSocket(port);
                    UDPsocket = new DatagramSocket(port);

                    appendText("Server is running...waiting for a client to connect...\n\n");

                    while(true){
                        // Create New Socket
                        socket = serverSocket.accept();

                        // Create New Output/Input Streams
                        strOut = new DataOutputStream(socket.getOutputStream());
                        strOut.flush();

                        if(clients.size() < 50){
                            strIn = new DataInputStream(socket.getInputStream());

                            // Listen for data
                            String username = strIn.readUTF();
                            handleNewClient(username);
                        }
                        else{
                            strOut.writeUTF("terminate-The server is full, please try again later.");
                            strOut.flush();
                        }
                    }
                }catch(IOException ex){
                    appendText("I/O Exception: \n" + ex.getMessage() + "\n\n");
                }
            }

        });
        thread.start();
    }

    private void handleNewClient(String username){
        // Create and start a new thread for the connection
        if(findClient(username)!= null){
            appendText(username + " is already connected to the server.\n");
        }
        else{
            // Client does not exist, so create new client
            ServerClient newClient = new ServerClient(socket, strIn, strOut, username);
            clients.add(newClient);
            appendText(username + " has connected to the server.\n");
        }
    }

    public static void handleMessage(String msgFrom, String input){
        String msgTo = "";
        String message = "";

        // Check if it is a private message
        if(input.length() >= 6 && input.substring(0,6).equals("secret")){
            int index = 6;
            for(int i = 6; i < input.length(); i++){
                char c = input.charAt(i);
                String ch = "" + c;
                if(ch.equals("/")){
                    break;
                }
                else{
                    msgTo += ch;
                    index++;
                }
            }
            // Build the message with the username the message is from
            message = msgFrom + input.substring(index,input.length());

            // Find the client to send private the message to
            ServerClient client = findClient(msgTo);
            if(client != null) {
                appendText("Found client " + msgTo + " to send private message." + "\n");
                client.send(message);
            }
            appendText(msgFrom + " sent a private message to " + msgTo + "\n");
        }
        else if(input.length() >= 3 && input.substring(0,3).equals("bye")){
            // Check if the message is bye
            BYE(msgFrom);
            ServerClient client = findClient(msgFrom);
            if(client != null)
                remove(client);
        }
        else{
            // Send group message
            message = msgFrom + "/" + input;
            sendGroupMsg(message);
            appendText(msgFrom + " sent a public message." + "\n");
        }
    }

    public static void BYE(String user){
        Iterator i = clients.iterator();
        //System.out.println("BYE");
        while(i.hasNext()){ //to go through the list of clients
            ServerClient client = (ServerClient)i.next();
            if(!client.username.equals(user)){
                //System.out.println("BYE");
                String message = user + "/goodbye";
                client.send(message);
            }
        }
    }

    // Remove A Client
    public static synchronized void remove(ServerClient client){ clients.remove(client); }

    // Find A Client
    public static synchronized ServerClient findClient(String username){
        Iterator i = clients.iterator();
        while(i.hasNext()){
            ServerClient client = (ServerClient) i.next();
            if(client.username.equals(username))
                return client;
        }
        return null;
    }

    // Send Group Message
    private static synchronized void sendGroupMsg(String message){
        Iterator i = clients.iterator();
        while(i.hasNext()){
            ServerClient client = (ServerClient) i.next();
            client.send(message);
        }
    }

    // Append Text To JTextPane
    public static void appendText(String message){
        txtArea.setEditable(true);
        SimpleAttributeSet attribute = new SimpleAttributeSet();
        StyleConstants.setForeground(attribute, Color.white);

        int len = txtArea.getDocument().getLength();
        txtArea.setCaretPosition(len);
        txtArea.setCharacterAttributes(attribute, false);
        txtArea.replaceSelection(message);
        txtArea.setEditable(false);
    }

    public static void main(String[] args){
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                server = new ServerMain();
                server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                server.setTitle("Chat Server");
                server.setSize(500, 400);
                server.setLocationRelativeTo(null);
                server.setFocusable(true);
                server.setVisible(true);
            }
        });
    }
}