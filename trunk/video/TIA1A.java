import netscape.javascript.JSObject;
import java.util.Stack;

class TIA1A extends Video
{
	//
	// Helper functions
	//
	int adjust(int i)
	{
		if(i >=0 && i < 160)
			return i;
		if(i < 0)
			return 160 + i;
		else //if(i >= 192)
			return i - 160;
	}

	//
	// Extended classe - Sprite
	//
	private abstract class Sprite
	{
		int color;
		boolean enabled;

		abstract void reset();
		abstract void draw(int x1, int x2);
		abstract void updateStack();
		abstract String describe();
	};

	//
	// Background
	//
	private class Background extends Sprite
	{
		void reset()
		{
			color = 0x0;
		}

		void draw(int x1, int x2)
		{
			for(int i = x1; i < x2; i++)
				setPixel(i, y, color);
		}

		void updateStack()
		{
			// updates.push(new Rect(0, 0, 1, 1));
		}

		String describe()
		{
			return "<table><tr><td>Color:&nbsp;</td><td bgcolor='#" + Integer.toHexString(color) + "'>&nbsp;&nbsp;&nbsp;</td></tr></table>";
		}
	};

	// 
	// Playfield
	//
	private class Playfield extends Sprite
	{
		boolean reflect;
		boolean score;
		boolean priority;

		boolean[] pf = new boolean[20];

		void reset()
		{
			enabled = false;
			reflect = false;
			score = false;
			for(int i=0; i<20; i++)
				pf[i] = false;
		}

		void checkEnabled()
		{
			for(int i=0; i<20; i++)
				if(pf[i])
				{
					enabled = true;
					return;
				}
			enabled = false;
		}
		
		void draw(int x1, int x2)
		{
			int cl1, cl2;

			if(!score)
				cl1 = cl2 = color;
			else
			{
				cl1 = p[0].color;
				cl2 = p[1].color;
			}

			for(int i = x1; i < x2; i++)
			{
				if(i < 80)
				{
					if(pf[(int)(i/4)])
						setPixel(i, y, color);
				}
				else
				{
					if(!reflect)
					{
						if(pf[(int)((i-80)/4)])
							setPixel(i, y, color);
					}
					else
					{
						if(pf[19-(int)((i-80)/4)])
							setPixel(i, y, color);
					}
				}
			}
		}

		void updateStack() {}

		String describe()
		{
			String s;
			s = "<p><b>Flags: </b>";
			if(enabled)
				s += "enabled ";
			else
				s += "disabled ";
			if(reflect)
				s += "reflected ";
			if(score)
				s += "score";
			s += "</p>";
			
			s += "<table border='1'>";
			int cl1, cl2;
			if(!score)
				cl1 = cl2 = color;
			else
			{
				cl1 = p[0].color;
				cl2 = p[1].color;
			}
			s += "<tr><td><b>Left side:</b></td>";
			for(int i=0; i<20; i++)
				if(pf[i])
					s += "<td bgcolor='#" + Integer.toHexString(cl1) + "'>&nbsp;&nbsp;</td>";
				else
					s += "<td>&nbsp;&nbsp;</td>";
			s += "</tr><tr><td><b>Right side:&nbsp;</b></td>";
			for(int i=0; i<20; i++)
				if(pf[19-i])
					s += "<td bgcolor='#" + Integer.toHexString(cl1) + "'>&nbsp;&nbsp;</td>";
				else
					s += "<td>&nbsp;&nbsp;</td>";
			s += "</tr></table>";
			return s;
		}
}

	//
	// Missile
	//
	class Missile extends Sprite
	{
		protected int pos;
		protected boolean[] pixel = new boolean[160];
		int size;
		int speed;
		int copies;
		int distance;
		boolean locked;
		Player player;

		void reset()
		{
			size = 1;
			copies = 1;
			distance = 1;
			pos = 80;
			speed = 0;
			locked = false;
			System.arraycopy(falseArray, 0, pixel, 0, 160);
		}

		void move()
		{
			if(speed == 0)
				return;
			if(locked)
			{
				if(player.size == 1)
					pos = player.pos + 3;
				else if(player.size == 2)
					pos = player.pos + 6;
				else if(player.size == 4)
					pos = player.pos + 10;
			}
			else
			{
				pos += speed;
				pos = adjust(pos);
			}
			redraw(); // TODO move the image in the array, instead of
			          //      redrawing everything
		}

