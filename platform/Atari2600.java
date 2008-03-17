import java.util.List;

public class Atari2600 extends JEmu
{
	public Atari2600()
	{
		cpu = new MOS6502();
		video = new TIA1A();
		devices.add(new PIA6532());
		initRAM(64*1024);
	}

	public void step()
	{
		cpu.step();
	}

	void loadROM(List<Short> d)
	{
		int pos = 0xf000;

		for(Short s: d)
		{
			JEmu.platform.setRAM(pos, s, 0);
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
		cpu.IP = (getRAM(0xfffd) * 0x100) + getRAM(0xfffc);
	}
}
