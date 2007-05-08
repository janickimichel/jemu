import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;
import java.io.IOException;

public abstract class JEmu extends JApplet
{
	public Memory memory;
	public CPU cpu;
	public static JSObject Window = null;

	// 
	// abstract methods
	//
	abstract void loadROM(String file);

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
	}

	//
	// javascript methods
	//
	public void step()
	{
		cpu.step();
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