		void redraw()
		{
			System.arraycopy(falseArray, 0, pixel, 0, 160);

			for(int i = 0; i < copies; i++)
				for(int j = 0; j < size; j++)
					pixel[adjust(pos + j + (i * distance))] = true;
		}

		void draw(int x1, int x2)
		{
			for(int i = x1; i < x2; i++)
				if(pixel[i])
					setPixel(i, y, color);
		}

		void updateStack() {}
	
		String describe()
		{
			String s;
			s = "<table>";
			s += "<tr><td><b>Enabled</b></td><td>" + (enabled ? "yes" : "no") + "</td></tr>";
			s += "<tr><td><b>Position locked&nbsp;</b></td><td>" + (locked ? "yes" : "no") + "</td></tr>";
			s += "<tr><td><b>Horiz. Position&nbsp;</b></td><td>" + pos + "</td></tr>";
			s += "<tr><td><b>Speed</b></td><td>" + speed + "</td></tr>";
			s += "</table>";
			if(enabled)
			{
				s += "<table border='1'><tr>";
				for(int i = 0; i < copies; i++)
				{
					for(int j = 0; j < size; j++)
						s += "<td bgcolor='#" + Integer.toHexString(color) + "'>&nbsp;&nbsp;</td>";
					if(copies > 1)
						s += "<td>" + distance + " pixels</td>";
				}
				s += "</table>";
			}

			return s;
		}
	}

	//
	// Ball
	//
	private class Ball extends Missile
	{
	}

	// 
	// Player
	//
	private class Player extends Missile
	{
		boolean reflect;
		int grp;

		void reset()
		{
			super.reset();
			reflect = false;
			grp = 0x0;
		}

		void draw(int x1, int x2)
		{
			for(int i = x1; i < x2; i++)
				if(pixel[i])
					setPixel(i, y, color);
		}

		void redraw()
		{
			System.arraycopy(falseArray, 0, pixel, 0, 160);

			if(!enabled)
				return;
			
			int ps = pos;

			for(int cp=0; cp<copies; cp++)
			{
				for(int i=0; i<8; i++)
				{
					int bt;
					if(reflect)
						bt = (grp >> i) & 1;
					else
						bt = (grp >> (7-i)) & 1;
					for(int j=0; j<size; j++)
					{
						if(bt != 0)
							pixel[adjust(ps)] = true;
						ps++;
					}
				}
				ps += distance;
			}
		}

		void updateStack() {}
	
		String describe()
		{
			String s;
			s = "<table>";
			s += "<tr><td><b>Enabled</b></td><td>" + (enabled ? "yes" : "no") + "</td></tr>";
			s += "<tr><td><b>Horiz. Position&nbsp;</b></td><td>" + pos + "</td></tr>";
			s += "<tr><td><b>Reflected</b></td><td>" + (reflect ? "yes" : "no") + "</td></tr>";
			s += "<tr><td><b>Speed</b></td><td>" + speed + "</td></tr>";
			s += "</table>";
			s += "<table border='1'><tr><td><b>GRP</b></td>";
			for(int i=0; i<8; i++)
			{
				if(((grp >> i) & 1) != 0)
					s += "<td bgcolor='#" + Integer.toHexString(color) + "'>&nbsp;&nbsp;</td>";
				else
					s += "<td>&nbsp;&nbsp;</td>";
			}
			s += "</tr>";
			if(enabled)
			{
				s += "</tr><tr><td><b>Actual Graphics</b></td>";
				for(int cp=0; cp<copies; cp++)
				{
					for(int i=0; i<8; i++)
					{
						int bt;
						if(reflect)
							bt = (grp >> i) & 1;
						else
							bt = (grp >> (7-i)) & 1;
						for(int j=0; j<size; j++)
						{
							if(bt != 0)
								s += "<td bgcolor='#" + Integer.toHexString(color) + "'>&nbsp;&nbsp;</td>";
							else
								s += "<td>&nbsp;&nbsp;</td>";
						}
					}
					if(copies > 1)
						s += "<td>" + distance + " pixels</td>";
				}

				s += "</tr>";
			}
			s += "</table>";
			return s;
		}
	}

