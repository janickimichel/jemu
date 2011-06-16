function M6502(memory) 
{
    //
    // registers
    //
    A = 0;
    X = 0;
    Y = 0;
    PC = 0;
    SP = 0;


    //
    // status registers
    // 
    P = { S:0, V:0, B:0, D:0, I:0, Z:0, C:0 };

    
    //
    // addressing mode
    //
    imp = {
        size: 0,
        parse: function() { return null; },
        toString: function(pos) { return ''; },
        memPos: function() { return null; }
    }

    acc = {
        size: 0,
        parse: function() { return A; },
        toString: function(pos) { return 'A'; },
        memPos: function() { return null; }
    }

    imm = {
        size: 1,
        parse: function() { return memory.get8(PC+1); },
        toString: function(pos) { return '#$' + toHex8(memory.get8(pos+1)); },
        memPos: function() { return null; }
    }

    rel = {
        size: 1,
        parse: function() { return memory.get8(PC+1); },
        toString: function(pos) { return memory.get8(pos+1).toString(); },
        memPos: function() { return null; }
    }

    abs = {
        size: 2,
        parse: function() { return memory.get8(
                                     memory.get16(PC+1)); },
        toString: function(pos) { return '$' + toHex16(memory.get16(pos+1)); },
        memPos: function() { return memory.get16(PC+1); }
    }

    zp = {
        size: 1,
        parse: function() { return memory.get8(
                                     memory.get8(PC+1)); },
        toString: function(pos) { return '$' + toHex8(memory.get8(pos+1)); },
        memPos: function() { return memory.get8(PC+1); }
    }

    absx = {
        size: 2,
        parse: function() { return memory.get8(
                                     (memory.get16(PC+1) + X) & 0xffff); },
        toString: function(pos) { return '$' + toHex16(memory.get16(pos+1)) + ',X'; },
        memPos: function() { return memory.get16(PC+1) + X & 0xffff; }
    }

    absy = {
        size: 2,
        parse: function() { return memory.get8(
                                     (memory.get16(PC+1) + Y) & 0xffff); },
        toString: function(pos) { return '$' + toHex16(memory.get16(pos+1)) + ',Y'; },
        memPos: function() { return memory.get16(PC+1) + Y & 0xffff; }
    }

    zpx = {
        size: 1,
        parse: function() { return memory.get8(
                                     (memory.get8(PC+1) + X) & 0xff); },
        toString: function(pos) { return '$' + toHex8(memory.get8(pos+1)) + ',X'; },
        memPos: function() { return memory.get8(PC+1) + X & 0xff; }
    }

    zpy = {
        size: 1,
        parse: function() { return memory.get8(
                                     (memory.get8(PC+1) + Y) & 0xff); },
        toString: function(pos) { return '$' + toHex8(memory.get8(pos+1)) + ',Y'; },
        memPos: function() { return memory.get8(PC+1) + Y & 0xff; }
    }

    ind = {
        size: 1,
        parse: function() 
        {
            l = memory.get8(memory.get8(PC+1));
            h = memory.get8((memory.get8(PC+1)+1) & 0xff) << 8;
            m = h | l;
            return memory.get8(m);
        },
        toString: function(pos) { return '($' + toHex8(memory.get8(pos+1)) + ')'; },
        memPos: function() { return null; }
    }

    indx = {
        size: 1,
        parse: function() 
        {
            l = memory.get8((memory.get8(PC+1) + X) & 0xff);
            h = memory.get8((memory.get8(PC+1) + 1 + X) & 0xff) << 8;
            m = h | l;
            return memory.get8(m);
        },
        toString: function(pos) { return '($' + toHex8(memory.get8(pos+1)) + ',X)'; },
        memPos: function()
        {
            l = memory.get8((memory.get8(PC+1) + X) & 0xff);
            h = memory.get8((memory.get8(PC+1) + 1 + X) & 0xff) << 8;
            return; h | l;
        }
    }
    
    indy = {
        size: 1,
        parse: function() 
        {
            l = memory.get8(memory.get8(PC+1));
            h = memory.get8((memory.get8(PC+1)+1) & 0xff) << 8;
            m = ((h | l) + Y) & 0xffff;
            return memory.get8(m);
        },
        toString: function(pos) { return '($' + toHex8(memory.get8(pos+1)) + '),Y'; },
        memPos: function()
        {
            l = memory.get8(memory.get8(PC+1));
            h = memory.get8((memory.get8(PC+1)+1) & 0xff) << 8;
            return ((h | l) + Y) & 0xffff;
        }
    }


    //
    // opcode execution
    //
    adc = function(M, D)
    {
        t = A + M + P.C;
        P.V = (((A >> 7) & 1) == ((t >> 7) & 1)) ? 1:0;
        P.N = ((A >> 7) & 1);
        P.Z = (t==0) ? 1:0;
        if(P.D)
        {
            t = bcd(A) + bcd(M) + P.C;
            P.C = (t>99) ? 1:0;
        }
        else
            P.C = (t>255) ? 1:0;
        A = t & 0xff;
    }

    and = function(M, D)
    {
        A = A & M;
        P.N = ((A >> 7) & 1);
        P.Z = (A==0) ? 1:0 ;
    }

    asl = function(M, D)
    {
        P.C = ((B>>7)&1);
        B = (B << 1) & 0xFE;
        P.N = ((B>>7)&1);
        P.Z = (B==0) ? 1:0;
        if(D == null)
            A = B;
        else
            memory.set8(D, B);
    }

    bcc = function(M, D)
    {
        if (P.C == 0) 
            PC = (PC+M);
    }

    bcs = function(M, D)
    {
        if (P.C == 1) 
            PC = (PC+M);
    }

    beq = function(M, D)
    {
        if (P.Z == 1) 
            PC = (PC+M);
    }

    bit = function(M, D)
    {
        t = A & M;
        P.N = (t>>7)&1;
        P.V = (t>>6)&1;
        P.Z = (t==0) ? 1:0;
    }

    bmi = function(M, D)
    {
        if (P.N == 1) 
            PC = (PC+M);
    }

    bne = function(M, D)
    {
        if (P.Z == 0) 
            PC = (PC+M);
    }

    bpl = function(M, D)
    {
        if (P.N == 0) 
            PC = (PC+M);
    }

    brk = function(M, D)
    {
        PC = PC + 1;
        memory.set8(SP, (PC>>8));
        SP = SP - 1;
        memory.set8(SP, PC & 0xff);
        SP = SP - 1;
        PSW = (P.N << 7) + (P.V << 6) + (P.B << 4) + (P.D << 3) + (P.I << 2) + (P.Z << 1) + P.C;
        memory.set8(SP, (PSW|0x10));
        SP = SP - 1;
        l = memory.get8(0xFFFE);
        h = memory.get8(0xFFFF)<<8;
        PC = h|l;
    }

    bvc = function(M, D)
    {
        if (P.V == 0) 
            PC = (PC+M);
    }

    bvs = function(M, D)
    {
        if (P.V == 1) 
            PC = (PC+M);
    }

    clc = function(M, D) { P.C = 0; }

    cld = function(M, D) { P.D = 0; }

    cli = function(M, D) { P.I = 0; }

    clv = function(M, D) { P.V = 0; }

    cmp = function(M, D)
    {
        t = A - M;
        P.N = (t>>7)&1;
        P.C = (A>=M) ? 1:0;
        P.Z = (t==0) ? 1:0;
    }

    cpx = function(M, D)
    {
        t = X - M;
        P.N = (t>>7)&1;
        P.C = (X>=M) ? 1:0;
        P.Z = (t==0) ? 1:0;
    }

    cpy = function(M, D)
    {
        t = Y - M;
        P.N = (t>>7)&1;
        P.C = (Y>=M) ? 1:0;
        P.Z = (t==0) ? 1:0;
    }

    dec = function(M, D)
    {
        M = (M - 1) & 0xFF;
        P.N = (M>>7)&1;
        P.Z = (M==0) ? 1:0;
        memory.set8(D, M);
    }

    dex = function(M, D)
    {
        X = X - 1;
        P.Z = (X==0) ? 1:0;
        P.N = (X>>7)&1; 
    }

    dey = function(M, D)
    {
        Y = Y - 1;
        P.Z = (Y==0) ? 1:0;
        P.N = (Y>>7)&1; 
    }

    eor = function(M, D)
    {
        A = A ^ M;
        P.N = (A>>7)&1;
        P.Z = (A==0) ? 1:0;
    }

    inc = function(M, D)
    {
        M = (M + 1) & 0xFF;
        P.N = (M>>7)&1;
        P.Z = (M==0) ? 1:0;
        memory.set8(D, M);
    }

    inx = function(M, D)
    {
        X = X + 1;
        P.Z = (X==0) ? 1:0;
        P.N = (X>>7)&1;
    }

    iny = function(M, D)
    {
        Y = Y + 1;
        P.Z = (Y==0) ? 1:0;
        P.N = (Y>>7)&1;
    }

    jmp = function(M, D)
    {
        PC = M;
    }

    jsr = function(M, D)
    {
        t = PC - 1;
        memory.set8(SP,(t>>8)&0xff);
        SP = SP - 1;
        memory.set8(SP,t&0xff);
        SP = SP - 1;
        PC = M;
    }

    lda = function(M, D)
    {
        A = M;
        P.N = (A>>7)&1;
        P.Z = (A==0) ? 1:0;
    }

    ldx = function(M, D)
    {
        X = M;
        P.N = (X>>7)&1;
        P.Z = (X==0) ? 1:0;
    }

    ldy = function(M, D)
    {
        Y = M;
        P.N = (Y>>7)&1;
        P.Z = (Y==0) ? 1:0;
    }

    lsr = function(M, D)
    {
        P.N = 0;
        P.C = B&1;
        B = (B >> 1) & 0x7F;
        P.Z = (B==0) ? 1:0;
        if(D == null)
            A = B;
        else
            memory.set8(D, B);
    }

    nop = function(M, D) { }

    ora = function(M, D)
    {
        A = A | M;
        P.N = (A>>7)&1;
        P.Z = (A==0) ? 1:0;
    }

    pha = function(M, D)
    {
        memory.set8(SP, A);
        SP = SP - 1;
    }

    php = function(M, D)
    {
        PSW = (P.N << 7) + (P.V << 6) + (P.B << 4) + (P.D << 3) + (P.I << 2) + (P.Z << 1) + P.C;
        memory.set8(SP, PSW);
        SP = SP - 1;
    }

    pla = function(M, D)
    {
        SP = SP + 1;
        A = memory.get8(SP);
        P.N = (A>>7)&1;
        P.Z = (A==0) ? 1:0;
    }

    plp = function(M, D)
    {
        SP = SP + 1;
        PSW = memory.get8(SP);
        P.N = (PSW >> 7) & 1;
        P.V = (PSW >> 6) & 1;
        P.B = (PSW >> 4) & 1;
        P.D = (PSW >> 3) & 1;
        P.I = (PSW >> 2) & 1;
        P.Z = (PSW >> 1) & 1;
        P.C = PSW & 1;
    }

    rol = function(M, D)
    {
        t = (B>>7)&1;
        B = (B << 1) & 0xFE;
        B = B | P.C;
        P.C = t;
        P.Z = (B==0) ? 1:0;
        P.N = (B>>7)&1;
        if(D == null)
            A = B;
        else
            memory.set8(D, B);
    }

    ror = function(M, D)
    {
        t = B&1;
        B = (B >> 1) & 0x7F;
        B = B | ((P.C) ? 0x80 : 0x00);
        P.C = t;
        P.Z = (B==0) ? 1:0;
        P.N = (B>>7)&1;
        if(D == null)
            A = B;
        else
            memory.set8(D, B);
    }

    rti = function(M, D)
    {
        SP = SP - 1;
        PSW = memory.get8(SP);
        P.N = (PSW >> 7) & 1;
        P.V = (PSW >> 6) & 1;
        P.B = (PSW >> 4) & 1;
        P.D = (PSW >> 3) & 1;
        P.I = (PSW >> 2) & 1;
        P.Z = (PSW >> 1) & 1;
        P.C = PSW & 1;
        SP = SP - 1;
        l = memory.get8(SP);
        SP = SP - 1
        h = memory.get8(SP)<<8;
        PC = h|l;
    }

    rts = function(M, D)
    {
        SP = SP + 1;
        l = memory.get8(SP);
        SP = SP + 1;
        h = memory.get8(SP)<<8;
        PC = (h|l) + 1;
    }

    sbc = function(M, D)
    {
        if(P.D)
        {
            t = bcd(A) - bcd(M) - (P.C == 1 ? 0 : 1);
            P.V = (t>99 || t<0) ? 1:0;
        }
        else
        {
            t = A - M - (P.C == 1 ? 0 : 1);
            P.V = (t>127 || t<(-128)) ? 1:0;
        }
        P.C = (t>=0) ? 1:0;
        P.N = (t>>7)&1;
        P.Z = (t==0) ? 1:0;
        A = t & 0xFF;
    }

    sec = function(M, D)
    {
        P.C = 1;
    }
    
    sed = function(M, D)
    {
        P.D = 1;
    }
    
    sei = function(M, D)
    {
        P.I = 1;
    }

    sta = function(M, D)
    {
        memory.set8(D, A);
    }
    
    stx = function(M, D)
    {
        memory.set8(D, X);
    }
    
    sty = function(M, D)
    {
        memory.set8(D, Y);
    }

    tax = function(M, D)
    {
        X = A;
        P.N = (X>>7)&1;
        P.Z = (X==0) ? 1:0;
    }
    
    tay = function(M, D)
    {
        Y = A;
        P.N = (Y>>7)&1;
        P.Z = (Y==0) ? 1:0;
    }

    tsx = function(M, D)
    {
        X = SP;
        P.N = (X>>7)&1;
        P.Z = (X==0) ? 1:0;
    }

    txa = function(M, D)
    {
        A = X;
        P.N = (A>>7)&1;
        P.Z = (A==0) ? 1:0;
    }

    txs = function(M, D)
    {
        SP = X;
    }
    
    tya = function(M, D)
    {
        A = Y;
        P.N = (A>>7)&1;
        P.Z = (A==0) ? 1:0;
    }

    
    //
    // opcodes
    //
    // opcode: [ instruction_fct, addressing, desc, inst_length, cycles, page_boundary ]
    opcodes = {
        0x00: [ brk, imp, 'BRK', 0, 7 ],
        0x01: [ ora, indx, 'ORA', 2, 6 ],
        0x05: [ ora, zp, 'ORA', 2, 3 ],
        0x06: [ asl, zp, 'ASL', 2, 5 ],
        0x08: [ php, imp, 'PHP', 1, 3 ],
        0x09: [ ora, imm, 'ORA', 2, 2 ],
        0x0A: [ asl, acc, 'ASL', 1, 2 ],
        0x0D: [ ora, abs, 'ORA', 3, 4 ],
        0x0E: [ asl, abs, 'ASL', 3, 6 ],
        0x10: [ bpl, rel, 'BPL', 2, 3 ],
        0x11: [ ora, indy, 'ORA', 2, 5 ],
        0x15: [ ora, zpx, 'ORA', 2, 4 ],
        0x16: [ asl, zpx, 'ASL', 2, 6 ],
        0x18: [ clc, imp, 'CLC', 1, 2 ],
        0x19: [ ora, absy, 'ORA', 3, 4 ],
        0x1D: [ ora, absx, 'ORA', 3, 4 ],
        0x1E: [ asl, absx, 'ASL', 3, 7 ],
        0x20: [ jsr, abs, 'JSR', 0, 6 ],
        0x21: [ and, indx, 'AND', 2, 6 ],
        0x24: [ bit, zp, 'BIT', 2, 3 ],
        0x25: [ and, zp, 'AND', 2, 3 ],
        0x26: [ rol, zp, 'ROL', 2, 5 ],
        0x28: [ plp, imp, 'PLP', 1, 4 ],
        0x29: [ and, imm, 'AND', 2, 2 ],
        0x2A: [ rol, acc, 'ROL', 1, 2 ],
        0x2C: [ bit, abs, 'BIT', 3, 4 ],
        0x2D: [ and, abs, 'AND', 3, 4 ],
        0x2E: [ rol, abs, 'ROL', 3, 6 ],
        0x30: [ bmi, rel, 'BMI', 2, 2 ],
        0x31: [ and, indy, 'AND', 2, 5 ],
        0x35: [ and, zpx, 'AND', 2, 4 ],
        0x36: [ rol, zpx, 'ROL', 2, 6 ],
        0x38: [ sec, imp, 'SEC', 1, 2 ],
        0x39: [ and, absy, 'AND', 3, 4 ],
        0x3D: [ and, absx, 'AND', 3, 4 ],
        0x3E: [ rol, absx, 'ROL', 3, 7 ],
        0x40: [ rti, imp, 'RTI', 0, 6 ],
        0x41: [ eor, indx, 'EOR', 2, 6 ],
        0x45: [ eor, zp, 'EOR', 2, 3 ],
        0x46: [ lsr, zp, 'LSR', 2, 5 ],
        0x48: [ pha, imp, 'PHA', 1, 3 ],
        0x49: [ eor, imm, 'EOR', 2, 2 ],
        0x4A: [ lsr, acc, 'LSR', 1, 2 ],
        0x4C: [ jmp, abs, 'JMP', 0, 3 ],
        0x4D: [ eor, abs, 'EOR', 3, 4 ],
        0x4E: [ lsr, abs, 'LSR', 3, 6 ],
        0x50: [ bvc, rel, 'BVC', 2, 3 ],
        0x51: [ eor, indy, 'EOR', 2, 5 ],
        0x55: [ eor, zpx, 'EOR', 2, 4 ],
        0x56: [ lsr, zpx, 'LSR', 2, 6 ],
        0x58: [ cli, imp, 'CLI', 1, 2 ],
        0x59: [ eor, absy, 'EOR', 3, 4 ],
        0x5D: [ eor, absx, 'EOR', 3, 4 ],
        0x5E: [ lsr, absx, 'LSR', 3, 7 ],
        0x60: [ rts, imp, 'RTS', 0, 6 ],
        0x61: [ adc, indx, 'ADC', 2, 6 ],
        0x65: [ adc, zp, 'ADC', 2, 3 ],
        0x66: [ ror, zp, 'ROR', 2, 5 ],
        0x68: [ pla, imp, 'PLA', 1, 4 ],
        0x69: [ adc, imm, 'ADC', 2, 2 ],
        0x6A: [ ror, acc, 'ROR', 1, 2 ],
        0x6C: [ jmp, ind, 'JMP', 0, 5 ],
        0x6D: [ adc, abs, 'ADC', 3, 4 ],
        0x6E: [ ror, abs, 'ROR', 3, 6 ],
        0x70: [ bvs, rel, 'BVS', 2, 2 ],
        0x71: [ adc, indy, 'ADC', 2, 5 ],
        0x75: [ adc, zpx, 'ADC', 2, 4 ],
        0x76: [ ror, zpx, 'ROR', 2, 6 ],
        0x78: [ sei, imp, 'SEI', 1, 2 ],
        0x79: [ adc, absy, 'ADC', 3, 4 ],
        0x7D: [ adc, absx, 'ADC', 3, 4 ],
        0x7E: [ ror, absx, 'ROR', 3, 7 ],
        0x81: [ sta, indx, 'STA', 2, 6 ],
        0x84: [ sty, zp, 'STY', 2, 3 ],
        0x85: [ sta, zp, 'STA', 2, 3 ],
        0x86: [ stx, zp, 'STX', 2, 3 ],
        0x88: [ dey, imp, 'DEY', 1, 2 ],
        0x8A: [ txa, imp, 'TXA', 1, 2 ],
        0x8C: [ sty, abs, 'STY', 3, 4 ],
        0x8D: [ sta, abs, 'STA', 3, 4 ],
        0x8E: [ stx, abs, 'STX', 3, 4 ],
        0x90: [ bcc, rel, 'BCC', 2, 3 ],
        0x91: [ sta, indy, 'STA', 2, 6 ],
        0x94: [ sty, zpx, 'STY', 2, 4 ],
        0x95: [ sta, zpx, 'STA', 2, 4 ],
        0x96: [ stx, zpy, 'STX', 2, 4 ],
        0x98: [ tya, imp, 'TYA', 1, 2 ],
        0x99: [ sta, absy, 'STA', 3, 5 ],
        0x9A: [ txs, imp, 'TXS', 0, 2 ],
        0x9D: [ sta, absx, 'STA', 3, 5 ],
        0xA0: [ ldy, imm, 'LDY', 2, 2 ],
        0xA1: [ lda, indx, 'LDA', 2, 6 ],
        0xA2: [ ldx, imm, 'LDX', 2, 2 ],
        0xA4: [ ldy, zp, 'LDY', 2, 3 ],
        0xA5: [ lda, zp, 'LDA', 2, 3 ],
        0xA6: [ ldx, zp, 'LDX', 2, 3 ],
        0xA8: [ tay, imp, 'TAY', 1, 2 ],
        0xA9: [ lda, imm, 'LDA', 2, 2 ],
        0xAA: [ tax, imp, 'TAX', 1, 2 ],
        0xAC: [ ldy, abs, 'LDY', 3, 4 ],
        0xAD: [ lda, abs, 'LDA', 3, 4 ],
        0xAE: [ ldx, abs, 'LDX', 3, 4 ],
        0xB0: [ bcs, rel, 'BCS', 2, 2 ],
        0xB1: [ lda, indy, 'LDA', 2, 5 ],
        0xB4: [ ldy, zpx, 'LDY', 2, 4 ],
        0xB5: [ lda, zpx, 'LDA', 2, 4 ],
        0xB6: [ ldx, zpy, 'LDX', 2, 4 ],
        0xB8: [ clv, imp, 'CLV', 1, 2 ],
        0xB9: [ lda, absy, 'LDA', 3, 4 ],
        0xBA: [ tsx, imp, 'TSX', 1, 2 ],
        0xBC: [ ldy, absx, 'LDY', 3, 4 ],
        0xBD: [ lda, absx, 'LDA', 3, 4 ],
        0xBE: [ ldx, absy, 'LDX', 3, 4 ],
        0xC0: [ cpy, imm, 'CPY', 2, 2 ],
        0xC1: [ cmp, indx, 'CMP', 2, 6 ],
        0xC4: [ cpy, zp, 'CPY', 2, 3 ],
        0xC5: [ cmp, zp, 'CMP', 2, 3 ],
        0xC6: [ dec, zp, 'DEC', 2, 5 ],
        0xC8: [ iny, imp, 'INY', 1, 2 ],
        0xC9: [ cmp, imm, 'CMP', 2, 2 ],
        0xCA: [ dex, imp, 'DEX', 1, 2 ],
        0xCC: [ cpy, abs, 'CPY', 3, 4 ],
        0xCD: [ cmp, abs, 'CMP', 3, 4 ],
        0xCE: [ dec, abs, 'DEC', 3, 6 ],
        0xD0: [ bne, rel, 'BNE', 2, 3 ],
        0xD1: [ cmp, indy, 'CMP', 2, 5 ],
        0xD5: [ cmp, zpx, 'CMP', 2, 4 ],
        0xD6: [ dec, zpx, 'DEC', 2, 6 ],
        0xD8: [ cld, imp, 'CLD', 1, 2 ],
        0xD9: [ cmp, absy, 'CMP', 3, 4 ],
        0xDD: [ cmp, absx, 'CMP', 3, 4 ],
        0xDE: [ dec, absx, 'DEC', 3, 7 ],
        0xE0: [ cpx, imm, 'CPX', 2, 2 ],
        0xE1: [ sbc, indx, 'SBC', 2, 6 ],
        0xE4: [ cpx, zp, 'CPX', 2, 3 ],
        0xE5: [ sbc, zp, 'SBC', 2, 3 ],
        0xE6: [ inc, zp, 'INC', 2, 5 ],
        0xE8: [ inx, imp, 'INX', 1, 2 ],
        0xE9: [ sbc, imm, 'SBC', 2, 2 ],
        0xEA: [ nop, imp, 'NOP', 1, 2 ],
        0xEC: [ cpx, abs, 'CPX', 3, 4 ],
        0xED: [ sbc, abs, 'SBC', 3, 4 ],
        0xEE: [ inc, abs, 'INC', 3, 6 ],
        0xF0: [ beq, rel, 'BEQ', 2, 2 ],
        0xF1: [ sbc, indy, 'SBC', 2, 5 ],
        0xF5: [ sbc, zpx, 'SBC', 2, 4 ],
        0xF6: [ inc, zpx, 'INC', 2, 6 ],
        0xF8: [ sed, imp, 'SED', 1, 2 ],
        0xF9: [ sbc, absy, 'SBC', 3, 4 ],
        0xFD: [ sbc, absx, 'SBC', 3, 4 ],
        0xFE: [ inc, absx, 'INC', 3, 7 ],

        /* UNDOCUMMENTED 
         
        0x02: [ kil, imp,  'KIL', 0, 0 ],
        0x03: [ slo, izx,  'SLO', 2, 8 ],
        0x04: [ nop, zp,  'NOP', 2, 3 ],
        0x07: [ slo, zp,  'SLO', 2, 5 ],
        0x0B: [ anc, imm,  'ANC', 2, 2 ],
        0x0C: [ nop, abs,  'NOP', 3, 4 ],
        0x0F: [ slo, abs,  'SLO', 3, 6 ],
        0x12: [ kil, imp,  'KIL', 0, 0 ],
        0x13: [ slo, izy,  'SLO', 2, 8 ],
        0x14: [ nop, zpx,  'NOP', 2, 4 ],
        0x17: [ slo, zpx,  'SLO', 2, 6 ],
        0x1A: [ nop, imp,  'NOP', 1, 2 ],
        0x1B: [ slo, absy,  'SLO', 3, 7 ],
        0x1C: [ nop, absx,  'NOP', 3, 4 ],
        0x1F: [ slo, absx,  'SLO', 3, 7 ],
        0x22: [ kil, imp,  'KIL', 0, 0 ],
        0x23: [ rla, izx,  'RLA', 2, 8 ],
        0x27: [ rla, zp,  'RLA', 2, 5 ],
        0x2B: [ anc, imm,  'ANC', 2, 2 ],
        0x2F: [ rla, abs,  'RLA', 3, 6 ],
        0x32: [ kil, imp,  'KIL', 0, 0 ],
        0x33: [ rla, izy,  'RLA', 2, 8 ],
        0x34: [ nop, zpx,  'NOP', 2, 4 ],
        0x37: [ rla, zpx,  'RLA', 2, 6 ],
        0x3A: [ nop, imp,  'NOP', 1, 2 ],
        0x3B: [ rla, absy,  'RLA', 3, 7 ],
        0x3C: [ nop, absx,  'NOP', 3, 4 ],
        0x3F: [ rla, absx,  'RLA', 3, 7 ],
        0x42: [ kil, imp,  'KIL', 0, 0 ],
        0x43: [ sre, izx,  'SRE', 2, 8 ],
        0x44: [ nop, zp,  'NOP', 2, 3 ],
        0x47: [ sre, zp,  'SRE', 2, 5 ],
        0x4B: [ alr, imm,  'ALR', 2, 2 ],
        0x4F: [ sre, abs,  'SRE', 3, 6 ],
        0x52: [ kil, imp,  'KIL', 0, 0 ],
        0x53: [ sre, izy,  'SRE', 2, 8 ],
        0x54: [ nop, zpx,  'NOP', 2, 4 ],
        0x57: [ sre, zpx,  'SRE', 2, 6 ],
        0x5A: [ nop, imp,  'NOP', 1, 2 ],
        0x5B: [ sre, absy,  'SRE', 3, 7 ],
        0x5C: [ nop, absx,  'NOP', 3, 4 ],
        0x5F: [ sre, absx,  'SRE', 3, 7 ],
        0x62: [ kil, imp,  'KIL', 0, 0 ],
        0x63: [ rra, izx,  'RRA', 2, 8 ],
        0x64: [ nop, zp,  'NOP', 2, 3 ],
        0x67: [ rra, zp,  'RRA', 2, 5 ],
        0x6B: [ arr, imm,  'ARR', 2, 2 ],
        0x6F: [ rra, abs,  'RRA', 3, 6 ],
        0x72: [ kil, imp,  'KIL', 0, 0 ],
        0x73: [ rra, izy,  'RRA', 2, 8 ],
        0x74: [ nop, zpx,  'NOP', 2, 4 ],
        0x77: [ rra, zpx,  'RRA', 2, 6 ],
        0x7A: [ nop, imp,  'NOP', 1, 2 ],
        0x7B: [ rra, absy,  'RRA', 3, 7 ],
        0x7C: [ nop, absx,  'NOP', 3, 4 ],
        0x7F: [ rra, absx,  'RRA', 3, 7 ],
        0x80: [ nop, imm,  'NOP', 2, 2 ],
        0x82: [ nop, imm,  'NOP', 2, 2 ],
        0x83: [ sax, izx,  'SAX', 2, 6 ],
        0x87: [ sax, zp,  'SAX', 2, 3 ],
        0x89: [ nop, imm,  'NOP', 2, 2 ],
        0x8B: [ xaa, imm,  'XAA', 2, 2 ],
        0x8F: [ sax, abs,  'SAX', 3, 4 ],
        0x92: [ kil, imp,  'KIL', 0, 0 ],
        0x93: [ ahx, izy,  'AHX', 2, 6 ],
        0x97: [ sax, zpy,  'SAX', 2, 4 ],
        0x9B: [ tas, aby,  'TAS', 0, 5 ],
        0x9C: [ shy, absx,  'SHY', 3, 5 ],
        0x9E: [ shx, absy,  'SHX', 3, 5 ],
        0x9F: [ ahx, absy,  'AHX', 3, 5 ],
        0xA3: [ lax, izx,  'LAX', 2, 6 ],
        0xA7: [ lax, zp,  'LAX', 2, 3 ],
        0xAB: [ lax, imm,  'LAX', 2, 2 ],
        0xAF: [ lax, abs,  'LAX', 3, 4 ],
        0xB2: [ kil, imp,  'KIL', 0, 0 ],
        0xB3: [ lax, izy,  'LAX', 2, 5 ],
        0xB7: [ lax, zpy,  'LAX', 2, 4 ],
        0xBB: [ las, absy,  'LAS', 3, 4 ],
        0xBF: [ lax, absy,  'LAX', 3, 4 ],
        0xC2: [ nop, imm,  'NOP', 2, 2 ],
        0xC3: [ dcp, izx,  'DCP', 2, 8 ],
        0xC7: [ dcp, zp,  'DCP', 2, 5 ],
        0xCB: [ axs, imm,  'AXS', 2, 2 ],
        0xCF: [ dcp, abs,  'DCP', 3, 6 ],
        0xD2: [ kil, imp,  'KIL', 0, 0 ],
        0xD3: [ dcp, izy,  'DCP', 2, 8 ],
        0xD4: [ nop, zpx,  'NOP', 2, 4 ],
        0xD7: [ dcp, zpx,  'DCP', 2, 6 ],
        0xDA: [ nop, imp,  'NOP', 1, 2 ],
        0xDB: [ dcp, absy,  'DCP', 3, 7 ],
        0xDC: [ nop, absx,  'NOP', 3, 4 ],
        0xDF: [ dcp, absx,  'DCP', 3, 7 ],
        0xE2: [ nop, imm,  'NOP', 2, 2 ],
        0xE3: [ isc, izx,  'ISC', 2, 8 ],
        0xE7: [ isc, zp,  'ISC', 2, 5 ],
        0xEB: [ sbc, imm,  'SBC', 2, 2 ],
        0xEF: [ isc, abs,  'ISC', 3, 6 ],
        0xF2: [ kil, imp,  'KIL', 0, 0 ],
        0xF3: [ isc, izy,  'ISC', 2, 8 ],
        0xF4: [ nop, zpx,  'NOP', 2, 4 ],
        0xF7: [ isc, zpx,  'ISC', 2, 6 ],
        0xFA: [ nop, imp,  'NOP', 1, 2 ],
        0xFB: [ isc, absy,  'ISC', 3, 7 ],
        0xFC: [ nop, absx,  'NOP', 3, 4 ],
        0xFF: [ isc, absx,  'ISC', 3, 7 ], */
    }

    //
    // step
    //
    this.step = function()
    {
        opc = memory.get8(PC);
        opcodes[opc][0](opcodes[opc][1].parse(), opcodes[opc][1].memPos());
        if(opcodes[opc][1].size != 0)
            PC += opcodes[opc][1].size + 1;
        //opcodes[opc][3]
    }

    //
    // debug
    //
    this.debug = function(pos)
    {
        opc = memory.get8(pos);
        if(opcodes[opc])
            inst = opcodes[opc][2] + " " + opcodes[opc][1].toString(pos);
        else
            inst = "DATA $" + opc.toString(16);
        return [ inst, opcodes[opc][1].size, opcodes[opc][4] ];
    }
}
