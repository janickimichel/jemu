abstract class Device
{
	protected String htmlField;

	public Timer timer = new Timer();

	public abstract String name();
	public abstract void reset();
	public abstract void step(int cycles);
	public abstract boolean memorySet(int pos, int data, int cycles);
	public abstract void rebuildDebugger();
}
