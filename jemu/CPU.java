import netscape.javascript.JSObject;

abstract class CPU
{
	public int IP;
	public BreakPoints breakPoints = new BreakPoints();
	public Timer timer = new Timer();

	protected String registerName[];
	protected int reg[];
	protected String flagName[];
	protected byte flag[];

	private int memPos[] = new int[16];

	public void rebuildDebugger(int pos)
	{
		// must we change the reference?
		boolean found = false;
		for(int i=0; i<16; i++)
			if(memPos[i] == IP)
				found = true;
		if(!found)
			pos = IP;

		JSObject table = (JSObject)JEmu.Window.eval("document.getElementById('cpu_table');");
		String s = "<table><tr><td>";

		// opcodes
		s += "<table border='1'><tr>";
		s += "<th>Bkp</th>";
		s += "<th>Address</th>";
		s += "<th>Instruction</th>";
		s += "<th>Opcodes</th>";
		s += "<th>Cycles</th></tr>";
		
		int bt = pos;
		for(int i=0; i<16; i++)
		{
			String color = "", bkpColor = "";
			int instSize = instructionSize(bt);

			memPos[i] = bt;

			if(bt == IP)
				color = " style='background-color: yellow;'";

			if(breakPoints.contains(bt))
				bkpColor = "background-color: red;";
			
			s += "<tr id='p" + bt + "'>";
			s += "<td style='cursor: pointer; " + bkpColor + "' onclick='set_breakpoint(this, " + bt + ")'>&nbsp;&nbsp;&nbsp;</td>";
			s += "<td " + color + ">" + JEmu.platform.hexSymbol() + Integer.toHexString(bt).toUpperCase() + "</td>";
			s += "<td " + color + "><b>" + debugInstruction(bt) + "</b></td>";
			s += "<td " + color + ">";
			for(int j=0; j<instSize; j++)
				s += Integer.toHexString(JEmu.ram[bt+j]).toUpperCase() + " ";
			s += "</td>";
			s += "<td " + color + ">" + debugNumCycles(bt) + "</td>";
			s += "</tr>";
			bt += instSize;
		}
		s += "</table></td>";

		// spacing
		s += "<td>&nbsp;&nbsp;&nbsp;</td>";

		// Registers
		s += "<td id='registers'>";

		// registers
		int n = 0;
		s += "<p><b>Registers</b><table border=1>";
		while(registerName(n) != "")
		{
			s += "<tr>";
			s += "<td><b>" + registerName(n) + "</b></td>";
			s += "<td>" + Integer.toHexString(registerValue(n)).toUpperCase() + "</td>";
			s += "</tr>";
			n += 1;
		}
		// s += "<tr><td><b>IP</b></td><td>" + Integer.toHexString(instructionPointer) + "</td></tr>";
		s += "</table></p>";

		// flags
		n = 0;
		s += "<p><b>Flags</b><table border=1>";
		while(flagName(n) != "")
		{
			String color;
			if(flagValue(n))
				color = " style='background-color: #aaffaa;'";
			else
				color = " style='background-color: #ffaaaa;'";

			s += "<tr>";
			s += "<td><b>" + flagName(n) + "</b></td>";
			s += "<td " + color + ">&nbsp;&nbsp;</td>";
			s += "</tr>";
			n += 1;
		}
		s += "</table></p>";

		s += "</td>";

		table.setMember("innerHTML", s + "</tr></table>");

		JSObject cpu_pos = (JSObject)JEmu.Window.eval("document.getElementById('cpu_pos');");
		cpu_pos.setMember("value", Integer.toHexString(pos).toUpperCase());	
	}

	/*
	 * Implement this
	 */
	public abstract String name();
	public abstract int step();
	protected abstract int instructionSize(int pos);
	protected abstract String debugInstruction(int pos);
	protected abstract String debugNumCycles(int pos);
	protected abstract String registerName(int n);
	protected abstract int registerValue(int n);
	protected abstract String flagName(int n);
	protected abstract boolean flagValue(int n);

}
