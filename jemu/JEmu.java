import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.net.*;
import java.io.*;

abstract class JEmu extends JApplet implements Runnable
{
	/*
	 * Fields
	 */
	public CPU cpu;
	public Video video;
	public List<Device> devices = new ArrayList<Device>();
	public static JSObject Window = null;
	public static JEmu platform;

	private short data[];
	public static boolean running = false;
	private Thread thread = null;

	/*
	 * Applet methods
	 */
	public JEmu()
	{
		JEmu.platform = this;
	}

	public void start()
	{
		JEmu.Window = JSObject.getWindow(this);
		// JSObject title = (JSObject)JEmu.Window.eval("document.title");
		// title.setMember("value", platformName());

		int i = 0;
		JSObject devs = (JSObject)JEmu.Window.eval("document.getElementById('devices');");
		for(Device d: devices)
		{
			devs.setMember("innerHTML", "<input type='checkbox' id='display_device_" + i + "' onclick='show_device(0)'>" + d.name());
			d.htmlField = "dev_table_" + i;
			i++;
		}

		((JSObject)JEmu.Window.eval("document.getElementById('cpu_name');")).setMember("innerHTML", cpu.name());
		((JSObject)JEmu.Window.eval("document.getElementById('video_name');")).setMember("innerHTML", video.name());
	}

	//
	// thread methods
	//
	public void run()
	{
		System.out.println("Started running!");
		while(JEmu.running)
		{
			synchronized(this)
			{
				step();
				if(cpu.breakPoints.hasBkp())
					if(cpu.breakPoints.contains(cpu.IP))
						running = false;

				// ...
			}
		}
		System.out.println("Stopped running!");

		JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
		run.setMember("value", "Run");
		rebuildDebuggers();
	}

	private void threadStart()
	{
		JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
		run.setMember("value", "Pause");
		/*
		Object o[] = new Object[2];
		o[0] = cpu.instructionPointer();
		o[1] = -1;
		JEmu.Window.call("debug_line", o);
		*/

		JEmu.running = true;
		thread = new Thread(this);
		thread.start();
	}

	private void threadSuspend()
	{
		JEmu.running = false;
		try
		{
			thread.join();
			/*
			JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
			run.setMember("value", "Run");
			rebuildDebuggers();
			*/
		} catch(InterruptedException e) { }
	}


	/*
	 * Memory
	 */
	public void loadROM(String file)
	{
		URL url = null;
		List<Short> d = new ArrayList<Short>();

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
				d.add(((Integer)c).shortValue());
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		loadROM(d);
		System.out.println("ROM loaded.");
		rebuildDebuggers();
	}

	void initRAM(int size)
	{
		System.out.print("Initializing memory... ");
		data = new short[size];
		for(short b: data)
			b = 0;
		System.out.println("ok!");
	}

	void setRAM(int pos, short d, int cycles)
	{
		setRAMDirect(pos, d);
	}

	void setRAMDirect(int pos, short d)
	{
		data[pos] = (short)(d & 0xFF);
	}

	void setRAM(int pos, int d, int cycles)
	{
		setRAM(pos, (short)d, cycles);
	}

	short getRAM(int pos)
	{
		return data[pos];
	}

	private void rebuildMemoryDebugger(int pos)
	{
	}

	/*
	 * Screen buttons and controls
	 */
	public void stepButton()
	{
		step();
		rebuildDebuggers();
	}

	public void runButton()
	{
		if(JEmu.running)
			threadSuspend();
		else
			threadStart();
	}

	public void nextScanlineButton()
	{
	}

	public void nextFrameButton()
	{
	}

	public void addBreakpoint(int pos)
	{
		cpu.breakPoints.add(pos);
	}

	public void removeBreakpoint(int pos)
	{
		cpu.breakPoints.remove(pos);
	}

	public void rebuildDebugger(int device)
	{
		JSObject cpu_pos = (JSObject)JEmu.Window.eval("document.getElementById('cpu_pos');");
		int cpu_p = 0;
		try
		{
			cpu_p = Integer.parseInt(cpu_pos.getMember("value").toString(), 16);
		}
		catch(NumberFormatException e)
		{
			Object[] args = { "Invalid number." };
			JEmu.Window.call("alert", args);
			return;
		}

		switch(device)
		{
			case -1:
				cpu.rebuildDebugger(cpu_p);
				break;
			case -2:
				video.rebuildDebugger();
				break;
			case -3:
				rebuildMemoryDebugger(0);
				break;
			default:
				devices.get(device).rebuildDebugger();
		}
	}

	/* 
	 * Private methods
	 */
	private void rebuildDebuggers()
	{
		// rebuild debugger
		for(int i=-3; i<devices.size(); i++)
			rebuildDebugger(i);
	}

	/*
	 * Implement this
	 */
	public abstract void step();
	abstract void loadROM(List<Short> d);
	abstract String platformName();
	abstract void reset();
	String hexSymbol() { return "$"; }
}
