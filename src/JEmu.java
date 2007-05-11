import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.io.IOException;

public abstract class JEmu extends JApplet implements Runnable
{
	//
	// fields
	//
	public Memory memory;
	public CPU cpu;
	public Video video;
	public Device devices[];
	public static JSObject Window = null;

	private boolean running = false;
	private Thread thread = null;
	private boolean stopOnNextScanline = false;
	private boolean stopOnNextFrame = false;

	// 
	// abstract methods
	//
	abstract void loadROM(String file);
	abstract void step();

	//
	// applet methods
	//
	public void start()
	{
		JEmu.Window = JSObject.getWindow(this);
		memory.rebuildDebugger(0x0);
		cpu.memory = memory;
		cpu.reset();
		cpu.rebuildDebugger(cpu.instructionPointer());
		video.memory = memory;
		video.reset();
		video.rebuildDebugger();
		for(Device d: devices)
		{
			d.memory = memory;
			d.reset();
			d.rebuildDebugger();
		}
	}

	public void update(Graphics g)
	{
		g.drawImage(video.backImage, 0, 0, this);
	}

	public void paint(Graphics g) 
	{
		update(g);
	}

	//
	// thread methods
	//
	public void run()
	{
		boolean bkp = false;

		while(running && !bkp)
		{
			synchronized(this)
			{
				step();
				if(cpu.breakpoints.contains(cpu.instructionPointer()))
					bkp = true;
				if(video.updateScreen)
				{
					repaint();
					if(stopOnNextFrame)
					{
						repaint();
						video.clearBackImage();
						stopOnNextFrame = false;
						bkp = true;
					}
				}
				if(stopOnNextScanline)
					if(video.updateLine)
					{
						repaint();
						stopOnNextScanline = false;
						bkp = true;
					}
			}
		}

		if(bkp)
		{
			running = false;
			JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
			run.setMember("value", "Run");
			cpu.rebuildDebugger();
			video.rebuildDebugger();
			for(Device d: devices)
				d.rebuildDebugger();
			memory.rebuildDebugger();
		}
	}

	private void threadStart()
	{
		JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
		run.setMember("value", "Pause");
		Object o[] = new Object[2];
		o[0] = cpu.instructionPointer();
		o[1] = -1;
		JEmu.Window.call("debug_line", o);

		running = true;
		thread = new Thread(this);
		thread.start();
	}

	private void threadSuspend()
	{
		running = false;
		try
		{
			thread.join();
		} catch(InterruptedException e) { }

		JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
		run.setMember("value", "Run");
		cpu.rebuildDebugger();
		video.rebuildDebugger();
		for(Device d: devices)
			d.rebuildDebugger();
		memory.rebuildDebugger();
	}

	//
	// javascript methods
	//
	public void stepButton()
	{
		step();
		repaint();
		cpu.rebuildDebugger();
		video.updateDebugger();
		for(Device d: devices)
			d.updateDebugger();
		memory.updateDebugger();
	}

	public void runButton()
	{
		if(running)
			threadSuspend();
		else
			threadStart();
	}

	public void nextScanlineButton()
	{
		if(running)
			return;
		stopOnNextScanline = true;
		threadStart();
	}

	public void nextFrameButton()
	{
		if(running)
			return;
		stopOnNextFrame = true;
		threadStart();
	}

	public void addBreakpoint(int pos)
	{
		if(!cpu.breakpoints.contains(pos))
			cpu.breakpoints.add(pos);
	}

	public void removeBreakpoint(int pos)
	{
		cpu.breakpoints.remove(cpu.breakpoints.indexOf(pos));
	}

	public void cpuPosChanged(String pos)
	{
		try
		{
			int p = Integer.parseInt(pos, 16);
			cpu.rebuildDebugger(p);
		}
		catch(NumberFormatException e)
		{
			Object[] args = { "Invalid number." };
			JEmu.Window.call("alert", args);
			JSObject cpu_pos = (JSObject)JEmu.Window.eval("document.getElementById('cpu_pos');");
			cpu_pos.setMember("value", Integer.toHexString(cpu.debugPos).toUpperCase());
		}
	}

	public void memoryPosChanged(String pos)
	{
		try
		{
			int p = Integer.parseInt(pos, 16);
			memory.rebuildDebugger(p);
		}
		catch(NumberFormatException e)
		{
			Object[] args = { "Invalid number." };
			JEmu.Window.call("alert", args);
			JSObject memory_pos = (JSObject)JEmu.Window.eval("document.getElementById('memory_pos');");
			memory_pos.setMember("value", Integer.toHexString(memory.debugPos).toUpperCase());
		}
	}
}
