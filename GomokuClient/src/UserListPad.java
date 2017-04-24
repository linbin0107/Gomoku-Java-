import java.awt.BorderLayout;
import java.awt.List;

import javax.swing.JPanel;


public class UserListPad extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public List userList = new List(10);
	
	public UserListPad() {
		setLayout(new BorderLayout());
		userList.add("No user");
		add(userList, BorderLayout.CENTER);
	}
}
