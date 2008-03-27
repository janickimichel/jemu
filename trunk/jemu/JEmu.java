import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.net.*;
import java.io.*;

abstract class JEmu extends JApplet implements Runnable
{
	/*
	 * Fields
	 */
	public CPU cpu; // emulador CPU
	public Video video; // emulator Video card
	public List<Device> devices = new ArrayList<Device>(); // emulator other devices

	protected MemoryMaps memoryMaps = new MemoryMaps();
	public static short ram[]; // RAM memory

	public static JSObject Window = null; // HTML window (javascript object)
	public static JEmu platform; // myself

	public static boolean running = false; // is the emulator running?
	private Thread thread = null; // thread that runs the emulator
	public int frameskip = 1; // video frameskip (TODO - move to video?)
	public static int currentFrame = 0; // used to control frameskip

	private Timer updatingTimer = new Timer();

	//
	// Applet methods
	//
	public JEmu()
	{
		JEmu.platform = this;
		// Timer.useTimer = true;
	}

	public void start()
	{
		try
		{
			JEmu.Window = JSObject.getWindow(this);
			// JSObject title = (JSObject)JEmu.Window.eval("document.title");
			// title.setMember("value", platformName());

			int i = 0;
			JSObject devs;
		   	devs = (JSObject)JEmu.Window.eval("document.getElementById('devices');");

			for(Device d: devices)
			{
				devs.setMember("innerHTML", "<input type='checkbox' id='display_device_" + i + "' onclick='show_device(0)'>" + d.name());
				d.htmlField = "dev_table_" + i;
				i++;
			}

			((JSObject)JEmu.Window.eval("document.getElementById('cpu_name');")).setMember("innerHTML", cpu.name());
			((JSObject)JEmu.Window.eval("document.getElementById('video_name');")).setMember("innerHTML", video.name());
		}
		catch(netscape.javascript.JSException e)
		{
			// using appletviewer
			loadROM("rom/atari2600/dot.bin");
			reset();
			runButton();
		}
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void paint(Graphics g) 
	{
		g.drawImage(video.image, 0, 0, this);
	}

	public void destroy()
	{
		System.out.println("CPU    = " + cpu.timer.timeByFrame());
		System.out.println("Video  = " + video.timer.timeByFrame());
		System.out.println("PIA    = " + devices.get(0).timer.timeByFrame());
		System.out.println("Update = " + updatingTimer.timeByFrame());
	}

	//
	// thread methods
	//
	public void run()
	{
		long timer = ((new Date()).getTime() + (1000 / video.fps));

		if(cpu.breakPoints.hasBkp())
		{
			while(JEmu.running)
			{
				step();
					
				// check for breakpoints
				if(cpu.breakPoints.contains(cpu.IP))
				{
					running = false;
					video.drawScreen();
				}
				
				// check if needs to update screen
				if(video.screenDone)
				{
					video.drawScreen();
					video.screenDone = false;
					while(timer > (new Date()).getTime())
						;
					timer = ((new Date()).getTime() + (1000 / video.fps));
				}
			}
		}
		else // no breakpoints 
		{
			while(JEmu.running)
			{
				step();
					
				// check if needs to update screen
				if(video.screenDone)
				{
					updatingTimer.start();

					if(JEmu.currentFrame == 0)
					{
						video.drawScreen();
						getToolkit().sync();
					}

					try
					{
						Thread.sleep(timer - (new Date()).getTime());
					} catch(InterruptedException e) {}
					  catch(java.lang.IllegalArgumentException ex) {}
					timer = ((new Date()).getTime() + (1000 / video.fps));

					video.screenDone = false;
					updatingTimer.stop();
					Timer.frames++;

					JEmu.currentFrame--;
					if(JEmu.currentFrame < 0)
						JEmu.currentFrame = frameskip;
				}
			}
		}

		try
		{
			JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
			run.setMember("value", "Run");
		}
		catch(netscape.javascript.JSException e)
		{
		}
		catch(java.lang.NullPointerException ex)
		{
		}
		rebuildDebuggers();
	}

	// start execution thread
	private void threadStart()
	{
		try
		{
			JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
			run.setMember("value", "Pause");
		}
		catch(netscape.javascript.JSException e)
		{
		}
		catch(java.lang.NullPointerException e)
		{
		}

		JEmu.running = true;
		thread = new Thread(this);
		thread.start();
	}

	// suspend execution thread
	private void threadSuspend()
	{
		JEmu.running = false;
	}


	//
	// Memory
	//
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
		ram = new short[size];
		for(short b: ram)
			b = 0;
		System.out.println("ok!");
	}

	void setRAM(int pos, short d, int cycles)
	{
		Device dev = memoryMaps.device(pos);
		d = (short)(d & 0xff);
		if(dev == null)
			JEmu.ram[pos] = d;
		else
			if(dev.memorySet(pos, d, cycles))
				JEmu.ram[pos] = d;
	}

	// TODO - do something about this waste of precious time
	void setRAM(int pos, int d, int cycles)
	{
		setRAM(pos, (short)d, cycles);
	}

	private void rebuildMemoryDebugger(int pos)
	{
		// TODO
	}

	/*
	 * Screen buttons and controls
	 */
	public void stepButton()
	{
		step();
		rebuildDebuggers();
		video.drawScreen();
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
		JSObject cpu_pos = null;
		try
		{
			cpu_pos = (JSObject)JEmu.Window.eval("document.getElementById('cpu_pos');");
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
		catch(netscape.javascript.JSException e)
		{
		}
		catch(java.lang.NullPointerException ex)
		{
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
