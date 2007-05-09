abstract class Device
{
	public Memory memory;

	abstract void reset();
	abstract void step(int cycles);
	abstract boolean memorySet(int pos, int data, int cycles);
	abstract void rebuildDebugger();
	abstract void updateDebugger();
}
