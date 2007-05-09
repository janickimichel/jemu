import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.io.IOException;

public abstract class JEmu extends JApplet
{
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

	public void paint(Graphics g) 
	{
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
