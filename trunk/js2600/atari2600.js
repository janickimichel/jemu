function Atari2600()
{
    this.memory = new Memory(64 * 0x1000);
    this.cpu = new M6502(this.memory);

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
    }

    this.update_debugger = function()
    {
        pos = 0xf000;
        instructions = Array();
        while(this.memory.get8(pos) != 255 && pos <= 0xffff)
        {
            dbg = this.cpu.debug(pos);
            instructions.push(dbg[0]);
            pos += dbg[1] + 1;
        }
        document.getElementById('cpu').innerHTML = '<pre>' + instructions.join('\n') + '</pre>';
    }
}
