
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ClientServer extends Thread {
    public  static int port;
    public  static String host;
    private static Socket socket;
    private DataInputStream strIn;
    private DataOutputStream strOut;
    public static String username;
    public boolean connected;
    
    public boolean connectUDP;
    private DatagramSocket UDPSocket;
    private InetAddress ip;
    
    private Thread sendUDP;

    public ClientServer(String username){
        this.socket = null;
        this.port  = 4000;
        this.host = "localhost";
        this.strIn = null;
        this.strOut  = null;
        this.username = username;
        
        boolean connectUDP = openUDPConnection(host, port);
        if(!connectUDP){
        	System.err.println("UDPConnection faild!");
        }

        start();
    }
    
    public boolean openUDPConnection(String host, int port){
    	try {
    		UDPSocket = new DatagramSocket();
			ip = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public String reciveUDP(){
    	byte[] data = new byte[1024];
    	DatagramPacket packet = new DatagramPacket(data, data.length);
    	try {
			UDPSocket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	String message = new String(packet.getData());
    	return message;
    }
    
    public void sendUDP(final byte[] data){
    	sendUDP = new Thread("SendUDP"){
    		public void run(){
    			//String line = data.toString(); //printing random letters
    			String s = new String(data); //Switching byte[] to string
    			
    			DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
    			try {
    				strOut.writeUTF(s); //sending the UDP to all
    	            strOut.flush();
					UDPSocket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}

    		}
    	};
    	sendUDP.start();
    }
    

    // Run Thread
    public void run(){
        // Try to connect to the server
        connected = false;
        try{
            socket         = new Socket(host, port);

            strOut = new DataOutputStream(socket.getOutputStream());
            strOut.flush();
            strIn = new DataInputStream(socket.getInputStream());
            send(username);
            connected = true;

            ClientMain.clientFrame.btnConnect.setEnabled(false);
            ClientMain.clientFrame.btnSend.setEnabled(true);
            ClientMain.clientFrame.btnQuit.setEnabled(true);
            ClientMain.clientFrame.setTitle(username);
            ClientMain.clientFrame.appendText(ClientMain.clientFrame.displayText,
                    username + ", you are now connected to the server.\n\n");
        } catch(IOException ex){
                ClientMain.clientFrame.appendText(ClientMain.clientFrame.displayText,
                    "Unable to connect to the server.\n" + ex.getMessage() + "\n\n");

        }

        // Listen for messages from the server
        while(connected){
            try{
                String line = strIn.readUTF();
                String message = "";
                String msgFrom = "";
                int index = 0;
                for(int i = 0; i < line.length(); i++){
                    char c = line.charAt(i);
                    String ch = "" + c;

                    if(ch.equals("/")) break;
                    else{
                        msgFrom += ch;
                        index++;
                    }
                }
                message = msgFrom + ": " + line.substring(index+1,line.length()) + "\n";
                ClientMain.clientFrame.appendText(ClientMain.clientFrame.displayText, message);
            } catch(IOException ex){
                if(connected){
                    ClientMain.clientFrame.appendText(ClientMain.clientFrame.displayText,
                        "You have been disconnected from the server.");
                    break;
                }
            }
        }
        if(connected){
            disconnect();
            connected = false;
        }
    }

    public void send(String line){
        try{
            strOut.writeUTF(line);
            strOut.flush();
        } catch (IOException e) {
            if(connected){
                ClientMain.clientFrame.appendText(ClientMain.clientFrame.displayText,
                    "Unable to send message!\n" + e.getMessage() + "\n");
            }
        }
    }

    public void disconnect(){
        if(strOut != null){
            send("bye");
        }
        ClientMain.clientFrame.resetGUI();
    }

    public void closeAll(){
        try{
            if(strIn != null)  strIn.close();
            if(strOut != null) strOut.close();
            if(socket != null) socket.close();
        } catch (IOException e){
            ClientMain.clientFrame.appendText(ClientMain.clientFrame.displayText,
                    "Unable to close:" + '\n' + e.getMessage() + "\n\n");
        }
    }
}