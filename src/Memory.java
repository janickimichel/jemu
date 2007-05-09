import netscape.javascript.*;
import java.util.ArrayList;
import java.util.List;

public class Memory
{
	private class MemoryMap
	{
		int init;
		int end;
		Device device;
		MemoryMap(int init, int end, Device device)
		{
			this.init = init;
			this.end = end;
			this.device = device;
		}
	}

	private int ram[];
	private int lastPosChanged = 0;
	private List<MemoryMap> memoryMaps = new ArrayList<MemoryMap>();

	public int debugPos;

	public Memory(int size_k)
	{
		ram = new int[size_k * 1024];
		for(int i=0; i<(size_k * 1024); i++)
			ram[i] = 0x0;
	}

	public int get(int pos)
	{
		return ram[pos];
	}

	public void set(int pos, int data, int cycles)
	{
		for(MemoryMap mm: memoryMaps)
			if(pos >= mm.init && pos <= mm.end)
				if(mm.device.memorySet(pos, data, cycles))
				{
					ram[pos] = data & 0xFF;
					lastPosChanged = pos;
				}
	}

	public void setDirect(int pos, int data)
	{
		ram[pos] = data & 0xFF;
	}

	public void addMemoryMap(int init, int end, Device device)
	{
		memoryMaps.add(new MemoryMap(init, end, device));
	}

	public void rebuildDebugger(int pos)
	{
		debugPos = pos;
		JSObject table = (JSObject)JEmu.Window.eval("document.getElementById('memory_table');");
		String s = "<table border='1'><th>Address</th>";
		
		// headers
		for(int i=0; i<16; i++)
			s += "<th>_" + Integer.toHexString(i).toUpperCase() + "</td>";

		// lines
		int bt = pos / 16;
		bt *= 16;
		for(int i=0; i<16; i++)
		{
			s += "<tr><td style='text-align: right;'><b>$" + Integer.toHexString(bt / 0x10).toUpperCase() + "_</td>";
			for(int j=0; j<16; j++)
			{
				s += "<td class='memory' id='d" + Integer.toString(bt) + "'>";
				s += Integer.toHexString(get(bt)).toUpperCase() + "</td>";
				bt++;
			}
			s += "</tr>";
		}

		table.setMember("innerHTML", s + "</table>");

		JSObject memory_pos = (JSObject)JEmu.Window.eval("document.getElementById('memory_pos');");
		memory_pos.setMember("value", Integer.toHexString(pos).toUpperCase());
	}

	public void updateDebugger()
	{
		try
		{
			JSObject memory_pos = (JSObject)JEmu.Window.eval("document.getElementById('d" + lastPosChanged + "');");
			memory_pos.setMember("value", Integer.toHexString(get(lastPosChanged)).toUpperCase());
		} catch(JSException e) { }
	}
}
