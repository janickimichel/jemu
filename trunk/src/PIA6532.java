import netscape.javascript.JSObject;

class PIA6532 extends Device
{
	final int INPT0  = 0x38;
	final int INPT1  = 0x39;
	final int INPT2  = 0x3A;
	final int INPT3  = 0x3B;
	final int INPT4  = 0x3C;
	final int INPT5  = 0x3D;
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

	int interval = 0, timer = 0;

	void reset()
	{
		interval = 0;
		timer = 0;

		/* Set SWCHA to 0xff, since each bit holds the value of 1 when
		 * the joystick isn't being pressed to any direction */
		memory.setDirect(SWCHA, 0xff);	
		memory.setDirect(SWCHB, 0xff);
	}

	void step(int cycles)
	{
		timer -= cycles;
		memory.setDirect(INTIM, (timer >> interval));
	}

	public boolean memorySet(int pos, int data, int cycles)
	{
		switch(pos)
		{
			case TIM1T:
				interval = 0;
				timer = data << interval;
				memory.setDirect(INTIM, data);
				return false;
			case TIM8T:
				interval = 3;
				timer = data << interval;
				memory.setDirect(INTIM, data);
				return false;
			case TIM64T:
				interval = 6;
				timer = data << interval;
				memory.setDirect(INTIM, data);
				return false;
			case T1024T:
				interval = 10;
				timer = data << interval;
				memory.setDirect(INTIM, data);
				return false;
		}
		return true;
	}

	public void rebuildDebugger()
	{
		String s;
		s = "<table border='0'>";
		s += "<tr><td>Interval:</td><td><b>" + interval + "</b></td></tr>";
		s += "<tr><td>Timer:</td><td><b>" + timer + "</b></td></tr>";
		s += "</table>";

		JSObject pia_table = (JSObject)JEmu.Window.eval("document.getElementById('pia_table');");
		pia_table.setMember("innerHTML", s);
	}

	public void updateDebugger()
	{
		rebuildDebugger();
	}

}