	//
	// Electron
	//
	private int x, y;
	private int width;
	private boolean lastWasSync = false;

	//
	// Sprites
	//
	private Background background = new Background();
	private Playfield playfield = new Playfield();
	private Missile[] m = new Missile[2];
	private Player[] p = new Player[2];
	private Ball ball = new Ball();

	// 
	// Memory
	//
	int posAfter;
	int dataAfter;

	public String name() { return "TIA 1A"; }
	public int height() { return 192; }
	public int width() { return 320; }

	public TIA1A()
	{
		super(60);
		width = width();
		m[0] = new Missile();
		m[1] = new Missile();
		p[0] = new Player();
		p[1] = new Player();
		m[0].player = p[0];
		m[1].player = p[1];
	}

	public void reset()
	{
		x = -68;
		y = -40;

		background.reset();
		playfield.reset();
		m[0].reset();
		m[1].reset();
		p[0].reset();
		p[1].reset();
		ball.reset();
	}

	private final void setPixel(int xx, int yy, int color)
	{
		if(pixels[(yy*width + (xx * 2))] != (0xff000000 | color))
		{
			pixels[(yy*width + (xx * 2))] = 0xff000000 | color;
			pixels[(yy*width + (xx * 2 + 1))] = 0xff000000 | color;
		}
	}

	private void draw(int x1, int x2)
	{
		// TODO - check priorities when in SCORE mode
		// http://nocash.emubase.de/2k6specs.htm#videopriority
		if(y >= 0 && y < 192)
		{
			background.draw(x1, x2);
			if(!playfield.priority)
			{
				if(playfield.enabled)
					playfield.draw(x1, x2);
				if(ball.enabled)
					ball.draw(x1, x2);
			}
			if(m[1].enabled)
				m[1].draw(x1, x2);
			if(p[1].enabled)
				p[1].draw(x1, x2);
			if(m[0].enabled)
				m[0].draw(x1, x2);
			if(p[0].enabled)
				p[0].draw(x1, x2);
			if(playfield.priority)
			{
				if(playfield.enabled)
					playfield.draw(x1, x2);
				if(ball.enabled)
					ball.draw(x1, x2);
			}
		}
	}

	private void updateStack()
	{
		background.updateStack();
		playfield.updateStack();
		m[1].updateStack();
		m[0].updateStack();
		p[1].updateStack();
		p[0].updateStack();
		ball.updateStack();
	}
	
	public void step(int cycles)
	{
		if(lastWasSync)
		{
			lastWasSync = false;
			return;
		}

		// draw
		int x1 = x < 0 ? 0 : x;
		int x2 = x + (cycles * 3) > 160 ? 160 : x + (cycles * 3);
		if(x2 >= 0)
			draw(x1, x2);
		
		x += (cycles * 3);

		if(x > 159)
		{
			x = -68 + (x - 160);
			y++;

			if(y == 192)
			{
				updateStack();
				screenDone = true;
			}
			else if(y == 40)
				screenBegin = true;
			else if(y == 260)
				y = 0;
		}

		if(posAfter != -1)
			memorySetAfter(posAfter, dataAfter, 0);
	}

	public boolean memorySet(int pos, int data, int cycles)
	{
		posAfter = -1;

		switch(pos)
		{
			/*
			 * TV Set
			 */
			case VSYNC:
				if((data & 0x2) > 0)
				{
					//updateStack();
					x = -68;
					y = -40;
					//screenDone = true;
					lastWasSync = true;
				}
				break;

			case WSYNC:
				draw((x < 0) ? 0 : x, 160);
				x = -68;
				y++;

				if(y == 192)
				{
					updateStack();
					screenDone = true;
				}
				else if(y == 40)
					screenBegin = true;
				else if(y == 260)
					y = 0;
				lastWasSync = true;
				break;

			default:
				posAfter = pos;
				dataAfter = data;
		}
		return false;
	}
	
