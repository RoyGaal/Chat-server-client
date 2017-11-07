
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientMain extends JFrame implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static ClientMain clientFrame;
    public static ClientServer server;

    // Text Areas
    public JTextPane displayText = new JTextPane();
    private JTextArea input = new JTextArea(4, 1);
    private JTextField usernameInput = new JTextField();
    private JScrollPane displayScrollBar;

    // Buttons
    public  JButton btnConnect = new JButton("Login");
    public JButton btnSend = new JButton("Send (TCP)");
    public JButton UDPSend = new JButton("Send (UDP)");
    public JButton btnQuit = new JButton("Sign Out");

     // JPanels
    private JPanel mainJP = new JPanel();
    private JPanel displayJP = new JPanel();
    private JPanel btnsJP = new JPanel();

    // JLabels
    private JLabel usernameLbl = new JLabel("Username:");

    public ClientMain(){

        // Text Areas
        displayText.setBackground(Color.black);
        displayText.setEditable(false);
        displayText.setBorder(BorderFactory.createEtchedBorder());
        input.setLineWrap(true);
        input.setBorder(BorderFactory.createEtchedBorder());

        // Scroll Bar
        displayScrollBar = new JScrollPane(displayText);
        displayScrollBar.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        displayScrollBar.setBorder(null);

        mainJP.setLayout(new BorderLayout());
        displayJP.setLayout(new BorderLayout());
        displayJP.add(displayScrollBar, BorderLayout.CENTER);
        displayJP.add(input, BorderLayout.SOUTH);
        btnsJP.setLayout(new GridLayout(1,6));

        // Labels
        usernameLbl.setHorizontalAlignment(JLabel.RIGHT);
        usernameLbl.setBackground(Color.white);
        

        // Buttons
        btnSend.setEnabled(false);
        UDPSend.setEnabled(false);
        btnQuit.setEnabled(false);
        btnConnect.addActionListener(this);
        btnSend.addActionListener(this);
        UDPSend.addActionListener(this);
        btnQuit.addActionListener(this);

        btnsJP.add(usernameLbl);
        btnsJP.add(usernameInput);
        btnsJP.add(btnConnect);
        btnsJP.add(btnSend);
        btnsJP.add(UDPSend);
        btnsJP.add(btnQuit);

        mainJP.add(displayJP, BorderLayout.CENTER);
        mainJP.add(btnsJP, BorderLayout.SOUTH);

        add(mainJP);
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource(); //Getting the object from the mouse clicked

        // Connect
        if(source.equals(btnConnect)){
            if(usernameInput.getText().trim().isEmpty()){
                appendText(displayText,"Please enter a username to connect.\n\n");
            }
            else{
                server = new ClientServer(usernameInput.getText());
                usernameInput.setText("");
                //enable buttons:
                btnSend.setEnabled(true);
                UDPSend.setEnabled(true);
                btnQuit.setEnabled(true);
            }
        }
        
        else if(source.equals(btnSend)){
        	// Send Public Message
            if(usernameInput.getText().trim().isEmpty() && !input.getText().trim().isEmpty()){
                server.send(input.getText());
                input.setText("");
            }
            else{
                // Send Private Message
                if(!input.getText().trim().isEmpty()){
                    String message = "secret" + usernameInput.getText() + "/" + input.getText();
                    appendText(displayText, "\n" + server.username + ": " + input.getText());
                    server.send(message);
                    input.setText("");
                }
            }
        }
        
        //Send UDP Message
        else if(source.equals(UDPSend)){
        	String message = input.getText();
        	String txt = "\n" + "(UDP message) " + ClientServer.username + ": " + message + "\n";
        	//appendText(displayText, txt);
            server.sendUDP(txt.getBytes());
            input.setText("");
        }
        
        // Disconnect
        else if(source.equals(btnQuit)){
            server.connected = false;
            ClientMain.server.disconnect();
            btnSend.setEnabled(false);
            UDPSend.setEnabled(false);
            btnQuit.setEnabled(false);
        }
    }

    // Reset GUI
    public void resetGUI(){
        btnConnect.setEnabled(true);
        btnSend.setEnabled(false);
        UDPSend.setEnabled(false);
        btnQuit.setEnabled(false);
        appendText(displayText, "Goodbye " + server.username + "!\n\n");
        clientFrame.setTitle("Chat Server");
        clientFrame.server.closeAll();
    }

    // Append Text To JTextPane
    public void appendText(JTextPane txtArea, String message){
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
                clientFrame = new ClientMain();
                clientFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                clientFrame.setTitle("Chat Window");
                clientFrame.setSize(600, 400);
                clientFrame.setLocationRelativeTo(null);
                clientFrame.setFocusable(true);
                clientFrame.setVisible(true);

                // Set Close Operation Listener
                clientFrame.addWindowListener(new java.awt.event.WindowAdapter(){
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent){ closeOperation(); }
                });
            }
        });
    }

    // Close Operation is for the frame "X" close button
    // This ensures that the connection is closed before
    // disposing of the frame
    public static void closeOperation(){
        ClientMain.server.disconnect();
        ClientMain.server.closeAll();
        System.exit(0);
    }
}
