import netscape.javascript.JSObject;

class PIA6532 extends Device
{
	final int INPT0  = 0x08;
	final int INPT1  = 0x09;
	final int INPT2  = 0x0A;
	final int INPT3  = 0x0B;
	final int INPT4  = 0x0C;
	final int INPT5  = 0x0D;
	final int SWCHA  = 0x280;
	final int SWACNT = 0x281;
	final int SWCHB  = 0x282;
	final int SWBCNT = 0x283;
	final int INTIM  = 0x284;
	final int INSTAT = 0x285;
	final int TIM1T  = 0x294;
	final int TIM8T  = 0x295;
	final int TIM64T = 0x296;
	final int T1024T = 0x297;

	int interval = 0, time = 0;
	boolean underflow = false;

	public String name() { return "PIA 6532"; }

	public void reset()
	{
		interval = 0;
		time = 0;
		underflow = false;

		/* Set SWCHA to 0xff, since each bit holds the value of 1 when
		 * the joystick isn't being pressed to any direction */
		JEmu.platform.setRAMDirect(SWCHA, 0xff);
		JEmu.platform.setRAMDirect(SWCHB, 0x0b);
		JEmu.platform.setRAMDirect(INPT4, 0x80);
		JEmu.platform.setRAMDirect(INPT5, 0x80);
	}

	public void step(int cycles)
	{
		time -= cycles;

		// check for underflow
		/*
		if(!underflow && time < 0)
		{
			time = -cycles;
			underflow = true;
		}
		*/

		JEmu.platform.setRAMDirect(INTIM, (time >> interval));
	}

	public boolean memorySet(int pos, int data, int cycles)
	{
		switch(pos)
		{
			case TIM1T:
				interval = 0;
				time = data << interval;
				underflow = true;
				JEmu.platform.setRAMDirect(INTIM, data);
				return false;
			case TIM8T:
				interval = 3;
				time = data << interval;
				underflow = true;
				JEmu.platform.setRAMDirect(INTIM, data);
				return false;
			case TIM64T:
				interval = 6;
				time = data << interval;
				underflow = true;
				JEmu.platform.setRAMDirect(INTIM, data);
				return false;
			case T1024T:
				interval = 10;
				time = data << interval;
				underflow = true;
				JEmu.platform.setRAMDirect(INTIM, data);
				return false;
		}
		return true;
	}

	public void rebuildDebugger()
	{
		String s;
		s = "<table border='0'>";
		s += "<tr><td>Interval:</td><td><b>" + interval + "</b></td></tr>";
		s += "<tr><td>Timer:</td><td><b>" + time + "</b></td></tr>";
		s += "</table>";
		
		s += "<h3>Registers</h3>";
		s += "<table border='0'>";
		s += "<tr><td>SWCHA</td><td><b>" + Integer.toBinaryString(JEmu.ram[SWCHA]) + "</b></td></tr>";
		s += "<tr><td>INPT4</td><td><b>" + Integer.toBinaryString(JEmu.ram[INPT4]) + "</b></td></tr>";
		s += "</table>";

		JSObject pia_table = (JSObject)JEmu.Window.eval("document.getElementById('" + htmlField + "');");
		pia_table.setMember("innerHTML", s);
	}

}

