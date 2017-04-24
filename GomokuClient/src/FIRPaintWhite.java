import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;



public class FIRPaintWhite extends Canvas
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	FIRPad padBelonged; // board for white user

	public FIRPaintWhite(FIRPad padBelonged)
	{
		setSize(20, 20); //set the size of piece
		this.padBelonged = padBelonged;
	}

	public void paint(Graphics g)
	{ // paint piece
		g.setColor(Color.white);
		g.fillOval(0, 0, 14, 14);
	}
}
