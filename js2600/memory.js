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
        return (this.data[pos] << 8) + this.data[pos+1];
    }

    this.set8 = function(pos, value)
    {
        this.data[pos] = value & 0xff;
    }
}
