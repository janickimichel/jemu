abstract class Device
{
	protected String htmlField;

	public abstract String name();
	public abstract void reset();
	public abstract void step(int cycles);
	public abstract boolean memorySet(int pos, short data, int cycles);
	public abstract void rebuildDebugger();
}
