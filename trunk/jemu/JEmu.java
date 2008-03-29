import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.net.*;
import java.io.*;

abstract class JEmu 
	extends JApplet 
	implements Runnable, KeyListener, FocusListener, MouseListener
{
	/*
	 * Fields
	 */
	public CPU cpu; // emulador CPU
	public Video video; // emulator Video card
	public List<Device> devices = new ArrayList<Device>(); // emulator other devices

	public static int ram[]; // RAM memory

	public static JSObject Window = null; // HTML window (javascript object)
	public static JEmu platform; // myself

	private Thread thread = null; // thread that runs the emulator
	public static boolean running = false; // is the emulator running?
	private boolean focused = false; // does the emulator has focus?
	private boolean ROMLoaded = false; // was a ROM loaded?

	public int frameskip = 1; // video frameskip (TODO - move to video?)
	public static int currentFrame = 0; // used to control frameskip
	private long timer; // control the time between frames

	private Timer updatingTimer = new Timer();

	// define if the user is debugging or just playing
	private boolean debugging = false;
	public void setDebugging(boolean d) 
	{ 
		debugging = d; 
		if(debugging)
			focused = true;
	}

	//
	// Applet methods
	//
	public JEmu()
	{
		JEmu.platform = this;
		// Timer.useTimer = true;
	}

	public void init()
	{
		addKeyListener(this);
		addFocusListener(this);
		addMouseListener(this);
		setFocusable(true);
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
			loadROM("rom/atari2600/adventure.bin");
		}
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void paint(Graphics g) 
	{
		g.drawImage(video.image, 0, 0, this);
		if(!focused)
		{
			g.setColor(Color.BLACK);
			g.fillRect(8, 8, 110, 20);
			g.setColor(Color.WHITE);
			g.drawString("Click here to play", 15, 22);
			g.drawString("Click here to play", 16, 22);
		}
		if(!ROMLoaded)
		{
			g.setColor(Color.BLACK);
			g.fillRect(8, 8, 110, 20);
			g.setColor(Color.WHITE);
			g.drawString("Choose a ROM to load", 15, 22);
			g.drawString("Choose a ROM to load", 16, 22);
		}

	}

	public void destroy()
	{
		System.out.println("CPU    = " + cpu.timer.timeByFrame());
		System.out.println("Video  = " + video.timer.timeByFrame());
		System.out.println("PIA    = " + devices.get(0).timer.timeByFrame());
		System.out.println("Update = " + updatingTimer.timeByFrame());
	}
	
	// 
	// events
	//
	public void keyPressed(KeyEvent e) {}  // must be implemented by child
	public void keyReleased(KeyEvent e) {} // must be implemented by child
	public void keyTyped(KeyEvent e) {}
	public void focusGained(FocusEvent evt) 
	{ 
		focused = true; 
	}

	public void focusLost(FocusEvent evt) 
	{
		if(!debugging)
		{
			focused = false; 
			repaint();
		}
	}

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}

	//
	// thread methods
	//
	public void run()
	{
		timer = (System.currentTimeMillis() + (1000 / video.fps));

		if(debugging)
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
					updateScreen();
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
					updateScreen();

					while(!focused) // check if the applet has focus
						try {
							Thread.sleep(100);
						} catch(InterruptedException e) {}
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

	// update the screen
	public void updateScreen()
	{
		updatingTimer.start();

		if(JEmu.currentFrame == 0)
		{
			video.drawScreen();
			getToolkit().sync();
		}

		try
		{
			Thread.sleep(timer - System.currentTimeMillis());
		} catch(InterruptedException e) {}
		  catch(java.lang.IllegalArgumentException ex) {}
		timer = (System.currentTimeMillis() + (1000 / video.fps));

		video.screenDone = false;
		updatingTimer.stop();
		Timer.frames++;

		JEmu.currentFrame--;
		if(JEmu.currentFrame < 0)
			JEmu.currentFrame = frameskip;
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
		List<Integer> d = new ArrayList<Integer>();

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
				d.add((Integer)c);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		loadROM(d);
		ROMLoaded = true;
		System.out.println("ROM loaded.");
		reset();
		if(debugging)
		{
			rebuildDebuggers();
			repaint();
			JSObject use_debugger = (JSObject)JEmu.Window.eval("document.getElementById('use_debugger');");
			use_debugger.setMember("disabled", true);
		}
		else
		{
			JEmu.running = false;
			runButton();
		}
	}

	void initRAM(int size)
	{
		System.out.print("Initializing memory... ");
		ram = new int[size];
		for(int b: ram)
			b = 0;
		System.out.println("ok!");
	}

	void setRAMDirect(int pos, int d)
	{
		JEmu.ram[pos] = d & 0xff;
	}

	int getRAM(int pos)
	{
		return JEmu.ram[pos];
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
	abstract void loadROM(List<Integer> d);
	abstract String platformName();
	abstract void reset();
	abstract void setRAM(int pos, int d, int cycles);
	String hexSymbol() { return "$"; }
}
