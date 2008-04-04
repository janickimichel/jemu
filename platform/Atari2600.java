import java.util.List;
import java.util.Date;
import java.awt.event.*;

public class Atari2600 extends JEmu
{
	PIA6532 pia6532;

	public Atari2600()
	{
		cpu = new MOS6502();
		video = new TIA1A();
		pia6532 = new PIA6532();
		devices.add(pia6532);
		initRAM(64*1024);
	}

	public void step()
	{
		printNextStep();

		cpu.timer.start();
		int cycles = cpu.step();
		cpu.timer.stop();

		video.timer.start();
		video.step(cycles);
		video.timer.stop();

		pia6532.timer.start();
		pia6532.step(cycles);
		pia6532.timer.stop();
	}

	void loadROM(List<Integer> d)
	{
		int pos = 0xf000;

		if(d.size() != 4096 && d.size() != 2048)
			System.out.println("Invalid size!");

		for(Integer s: d)
		{
			setRAM(pos, s, 0);
			pos++;
		}
		
		if(d.size() == 2048)
			for(Integer s: d)
			{
				setRAM(pos, s, 0);
				pos++;
			}

		reset();
	}

	String platformName()
	{
		return "Atari 2600";
	}

	void reset()
	{
		cpu.IP = (JEmu.ram[0xfffd] * 0x100) + JEmu.ram[0xfffc];
		// cpu.reset();
		video.reset();
		pia6532.reset();
	}

	// 
	// Memory
	//
	void setRAM(int pos, int d, int cycles)
	{
		// memory maps & mirrors
		if(pos >= 0x100 && pos < 0x200)
			pos -= 0x100;
		
		if(pos < 0x80)
		{
			if(pos >= 0x40) // TIA fist mirror
				pos -= 0x40;
			// TODO - check for other mirrors
			// http://www.qotile.net/minidig/docs/2600_mem_map.txt
			if(!video.memorySet(pos, d, cycles))
				return;
		}
		if(pos >= 0x280 && pos <= 0x297)
			if(!pia6532.memorySet(pos, d, cycles))
				return;

		// normal operation
		setRAMDirect(pos, d);
	}

	void setRAMDirect(int pos, int d)
	{
		JEmu.ram[pos] = d & 0xff;
		// System.out.println("RAM position " + pos + " = " + (d & 0xff));
	}

	int getRAM(int pos)
	{
		if(pos >= 0x30 && pos <= 0x3d)
			return JEmu.ram[pos - 0x30];
		if(pos >= 0x294 && pos <= 0x297)
			return JEmu.ram[pos - 0x10];
		return JEmu.ram[pos];
	}

	// 
	// Joystick & other buttons
	// 
	public void keyPressed(KeyEvent e)
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_UP:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) ^ 0x10);
				break;
			case KeyEvent.VK_DOWN:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) ^ 0x20);
				break;
			case KeyEvent.VK_LEFT:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) ^ 0x40);
				break;
			case KeyEvent.VK_RIGHT:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) ^ 0x80);
				break;
			case KeyEvent.VK_SPACE:
				setRAMDirect(pia6532.INPT4, getRAM(pia6532.INPT4) ^ 0x80);
				break;
			case KeyEvent.VK_R:
				setRAMDirect(pia6532.SWCHB, getRAM(pia6532.SWCHB) ^ 0x01);
				break;
			case KeyEvent.VK_S:
				setRAMDirect(pia6532.SWCHB, getRAM(pia6532.SWCHB) ^ 0x02);
				break;
			case KeyEvent.VK_C:
				setRAMDirect(pia6532.SWCHB, getRAM(pia6532.SWCHB) ^ 0x08);
				break;
		}
	}

	public void keyReleased(KeyEvent e) 
	{
		switch(e.getKeyCode())
		{
			case KeyEvent.VK_UP:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) | 0x10);
				break;
			case KeyEvent.VK_DOWN:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) | 0x20);
				break;
			case KeyEvent.VK_LEFT:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) | 0x40);
				break;
			case KeyEvent.VK_RIGHT:
				setRAMDirect(pia6532.SWCHA, getRAM(pia6532.SWCHA) | 0x80);
				break;
			case KeyEvent.VK_SPACE:
				setRAMDirect(pia6532.INPT4, getRAM(pia6532.INPT4) | 0x80);
				break;
			case KeyEvent.VK_R:
				setRAMDirect(pia6532.SWCHB, getRAM(pia6532.SWCHB) | 0x01);
				break;
			case KeyEvent.VK_S:
				setRAMDirect(pia6532.SWCHB, getRAM(pia6532.SWCHB) | 0x02);
				break;
			case KeyEvent.VK_C:
				setRAMDirect(pia6532.SWCHB, getRAM(pia6532.SWCHB) | 0x08);
				break;
		}
	}

	private void printNextStep()
	{
		System.out.println((video.y + 40) + " " + 
				((TIA1A)video).x + " " +
				((MOS6502)cpu).A + " " + 
				((MOS6502)cpu).X + " " + 
				((MOS6502)cpu).Y + " " + 
				((MOS6502)cpu).SP + " " + 
				Integer.toHexString(cpu.IP));
	}
}
