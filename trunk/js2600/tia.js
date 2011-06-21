function TIA()
{
    this.x = -68;
    this.y = -40;
    this.COLUBK = 255;
    this.pixels = [];
    this.ctx = document.getElementById('tv').getContext('2d');
    this.image_data = this.ctx.getImageData(0, 0, 198, 326);

    this.step = function(cycles, debug)
    {
        prev = this.x;
        this.x += cycles;
        if(this.x > 159)
        {
            this.x -= 228;
            this.y += 1;
            this.draw(prev, 160, this.y-1);
        }
        else
            this.draw(prev, this.x, this.y);
        if(debug)
            this.update_screen();
    }


    this.draw = function(x1, x2, y)
    {
        if(y < 0 || y >= 192)
            return;
        for(x=x1; x<x2; x++)
            if(x >= 0)
            {
                p = this.COLUBK;
                this.set_pixel(x, y, p);
            }
    }


    this.set_pixel = function(x, y, c)
    {
        r = (colors[c] >> 24) & 0xff;
        g = (colors[c] >> 16) & 0xff;
        b = colors[c] & 0xff;
        p = ((y*(this.image_data.width*4)) + (x * 4 * 2));
        if(p > this.image_data.length)
            alert('Error');
        this.image_data.data[p] = r;
        this.image_data.data[p+1] = g;
        this.image_data.data[p+2] = b;
        this.image_data.data[p+3] = 255;
        this.image_data.data[p+4] = r;
        this.image_data.data[p+5] = g;
        this.image_data.data[p+6] = b;
        this.image_data.data[p+7] = 255;
    }


    this.update_screen = function()
    {
        this.ctx.putImageData(this.image_data, 0, 0);
    }


    this.set_register = function(pos, value)
    {
        switch(pos)
        {
            case COLUBK:
                this.COLUBK = value;
                break;
        }
    }
}
