import netscape.javascript.JSObject;
import javax.swing.JApplet;
import java.awt.*;

public class JEmu extends JApplet
{
	public Memory memory;
	public CPU cpu;
	public static JSObject Window = null;

	public void step()
	{
		JSObject teste = (JSObject)JEmu.Window.eval("document.getElementById('step');");
		teste.setMember("value", "Hello World");
	}

	public void start()
	{
		JEmu.Window = JSObject.getWindow(this);
		memory.rebuildDebugger(0x0);
		cpu.rebuildDebugger(0x0);
	}
}
