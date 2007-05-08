import javax.swing.JApplet;
import java.awt.*;
import java.net.*;
import java.io.*;

public class JAtari extends JEmu
{
	// 
	// applet methods
	//
	public void init()
	{
		memory = new Memory(64);
		cpu = new m6502();
		loadROM("rom/atari2600/simple.bin");
	}

	public void paint(Graphics g) 
	{
	}

	//
	// overritten methods
	//
	protected void loadROM(String file)
	{
		URL url = null;
		int pos = 0xf000;
		try
		{
			url = new URL(getCodeBase(), file);
		}
		catch(MalformedURLException e) {}
		try
		{
			InputStream in = new BufferedInputStream(url.openStream());
			// BufferedReader bf = new BufferedReader(new InputStreamReader(in));
			int c;
			while((c = in.read()) != -1)
			{
				System.out.println(Integer.toHexString(c));
				memory.setDirect(pos, ((Integer)c).shortValue());
				pos++;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
