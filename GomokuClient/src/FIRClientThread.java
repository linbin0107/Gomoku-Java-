import java.io.IOException;
import java.util.StringTokenizer;

public class FIRClientThread extends Thread {
	public FIRClient firClient;

	public FIRClientThread(FIRClient firClient) {
		this.firClient = firClient;
	}

	public void dealWithMsg(String msgReceived) {
		if (msgReceived.startsWith("/userlist ")) {
			// if the message about user list
			StringTokenizer userToken = new StringTokenizer(msgReceived, " ");
			// System.out.println((String)userToken.nextToken(" "));
			int userNumber = 0;
			// clear user list
			firClient.userListPad.userList.removeAll();
			// clear client user drop down menu
			firClient.userInputPad.userChoice.removeAllItems();
			// add a new item to drop down menu
			firClient.userInputPad.userChoice.addItem("All users");
			while (userToken.hasMoreTokens()) {
				// get the user information
				String user = (String) userToken.nextToken(" ");
				// System.out.println(user);
				if (userNumber > 0 && !user.startsWith("[inchess]")) {
				//	if (userNumber > 0) {
					// add the user to the user list
					firClient.userListPad.userList.add(user);
					// add the user to the drop down menu
					firClient.userInputPad.userChoice.addItem(user);
				}
				userNumber++;
			}

			firClient.userInputPad.userChoice.setSelectedIndex(0);
		} else if (msgReceived.startsWith("/yourname ")) {

			firClient.chessClientName = msgReceived.substring(10);

			firClient.setTitle("Gobang " + " Username: "
					+ firClient.chessClientName);
		} else if (msgReceived.equals("/reject")) {

			try {
				firClient.firPad.statusText.setText("Can't join the game!");
				firClient.userControlPad.cancelButton.setEnabled(false);
				firClient.userControlPad.joinButton.setEnabled(true);
				firClient.userControlPad.createButton.setEnabled(true);
			} catch (Exception ef) {
				firClient.userChatPad.chatTextArea.setText("Cannot close!");
			}
			firClient.userControlPad.joinButton.setEnabled(true);
		} else if (msgReceived.startsWith("/peer ")) {
			// the message indicates waiting in the game
			firClient.firPad.chessPeerName = msgReceived.substring(6);
			if (firClient.isCreator) {
				// creator of game, black and play first
				firClient.firPad.chessColor = 1;
				firClient.firPad.isMouseEnabled = true;
				firClient.firPad.statusText.setText("The black play...");
				firClient.firPad.property.setVisible(true);
				firClient.firPad.property.setText("Black");
			} else if (firClient.isParticipant) {
				// participant of game, white and play later
				firClient.firPad.chessColor = -1;
				firClient.firPad.property.setVisible(true);
				firClient.firPad.property.setText("White");
				firClient.firPad.statusText
						.setText("Joining the game successfully, waiting for the player.");
			}
		} else if (msgReceived.equals("/youwin")) {
			// the message of win
			firClient.isOnChess = false;
			firClient.firPad.setVicStatus(firClient.firPad.chessColor);
			// if (firClient.isCreator) {
			// firClient.firPad.property.setVisible(true);
			// firClient.firPad.property.setText("Black");
			// } else {
			// firClient.firPad.property.setVisible(true);
			// firClient.firPad.property.setText("White");
			// }
			firClient.firPad.statusText.setText("The other player quits");
			firClient.firPad.isMouseEnabled = false;
		} else if (msgReceived.equals("/OK")) {
			// create game successfully
			firClient.firPad.statusText
					.setText("Game is created, waiting for player!");
		} else if (msgReceived.startsWith("/chat ")) {
			String chatMsg = msgReceived.substring(5);
			//System.out.println(msgReceived);
			//System.out.println("chatMsg: " + chatMsg);
			firClient.userChatPad.chatTextArea.append(chatMsg + "\n");
			firClient.userChatPad.chatTextArea
					.setCaretPosition(firClient.userChatPad.chatTextArea
							.getText().length());

		} else if (msgReceived.equals("/error")) {
			// error message
			firClient.userChatPad.chatTextArea
					.append("Error, quit the game.\n");
		} else {
			firClient.userChatPad.chatTextArea.append(msgReceived + "\n");
			firClient.userChatPad.chatTextArea
					.setCaretPosition(firClient.userChatPad.chatTextArea
							.getText().length());
		}
	}

	// // send message
	public void sendMessage(String sndMessage) {
		try {
			//System.out.println(sndMessage);
			if (!(sndMessage == null || sndMessage.length() <= 0)) {
				firClient.outputStream.writeUTF(sndMessage);
				firClient.outputStream.flush();
			}
		} catch (Exception ea) {
			ea.printStackTrace();
		}
	}

	public void run() {
		String message = "";
		try {
			while (true) {
				// waiting for the message
				message = firClient.inputStream.readUTF();
				dealWithMsg(message);
			}
		} catch (IOException es) {
		}
	}
}
