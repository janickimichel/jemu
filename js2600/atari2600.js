function Atari2600()
{
    this.memory = new Memory(64 * 0x1000);
    this.cpu = new M6502(this.memory);
    this.video = new TIA();
    this.memory.video = this.video;

	this.last_elem_debug = null;
    this.breakpoints = {}

    this.load_rom = function(data)
    {
        bytes = decode64(data);
        pos = 0xf000;
        for(b in bytes)
        {
            this.memory.set8(pos, bytes[b]);
            pos++;
        }
    }

    this.step = function()
    {
        cycles = this.cpu.step();
        this.video.step(cycles * 3, true);
		this.update_debugger();
    }

    this.run = function()
    {
        while(this.breakpoints[this.cpu.getPC()] == null)
        {
            cycles = this.cpu.step();
            this.video.step(cycles * 3, false);
        }
		this.update_debugger();
    }

    this.next_line = function()
    {
        new_y = this.video.y + 1;
        while(this.video.y != new_y)
        {
            cycles = this.cpu.step();
            this.video.step(cycles * 3, false);
        }
        this.update_debugger();
    }

    this.create_debugger = function()
    {
        pos = 0xf000;
        instructions = Array();
        while(this.memory.get8(pos) != 255 && pos <= 0xffff)
        {
            dbg = this.cpu.debug(pos);
            instructions.push('<tr ondblclick="atari.set_bkp(0x' + toHex16(pos) + ');" id="pos_' + toHex16(pos) + '">' + 
                                 '<td class="instruction" id="bkp_' + toHex16(pos) + '" style="color: red;">&nbsp;</td>' + 
                                 '<td class="instruction">' + toHex16(pos) + '</td>' + 
                                 '<td class="instruction">' + dbg[0] + '</td>' + 
                              '</tr>');
            pos += dbg[1] + 1;
        }
        document.getElementById('cpu').innerHTML = instructions.join('\n');
		this.update_debugger();
    }
	
	this.update_debugger = function()
	{
        reg = this.cpu.registers();

        // cpu
		if(this.last_elem_debug)
			document.getElementById('pos_' + this.last_elem_debug).style.backgroundColor = null;
		document.getElementById('pos_' + toHex16(reg.PC)).style.backgroundColor = 'Khaki';
		this.last_elem_debug = toHex16(reg.PC);
		
		// registers
		document.getElementById('reg_a').innerHTML = toHex8(reg.A);
		document.getElementById('reg_x').innerHTML = toHex8(reg.X);
		document.getElementById('reg_y').innerHTML = toHex8(reg.Y);
		document.getElementById('reg_pc').innerHTML = toHex8(reg.PC);
		document.getElementById('reg_sp').innerHTML = toHex8(reg.SP);

		// flags
		document.getElementById('flag_s').innerHTML = reg.P.S;
		document.getElementById('flag_v').innerHTML = reg.P.V;
		document.getElementById('flag_b').innerHTML = reg.P.B;
		document.getElementById('flag_d').innerHTML = reg.P.D;
		document.getElementById('flag_i').innerHTML = reg.P.I;
		document.getElementById('flag_z').innerHTML = reg.P.Z;
		document.getElementById('flag_c').innerHTML = reg.P.C;
        
        // TIA
        document.getElementById('tia_x').innerHTML = this.video.x;
        document.getElementById('tia_y').innerHTML = this.video.y;
        document.getElementById('tia_colubk').innerHTML = this.video.COLUBK;
        document.getElementById('tia_colubk_c').style.backgroundColor = htmlColor(colors[this.video.COLUBK]);
	}

    this.set_bkp = function(address)
    {
        if(this.breakpoints[address])
        {
            this.breakpoints[address] = null;
            document.getElementById('bkp_' + toHex16(address)).innerHTML = '&nbsp;';
        }
        else
        {
            this.breakpoints[address] = 1;
            document.getElementById('bkp_' + toHex16(address)).innerHTML = '&#9679;';
        }
    }
}
