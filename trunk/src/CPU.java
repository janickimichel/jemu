import netscape.javascript.JSObject;
import java.util.ArrayList;
import java.util.List;

abstract class CPU
{
	//
	// nested classes
	//
	protected class DebugValues
	{
		String instruction;
		int n_opcodes;
		int cycles;
	}

	protected class Register
	{
		String name;
		int value;
		Register(String name, int value)
		{
			this.name = name;
			this.value = value;
		}
	}

	protected class Flag
	{
		String name;
		boolean value;
		Flag(String name, boolean value)
		{
			this.name = name;
			this.value = value;
		}
	}

	//
	// fields
	//
	public Memory memory;
	public int debugPos;
	public int lastPos;
	public List<Integer> breakpoints = new ArrayList<Integer>();
	protected int lastInstructionPointer;

	//
	// abstract methods
	//
	abstract int step();
	abstract int instructionPointer();
	abstract DebugValues debug(int pos);
	abstract Register[] registers();
	abstract Flag[] flags();
	abstract void reset();

	//
	// public methods
	//
	public void rebuildDebugger()
	{
		if(instructionPointer() < debugPos || instructionPointer() > lastPos)
			rebuildDebugger(instructionPointer());
		else
		{
			Object o[] = new Object[2];
			o[0] = lastInstructionPointer;
			o[1] = instructionPointer();
			JEmu.Window.call("debug_line", o);
			rebuildRegisters();
		}
	}

	public void rebuildDebugger(int pos)
	{
		debugPos = pos;
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
			DebugValues dv = debug(bt);
			String color = "";

			if(bt == instructionPointer())
				color = " style='background-color: yellow;'";
			
			if(dv != null)
			{
				s += "<tr id='p" + bt + "'>";
				s += "<td style='cursor: pointer;' onclick='set_breakpoint(this, " + bt + ")'>&nbsp;&nbsp;&nbsp;</td>";
				s += "<td " + color + ">$" + Integer.toHexString(bt).toUpperCase() + "</td>";
				s += "<td " + color + "><b>" + dv.instruction + "</b></td>";
				s += "<td " + color + ">";
				for(int j=0; j<dv.n_opcodes; j++)
					s += Integer.toHexString(memory.get(bt+j)).toUpperCase() + " ";
				s += "</td>";
				s += "<td " + color + ">" + Integer.toString(dv.cycles) + "</td>";
				s += "</tr>";
				lastPos = bt;
				bt += dv.n_opcodes;
			}
			else
			{
				s += "<tr>";
				s += "<td style='cursor: pointer;' onclick='set_breakpoint(this, " + bt + ")'>&nbsp;&nbsp;&nbsp;</td>";
				s += "<td " + color + ">$" + Integer.toHexString(bt).toUpperCase() + "</td>";
				s += "<td " + color + ">Invalid</td>";
				s += "<td " + color + ">" + Integer.toHexString(memory.get(bt)).toUpperCase() + "</td>";
				s += "<td " + color + ">?</td>";
				s += "</tr>";
				lastPos = bt;
				bt++;
			}
		}
		s += "</table></td>";

		// spacing
		s += "<td>&nbsp;&nbsp;&nbsp;</td>";

		// Registers
		s += "<td id='registers'>";
		s += "</td>";

		table.setMember("innerHTML", s + "</tr></table>");
		rebuildRegisters();

		JSObject cpu_pos = (JSObject)JEmu.Window.eval("document.getElementById('cpu_pos');");
		cpu_pos.setMember("value", Integer.toHexString(pos).toUpperCase());
	}

	protected void rebuildRegisters()
	{
		String s;

		// registers
		s = "<p><b>Registers</b><table border=1>";
		for(Register r: registers())
		{
			s += "<tr>";
			s += "<td><b>" + r.name + "</b></td>";
			s += "<td>" + Integer.toHexString(r.value).toUpperCase() + "</td>";
			s += "</tr>";
		}
		s += "</table></p>";

		// flags
		s += "<p><b>Flags</b><table border=1>";
		for(Flag f: flags())
		{
			String color;
			if(f.value)
				color = " style='background-color: #aaffaa;'";
			else
				color = " style='background-color: #ffaaaa;'";

			s += "<tr>";
			s += "<td><b>" + f.name + "</b></td>";
			s += "<td " + color + ">&nbsp;&nbsp;</td>";
			s += "</tr>";
		}
		s += "</table></p>";

		JSObject reg = (JSObject)JEmu.Window.eval("document.getElementById('registers');");
		reg.setMember("innerHTML", s);
	}
}
