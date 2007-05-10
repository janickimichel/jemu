import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.io.IOException;

public abstract class JEmu extends JApplet
{
	//
	// fields
	//
	public Memory memory;
	public CPU cpu;
	public Video video;
	public Device devices[];
	public static JSObject Window = null;

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
	// javascript methods
	//
	public void stepButton()
	{
		step();
		cpu.rebuildDebugger();
		video.updateDebugger();
		for(Device d: devices)
			d.updateDebugger();
		memory.updateDebugger();
	}

	public void runButton()
	{
		JSObject run = (JSObject)JEmu.Window.eval("document.getElementById('run');");
		run.setMember("value", "Pause");
		Object o[] = new Object[2];
		o[0] = cpu.instructionPointer();
		o[1] = -1;
		JEmu.Window.call("debug_line", o);

		while(true)
		{
			step();
			if(cpu.breakpoints.contains(cpu.instructionPointer()))
				break;
		}
		run.setMember("value", "Run");

		cpu.rebuildDebugger();
		video.rebuildDebugger();
		for(Device d: devices)
			d.rebuildDebugger();
		memory.rebuildDebugger();
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
