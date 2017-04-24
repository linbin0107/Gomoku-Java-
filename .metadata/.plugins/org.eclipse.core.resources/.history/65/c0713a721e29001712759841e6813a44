import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class UserControlPad extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JLabel ipLabel = new JLabel("IP", JLabel.LEFT);
	public JTextField ipInputted = new JTextField("localhost", 10);
	public JButton connectButton = new JButton("ConnectToServer");
	public JButton createButton = new JButton("CreateGame");
	public JButton joinButton = new JButton("JoinGame");
	public JButton cancelButton = new JButton("CancelGame");
	public JButton exitButton = new JButton("Quit");
	
	public UserControlPad() {
		setLayout(new FlowLayout(FlowLayout.LEFT));
		setBackground(Color.LIGHT_GRAY);
		ipInputted.setEditable(false);
		add(ipLabel);
		add(ipInputted);
		add(connectButton);
		add(createButton);
		add(joinButton);
		add(cancelButton);
		add(exitButton);
	}
}
