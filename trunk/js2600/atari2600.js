function Atari2600()
{
    this.memory = new Memory(64 * 0x1000);
    this.cpu = new M6502(this.memory);
	this.last_elem_debug = null;

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
        this.cpu.step();
		this.update_debugger();
    }

    this.create_debugger = function()
    {
        pos = 0xf000;
        instructions = Array();
        while(this.memory.get8(pos) != 255 && pos <= 0xffff)
        {
            dbg = this.cpu.debug(pos);
            instructions.push('<tr id="pos_' + toHex16(pos) + '"><td class="instruction">' + toHex16(pos) + '</td><td class="instruction">' + dbg[0] + '</td></tr>');
            pos += dbg[1] + 1;
        }
        document.getElementById('cpu').innerHTML = instructions.join('\n');
		this.update_debugger();
    }
	
	this.update_debugger = function()
	{
		// cpu
		if(this.last_elem_debug)
			document.getElementById('pos_' + this.last_elem_debug).style.backgroundColor = null;
		document.getElementById('pos_' + toHex16(this.cpu.getPC())).style.backgroundColor = 'yellow';
		this.last_elem_debug = toHex16(this.cpu.getPC());
		
		// registers
		document.getElementById('reg_a').innerHTML = toHex8(this.cpu.A);
		document.getElementById('reg_x').innerHTML = toHex8(this.cpu.X);
		document.getElementById('reg_y').innerHTML = toHex8(this.cpu.Y);
		document.getElementById('reg_pc').innerHTML = toHex8(this.cpu.getPC());
		document.getElementById('reg_sp').innerHTML = toHex8(this.cpu.SP);

		// flags
		document.getElementById('flag_s').innerHTML = this.cpu.P.S;
		document.getElementById('flag_v').innerHTML = this.cpu.P.V;
		document.getElementById('flag_b').innerHTML = this.cpu.P.B;
		document.getElementById('flag_d').innerHTML = this.cpu.P.D;
		document.getElementById('flag_i').innerHTML = this.cpu.P.I;
		document.getElementById('flag_z').innerHTML = this.cpu.P.Z;
		document.getElementById('flag_c').innerHTML = this.cpu.P.C;
	}
}
