import netscape.javascript.JSObject;

public class Memory
{
	private byte ram[];

	public Memory(int size_k)
	{
		ram = new byte[size_k * 1024];
		for(int i=0; i<(size_k * 1024); i++)
			ram[i] = 0x0;
	}

	public byte get(int pos)
	{
		return ram[pos];
	}

	public void rebuildDebugger(int pos)
	{
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
	}
}
