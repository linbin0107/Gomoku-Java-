import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class ServerThread extends Thread {
	Socket clientSocket;
	Hashtable<Socket, DataOutputStream> clientDataHash;
	Hashtable<Socket, Object> clientNameHash;
	Hashtable<Object, Object> chessPeerHash;
	ServerMsgPanel serverMsgPanel;
	boolean isClientClosed = false;

	public ServerThread(Socket clientSocket,
			Hashtable<Socket, DataOutputStream> clientDataHash,
			Hashtable<Socket, Object> clientNameHash,
			Hashtable<Object, Object> chessPeerHash, ServerMsgPanel server) {
		this.clientSocket = clientSocket;
		this.clientDataHash = clientDataHash;
		this.clientNameHash = clientNameHash;
		this.chessPeerHash = chessPeerHash;
		this.serverMsgPanel = server;
	}

	public void dealWithMsg(String msgReceived) {
		// String clientName;
		String peerName;
		if (msgReceived.startsWith("/")) {
			if (msgReceived.equals("/list")) {
				// the msg for refreshing user list
				Feedback(getUserList());
			} else if (msgReceived.startsWith("/creatgame [inchess]")) {
				// the msg received for creating game
				String gameCreaterName = msgReceived.substring(20);
				synchronized (clientNameHash) {
					// put the port number of client into user list
					clientNameHash.put(clientSocket, msgReceived.substring(11));
				}
				synchronized (chessPeerHash) {
					// make the host waiting
					chessPeerHash.put(gameCreaterName, "wait");
				}
				Feedback("/yourname " + clientNameHash.get(clientSocket));
				sendGamePeerMsg(gameCreaterName, "/OK");
				sendPublicMsg(getUserList());
			} else if (msgReceived.startsWith("/joingame ")) {
				// the msg received for joining the game
				StringTokenizer userTokens = new StringTokenizer(msgReceived,
						" ");
				String userToken;
				String gameCreatorName;
				String gamePaticipantName;
				String[] playerNames = { "0", "0" };
				int nameIndex = 0;
				while (userTokens.hasMoreTokens()) {
					userToken = (String) userTokens.nextToken(" ");
					if (nameIndex >= 1 && nameIndex <= 2) {
						// get the command of player
						playerNames[nameIndex - 1] = userToken;
					}
					nameIndex++;
				}
				gameCreatorName = playerNames[0];
				gamePaticipantName = playerNames[1];
				if (chessPeerHash.containsKey(gameCreatorName)
						&& chessPeerHash.get(gameCreatorName).equals("wait")) {
					// The game is created already
					synchronized (clientNameHash) {
						// add the socket and namer of game participant
						clientNameHash.put(clientSocket,
								("[inchess]" + gamePaticipantName));
					}
					synchronized (chessPeerHash) {
						// add the pair of game creater and paticipant
						chessPeerHash.put(gameCreatorName, gamePaticipantName);
					}
					sendPublicMsg(getUserList());
					// send msg to the game participant
					sendGamePeerMsg(gamePaticipantName,
							("/peer " + "[inchess]" + gameCreatorName));
					// send msg to the creater of game
					sendGamePeerMsg(gameCreatorName,
							("/peer " + "[inchess]" + gamePaticipantName));
				}  else {
					// reject join in if the game is not created yet
					sendGamePeerMsg(gamePaticipantName, "/reject");
					try {
						closeClient();
					} catch (Exception ez) {
						ez.printStackTrace();
					}
				}
			} else if (msgReceived.startsWith("/chat ")) {
				serverMsgPanel.msgTextArea.append(msgReceived + "\n");
				//String chatMsg = msgReceived.substring(7);
				//System.out.println("chat message has been received!");
				sendPublicMsg(msgReceived);
			}
			else if (msgReceived.startsWith("/[inchess]")) {
				// the msg received when playing game
				int firstLocation = 0, lastLocation;
				lastLocation = msgReceived.indexOf(" ", 0);
				peerName = msgReceived.substring((firstLocation + 1),
						lastLocation);
				msgReceived = msgReceived.substring((lastLocation + 1));
				if (sendGamePeerMsg(peerName, msgReceived)) {
					Feedback("/error");
				}
			} else if (msgReceived.startsWith("/giveup ")) {
				// The msg receive for quitting game
				String chessClientName = msgReceived.substring(8);
				if (chessPeerHash.containsKey(chessClientName)
						&& !((String) chessPeerHash.get(chessClientName))
								.equals("wait")) {
					// send msg to winner if the winner is participant
					sendGamePeerMsg(
							(String) chessPeerHash.get(chessClientName),
							"/youwin");
					synchronized (chessPeerHash) {
						// delete the player who quits the game
						chessPeerHash.remove(chessClientName);
					}
				}
				if (chessPeerHash.containsValue(chessClientName)) {
					// send msg to winner if the winner is game creater
					sendGamePeerMsg(
							(String) getHashKey(chessPeerHash, chessClientName),
							"/youwin");
					synchronized (chessPeerHash) {// delete the player who quits
													// the game
						chessPeerHash.remove((String) getHashKey(chessPeerHash,
								chessClientName));
					}
				}
			} else { // the msg received is sth. else
				int lastLocation = msgReceived.indexOf(" ", 0);
				if (lastLocation == -1) {
					Feedback("Invalid Command");
					return;
				}
			}
		} else {
			msgReceived = clientNameHash.get(clientSocket) + ">" + msgReceived;
			serverMsgPanel.msgTextArea.append(msgReceived + "\n");
			sendPublicMsg(msgReceived);
			serverMsgPanel.msgTextArea
					.setCaretPosition(serverMsgPanel.msgTextArea.getText()
							.length());
		}
	}

	// send public info.
	public void sendPublicMsg(String publicMsg) {
		synchronized (clientDataHash) {
			for (Enumeration enu = clientDataHash.elements(); enu
					.hasMoreElements();) {
				DataOutputStream outputData = (DataOutputStream) enu
						.nextElement();
				try {
					outputData.writeUTF(publicMsg);
				} catch (IOException es) {
					es.printStackTrace();
				}
			}
		}
	}

	// send msg to a specific user in the game
	public boolean sendGamePeerMsg(String gamePeerTarget, String gamePeerMsg) {
		for (Enumeration enu = clientDataHash.keys(); enu.hasMoreElements();) {
			// traverse to get the socket of client in the game
			Socket userClient = (Socket) enu.nextElement();
			if (gamePeerTarget.equals((String) clientNameHash.get(userClient))
					&& !gamePeerTarget.equals((String) clientNameHash
							.get(clientSocket))) {
				// find the user to whom the message is sent
				synchronized (clientDataHash) {
					// create the output stream
					DataOutputStream peerOutData = (DataOutputStream) clientDataHash
							.get(userClient);
					try {
						// send the message
						peerOutData.writeUTF(gamePeerMsg);
					} catch (IOException es) {
						es.printStackTrace();
					}
				}
				return false;
			}
		}
		return true;
	}

	// send message to the user who connect to the server
	public void Feedback(String feedBackMsg) {
		synchronized (clientDataHash) {
			DataOutputStream outputData = (DataOutputStream) clientDataHash
					.get(clientSocket);
			try {
				outputData.writeUTF(feedBackMsg);
			} catch (Exception eb) {
				eb.printStackTrace();
			}
		}
	}

	// get user list
	public String getUserList() {
		String userList = "/userlist";
		for (Enumeration enu = clientNameHash.elements(); enu.hasMoreElements();) {
			userList = userList + " " + (String) enu.nextElement();
		}
		return userList;
	}

	// get key from hash table based on value
	public Object getHashKey(Hashtable targetHash, Object hashValue) {
		Object hashKey;
		for (Enumeration enu = targetHash.keys(); enu.hasMoreElements();) {
			hashKey = (Object) enu.nextElement();
			if (hashValue.equals((Object) targetHash.get(hashKey)))
				return hashKey;
		}
		return null;
	}

	// this method is called when connecting to the server
	public void sendInitMsg() {
		sendPublicMsg(getUserList());
		Feedback("/yourname " + (String) clientNameHash.get(clientSocket));
	}

	public void closeClient() {
		serverMsgPanel.msgTextArea.append("User "
				+ clientNameHash.get(clientSocket) + " left!\n");
		synchronized (chessPeerHash) {
			if (chessPeerHash.containsKey(clientNameHash.get(clientSocket))) {
				chessPeerHash.remove((String) clientNameHash.get(clientSocket));
			}
			if (chessPeerHash.containsValue(clientNameHash.get(clientSocket))) {
				chessPeerHash.put(
						(String) getHashKey(chessPeerHash,
								(String) clientNameHash.get(clientSocket)),
						"tobeclosed");
			}
		}
		// delete the user information
		synchronized (clientDataHash) {
			clientDataHash.remove(clientSocket);
		}
		synchronized (clientNameHash) {
			clientNameHash.remove(clientSocket);
		}
		sendPublicMsg(getUserList());
		serverMsgPanel.statusLabel.setText("Current connected users:"
				+ clientDataHash.size());
		try {
			clientSocket.close();
		} catch (IOException exx) {
			exx.printStackTrace();
		}
		isClientClosed = true;
	}

	public void run() {
		DataInputStream inputData = null;
		synchronized (clientDataHash) {
			serverMsgPanel.statusLabel
					.setText("The number of current connected:"
							+ clientDataHash.size());
		}
		try { // waiting for the msg from clients
			inputData = new DataInputStream(clientSocket.getInputStream());
			sendInitMsg();
			while (true) {
				//System.out.println("cc");
				//serverMsgPanel.msgTextArea.append("ccc\n");
				String message = inputData.readUTF();
				serverMsgPanel.msgTextArea.append(message + " RUN\n");
				//System.out.println(message);
				dealWithMsg(message);
			}
		} catch (IOException esx) {
		} finally {
			if (!isClientClosed) {
				closeClient();
			}
		}
	}
}
