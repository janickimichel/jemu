import java.util.*;

class Timer
{
	public static long frames = 0;
	public static boolean useTimer = false;

	private long fullTime;
	private long timeBegin;

	public void start()
	{
		if(!useTimer)
			return;
		timeBegin = System.currentTimeMillis();
	}

	public void stop()
	{
		if(!useTimer)
			return;
		fullTime += (System.currentTimeMillis() - timeBegin);
	}

	public double timeByFrame()
	{
		return (double)fullTime / frames;
	}
}
