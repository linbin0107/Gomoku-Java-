import java.io.IOException;
import java.util.StringTokenizer;

public class FIRThread extends Thread {
	FIRPad currPad; // board of current thread

	public FIRThread(FIRPad currPad) {
		this.currPad = currPad;
	}

	// process the message
	public void dealWithMsg(String msgReceived) {
		if (msgReceived.startsWith("/chess ")) { // the msg recieved indicates
													// that it is in the game
			StringTokenizer userMsgToken = new StringTokenizer(msgReceived, " ");
			// Info about pieces, 0: X coordinate, 1: Y coordinate, 2: color of
			// the piece
			String[] chessInfo = { "-1", "-1", "0" };
			int i = 0; // flag
			String chessInfoToken;
			while (userMsgToken.hasMoreTokens()) {
				chessInfoToken = (String) userMsgToken.nextToken(" ");
				if (i >= 1 && i <= 3) {
					chessInfo[i - 1] = chessInfoToken;
				}
				i++;
			}
			currPad.paintNetFirPoint(Integer.parseInt(chessInfo[0]),
					Integer.parseInt(chessInfo[1]),
					Integer.parseInt(chessInfo[2]));
		} else if (msgReceived.startsWith("/yourname ")) { // the msg received
															// indicates name
															// changing
			currPad.chessSelfName = msgReceived.substring(10);
		} else if (msgReceived.equals("/error")) { // error msg
			currPad.statusText.setText("The user doesn't exit!");
		}
	}

	// send message
	public void sendMessage(String sndMessage) {
		try {
			//System.out.println(sndMessage);
			if (!(sndMessage == null || sndMessage.length() <= 0)){
			currPad.outputData.writeUTF(sndMessage);
			currPad.outputData.flush();
			}
		} catch (Exception ea) {
			ea.printStackTrace();
		}
	}

	public void run() {
		String msgReceived = "";
		try {
			while (true) { // waiting for the input
				msgReceived = currPad.inputData.readUTF();
				dealWithMsg(msgReceived);
			}
		} catch (IOException es) {
		}
	}
}