	public void memorySetAfter(int pos, int data, int cycles) 
	{
		posAfter = -1; // avoids infinite loop
		Missile mp;

		switch(pos)
		{
			// 
			// Playfield
			//
			case CTRLPF:
				{
					int j = 1;
					playfield.reflect = (data & 0x1) > 0 ? true : false;
					playfield.score = (data & 0x2) > 0 ? true : false;
					playfield.priority = (data & 0x4) > 0 ? true : false;
					switch((data & 0x30) >> 5)
					{
						case 0: j = 1; break;
						case 1: j = 2; break;
						case 2: j = 4; break;
						case 3: j = 8; break;
					}
					if(ball.size != j)
					{
						ball.size = j;
						ball.redraw();
					}
				}
				break;

			case PF0: /* Playfield graphics 0 */
				for(int i=4; i<8; i++)
					playfield.pf[i-4] = (data & (1 << i)) >> i > 0;
				playfield.checkEnabled();
				break;
	
			case PF1: /* Playfield graphics 1 */
				for(int i=0; i<8; i++)
					playfield.pf[i+4] = (data & (0x80 >> i)) != 0;
				playfield.checkEnabled();
				break;

			case PF2: /* Playfield graphics 2 */
				for(int i=0; i<8; i++)
					playfield.pf[i+12] = (data & (1 << i)) >> i > 0;
				playfield.checkEnabled();
				break;


			//
			// Shape / Enable
			//
			case NUSIZ0:
			case NUSIZ1:
				{
					int n = (pos == NUSIZ0 ? 0 : 1);

					int j = 1, i = (data >> 4) & 0x3;
					switch(i)
					{
						case 0: j = 1; break;
						case 1: j = 2; break;
						case 2: j = 4; break;
						case 3: j = 8; break;
					}
					m[n].size = j;
					
					switch(data & 0x7)
					{
						case 0: 
							p[n].size = 1;
							p[n].copies = m[n].copies = 1;
							p[n].distance = m[n].distance = 0;
							break;
						case 1:
							p[n].size = 1;
							p[n].copies = m[n].copies = 2;
							p[n].distance = m[n].distance = 16;
							break;
						case 2:
							p[n].size = 1;
							p[n].copies = m[n].copies = 2;
							p[n].distance = m[n].distance = 32;
							break;
						case 3:
							p[n].size = 1;
							p[n].copies = m[n].copies = 3;
							p[n].distance = m[n].distance = 16;
							break;
						case 4:
							p[n].size = 1;
							p[n].copies = m[n].copies = 2;
							p[n].distance = m[n].distance = 32;
							break;
						case 5:
							p[n].size = 2;
							p[n].copies = m[n].copies = 1;
							p[n].distance = m[n].distance = 0;
							break;
						case 6:
							p[n].size = 1;
							p[n].copies = m[n].copies = 3;
							p[n].distance = m[n].distance = 32;
							break;
						case 7:
							p[n].size = 4;
							p[n].copies = m[n].copies = 1;
							p[n].distance = m[n].distance = 0;
							break;
					}
					m[n].redraw();
					p[n].redraw();
				}
				break;

			case GRP0:
				p[0].grp = data;
				p[0].enabled = (data != 0x0);
				p[0].redraw();
				break;

			case GRP1:
				p[1].grp = data;
				p[1].enabled = (data != 0x0);
				p[1].redraw();
				break;

			case ENAM0:
				m[0].enabled = ((data & 0x2) != 0);
				break;

			case ENAM1:
				m[1].enabled = ((data & 0x2) != 0);
				break;

			case ENABL:
				ball.enabled = ((data & 0x2) != 0);
				break;

			case REFP0:
				p[0].reflect = (data & 0x8) > 0 ? true : false;
				p[0].redraw();
				break;

			case REFP1:
				p[1].reflect = (data & 0x8) > 0 ? true : false;
				p[1].redraw();
				break;

			case VDELP0:
				// TODO
				break;

			case VDELP1:
				// TODO
				break;

			case VDELBL:
				// TODO
				break;

			// 
			// Horizontal positioning
			//
			case RESP0:
			case RESP1:
			case RESM0:
			case RESM1:
			case RESBL:
				mp = null;
				if(pos == RESP0) mp = p[0];
				else if(pos == RESP1) mp = p[1];
				else if(pos == RESM0) mp = m[0];
				else if(pos == RESM1) mp = m[1];
				else if(pos == RESBL) mp = ball;
				mp.pos = x + cycles + 5;
				if(mp.pos < 2)
					mp.pos = 2;
				else if(mp.pos > 160)
					mp.pos = 160;
				mp.redraw();
				break;

			case RESMP0:
				m[0].locked = (data & 0x2) != 0;
				m[0].move();
				break;

			case RESMP1:
				m[0].locked = (data & 0x2) != 0;
				m[1].move();
				break;

			// 
			// Horizontal motion
			//
			case HMP0:
			{
				int hmp0 = data >> 4;
				if(hmp0 >= 1 && hmp0 <= 7)
					p[0].speed = -hmp0;
				else if(hmp0 >= 8 && hmp0 <= 15)
					p[0].speed = (16 - hmp0);
				else
					p[0].speed = 0;
			}
			break;

			case HMP1:
			{
				int hmp1 = data >> 4;
				if(hmp1 >= 1 && hmp1 <= 7)
					p[1].speed = -hmp1;
				else if(hmp1 >= 8 && hmp1 <= 15)
					p[1].speed = (16 - hmp1);
				else
					p[1].speed = 0;
			}
			break;

			case HMM0:
			{
				int hmm0 = data >> 4;
				if(hmm0 >= 1 && hmm0 <= 7)
					m[0].speed = -hmm0;
				else if(hmm0 >= 8 && hmm0 <= 15)
					m[0].speed = (16 - hmm0);
				else
					m[0].speed = 0;
			}
			break;

			case HMM1:
			{
				int hmm1 = data >> 4;
				if(hmm1 >= 1 && hmm1 <= 7)
					m[1].speed = -hmm1;
				else if(hmm1 >= 8 && hmm1 <= 15)
					m[1].speed = (16 - hmm1);
				else
					m[1].speed = 0;
			}
			break;

			case HMBL:
			{
				int hmbl = data >> 4;
				if(hmbl >= 1 && hmbl <= 7)
					ball.speed = -hmbl;
				else if(hmbl >= 8 && hmbl <= 15)
					ball.speed = (16 - hmbl);
				else
					ball.speed = 0;
			}
			break;

			case HMOVE:
				m[0].move();
				m[1].move();
				p[0].move();
				p[1].move();
				ball.move();
				break;

			case HMCLR:
				m[0].speed = 0;
				m[1].speed = 0;
				p[0].speed = 0;
				p[1].speed = 0;
				ball.speed = 0;
				break;

			//
			// Colors
			//

			case COLUP0:
				p[0].color = m[0].color = color[data];
				break;

			case COLUP1:
				p[1].color = m[0].color = color[data];
				break;

			case COLUBK:
				background.color = color[data];
				break;

			case COLUPF:
				playfield.color = ball.color = color[data];
				break;
		}
	}

