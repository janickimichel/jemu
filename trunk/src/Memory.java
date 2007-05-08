import netscape.javascript.JSObject;

public class Memory
{
	private short ram[];
	public int debugPos;

	public Memory(int size_k)
	{
		ram = new short[size_k * 1024];
		for(int i=0; i<(size_k * 1024); i++)
			ram[i] = 0x0;
	}

	public short get(int pos)
	{
		return ram[pos];
	}

	/*
	public void set(int pos, short data)
	{
		ram[pos] = data;
	}
	*/

	public void set(int pos, short data, int cycles)
	{
		ram[pos] = data;
	}

	public void setDirect(int pos, short data)
	{
		ram[pos] = data;
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
}
