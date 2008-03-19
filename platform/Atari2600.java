import java.util.List;

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

		memoryMaps.add(video, 0x0, 0x2c);
		memoryMaps.add(pia6532, 0x80, 0xff);
		memoryMaps.add(pia6532, 0x280, 0x297);
	}

	public void step()
	{
		int cycles = cpu.step();
		video.step(cycles);
		video.step(cycles);
		video.step(cycles);
		pia6532.step(cycles);
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
		video.reset();
		pia6532.reset();
	}
}
