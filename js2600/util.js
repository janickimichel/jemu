function toHex8(v)
{
    if(v < 0x10)
        return '0' + v.toString(16).toUpperCase();
    else
        return v.toString(16).toUpperCase();
}

function toHex16(v)
{
    s = '';
    if(v < 0x10)
        s = '000';
    else if(v < 0x100)
        s = '00';
    else if(v < 0x1000)
        s = '0';
    return s + v.toString(16).toUpperCase();
}

function htmlColor(c)
{
    return '#' + toHex8(c >> 24) + toHex8((c >> 16) & 0xff) + toHex8(c & 0xff);
}

function decode64(input)
{ 
        var output = Array();
		var chr1, chr2, chr3;
		var enc1, enc2, enc3, enc4;
		var i = 0;
        var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
 
		input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");
 
		while (i < input.length) {
 
			enc1 = _keyStr.indexOf(input.charAt(i++));
			enc2 = _keyStr.indexOf(input.charAt(i++));
			enc3 = _keyStr.indexOf(input.charAt(i++));
			enc4 = _keyStr.indexOf(input.charAt(i++));
 
			chr1 = (enc1 << 2) | (enc2 >> 4);
			chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
			chr3 = ((enc3 & 3) << 6) | enc4;
 
			output.push(chr1);
 
			if (enc3 != 64) {
				output.push(chr2);
			}
			if (enc4 != 64) {
				output.push(chr3);
			}
		}
		return output;
}