	public void rebuildDebugger()
	{
		String s;
		s = "<table border='0'>";
		s += "<tr><td><b>x =</b></td><td>" + x + "</td></tr>";
		s += "<tr><td><b>y =</b></td><td>" + y + "</td></tr>";
		s += "</table>";

		// Sprites
		s += "<h3>Background</h3>";
		s += "<p><blockquote>" + background.describe() + "</blockquote></p>";

		s += "<h3>Playfield</h3>";
		s += "<p><blockquote>" + playfield.describe() + "</blockquote></p>";

		s += "<h3>Player 0</h3>";
		s += "<p><blockquote>" + p[0].describe() + "</blockquote></p>";

		s += "<h3>Missile 0</h3>";
		s += "<p><blockquote>" + m[0].describe() + "</blockquote></p>";

		s += "<h3>Player 1</h3>";
		s += "<p><blockquote>" + p[1].describe() + "</blockquote></p>";

		s += "<h3>Missile 1</h3>";
		s += "<p><blockquote>" + m[1].describe() + "</blockquote></p>";

		s += "<h3>Ball</h3>";
		s += "<p><blockquote>" + ball.describe() + "</blockquote></p>";

		JSObject tia_table = (JSObject)JEmu.Window.eval("document.getElementById('video_table');");
		tia_table.setMember("innerHTML", s);
	}

