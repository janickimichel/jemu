abstract class Device
{
	public abstract void reset();
	public abstract void step(int cycles);
	public abstract boolean memorySet(int pos, short data, int cycles);
	public abstract void rebuildDebugger();
}
