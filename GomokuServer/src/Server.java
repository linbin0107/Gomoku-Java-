import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JFrame;

public class Server extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JButton clearMsgButton = new JButton("Clear List");
	JButton serverStatusButton = new JButton("Server Status");
	JButton closeServerButton = new JButton("Close");
	Panel buttonPanel = new Panel();
	ServerMsgPanel serverMsgPanel = new ServerMsgPanel();
	ServerSocket serverSocket;
	// Bind client socket with output stream
	Hashtable<Socket, DataOutputStream> clientDataHash = new Hashtable<Socket, DataOutputStream>(
			50);
	// Bind client socket with the name of user
	Hashtable<Socket, Object> clientNameHash = new Hashtable<Socket, Object>(50);
	// Bind the creater of game with participant
	Hashtable<Object, Object> chessPeerHash = new Hashtable<Object, Object>(50);

	public Server() {
		super("Gomoku Server");
		setBackground(Color.LIGHT_GRAY);
		buttonPanel.setLayout(new FlowLayout());
		clearMsgButton.setSize(60, 25);
		buttonPanel.add(clearMsgButton);
		clearMsgButton.addActionListener(this);
		serverStatusButton.setSize(75, 25);
		buttonPanel.add(serverStatusButton);
		serverStatusButton.addActionListener(this);
		closeServerButton.setSize(75, 25);
		buttonPanel.add(closeServerButton);
		closeServerButton.addActionListener(this);
		add(serverMsgPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		validate();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		pack();
		setVisible(true);
		setSize(400, 300);
		setResizable(false);
		validate();

		try {
			createServer(4331, serverMsgPanel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Specify port number and panel to create server
	public void createServer(int port, ServerMsgPanel serverMsgPanel)
			throws IOException {
		Socket clientSocket;
		// the number of client connected to the server
		long clientAccessNumber = 1;
		// set up current host
		this.serverMsgPanel = serverMsgPanel;
		try {
			serverSocket = new ServerSocket(port);
			serverMsgPanel.msgTextArea.setText("The server started:"
					+ InetAddress.getLocalHost() + ":"
					+ serverSocket.getLocalPort() + "\n");
			while (true) {
				// Listen to the request from clients
				clientSocket = serverSocket.accept();
				serverMsgPanel.msgTextArea.append("Connected client:"
						+ clientSocket + "\n");
				// create the output stream of client
				DataOutputStream outputData = new DataOutputStream(
						clientSocket.getOutputStream());
				// bind the client socket with output stream
				clientDataHash.put(clientSocket, outputData);
				// bind the client socket with the name of user
				clientNameHash.put(clientSocket,
						("Player" + clientAccessNumber++));
				// create and run the thread in server
				ServerThread thread = new ServerThread(clientSocket,
						clientDataHash, clientNameHash, chessPeerHash,
						serverMsgPanel);
				thread.start();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == clearMsgButton) {
			// clear the information of server
			serverMsgPanel.msgTextArea.setText("");
		}
		if (e.getSource() == serverStatusButton) {
			// Display the information of server
			try {
				serverMsgPanel.msgTextArea.append("Server Info:"
						+ InetAddress.getLocalHost() + ":"
						+ serverSocket.getLocalPort() + "\n");
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
		if (e.getSource() == closeServerButton) {
			// close server
			System.exit(0);
		}
	}

	public static void main(String args[]) {
		Server Server = new Server();
	}
}
