class Rect
{
	public int x, y, w, h;

	public Rect(int x, int y, int x2, int y2)
	{
		this.x = x;
		this.y = y;
		this.w = x2 - x;
		this.h = y2 - y;
	}

	public String toString()
	{
		return x + ", " + y + ", " + w + ", " + h;
	}
}
