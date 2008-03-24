import netscape.javascript.JSObject;

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
				cl1 = cl2 = color; // TODO
				// cl1 = player[0].color;
				// cl1 = player[1].color;
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
	}

	//
	// Missile
	//
	class Missile extends Sprite
	{
		private int n;
		protected Missile() {}
		Missile(int n) { this.n = n; }

		protected int size;
		protected int pos;
		private boolean[] pixel = new boolean[160];
		int speed;

		void reset()
		{
			size = 1;
			pos = 80;
			speed = 0;
			for(int i=0; i<160; i++)
				pixel[i] = false;
		}

		void setSize(int i)
		{
			size = i;
			redraw();
		}

		void move()
		{
			pos += speed;
			pos = adjust(pos);
			redraw();
		}

		void redraw()
		{
			for(int i = 0; i < 160; i++)
				pixel[i] = (i == pos);
		}

		void draw(int x1, int x2)
		{
			for(int i = x1; i < x2; i++)
				if(pixel[i])
					setPixel(i, y, p[n].color);
		}
	}

	// 
	// Player
	//
	private class Player extends Missile
	{
		void draw(int x1, int x2)
		{
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

	// 
	// Memory
	//
	int posAfter;
	short dataAfter;

	public String name() { return "TIA 1A"; }
	public int height() { return 192; }
	public int width() { return 320; }

	public TIA1A()
	{
		super(60);
		width = width();
		m[0] = new Missile(0);
		m[1] = new Missile(1);
		p[0] = new Player();
		p[1] = new Player();
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
		// ball.reset();
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
		if(y >= 0 && y < 192)
		{
			background.draw(x1, x2);
			if(playfield.enabled)
				playfield.draw(x1, x2);
			if(m[1].enabled)
				m[1].draw(x1, x2);
			if(m[0].enabled)
				m[0].draw(x1, x2);
		}
	}
	
	public void step(int cycles)
	{
		if(lastWasSync)
		{
			lastWasSync = false;
			return;
		}

		// draw
		if(JEmu.currentFrame == 0)
		{
			int x1 = x < 0 ? 0 : x;
			int x2 = x + (cycles * 3) > 160 ? 160 : x + (cycles * 3);
			draw(x1, x2);
		}

		x += (cycles * 3);

		if(x > 159)
		{
			x = -68 + (x - 160);
			y++;

			if(y == 192)
				screenDone = true;
			else if(y == 40)
				screenBegin = true;
			else if(y == 260)
				y = 0;
		}
	
		if(posAfter != -1)
			memorySetAfter(posAfter, dataAfter, 0);
	}

	public boolean memorySet(int pos, short data, int cycles)
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
					x = -68;
					y = -40;
					screenDone = true;
					lastWasSync = true;
				}
				break;

			case WSYNC:
				draw(x<0 ? 0 : x, 160);
				x = -68;
				y++;

				if(y == 192)
					screenDone = true;
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
	
	public void memorySetAfter(int pos, short data, int cycles) 
	{
		posAfter = -1; // avoids infinite loop

		switch(pos)
		{
			//
			// Background
			//
			case COLUBK:
				background.color = color[data];
				break;

			// 
			// Playfield
			//
			case COLUPF:
				playfield.color = color[data];
				break;

			case CTRLPF:
				playfield.reflect = (data & 0x1) > 0 ? true : false;
				playfield.score = (data & 0x2) > 0 ? true : false;
				playfield.priority = (data & 0x4) > 0 ? true : false;
				// ball.size = (data & 0x30) >> 5;
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
			// Player
			//
			case COLUP0:
				p[0].color = color[data];
				break;

			case COLUP1:
				p[1].color = color[data];
				break;

			case NUSIZ0:
				{
					// TODO - player
					int j = 1, i = (data >> 4) & 0x3;
					switch(i)
					{
						case 0: j = 1; break;
						case 1: j = 2; break;
						case 2: j = 4; break;
						case 3: j = 8; break;
					}
					m[0].setSize(j);
				}
				break;

			case NUSIZ1:
				{
					// TODO - player
					int j = 1, i = (data >> 4) & 0x3;
					switch(i)
					{
						case 0: j = 1; break;
						case 1: j = 2; break;
						case 2: j = 4; break;
						case 3: j = 8; break;
					}
					m[1].setSize(j);
				}
				break;

			//
			// Missile
			//
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

			case HMOVE:
				m[0].move();
				break;

			case ENAM0:
				m[0].enabled = ((data & 0x2) != 0);
				break;

			case ENAM1:
				m[1].enabled = ((data & 0x2) != 0);
				break;
		}
	}

	public void rebuildDebugger()
	{
		String s;
		s = "<table border='0'>";
		s += "<tr><td><b>x =</b></td><td>" + x + "</td></tr>";
		s += "<tr><td><b>y =</b></td><td>" + y + "</td></tr>";

		// COLUBK
		s += "<tr>";
		s += "<td>COLUBK</td>";
		s += "<td span='8' style='background-color: #" + Integer.toHexString(0x1000000 | background.color).substring(1) + "'>&nbsp;&nbsp;&nbsp;</td>";
		for(int i=0; i<256; i++)
			if(color[i] == background.color)
				s += "<td>" + i + "</td>";
		s += "</tr>";

		/*
		// Players
		for(int i=0; i<2; i++)
		{
			s += "<tr>";
			s += "<td>COLUP" + i + "</td>";
			s += "<td span='8' style='background-color: #" + Integer.toHexString(0x1000000 | p[i].color).substring(1) + "'>&nbsp;</td>";
			s += "</tr>";
		}

		// Missiles
		for(int i=0; i<2; i++)
		{
			s += "<tr>";
			s += "<td>M" + i + " pos</td>";
			s += "<td>" + p[0].missile.x + "</td>";
			s += "</tr>";
		}
		*/

		s += "</table>";

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
