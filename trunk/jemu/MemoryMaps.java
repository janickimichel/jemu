import java.util.ArrayList;
import java.util.List;

class MemoryMaps
{
	private class Map
	{
		int begin;
		int end;
		Device device;
	}

	private List<Map> maps = new ArrayList<Map>();

	public void add(Device device, int begin, int end)
	{
		Map m = new Map();
		m.begin = begin;
		m.end = end;
		m.device = device;
		maps.add(m);
	}

	public Device device(int pos)
	{
		for(Map m: maps)
			if(m.begin <= pos && m.end >= pos)
				return m.device;
		return null;
	}
}
