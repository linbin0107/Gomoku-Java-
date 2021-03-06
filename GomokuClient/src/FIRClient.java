import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class FIRClient extends JFrame implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// client socket
	Socket clientSocket;
	DataInputStream inputStream;
	DataOutputStream outputStream;
	String chessClientName = null;
	String host = null;
	int port = 4331;
	boolean isOnChat = false;
	boolean isOnChess = false;
	boolean isGameConnected = false;
	boolean isCreator = false;
	boolean isParticipant = false;
	// user list area
	UserListPad userListPad = new UserListPad();
	// user chat area
	UserChatPad userChatPad = new UserChatPad();
	// user operating area
	UserControlPad userControlPad = new UserControlPad();
	// user input area
	UserInputPad userInputPad = new UserInputPad();
	// game area
	FIRPad firPad = new FIRPad();

	JPanel southPanel = new JPanel();
	JPanel northPanel = new JPanel();
	JPanel centerPanel = new JPanel();
	JPanel eastPanel = new JPanel();

	FIRClientThread clientthread = new FIRClientThread(this);

	public FIRClient() {
		super("Gomoku Client");
		Container container = getContentPane();
		setLayout(new BorderLayout());
		host = userControlPad.ipInputted.getText();

		eastPanel.setLayout(new BorderLayout());
		eastPanel.add(userListPad, BorderLayout.NORTH);
		eastPanel.add(userChatPad, BorderLayout.CENTER);
		eastPanel.setBackground(Color.LIGHT_GRAY);

		// firPad.setSize(440, 440);
		centerPanel.setLayout(new BorderLayout());
		// centerPanel.setBackground(Color.blue);
		//
		// userInputPad.contentInputted.addKeyListener(this);
		//
		// firPad.host = userControlPad.ipInputted.getText();
		centerPanel.add(firPad, BorderLayout.CENTER);
		centerPanel.add(userInputPad, BorderLayout.SOUTH);
		centerPanel.setBackground(Color.LIGHT_GRAY);
		userControlPad.connectButton.addActionListener(this);
		userControlPad.createButton.addActionListener(this);
		userControlPad.joinButton.addActionListener(this);
		userControlPad.cancelButton.addActionListener(this);
		userControlPad.exitButton.addActionListener(this);
		userControlPad.createButton.setEnabled(false);
		userControlPad.joinButton.setEnabled(false);
		userControlPad.cancelButton.setEnabled(false);

		southPanel.add(userControlPad, BorderLayout.CENTER);
		southPanel.setBackground(Color.LIGHT_GRAY);

		container.add(eastPanel, BorderLayout.EAST);
		container.add(centerPanel, BorderLayout.CENTER);
		container.add(southPanel, BorderLayout.SOUTH);
		pack();
		setSize(670, 560);
		setVisible(true);
		setResizable(false);
		this.validate();
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		userInputPad.contentInputted.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String chatMessage = event.getActionCommand().trim();
				if (chatMessage == null || chatMessage.length() <= 0) {
					JOptionPane.showMessageDialog(null,
							"The content cannnot be null!");
				} else {
					clientthread.sendMessage("/chat " + chessClientName + ": "
							+ chatMessage);
					// System.out.println("chat message has sent");
					userInputPad.contentInputted.setText(null);
				}
			}
		});
	}

	public boolean connectToServer(String serverIP, int serverPort)
			throws Exception {
		try {
			// create client socket
			clientSocket = new Socket(serverIP, serverPort);
			inputStream = new DataInputStream(clientSocket.getInputStream());
			outputStream = new DataOutputStream(clientSocket.getOutputStream());
			// create client thread
			// FIRClientThread clientthread = new FIRClientThread(this);
			// start the thread, waiting for chatting
			clientthread.start();
			isOnChat = true;
			return true;
		} catch (IOException ex) {
			userChatPad.chatTextArea.setText("Connecting to server failed!\n");
		}
		return false;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == userControlPad.connectButton) {
			// request to connect to server event
			host = firPad.host = userControlPad.ipInputted.getText();
			try {
				if (connectToServer(host, port)) {
					// When connect successfully, set up some status of client
					userChatPad.chatTextArea.setText("");
					userControlPad.connectButton.setEnabled(false);
					userControlPad.createButton.setEnabled(true);
					userControlPad.joinButton.setEnabled(true);
					firPad.statusText
							.setText("Connecting successfully，please wait!");
				}
			} catch (Exception ei) {
				userChatPad.chatTextArea.setText("Cannot be connected!\n");
			}
		}

		if (e.getSource() == userControlPad.exitButton) {
			// quit the application
			try {
				closeConnection();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.exit(0);
		}

		if (e.getSource() == userControlPad.createButton) { // create game event
			try {
				if (!isGameConnected) { // if it is not in the game
					if (firPad.connectServer(firPad.host, firPad.port)) {
						// if succeed
						isGameConnected = true;
						isOnChess = true;
						isCreator = true;
						userControlPad.createButton.setEnabled(false);
						userControlPad.joinButton.setEnabled(false);
						userControlPad.cancelButton.setEnabled(true);
						firPad.firThread.sendMessage("/creatgame "
								+ "[inchess]" + chessClientName);
					}
				} else {
					isOnChess = true;
					isCreator = true;
					userControlPad.createButton.setEnabled(false);
					userControlPad.joinButton.setEnabled(false);
					userControlPad.cancelButton.setEnabled(true);
					firPad.firThread.sendMessage("/creatgame " + "[inchess]"
							+ chessClientName);
				}
			} catch (Exception ec) {
				isGameConnected = false;
				isOnChess = false;
				isCreator = false;
				userControlPad.createButton.setEnabled(true);
				userControlPad.joinButton.setEnabled(true);
				userControlPad.cancelButton.setEnabled(false);
				ec.printStackTrace();
				userChatPad.chatTextArea.setText("Creating game failed: \n"
						+ ec);
			}
		}

		if (e.getSource() == userControlPad.cancelButton) {
			// quit the game
			if (isOnChess) { // in the game
				firPad.firThread.sendMessage("/giveup " + chessClientName);
				firPad.setVicStatus(-1 * firPad.chessColor);
				userControlPad.createButton.setEnabled(true);
				userControlPad.joinButton.setEnabled(true);
				userControlPad.cancelButton.setEnabled(false);
				firPad.statusText.setText("Please create or join a game!");
			}
			if (!isOnChess) { // not in the game
				userControlPad.createButton.setEnabled(true);
				userControlPad.joinButton.setEnabled(true);
				userControlPad.cancelButton.setEnabled(false);
				firPad.statusText.setText("Please create or join a game!");
			}
			isParticipant = isCreator = false;
		}

		if (e.getSource() == userControlPad.joinButton) { // join game event
			// get the user who you want to play with
			String selectedUser = (String) userListPad.userList
					.getSelectedItem();
			//System.out.println(selectedUser);
			if (selectedUser == null)
				// if no user is selected
				firPad.statusText.setText("Please select an user!");
			else if (selectedUser.startsWith("[inchess]"))
				// the user being selected is in the game
				firPad.statusText
						.setText("The user you selected is in the game, please select another user!");
			else if (selectedUser.equals(chessClientName)) {
				// the user being selected is itself
				firPad.statusText
						.setText("Cannot select yourself, please choose another user!");
			} else { // join the game
				try {
					if (!isGameConnected) {
						if (firPad.connectServer(firPad.host, firPad.port)) {
							isGameConnected = true;
							isOnChess = true;
							isParticipant = true;
							userControlPad.createButton.setEnabled(false);
							userControlPad.joinButton.setEnabled(false);
							userControlPad.cancelButton.setEnabled(true);
							// System.out.println("aa");
							firPad.firThread.sendMessage("/joingame "
									+ selectedUser + " " + chessClientName);
						}
					} else {
						isOnChess = true;
						isParticipant = true;
						userControlPad.createButton.setEnabled(false);
						userControlPad.joinButton.setEnabled(false);
						userControlPad.cancelButton.setEnabled(true);
						// System.out.println("bb");
						firPad.firThread.sendMessage("/joingame "
								+ selectedUser + " " + chessClientName);
					}
				} catch (Exception ee) {
					isGameConnected = false;
					isOnChess = false;
					isParticipant = false;
					userControlPad.createButton.setEnabled(true);
					userControlPad.joinButton.setEnabled(true);
					userControlPad.cancelButton.setEnabled(false);
					userChatPad.chatTextArea
							.setText("Joining the game failed: \n" + ee);
				}
			}
		}
	}

	private void closeConnection() throws IOException {
		outputStream.close();
		inputStream.close();
		clientSocket.close();
	}

	public static void main(String args[]) {
		FIRClient chessClient = new FIRClient();
	}

}
