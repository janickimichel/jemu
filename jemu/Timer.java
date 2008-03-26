import java.util.*;

class Timer
{
	public static long frames = 0;
	public static boolean useTimer = false;

	private long fullTime;
	private long timeBegin, timeEnd;

	public void start()
	{
		if(!useTimer)
			return;
		timeBegin = (new Date()).getTime();
	}

	public void stop()
	{
		if(!useTimer)
			return;
		timeEnd = (new Date()).getTime();
		fullTime += (timeEnd - timeBegin);
	}

	public double timeByFrame()
	{
		return fullTime / frames;
	}
}