	//
	// color table
	//
	final private int[] color =
	{
	  /* Grey */
	  0x0, 0x1c1c1c, 0x393939, 0x595959, 
	  0x797979, 0x929292, 0xababab, 0xbcbcbc, 
	  0xcdcdcd, 0xd9d9d9, 0xe6e6e6, 0xececec, 
	  0xf2f2f2, 0xf8f8f8, 0xffffff, 0xffffff, 
	  
	  /* Gold */
	  0x391701, 0x5e2304, 0x833008, 0xa54716, 
	  0xc85f24, 0xe37820, 0xff911d, 0xffab1d, 
	  0xffc51d, 0xffce34, 0xffd84c, 0xffe651, 
	  0xfff456, 0xfff977, 0xffff98, 0xffff98, 
	  
	  /* Orange */
	  0x451904, 0x721e11, 0x9f241e, 0xb33a20, 
	  0xc85122, 0xe36920, 0xff811e, 0xff8c25, 
	  0xff982c, 0xffae38, 0xffc545, 0xffc559, 
	  0xffc66d, 0xffd587, 0xffe4a1, 0xffe4a1, 
	  
	  /* Red Orange */
	  0x4a1704, 0x7e1a0d, 0xb21d17, 0xc82119, 
	  0xdf251c, 0xec3b38, 0xfa5255, 0xfc6161, 
	  0xff706e, 0xff7f7e, 0xff8f8f, 0xff9d9e, 
	  0xffabad, 0xffb9bd, 0xffc7ce, 0xffc7ce, 

	  /* Pink */
	  0x50568, 0x3b136d, 0x712272, 0x8b2a8c, 
	  0xa532a6, 0xb938ba, 0xcd3ecf, 0xdb47dd, 
	  0xea51eb, 0xf45ff5, 0xfe6dff, 0xfe7afd, 
	  0xff87fb, 0xff95fd, 0xffa4ff, 0xffa4ff, 

	  /* Purple */
	  0x280479, 0x400984, 0x590f90, 0x70249d, 
	  0x8839aa, 0xa441c3, 0xc04adc, 0xd054ed, 
	  0xe05eff, 0xe96dff, 0xf27cff, 0xf88aff, 
	  0xff98ff, 0xfea1ff, 0xfeabff, 0xfeabff, 

	  /* Blue Purple */
	  0x35088a, 0x420aad, 0x500cd0, 0x6428d0, 
	  0x7945d0, 0x8d4bd4, 0xa251d9, 0xb058ec, 
	  0xbe60ff, 0xc56bff, 0xcc77ff, 0xd183ff, 
	  0xd790ff, 0xdb9dff, 0xdfaaff, 0xdfaaff, 

	  /* Blue */
	  0x51e81, 0x626a5, 0x82fca, 0x263dd4, 
	  0x444cde, 0x4f5aee, 0x5a68ff, 0x6575ff, 
	  0x7183ff, 0x8091ff, 0x90a0ff, 0x97a9ff, 
	  0x9fb2ff, 0xafbeff, 0xc0cbff, 0xc0cbff, 

	  /* Blue */
	  0xc048b, 0x2218a0, 0x382db5, 0x483ec7, 
	  0x584fda, 0x6159ec, 0x6b64ff, 0x7a74ff, 
	  0x8a84ff, 0x918eff, 0x9998ff, 0xa5a3ff, 
	  0xb1aeff, 0xb8b8ff, 0xc0c2ff, 0xc0c2ff, 

	  /* Light Blue */
	  0x1d295a, 0x1d3876, 0x1d4892, 0x1c5cac, 
	  0x1c71c6, 0x3286cf, 0x489bd9, 0x4ea8ec, 
	  0x55b6ff, 0x70c7ff, 0x8cd8ff, 0x93dbff, 
	  0x9bdfff, 0xafe4ff, 0xc3e9ff, 0xc3e9ff, 

	  /* Turquoise */
	  0x2f4302, 0x395202, 0x446103, 0x417a12, 
	  0x3e9421, 0x4a9f2e, 0x57ab3b, 0x5cbd55, 
	  0x61d070, 0x69e27a, 0x72f584, 0x7cfa8d, 
	  0x87ff97, 0x9affa6, 0xadffb6, 0xadffb6, 

	  /* Green blue */
	  0xa4108, 0xd540a, 0x10680d, 0x137d0f, 
	  0x169212, 0x19a514, 0x1cb917, 0x1ec919, 
	  0x21d91b, 0x47e42d, 0x6ef040, 0x78f74d, 
	  0x83ff5b, 0x9aff7a, 0xb2ff9a, 0xb2ff9a, 

	  /* Green */
	  0x4410b, 0x5530e, 0x66611, 0x77714, 
	  0x88817, 0x99b1a, 0xbaf1d, 0x48c41f, 
	  0x86d922, 0x8fe924, 0x99f927, 0xa8fc41, 
	  0xb7ff5b, 0xc9ff6e, 0xdcff81, 0xdcff81, 

	  /* Yellow Green */
	  0x2350f, 0x73f15, 0xc4a1c, 0x2d5f1e, 
	  0x4f7420, 0x598324, 0x649228, 0x82a12e, 
	  0xa1b034, 0xa9c13a, 0xb2d241, 0xc4d945, 
	  0xd6e149, 0xe4f04e, 0xf2ff53, 0xf2ff53, 

	  /* Orange Green */
	  0x263001, 0x243803, 0x234005, 0x51541b, 
	  0x806931, 0x978135, 0xaf993a, 0xc2a73e, 
	  0xd5b543, 0xdbc03d, 0xe1cb38, 0xe2d836, 
	  0xe3e534, 0xeff258, 0xfbff7d, 0xfbff7d, 

	  /* Light Orange */
	  0x401a02, 0x581f05, 0x702408, 0x8d3a13, 
	  0xab511f, 0xb56427, 0xbf7730, 0xd0853a, 
	  0xe19344, 0xeda04e, 0xf9ad58, 0xfcb75c, 
	  0xffc160, 0xffc671, 0xffcb83, 0xffcb83, 
	};

