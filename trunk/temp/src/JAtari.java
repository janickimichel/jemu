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
		// architecturs
		memory = new Memory(64);
		cpu = new m6502();
		Device pia = new PIA6532();
		video = new TIA1A();

		// add devices
		devices = new Device[1];
		devices[0] = pia;

		// memory maps
		memory.addMemoryMap(0x0, 0x7F, video);
		memory.addMemoryMap(0x294, 0x297, pia);

		// load ROM
		loadROM("rom/atari2600/simple.bin");
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
				memory.setDirect(pos, ((Integer)c).shortValue());
				pos++;
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public void step()
	{
		int cycles = cpu.step();
		video.step(cycles * 3);
		for(Device d: devices)
			d.step(cycles);
	}
}
