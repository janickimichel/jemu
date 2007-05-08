class m6502 extends CPU
{
	public void reset()
	{
		PC = 0xf000;
	}

	public int step()
	{

		return 0;
	}

	protected DebugValues debug()
	{
		DebugValues dv = new DebugValues();
		dv.instruction = "NOP";
		dv.opcodes = new int[1];
		dv.opcodes[0] = 0xfa;
		dv.n_opcodes = 1;
		dv.cycles = 2;
		return dv;
	}
}