	private final boolean[] falseArray = { false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false, false, false, false, false, false, false, 
		false, false, false, false };

	//
	// Registers (writing)
	//
	final int VSYNC		= 0x00;
	final int VBLANK 	= 0x01;
	final int WSYNC		= 0x02;
	final int RSYNC   	= 0x03;
	final int NUSIZ0	= 0x04;
	final int NUSIZ1	= 0x05;
	final int COLUP0	= 0x06;
	final int COLUP1	= 0x07;
	final int COLUPF  	= 0x08;
	final int COLUBK	= 0x09;
	final int CTRLPF  	= 0x0a;
	final int REFP0		= 0x0b;
	final int REFP1		= 0x0c;
	final int PF0		= 0x0d;
	final int PF1		= 0x0e;
	final int PF2		= 0x0f;
	final int RESP0		= 0x10;
	final int RESP1		= 0x11;
	final int RESM0		= 0x12;
	final int RESM1		= 0x13;
	final int RESBL		= 0x14;
	final int GRP0		= 0x1b;
	final int GRP1    	= 0x1c;
	final int ENAM0 	= 0x1d;
	final int ENAM1 	= 0x1e;
	final int ENABL   	= 0x1f;
	final int HMP0		= 0x20;
	final int HMP1    	= 0x21;
	final int HMM0  	= 0x22;
	final int HMM1  	= 0x23;
	final int HMBL    	= 0x24;
	final int VDELP0	= 0x25;
	final int VDELP1	= 0x26;
	final int VDELBL	= 0x27;
	final int RESMP0	= 0x28;
	final int RESMP1	= 0x29;
	final int HMOVE		= 0x2a;
	final int HMCLR   	= 0x2b;
	final int CXCLR		= 0x2c;
	
	// registers (reading)
	final int CXM0P		= 0x0;
	final int CXM1P		= 0x1;
	final int CXP0FB	= 0x2;
	final int CXP1FB	= 0x3;
	final int CXM0FB	= 0x4;
	final int CXM1FB	= 0x5;
	final int CXBLPF	= 0x6;
	final int CXPPMM	= 0x7;
	final int INPT0  	= 0x8;
	final int INPT1  	= 0x9;
	final int INPT2  	= 0xa;
	final int INPT3  	= 0xb;
	final int INPT4  	= 0xc;
	final int INPT5  	= 0xd;

}
