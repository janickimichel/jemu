import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.*;
import java.util.Stack;

abstract class Video extends Device
{
	public boolean scanlineDone = false;
	public boolean screenDone = false;
	public boolean screenBegin = false;
	public int fps;

	public Stack<Rect> updates = new Stack<Rect>();
	public Stack<Rect> lastUpdates = new Stack<Rect>();

	public BufferedImage image;

	protected int[] pixels;

	public Video(int fps)
	{
		this.fps = fps;

		image = new BufferedImage(width(), height(), BufferedImage.TYPE_INT_ARGB);

		WritableRaster wr = image.getRaster();
		DataBuffer db = (DataBuffer)wr.getDataBuffer();
		DataBufferInt dbi = (DataBufferInt)db;
		pixels = dbi.getData();

		int w = width();
		int h = height();

		for(int i=0; i<(w*h); i++)
			pixels[i] = 0xff000000;

		JEmu.platform.repaint();
	}

	public void clearScreen()
	{
		for(int i=0; i<(width()*height()); i++)
			pixels[i] = 0xff000000;
		JEmu.platform.repaint();
	}

	public void drawScreen()
	{
		/*
		try
		{
			while(true)
			{
				Rect r = lastUpdates.pop();
				JEmu.platform.repaint(r.x, r.y, r.w, r.h);
				// System.out.println("last: " + r);
			}
		}
		catch(java.util.EmptyStackException e) {}

		try
		{
			while(true)
			{
				Rect r = updates.pop();
				lastUpdates.push(r);
				JEmu.platform.repaint(r.x, r.y, r.w, r.h);
				// System.out.println("updates: " + r);
			}
		}
		catch(java.util.EmptyStackException e) {}
		*/

		JEmu.platform.repaint();
	}

	public abstract int height();
	public abstract int width();
}
