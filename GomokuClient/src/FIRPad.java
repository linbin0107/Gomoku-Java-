import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.JPanel;

public class FIRPad extends JPanel implements MouseListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8713591647928216219L;
	public boolean isMouseEnabled = false;
	// whether win or not
	public boolean isWinned = false;
	// whether in game
	public boolean isGaming = false;
	// the X coordinate of piece
	public int chessX_POS = -1;
	// the Y coordinate of piece
	public int chessY_POS = -1;
	// color of pieces
	public int chessColor = 1;
	// array of X coordinate of black
	public int chessBlack_XPOS[] = new int[200];
	// array of Y coordinate of black
	public int chessBlack_YPOS[] = new int[200];
	// array of X coordinate of white
	public int chessWhite_XPOS[] = new int[200];
	// array of Y coordinate of black
	public int chessWhite_YPOS[] = new int[200];
	// the number of black pieces
	public int chessBlackCount = 0;
	// the number of white pieces
	public int chessWhiteCount = 0;
	// the times of win of black
	public int chessBlackVicTimes = 0;
	// the times of win of white
	public int chessWhiteVicTimes = 0;
	String host = null;
	int port = 4331;
	public Socket chessSocket;
	public DataInputStream inputData = null;
	public DataOutputStream outputData = null;
	public String chessSelfName = null;
	public String chessPeerName = null;
	public TextField statusText = new TextField("Please connect to the server！");
	public Label property = new Label("");
	public FIRThread firThread = new FIRThread(this);

	public FIRPad() {
		// super();
		// setLayout(new BorderLayout());
		setSize(480, 480);
		setLayout(null);
		// setBackground(Color.YELLOW);
		addMouseListener(this);
		add(statusText, BorderLayout.NORTH);
		add(property, BorderLayout.SOUTH);
		property.setBounds(new Rectangle(180, 410, 50, 24));
		property.setVisible(false);
		statusText.setBounds(new Rectangle(40, 5, 360, 24));
		statusText.setEditable(false);
	}

	// connect to server
	public boolean connectServer(String ServerIP, int ServerPort)
			throws Exception {
		try {
			// create a client socket
			chessSocket = new Socket(ServerIP, ServerPort);
			// create input stream
			inputData = new DataInputStream(chessSocket.getInputStream());
			// create output stream
			outputData = new DataOutputStream(chessSocket.getOutputStream());
			firThread.start();
			return true;
		} catch (IOException ex) {
			statusText.setText("Connecting to server failed! \n");
		}
		return false;
	}

	// set the win status of board
	public void setVicStatus(int vicChessColor) {
		// clear board
		this.removeAll();
		// set the location of black user to zero
		for (int i = 0; i <= chessBlackCount; i++) {
			chessBlack_XPOS[i] = 0;
			chessBlack_YPOS[i] = 0;
		}
		// set the location of white user to zero
		for (int i = 0; i <= chessWhiteCount; i++) {
			chessWhite_XPOS[i] = 0;
			chessWhite_YPOS[i] = 0;
		}
		// clear the black pieces
		chessBlackCount = 0;
		// clear the white pieces
		chessWhiteCount = 0;
		add(statusText);
		statusText.setBounds(40, 5, 360, 24);
		if (vicChessColor == 1) { // The black wins
			chessBlackVicTimes++;
			statusText.setText("The black wins, BLACK : WHITE = "
					+ chessBlackVicTimes + ":" + chessWhiteVicTimes
					+ ", Game restarted, waiting for the white...");
			// add(property, BorderLayout.SOUTH);
			// property.setVisible(true);
			// property.setText("Black");
		} else if (vicChessColor == -1) { // The white wins
			chessWhiteVicTimes++;
			statusText.setText("The white wins, BLACK : WHITE = "
					+ chessBlackVicTimes + ":" + chessWhiteVicTimes
					+ ", Game restarted, waiting for the black...");
			// add(property, BorderLayout.SOUTH);
			// property.setVisible(true);
			// property.setText("White");
		}
	}

	// Get the position of the piece
	public void setLocation(int xPos, int yPos, int chessColor) {
		if (chessColor == 1) { // black piece
			chessBlack_XPOS[chessBlackCount] = xPos * 20;
			chessBlack_YPOS[chessBlackCount] = yPos * 20;
			chessBlackCount++;
		} else if (chessColor == -1) { // white piece
			chessWhite_XPOS[chessWhiteCount] = xPos * 20;
			chessWhite_YPOS[chessWhiteCount] = yPos * 20;
			chessWhiteCount++;
		}
	}

	// check current status to see if one wins
	public boolean checkVicStatus(int xPos, int yPos, int chessColor) {
		int chessLinkedCount = 1; // number of connected pieces
		int chessLinkedCompare = 1;
		int chessToCompareIndex = 0;
		int closeGrid = 1;
		if (chessColor == 1) {
			chessLinkedCount = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if (((xPos + closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos * 20) == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount = chessLinkedCount + 1;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if (((xPos - closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& (yPos * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}

			chessLinkedCount = 1;
			chessLinkedCompare = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if ((xPos * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos + closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if ((xPos * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos - closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			chessLinkedCount = 1;
			chessLinkedCompare = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if (((xPos - closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos + closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if (((xPos + closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos - closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			chessLinkedCount = 1;
			chessLinkedCompare = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if (((xPos + closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos + closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessBlackCount; chessToCompareIndex++) {
					if (((xPos - closeGrid) * 20 == chessBlack_XPOS[chessToCompareIndex])
							&& ((yPos - closeGrid) * 20 == chessBlack_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
		} else if (chessColor == -1) { // if white pieces
			chessLinkedCount = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if (((xPos + closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& (yPos * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if (((xPos - closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& (yPos * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			chessLinkedCount = 1;
			chessLinkedCompare = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if ((xPos * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& ((yPos + closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if ((xPos * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& ((yPos - closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			chessLinkedCount = 1;
			chessLinkedCompare = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if (((xPos - closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& ((yPos + closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if (((xPos + closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& ((yPos - closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			chessLinkedCount = 1;
			chessLinkedCompare = 1;
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if (((xPos + closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& ((yPos + closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return true;
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
			for (closeGrid = 1; closeGrid <= 4; closeGrid++) {
				for (chessToCompareIndex = 0; chessToCompareIndex <= chessWhiteCount; chessToCompareIndex++) {
					if (((xPos - closeGrid) * 20 == chessWhite_XPOS[chessToCompareIndex])
							&& ((yPos - closeGrid) * 20 == chessWhite_YPOS[chessToCompareIndex])) {
						chessLinkedCount++;
						if (chessLinkedCount == 5) {
							return (true);
						}
					}
				}
				if (chessLinkedCount == (chessLinkedCompare + 1)) {
					chessLinkedCompare++;
				} else {
					break;
				}
			}
		}
		return false;
	}

	public void paint(Graphics g) {
		Color c = new Color(255, 176, 98);
		g.setColor(c);
		g.fillRect(40, 40, 360, 360);
		g.setColor(Color.black);

		for (int i = 40; i <= 380; i = i + 20) {
			g.drawLine(40, i, 400, i);
		}
		g.drawLine(40, 400, 400, 400);
		for (int j = 40; j <= 380; j = j + 20) {
			g.drawLine(j, 40, j, 400);
		}
		g.drawLine(400, 40, 400, 400);
		g.fillOval(97, 97, 6, 6);
		g.fillOval(337, 97, 6, 6);
		g.fillOval(97, 337, 6, 6);
		g.fillOval(337, 337, 6, 6);
		g.fillOval(217, 217, 6, 6);
	}

	// paint pieces
	public void paintFirPoint(int xPos, int yPos, int chessColor) {
		FIRPaintBlack firPBlack = new FIRPaintBlack(this);
		FIRPaintWhite firPWhite = new FIRPaintWhite(this);
		if (chessColor == 1 && isMouseEnabled) {
			// black, set the position of piece
			setLocation(xPos, yPos, chessColor);
			// get the current situation
			isWinned = checkVicStatus(xPos, yPos, chessColor);
			if (isWinned == false) { // game is not over
				firThread.sendMessage("/" + chessPeerName + " /chess " + xPos
						+ " " + yPos + " " + chessColor);
				// add the piece
				this.add(firPBlack);
				// set the bound of piece
				firPBlack.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				statusText.setText("Black (No. " + chessBlackCount + " steps)("
						+ xPos + " " + yPos + "), the white's turn!");
				// disable the mouse
				isMouseEnabled = false;
			} else { // Game over
				firThread.sendMessage("/" + chessPeerName + " /chess " + xPos
						+ " " + yPos + " " + chessColor);
				this.add(firPBlack);
				firPBlack.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				// call set victory status, passing the black wins
				setVicStatus(1);
				// add(property, BorderLayout.SOUTH);
				// property.setVisible(true);
				// property.setText("Black");
				isMouseEnabled = false;
			}
		} else if (chessColor == -1 && isMouseEnabled) { // the white
			setLocation(xPos, yPos, chessColor);
			isWinned = checkVicStatus(xPos, yPos, chessColor);
			if (isWinned == false) {
				firThread.sendMessage("/" + chessPeerName + " /chess " + xPos
						+ " " + yPos + " " + chessColor);
				this.add(firPWhite);
				firPWhite.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				statusText.setText("White (No. " + chessWhiteCount + " steps)("
						+ xPos + " " + yPos + "), the black's turn");
				isMouseEnabled = false;
			} else {
				firThread.sendMessage("/" + chessPeerName + " /chess " + xPos
						+ " " + yPos + " " + chessColor);
				this.add(firPWhite);
				firPWhite.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				// call set victory status, passing the white wins
				setVicStatus(-1);
				// add(property, BorderLayout.SOUTH);
				// property.setVisible(true);
				// property.setText("White");
				isMouseEnabled = false;
			}
		}
	}

	public void paintNetFirPoint(int xPos, int yPos, int chessColor) {
		FIRPaintBlack firPBlack = new FIRPaintBlack(this);
		FIRPaintWhite firPWhite = new FIRPaintWhite(this);
		setLocation(xPos, yPos, chessColor);
		if (chessColor == 1) {
			isWinned = checkVicStatus(xPos, yPos, chessColor);
			if (isWinned == false) {
				this.add(firPBlack);
				firPBlack.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				statusText.setText("Black (No. " + chessBlackCount + " steps)("
						+ xPos + " " + yPos + "), the white's turn!");
				isMouseEnabled = true;
			} else {
				firThread.sendMessage("/" + chessPeerName + " /victory "
						+ chessColor);
				this.add(firPBlack);
				firPBlack.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				setVicStatus(1);
				isMouseEnabled = true;
			}
		} else if (chessColor == -1) {
			isWinned = checkVicStatus(xPos, yPos, chessColor);
			if (isWinned == false) {
				this.add(firPWhite);
				firPWhite.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				statusText.setText("White (No. " + chessWhiteCount + " steps)("
						+ xPos + " " + yPos + "), the black's turn!");
				isMouseEnabled = true;
			} else {
				firThread.sendMessage("/" + chessPeerName + " /victory "
						+ chessColor);
				this.add(firPWhite);
				firPWhite.setBounds(xPos * 20 - 7, yPos * 20 - 7, 16, 16);
				setVicStatus(-1);
				isMouseEnabled = true;
			}
		}
	}

	// capture mouse event
	public void mousePressed(MouseEvent e) {
		if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
			chessX_POS = (int) e.getX();
			chessY_POS = (int) e.getY();
			int a = (chessX_POS + 10) / 20, b = (chessY_POS + 10) / 20;
			if (chessX_POS / 20 < 2 || chessY_POS / 20 < 2
					|| chessX_POS / 20 > 19 || chessY_POS / 20 > 19) {
				// if the position is not correct, do nothing
			} else {
				paintFirPoint(a, b, chessColor);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub

	}

}
