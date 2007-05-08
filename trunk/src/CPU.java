import netscape.javascript.JSObject;

abstract class CPU
{
	protected class DebugValues
	{
		String instruction;
		int[] opcodes;
		int n_opcodes;
		int cycles;
	}

	int PC;

	abstract void reset();
	abstract int step();
	abstract DebugValues debug();

	void rebuildDebugger(int pos)
	{
		JSObject table = (JSObject)JEmu.Window.eval("document.getElementById('cpu_table');");
		String s = "<table border='1'><tr>";
		s += "<th></th>";
		s += "<th>Address</th>";
		s += "<th>Instruction</th>";
		s += "<th>Opcodes</th>";
		s += "<th>Cycles</th></tr>";
		
		int bt = pos;
		for(int i=0; i<16; i++)
		{
			DebugValues dv = debug();
			
			s += "<tr>";
			s += "<td></td>";
			s += "<td>$" + Integer.toHexString(bt).toUpperCase() + "</td>";
			s += "<td>" + dv.instruction + "</td>";
			s += "<td>";
			for(int j=0; j<dv.n_opcodes; j++)
				s += Integer.toHexString(dv.opcodes[j]).toUpperCase() + " ";
			s += "</td>";
			s += "<td>" + Integer.toString(dv.cycles) + "</td>";
			s += "</tr>";

			if(dv.n_opcodes > 0)
				bt += dv.n_opcodes;
			else
				bt++;
		}

		table.setMember("innerHTML", s + "</table>");
	}
}
