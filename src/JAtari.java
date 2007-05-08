import javax.swing.JApplet;
import java.awt.*;

public class JAtari extends JEmu
{
	public void init()
	{
		memory = new Memory(64);
		cpu = new m6502();
	}

	public void paint(Graphics g) 
	{
	}
}
