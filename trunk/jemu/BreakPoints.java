import java.util.List;
import java.util.ArrayList;

class BreakPoints
{
	private List<Integer> bkp = new ArrayList<Integer>();
	
	public boolean hasBkp()
	{
		return (bkp.size() > 0);
	}

	public void add(int pos)
	{
		if(!contains(pos))
			bkp.add(pos);
	}

	public void remove(int pos)
	{
		int i = bkp.indexOf(pos);
		if(i != -1)
			bkp.remove(i);
	}

	public boolean contains(int pos)
	{
		// TODO make it faster
		for(int i=0; i<bkp.size(); i++)
			if(bkp.get(i) == pos)
				return true;
		return false;
	}
}
