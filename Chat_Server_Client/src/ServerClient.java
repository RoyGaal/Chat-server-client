
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerClient implements Runnable {
    private Socket socket;
    public  String username;
    private DataInputStream strIn = null;
    private DataOutputStream strOut = null;
    public  Thread thread;

    // Server Client
    public ServerClient(Socket socket, DataInputStream strIn, DataOutputStream strOut, String username){
        this.socket = socket;
        this.strIn  = strIn;
        this.strOut = strOut;
        this.username = username;

        run();
    }

    // Run Thread to handle client
    @Override
    public void run(){
        Thread thread = new Thread(new Runnable() {
            public void run(){
            while(true){
                try{
                    String message = strIn.readUTF();
                    ServerMain.handleMessage(username, message);
                } catch (IOException e){
                    ServerMain.appendText(username + " has disconnected from the server.\n\n");
                    break;
                }
            }
            disconnect();
            }
        });
        thread.start();
    }

    public void send(String message){
        try{ strOut.writeUTF(message);
        } catch (IOException e){
            ServerMain.appendText("Error: sending data to client" + '\n' + e.getMessage() + "\n\n");
        }
    }

    public void disconnect(){
        try{
            if(strIn != null)  strIn.close();
            if(strOut != null) strOut.close();
            if(socket != null) socket.close();
        } catch (IOException e){
            ServerMain.appendText("Error: Unable to close:" + '\n' + e.getMessage() + "\n\n");
        }
    }
}