import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class UserInputPad extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JTextField contentInputted = new JTextField("", 30);
	public JComboBox<String> userChoice = new JComboBox<String>();

	public UserInputPad()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		userChoice.addItem("No user");
		userChoice.setSize(60, 24);

		add(userChoice);
		add(contentInputted);
	}

}
