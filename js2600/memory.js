function Memory(size) 
{
    this.data = Array();
    for(i=0; i<size; i++)
        this.data[i] = 0x0;

    this.get8 = function(pos)
    {
        return this.data[pos];
    }

    this.get16 = function(pos)
    {
        return (this.get8(pos+1) << 8) + this.get8(pos);
    }

    this.set8 = function(pos, value)
    {
        if(pos <= 0x2c)
            this.video.set_register(pos, value);
        else
            this.data[pos] = value & 0xff;
    }
}
