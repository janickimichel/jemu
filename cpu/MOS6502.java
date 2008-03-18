class MOS6502 extends CPU
{
	// registers
	private int A, X, Y;

	// flags
	private boolean S, O, B, D, I, Z, C;

	// stack pointer
	private int SP;

	public String name() { return "MOS 6502"; }

	public int step()
	{
		int add = IP;
		int opcode = JEmu.platform.getRAM(IP);
		int src, address, cycles = 0;
		
		switch(opcode)
		{
			// ADC (add with carry)
			case 0x69: cycles = 2; ADC(getSrc(imm(IP))); break;
			case 0x65: cycles = 3; ADC(getSrc(zpa(IP))); break;
			case 0x75: cycles = 4; ADC(getSrc(zpx(IP))); break;
			case 0x6D: cycles = 4; ADC(getSrc(abs(IP))); break;
			case 0x7D: cycles = 4; ADC(getSrc(abx(IP))); break;
			case 0x79: cycles = 4; ADC(getSrc(aby(IP))); break;
			case 0x61: cycles = 6; ADC(getSrc(inx(IP))); break;
			case 0x71: cycles = 5; ADC(getSrc(iny(IP))); break;

			// AND (and with memory and A)
			case 0x29: cycles = 2; AND(getSrc(imm(IP))); break;
			case 0x25: cycles = 3; AND(getSrc(zpa(IP))); break;
			case 0x35: cycles = 4; AND(getSrc(zpx(IP))); break;
			case 0x2D: cycles = 4; AND(getSrc(abs(IP))); break;
			case 0x3D: cycles = 4; AND(getSrc(abx(IP))); break;
			case 0x39: cycles = 4; AND(getSrc(aby(IP))); break;
			case 0x21: cycles = 6; AND(getSrc(inx(IP))); break;
			case 0x31: cycles = 5; AND(getSrc(iny(IP))); break;

			// ASL (A shift left)
			case 0x0A: cycles = 2; ASL(0, A, cycles, opcode); break;
			case 0x06: cycles = 5; address = zpa(IP); ASL(address, getSrc(address), cycles, opcode); break;
			case 0x16: cycles = 6; address = zpx(IP); ASL(address, getSrc(address), cycles, opcode); break;
			case 0x0E: cycles = 6; address = abs(IP); ASL(address, getSrc(address), cycles, opcode); break;
			case 0x1E: cycles = 7; address = abx(IP); ASL(address, getSrc(address), cycles, opcode); break;

			// BCC (branch on C)
			case 0x90: cycles = 2 + BCC(rel(IP)); break;

			// BCS (branch on carry set)
			case 0xB0: cycles = 2 + BCS(rel(IP)); break;

			// BEQ (branch on result zero)
			case 0xF0: cycles = 2 + BEQ(rel(IP)); break;

			// BIT (test bits in memory with A)
			case 0x24: cycles = 3; BIT(getSrc(zpa(IP))); break;
			case 0x2C: cycles = 4; BIT(getSrc(abs(IP))); break;

			// BMI (branch on result minus)
			case 0x30: cycles = 2 + BMI(rel(IP)); break;

			// BNE (branch on result not zero)
			case 0xD0: cycles = 2 + BNE(rel(IP)); break;

			// BPL (branch on result plus)
			case 0x10: cycles = 2 + BPL(rel(IP)); break;

			// BRK (force break)
			case 0x00: cycles = 7; BRK(cycles); break;

			// BVC (branch on overflow clear)
			case 0x50: cycles = 2 + BVC(rel(IP)); break;

			// BVS (branch on overflow set)
			case 0x70: cycles = 2 + BVS(rel(IP)); break;

			// CLC (clear carry flag)
			case 0x18: cycles = 2; CLC(); break;

			// CLD (clear decimal mode)
			case 0xD8: cycles = 2; CLD(); break;

			// CLI (clear interrupt disable bit)
			case 0x58: cycles = 2; CLI(); break;

			// CLV (clear overflow flag)
			case 0xB8: cycles = 2; CLV(); break;

			// CMP (compare memory and A)
			case 0xC9: cycles = 2; CMP(getSrc(imm(IP))); break;
			case 0xC5: cycles = 2; CMP(getSrc(zpa(IP))); break;
			case 0xD5: cycles = 4; CMP(getSrc(zpx(IP))); break;
			case 0xCD: cycles = 4; CMP(getSrc(abs(IP))); break;
			case 0xDD: cycles = 4; CMP(getSrc(abx(IP))); break;
			case 0xD9: cycles = 4; CMP(getSrc(aby(IP))); break;
			case 0xC1: cycles = 6; CMP(getSrc(inx(IP))); break;
			case 0xD1: cycles = 5; CMP(getSrc(iny(IP))); break;

			// CPX (compare memory and X)
			case 0xE0: cycles = 2; CPX(getSrc(imm(IP))); break;
			case 0xE4: cycles = 3; CPX(getSrc(zpa(IP))); break;
			case 0xEC: cycles = 4; CPX(getSrc(abs(IP))); break;

			// CPY (compare memory and Y)
			case 0xC0: cycles = 2; CPY(getSrc(imm(IP))); break;
			case 0xC4: cycles = 3; CPY(getSrc(zpa(IP))); break;
			case 0xCC: cycles = 4; CPY(getSrc(abs(IP))); break;

			// DEC (decrement memory)
			case 0xC6: cycles = 5; address = zpa(IP); DEC(address, getSrc(address), cycles); break;
			case 0xD6: cycles = 6; address = zpx(IP); DEC(address, getSrc(address), cycles); break;
			case 0xCE: cycles = 6; address = abs(IP); DEC(address, getSrc(address), cycles); break;
			case 0xDE: cycles = 7; address = abx(IP); DEC(address, getSrc(address), cycles); break;

			// DEX (decrement X)
			case 0xCA: cycles = 2; DEX(); break;

			// DEY (decrement Y)
			case 0x88: cycles = 2; DEY(); break;
			
			// EOR (exclusive-or memory with A)
			case 0x49: cycles = 2; EOR(getSrc(imm(IP))); break;
			case 0x45: cycles = 3; EOR(getSrc(zpa(IP))); break;
			case 0x55: cycles = 4; EOR(getSrc(zpx(IP))); break;
			case 0x40: cycles = 4; EOR(getSrc(abs(IP))); break;
			case 0x5D: cycles = 4; EOR(getSrc(abx(IP))); break;
			case 0x59: cycles = 4; EOR(getSrc(aby(IP))); break;
			case 0x41: cycles = 6; EOR(getSrc(inx(IP))); break;
			case 0x51: cycles = 5; EOR(getSrc(iny(IP))); break;

			// INC (increment memory)
			case 0xE6: cycles = 5; address = zpa(IP); INC(address, getSrc(address), cycles); break;
			case 0xF6: cycles = 6; address = zpx(IP); INC(address, getSrc(address), cycles); break;
			case 0xEE: cycles = 6; address = abs(IP); INC(address, getSrc(address), cycles); break;
			case 0xFE: cycles = 7; address = abx(IP); INC(address, getSrc(address), cycles); break;

			// INX (increment X)
			case 0xE8: cycles = 2; INX(); break;

			// INY (increment Y)
			case 0xC8: cycles = 2; INY(); break;

			// JMP (jump)
			case 0x4C: cycles = 3; JMP(abs(IP)); break;
			case 0x6C: cycles = 5; JMP(ind(IP)); break;

			// JSR (jump to subroutine)
			case 0x20: cycles = 6; JSR(abs(IP), cycles); break;

			// LDA (load A with memory)
			case 0xA9: cycles = 2; LDA(getSrc(imm(IP))); break;
			case 0xA5: cycles = 3; LDA(getSrc(zpa(IP))); break;
			case 0xB5: cycles = 4; LDA(getSrc(zpx(IP))); break;
			case 0xAD: cycles = 4; LDA(getSrc(abs(IP))); break;
			case 0xBD: cycles = 4; LDA(getSrc(abx(IP))); break;
			case 0xB9: cycles = 4; LDA(getSrc(aby(IP))); break;
			case 0xA1: cycles = 6; LDA(getSrc(inx(IP))); break;
			case 0xB1: cycles = 5; LDA(getSrc(iny(IP))); break;

			// LDX (load X with memory)
			case 0xA2: cycles = 2; LDX(getSrc(imm(IP))); break;
			case 0xA6: cycles = 3; LDX(getSrc(zpa(IP))); break;
			case 0xB6: cycles = 4; LDX(getSrc(zpx(IP))); break;
			case 0xAE: cycles = 4; LDX(getSrc(abs(IP))); break;
			case 0xBE: cycles = 4; LDX(getSrc(aby(IP))); break;

			// LDY (load Y with memory)
			case 0xA0: cycles = 2; LDY(getSrc(imm(IP))); break;
			case 0xA4: cycles = 3; LDY(getSrc(zpa(IP))); break;
			case 0xB4: cycles = 4; LDY(getSrc(zpx(IP))); break;
			case 0xAC: cycles = 4; LDY(getSrc(abs(IP))); break;
			case 0xBC: cycles = 4; LDY(getSrc(aby(IP))); break;

			// LSR (shift right)
			case 0x4A: cycles = 2; LSR(0, A, cycles, opcode); break;
			case 0x46: cycles = 5; address = zpa(IP); LSR(address, getSrc(address), cycles, opcode); break;
			case 0x56: cycles = 6; address = zpx(IP); LSR(address, getSrc(address), cycles, opcode); break;
			case 0x4E: cycles = 6; address = abs(IP); LSR(address, getSrc(address), cycles, opcode); break;
			case 0x5E: cycles = 7; address = abx(IP); LSR(address, getSrc(address), cycles, opcode); break;

			// NOP (no operation)
			case 0xEA: cycles = 2; break;

			// ORA (OR memory with A)
			case 0x09: cycles = 2; ORA(getSrc(imm(IP))); break;
			case 0x05: cycles = 3; ORA(getSrc(zpa(IP))); break;
			case 0x15: cycles = 4; ORA(getSrc(zpx(IP))); break;
			case 0x0D: cycles = 4; ORA(getSrc(abs(IP))); break;
			case 0x1D: cycles = 4; ORA(getSrc(abx(IP))); break;
			case 0x19: cycles = 4; ORA(getSrc(aby(IP))); break;
			case 0x01: cycles = 6; ORA(getSrc(inx(IP))); break;
			case 0x11: cycles = 5; ORA(getSrc(iny(IP))); break;

			// PHA (push A on stack)
			case 0x48: cycles = 3; PHA(cycles); break;

			// PHA (push SP on stack)
			case 0x08: cycles = 3; PHP(cycles); break;

			// PLA (push A from stack)
			case 0x68: cycles = 4; PLA(); break;

			// PLA (push A from stack)
			case 0x28: cycles = 4; PLP(); break;

			// ROL (rotate one bit left)
			case 0x2A: cycles = 2; ROL(0, A, cycles, opcode); break;
			case 0x26: cycles = 5; address = zpa(IP); ROL(address, getSrc(address), cycles, opcode); break;
			case 0x36: cycles = 6; address = zpx(IP); ROL(address, getSrc(address), cycles, opcode); break;
			case 0x2E: cycles = 6; address = abs(IP); ROL(address, getSrc(address), cycles, opcode); break;
			case 0x3E: cycles = 7; address = abx(IP); ROL(address, getSrc(address), cycles, opcode); break;

			// ROR (rotate one bit right)
			case 0x6A: cycles = 2; ROR(0, A, cycles, opcode); break;
			case 0x66: cycles = 5; address = zpa(IP); ROR(address, getSrc(address), cycles, opcode); break;
			case 0x76: cycles = 6; address = zpx(IP); ROR(address, getSrc(address), cycles, opcode); break;
			case 0x6E: cycles = 6; address = abs(IP); ROR(address, getSrc(address), cycles, opcode); break;
			case 0x7E: cycles = 7; address = abx(IP); ROR(address, getSrc(address), cycles, opcode); break;

			// RTI (return from interrupt)
			case 0x4D: cycles = 6; RTI(); break;

			// RTS (return from subroutine)
			case 0x60: cycles = 6; RTS(); break;

			// SBC (substract from A with carry)
			case 0xE9: cycles = 2; SBC(getSrc(imm(IP))); break;
			case 0xE5: cycles = 3; SBC(getSrc(zpa(IP))); break;
			case 0xF5: cycles = 4; SBC(getSrc(zpx(IP))); break;
			case 0xED: cycles = 4; SBC(getSrc(abs(IP))); break;
			case 0xFD: cycles = 4; SBC(getSrc(abx(IP))); break;
			case 0xF9: cycles = 4; SBC(getSrc(aby(IP))); break;
			case 0xE1: cycles = 6; SBC(getSrc(inx(IP))); break;
			case 0xF1: cycles = 5; SBC(getSrc(iny(IP))); break;

			// SEC (set carry flag)
			case 0x38: cycles = 2; SEC(); break;

			// SED (set decimal mode)
			case 0xF8: cycles = 2; SED(); break;

		    // SEI (set interrupt disable status)
			case 0x78: cycles = 2; SEC(); break;

		    // STA (store A in memory)
			case 0x85: cycles = 3; STA(zpa(IP), cycles); break;
			case 0x95: cycles = 4; STA(zpx(IP), cycles); break;
			case 0x8D: cycles = 4; STA(abs(IP), cycles); break;
			case 0x9D: cycles = 5; STA(abx(IP), cycles); break;
			case 0x99: cycles = 5; STA(aby(IP), cycles); break;
			case 0x81: cycles = 6; STA(inx(IP), cycles); break;
			case 0x91: cycles = 6; STA(iny(IP), cycles); break;

		    // STX (store X in memory)
			case 0x86: cycles = 3; STX(zpa(IP), cycles); break;
			case 0x96: cycles = 4; STX(zpy(IP), cycles); break;
			case 0x8E: cycles = 4; STX(abs(IP), cycles); break;

		    // STY (store Y in memory)
			case 0x84: cycles = 3; STX(zpa(IP), cycles); break;
			case 0x94: cycles = 4; STX(zpx(IP), cycles); break;
			case 0x8C: cycles = 4; STX(abs(IP), cycles); break;
			
			// TAX (transfer A into X)
			case 0xAA: cycles = 2; TAX(); break;

			// TAY (transfer A into Y)
			case 0xA8: cycles = 2; TAY(); break;

			// TSX (transfer SP into X)
			case 0xBA: cycles = 2; TSX(); break;

			// TXA (transfer X into A)
			case 0x8A: cycles = 2; TXA(); break;

			// TXS (transfer X into SP)
			case 0x9A: cycles = 2; TXS(); break;

			// TYA (transfer Y into A)
			case 0x98: cycles = 2; TYA(); break;
		}

		if(add == IP)
			IP += instructionSize(IP);

		return cycles;
	}


	/*
	 *
	 * ADRESSING MODES
	 *
	 */
	private int imm(int pos)
	{
		return pos + 1;
	}

	private int zpa(int pos)
	{
		return JEmu.platform.getRAM(pos + 1);
	}

	private int zpx(int pos)
	{
		return JEmu.platform.getRAM(pos + 1) + X;
	}

	private int zpy(int pos)
	{
		return JEmu.platform.getRAM(pos + 1) + Y;
	}

	private int abs(int pos)
	{
		return ((JEmu.platform.getRAM(pos+1))|((JEmu.platform.getRAM(pos+2))<<8));
	}

	private int abx(int pos)
	{
		return ((JEmu.platform.getRAM(pos+1))|((JEmu.platform.getRAM(pos+2))<<8)) + X;
	}

	private int aby(int pos)
	{
		return ((JEmu.platform.getRAM(pos+1))|((JEmu.platform.getRAM(pos+2))<<8)) + Y;
	}

	private int inx(int pos)
	{
		int lsrc = JEmu.platform.getRAM(pos + 1);
		return ((lsrc+X)|((JEmu.platform.getRAM(lsrc+X+1))<<8));
	}

	private int iny(int pos)
	{
		int lsrc = JEmu.platform.getRAM(pos + 1);
		return ((lsrc)|((lsrc+1)<<8)) + Y;
	}

	private int rel(int pos) // WARNING: returns src, not address
	{
		return (pos + instructionSize(pos) - ((0x100 - JEmu.platform.getRAM(pos + 1)) & 0xff));
	}

	private int ind(int pos)
	{
		int lsrc = JEmu.platform.getRAM(pos + 1);
		int hsrc = JEmu.platform.getRAM(pos + 2);
		int src = ((lsrc)|((hsrc)<<8));
		return (JEmu.platform.getRAM(src) | JEmu.platform.getRAM(src+1) << 8);
	}

	private int getSrc(int address)
	{
		return JEmu.platform.getRAM(address);
	}


	/* 
	 *
	 * INSTRUCTIONS
	 *
	 */
	private void ADC(int src)
	{
		int temp = src + A + ((C) ? 1 : 0);
		// This is not valid in decimal mode
		Z = ((temp & 0xff) == 0);
		if (D) 
		{
			if (((A&0xf)+(src&0xf)+((C) ? 1 : 0))>9)
				temp += 6;
			S = ((temp & 0x80) > 0);
			O = !((((A^src)&0x80)&((A^temp)&0x80)) > 0);
			if (temp > 0x99) 
				temp += 96;
			C = ((temp > 0x99));
		} 
		else 
		{
			S = ((temp & 0x80) > 0);
			O = !((((A^src)&0x80)&((A^temp)&0x80)) > 0);
			C = (temp > 0xff);
		}
		A = temp & 0xFF;
	}

	private void AND(int src)
	{
		A &= src;
		S = ((A & 0x80) > 0);
		Z = (A == 0);
	}

	private void ASL(int address, int src, int cycles, int opcode)
	{
		C = ((src & 0x80) > 0);
		src <<= 1;
		src &= 0xFF;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		if (opcode == 0x0A)
			A = src;
		else
			JEmu.platform.setRAM(address,src,cycles);
	}

	private int BCC(int src)
	{
		if (!C)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private int BCS(int src)
	{
		if (C)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private int BEQ(int src)
	{
		if (Z)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private void BIT(int src)
	{
		S = ((src & 0x80) > 0);
		/* Copy bit 6 to OVERFLOW flag. */
		O = ((0x40 & src) > 0);
		Z = ((src & A) == 0);
	}

	private int BMI(int src)
	{
		if (S)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private int BNE(int src)
	{
		if (!Z)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private int BPL(int src)
	{
		if (!S)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private void BRK(int cycles)
	{
		PUSH((IP >> 8) & 0xFF, cycles);
		PUSH(IP & 0xFF, cycles);
		B = true;
		PUSH(SP, cycles);
		I = true;
		IP = (JEmu.platform.getRAM(0xFFFE)|(JEmu.platform.getRAM(0xFFFF)<<8));
	}

	private int BVC(int src)
	{
		if (!O)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private int BVS(int src)
	{
		if (O)
		{
			IP = src;
			return 1;
		}
		return 0;
	}

	private void CLC()
	{
		C = false;
	}

	private void CLD()
	{
		D = false;
	}

	private void CLI()
	{
		I = false;
	}

	private void CLV()
	{
		O = false;
	}

	private void CMP(int src)
	{
		src = A - src;
		C = (src < 0x100);
		S = ((src & 0x80) > 0);
		Z = ((src &= 0xff) == 0);
	}

	private void CPX(int src)
	{
		src = X - src;
		C = (src < 0x100);
		S = ((src & 0x80) > 0);
		Z = ((src &= 0xff) == 0);
	}

	private void CPY(int src)
	{
		src = Y - src;
		C = (src < 0x100);
		S = ((src & 0x80) > 0);
		Z = ((src &= 0xff) == 0);
	}

	private void DEC(int address, int src, int cycles)
	{
		src = (src - 1) & 0xff;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		JEmu.platform.setRAM(address,src,cycles);
	}

	private void DEX()
	{
		X = (X-1) & 0xFF;
		S = ((X & 0x80) > 0);
		Z = (X == 0);
	}

	private void DEY()
	{
		Y = (Y-1) & 0xFF;
		S = ((Y & 0x80) > 0);
		Z = (Y == 0);
	}

	private void EOR(int src)
	{
		src ^= A;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		A = src;
	}

	private void INC(int address, int src, int cycles)
	{
		src = (src + 1) & 0xff;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		JEmu.platform.setRAM(address, src, cycles);
	}

	private void INX()
	{
		X = (X+1) & 0xFF;
		S = ((X & 0x80) > 0);
		Z = (X == 0);
	}

	private void INY()
	{
		Y = (Y+1) & 0xFF;
		S = ((Y & 0x80) > 0);
		Z = (Y == 0);
	}

	private void JMP(int address)
	{
		IP = address;
	}

	private void JSR(int address, int cycles)	
	{
		IP--;
		/* push return address onto the stack */
		PUSH((IP >> 8) & 0xFF, cycles);
		PUSH(IP & 0xFF, cycles);
		IP = address;
	}

	private void LDA(int src)
	{
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		A = src;
	}

	private void LDX(int src)
	{
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		X = src;
	}

	private void LDY(int src)
	{
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		Y = src;
	}

	private void LSR(int address, int src, int cycles, int opcode)
	{
		C = ((src & 0x01) > 0);
		src >>= 1;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		if(opcode == 0x4A)
			A = src;
		else
			JEmu.platform.setRAM(address, src, cycles);
	}

	// private void NOP() {}
	
	private void ORA(int src)
	{
		src |= A;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		A = src;
	}

	private void PHA(int cycles)
	{
		PUSH(A, cycles);
	}

	private void PHP(int cycles)
	{
		PUSH(SP, cycles);
	}

	private void PLA()
	{
		int src = PULL();
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		A = src;
	}

	private void PLP()
	{
		int src = PULL();
		SP = src;
	}

	private void ROL(int address, int src, int cycles, int opcode)
	{
		src <<= 1;
		if(C)
			src |= 0x1;
		C = (src > 0xff);
		src &= 0xff;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		if(opcode == 0x2A)
			A = src;
		else
			JEmu.platform.setRAM(address, src, cycles);
	}

	private void ROR(int address, int src, int cycles, int opcode)
	{
		if(C)
			src |= 0x100;
		C = ((src & 0x01) > 0);
		src >>= 1;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		if(opcode == 0x6A)
			A = src;
		else
			JEmu.platform.setRAM(address, src, cycles);
	}

	private void RTI()
	{
		int src = PULL();
		SP = src;
		src = PULL();
		src |= (PULL() << 8);
		IP = src;
	}

	private void RTS()
	{
		int src = PULL();
		src += ((PULL()) << 8) + 1;
		IP = src;
	}
	
	private	void SBC(int src)
	{
		int temp = A - src - (C ? 0 : 1);
		S = ((temp & 0x80) > 0);
		Z = ((temp & 0xFF) == 0);
		O = ((((A^temp)&0x80)&((A^src)&0x80)) > 0);
		if(D) 
		{
			if(((A&0xf)-(C?0:1))<(src&0xf))
				temp -= 6;
			if(temp>0x99)
				temp -= 0x60;
		}
		C = (temp < 0x100);
		A = (temp & 0xFF);
	}

	private void SEC()
	{
		C = true;
	}

	private void SED()
	{
		D = true;
	}

	private void SEI()
	{
		I = true;
	}

	private void STA(int address, int cycles)
	{
		JEmu.platform.setRAM(address, A, cycles);
	}
	
	private void STX(int address, int cycles)
	{
		JEmu.platform.setRAM(address, X, cycles);
	}

	private void STY(int address, int cycles)
	{
		JEmu.platform.setRAM(address, Y, cycles);
	}

	private void TAX()
	{
		int src = A;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		X = src;
	}

	private void TAY()
	{
		int src = A;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		Y = src;
	}

	private void TSX()
	{
		int src = SP;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		X = src;
	}

	private void TXA()
	{
		int src = X;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		A = src;
	}

	private void TXS()
	{
		int src = X;
		SP = src;
	}

	private void TYA()
	{
		int src = Y;
		S = ((src & 0x80) > 0);
		Z = (src == 0);
		A = src;
	}


	/*
	 * 
	 * STACK OPERATIONS
	 *
	 */
	private void PUSH(int b, int cycles) 
	{ 
		JEmu.platform.setRAM(SP+0x100, b ,cycles);
		SP--;
	}
	private int PULL()
	{
		return JEmu.platform.getRAM((++SP)+0x100);
	}



	/*
	 *
	 * DEBUGGING
	 *
	 */
	protected int instructionSize(int pos)
	{
			short opcode = JEmu.platform.getRAM(pos);

			/* find out how many bytes this opcode uses */
			switch(opcode)
			{
				/* 1 byte */
				case 0x0A: /* ASL */
				case 0x00: /* BRK */
				case 0x18: /* CLC */
				case 0xD8: /* CLD */
				case 0x58: /* CLI */
				case 0xB8: /* CLV */
				case 0xCA: /* DEX */
				case 0x88: /* DEY */
				case 0xE8: /* INX */
				case 0xC8: /* INY */
				case 0x4A: /* LSR */
				case 0xEA: /* NOP */
				case 0x48: /* PHA */
				case 0x08: /* PHP */
				case 0x68: /* PLA */
				case 0x28: /* PLP */
				case 0x2A: /* ROL */
				case 0x6A: /* ROR */
				case 0x4D: /* RTI */
				case 0x60: /* RTS */
				case 0x38: /* SEC */
				case 0xF8: /* SED */
				case 0x78: /* SEI */
				case 0xAA: /* TAX */
				case 0xA8: /* TAY */
				case 0xBA: /* TSX */
				case 0x8A: /* TXA */
				case 0x9A: /* TXS */
				case 0x98: /* TYA */
					return 1;
					
				/* 2 bytes */
				case 0x61: /* ADC */
				case 0x69:
				case 0x65:
				case 0x71:
				case 0x75:
				case 0x21: /* AND */
				case 0x25:
				case 0x29:
				case 0x31:
				case 0x35:
				case 0x06: /* ASL */
				case 0x16:
				case 0x90: /* BCC */
				case 0xB0: /* BCS */
				case 0xF0: /* BEQ */
				case 0x24: /* BIT */
				case 0x30: /* BMI */
				case 0xD0: /* BNE */
				case 0x10: /* BPL */
				case 0x50: /* BVC */
				case 0x70: /* BVS */
				case 0xC9: /* CMP */
				case 0xC5:
				case 0xD5:
				case 0xC1:
				case 0xD1:
				case 0xE0: /* CPX */
				case 0xE4:
				case 0xC0: /* CPY */
				case 0xC4:
				case 0xC6: /* DEC */
				case 0xD6:
				case 0x49: /* EOR */
				case 0x45:
				case 0x55:
				case 0x41:
				case 0x51:
				case 0xE6: /* INC */
				case 0xF6:
				case 0xA9: /* LDA */
				case 0xA5:
				case 0xB5:
				case 0xA1:
				case 0xB1:
				case 0xA2: /* LDX */
				case 0xA6:
				case 0xB6:
				case 0xA0: /* LDY */
				case 0xA4:
				case 0xB4:
				case 0x46: /* LSR */
				case 0x56:
				case 0x04: /* NOP */
				case 0x09: /* ORA */
				case 0x05:
				case 0x15:
				case 0x01:
				case 0x11:
				case 0x26: /* ROL */
				case 0x36:
				case 0x66: /* ROR */
				case 0x76:
				case 0xE9: /* SBC */
				case 0xE5:
				case 0xF5:
				case 0xE1:
				case 0xF1:
				case 0x85: /* STA */
				case 0x95:
				case 0x81:
				case 0x91:
				case 0x86: /* STX */
				case 0x96:
				case 0x84: /* STY */
				case 0x94:
					return 2;

				/* 3 bytes */
				case 0x6D: /* ADC */
				case 0x79:
				case 0x7D:
				case 0x2D: /* AND */
				case 0x39:
				case 0x3D:
				case 0x0E: /* ASL */
				case 0x1E:
				case 0x2C: /* BIT */
				case 0xCD: /* CMP */
				case 0xDD:
				case 0xD9:
				case 0xEC: /* CPX */
				case 0xCC: /* CPY */
				case 0xCE: /* DEC */
				case 0xDE:
				case 0x40: /* EOR */
				case 0x5D:
				case 0x59:
				case 0xEE: /* INC */
				case 0xFE:
				case 0x4C: /* JMP */
				case 0x6C:
				case 0x20: /* JSR */
				case 0xAD: /* LDA */
				case 0xBD:
				case 0xB9:
				case 0xAE: /* LDX */
				case 0xBE:
				case 0xAC: /* LDY */
				case 0xBC:
				case 0x4E: /* LSR */
				case 0x5E:
				case 0x1D: /* ORA */
				case 0x19:
				case 0x2E: /* ROL */
				case 0x3E:
				case 0x6E: /* ROR */
				case 0x7E:
				case 0xED: /* SBC */
				case 0xFD:
				case 0x8D: /* STA */
				case 0x9D:
				case 0x99:
				case 0x8E: /* STX */
				case 0x8C: /* STY */
					return 3;
	
				/* Invalid operation */
				default:
					return 1;
			}
	}

	protected String debugInstruction(int pos)
	{
		int instSize = instructionSize(pos);
		short lsrc = 0, hsrc = 0;
		short opcode = JEmu.platform.getRAM(pos);
		int src = 0;

		if(instSize > 1)
				lsrc = JEmu.platform.getRAM(pos + 1);
		if(instSize > 2)
				hsrc = JEmu.platform.getRAM(pos + 2);

		String debug_par = "", debug_opcode = "";

		switch (opcode)
		{
			/* Implied: the opcode uses no parameter */
			case 0x00: /* BRK */
			case 0x4D: /* RTI */
			case 0x60: /* RTS */
			case 0x68: /* PLA */
			case 0x28: /* PLP */
			case 0x48: /* PHA */
			case 0x08: /* PHP */
			case 0x18: /* CLC */
			case 0xD8: /* CLD */
			case 0x58: /* CLI */
			case 0xB8: /* CLV */
			case 0xCA: /* DEX */
			case 0x88: /* DEY */
			case 0xE8: /* INX */
			case 0xC8: /* INY */
			case 0xEA: /* NOP */
			case 0x38: /* SEC */
			case 0xF8: /* SED */
			case 0x78: /* SEI */
			case 0xAA: /* TAX */
			case 0xA8: /* TAY */
			case 0xBA: /* TSX */
			case 0x8A: /* TXA */
			case 0x9A: /* TXS */
			case 0x98: /* TYA */
				debug_par = " ";
				break;

			/* Accumulator: it'll use the accumulator (A)
			 * as parameter. */
			case 0x0A: /* ASL */
			case 0x4A: /* LSR */
			case 0x2A: /* ROL */
			case 0x6A: /* ROR */
				debug_par = "A";
				break;

			/* Immediate: it'll pass an absoulte number
			 * as a parameter. */
			case 0x04: /* NOP - undocumented! */
			case 0x69: /* ADC */
			case 0x29: /* AND */
			case 0xC9: /* CMP */
			case 0xE0: /* CPX */
			case 0xC0: /* CPY */
			case 0x49: /* EOR */
			case 0xA9: /* LDA */
			case 0xA2: /* LDX */
			case 0xA0: /* LDY */
			case 0x09: /* ORA */
			case 0xE9: /* SBC */
				debug_par = "#$" + Integer.toHexString(lsrc);
				break;

			/* Zero page: it'll use only one byte to access the
			 * addresses $0000-00FF from the memory. */
			case 0x06: /* ASL */
			case 0xC6: /* DEC */
			case 0xE6: /* INC */
			case 0x46: /* LSR */
			case 0x26: /* ROL */
			case 0x66: /* ROR */
			case 0x65: /* ADC */
			case 0x25: /* AND */
			case 0x24: /* BIT */
			case 0xC5: /* CMP */
			case 0xE4: /* CPX */
			case 0xC4: /* CPY */
			case 0x45: /* EOR */
			case 0xA5: /* LDA */
			case 0xA6: /* LDX */
			case 0xA4: /* LDY */
			case 0x05: /* ORA */
			case 0xE5: /* SBC */
			case 0x85: /* STA */
			case 0x86: /* STX */
			case 0x84: /* STY */
				debug_par = "$" + Integer.toHexString(lsrc);
				break;

			/* Zero page, X: loads from the (zero page + X) */
			case 0x16: /* ASL */
			case 0xD6: /* DEC */
			case 0xF6: /* INC */
			case 0x56: /* LSR */
			case 0x36: /* ROL */
			case 0x76: /* ROR */
			case 0x75: /* ADC */
			case 0x35: /* AND */
			case 0xD5: /* CMP */
			case 0x55: /* EOR */
			case 0xB5: /* LDA */
			case 0xB4: /* LDY */
			case 0x15: /* ORA */
			case 0xF5: /* SBC */
			case 0x95: /* STA */
			case 0x96: /* STX */
			case 0x94: /* STY */
				debug_par = "$" + Integer.toHexString(lsrc) + ",X";
				break;

			/* Zero page, Y: loads from the (zero page + Y) */
			case 0xB6: /* LDX */
				debug_par = "$" + Integer.toHexString(lsrc) + ",Y";
				break;

			/* Absolute: it'll pass an absolute 16-bit memory
			 * address. */
			case 0x0E: /* ASL */
			case 0xCE: /* DEC */
			case 0xEE: /* INC */
			case 0x20: /* JSR */
			case 0x4E: /* LSR */
			case 0x2E: /* ROL */
			case 0x6E: /* ROR */
			case 0x6D: /* ADC */
			case 0x2D: /* AND */
			case 0x2C: /* BIT */
			case 0xCD: /* CMP */
			case 0xEC: /* CPX */
			case 0xCC: /* CPY */
			case 0x40: /* EOR */
			case 0xAD: /* LDA */
			case 0xAE: /* LDX */
			case 0xAC: /* LDY */
			case 0x0D: /* ORA */
			case 0xED: /* SBC */
			case 0x8D: /* STA */
			case 0x8E: /* STX */
			case 0x8C: /* STY */
			case 0x4C: /* JMP */
				debug_par = "$" + Integer.toHexString(lsrc | (hsrc << 8));
				break;

			/* Absoulte, X: get the byte in the (absolute + X)
			 * position. */
			case 0x1E: /* ASL */
			case 0xDE: /* DEC */
			case 0xFE: /* INC */
			case 0x5E: /* LSR */
			case 0x3E: /* ROL */
			case 0x7E: /* ROR */
			case 0x9D: /* STA */
			case 0x7D: /* ADC */
			case 0x3D: /* AND */
			case 0xDD: /* CMP */
			case 0x5D: /* EOR */
			case 0xBD: /* LDA */
			case 0xBC: /* LDY */
			case 0x1D: /* ORA */
			case 0xFD: /* SBC */
				debug_par = "$" + Integer.toHexString(lsrc | (hsrc << 8)) + ",X";
				break;
				
			/* Absoulte, Y: get the byte in the (absolute + Y)
			 * position. */
			case 0x99: /* STA */
			case 0x79: /* ADC */
			case 0x39: /* AND */
			case 0xD9: /* CMP */
			case 0x59: /* EOR */
			case 0xB9: /* LDA */
			case 0xBE: /* LDY */
			case 0x19: /* ORA */
			case 0xF9: /* SBC */
				debug_par = "$" + Integer.toHexString(lsrc | (hsrc << 8)) + ",Y";
				break;

			/* (Indirect, X): get the byte into the (zero page + X)
			 * and the following one and makes an adress Z. Takes 
			 * the byte from Z. */
			case 0x61: /* ADC */
			case 0x21: /* AND */
			case 0xC1: /* CMP */
			case 0x41: /* EOR */
			case 0xA1: /* LDA */
			case 0x01: /* ORA */
			case 0xE1: /* SBC */
			case 0x81: /* STA */
				debug_par = "($" + Integer.toHexString(lsrc) + ",X)";
				break;

			/* (Indirect), Y: get the byte into the zero page and
			 * the following one and makes an adress Z. Takes the
			 * byte from (Z + Y). */
			case 0x91: /* STA */
			case 0x71: /* ADC */
			case 0x31: /* AND */
			case 0xD1: /* CMP */
			case 0x51: /* EOR */
			case 0xB1: /* LDA */
			case 0x11: /* ORA */
			case 0xF1: /* SBC */
				debug_par = "($" + Integer.toHexString(lsrc) + "),Y";
				break;

			/* Relative: used in branches. Adds to IP if the
			 * highest bit of the parameter is 0, and substracts
			 * from IP if it's 1. */
			case 0x90: /* BCC */
			case 0xB0: /* BCS */
			case 0xF0: /* BEQ */
			case 0x30: /* BMI */
			case 0xD0: /* BNE */
			case 0x10: /* BPL */
			case 0x50: /* BVC */
			case 0x70: /* BVS */
				debug_par = "$" + Integer.toHexString(pos + instSize - (0x100 - lsrc));
				break;

			/* Indirect: it takes the byte that's into the given
			 * position of the memory and the next byte. */
			case 0x6C: /* JMP */
				debug_par = "($" + Integer.toHexString(src) + ")";
				break;
		}

		/* Instruction */
		switch (opcode)
		{

			case 0x6D:
			case 0x61:
			case 0x65:
			case 0x69:
			case 0x71:
			case 0x75:
			case 0x79:
			case 0x7D:
				debug_opcode = "ADC";
				break;
			case 0x21:
			case 0x25:
			case 0x29:
			case 0x2D:
			case 0x31:
			case 0x35:
			case 0x39:
			case 0x3D:
				debug_opcode = "AND";
				break;
			case 0x0A:
			case 0x06:
			case 0x16:
			case 0x0E:
			case 0x1E:
				debug_opcode = "ASL";
				break;
			case 0x90:
				debug_opcode = "BCC";
				break;
			case 0xB0:
				debug_opcode = "BCS";
				break;
			case 0xF0:
				debug_opcode = "BEQ";
				break;
			case 0x24:
			case 0x2C:
				debug_opcode = "BIT";
				break;
			case 0x30:
				debug_opcode = "BMI";
				break;
			case 0xD0:
				debug_opcode = "BNE";
				break;
			case 0x10:
				debug_opcode = "BPL";
				break;
			case 0x00:
				debug_opcode = "BRK";
				break;
			case 0x50:
				debug_opcode = "BVC";
				break;
			case 0x70:
				debug_opcode = "BVS";
				break;
			case 0x18:
				debug_opcode = "CLC";
				break;
			case 0xD8:
				debug_opcode = "CLD";
				break;
			case 0x58:
				debug_opcode = "CLI";
				break;
			case 0xB8:
				debug_opcode = "CLV";
				break;
			case 0xC9:
			case 0xC5:
			case 0xD5:
			case 0xCD:
			case 0xDD:
			case 0xD9:
			case 0xC1:
			case 0xD1:
				debug_opcode = "CMP";
				break;
			case 0xE0:
			case 0xE4:
			case 0xEC:
				debug_opcode = "CPX";
				break;
			case 0xC0:
			case 0xC4:
			case 0xCC:
				debug_opcode = "CPY";
				break;
			case 0xC6:
			case 0xD6:
			case 0xCE:
			case 0xDE:
				debug_opcode = "DEC";
				break;
			case 0xCA:
				debug_opcode = "DEX";
				break;
			case 0x88:
				debug_opcode = "DEY";
				break;
			case 0x49:
			case 0x45:
			case 0x55:
			case 0x40:
			case 0x5D:
			case 0x59:
			case 0x41:
			case 0x51:
				debug_opcode = "EOR";
				break;
			case 0xE6:
			case 0xF6:
			case 0xEE:
			case 0xFE:
				debug_opcode = "INC";
				break;
			case 0xE8:
				debug_opcode = "INX";
				break;
			case 0xC8:
				debug_opcode = "INY";
				break;
			case 0x4C:
			case 0x6C:
				debug_opcode = "JMP";
				break;
			case 0x20:
				debug_opcode = "JSR";
				break;
			case 0xA9:
			case 0xA5:
			case 0xB5:
			case 0xAD:
			case 0xBD:
			case 0xB9:
			case 0xA1:
			case 0xB1:
				debug_opcode = "LDA";
				break;
			case 0xA2:
			case 0xA6:
			case 0xB6:
			case 0xAE:
			case 0xBE:
				debug_opcode = "LDX";
				break;
			case 0xA0:
			case 0xA4:
			case 0xB4:
			case 0xAC:
			case 0xBC:
				debug_opcode = "LDY";
				break;
			case 0x4A:
			case 0x46:
			case 0x56:
			case 0x4E:
			case 0x5E:
				debug_opcode = "LSR";
				break;
			case 0x04:
			case 0xEA:
				debug_opcode = "NOP";
				break;
			case 0x09:
			case 0x05:
			case 0x15:
			case 0x0D:
			case 0x1D:
			case 0x19:
			case 0x01:
			case 0x11:
				debug_opcode = "ORA";
				break;
			case 0x48:
				debug_opcode = "PHA";
				break;
			case 0x08:
				debug_opcode = "PHP";
				break;
			case 0x68:
				debug_opcode = "PLA";
				break;
			case 0x28:
				debug_opcode = "PLP";
				break;
			case 0x2A:
			case 0x26:
			case 0x36:
			case 0x2E:
			case 0x3E:
				debug_opcode = "ROL";
				break;
			case 0x6A:
			case 0x66:
			case 0x76:
			case 0x6E:
			case 0x7E:
				debug_opcode = "ROR";
				break;
			case 0x4D:
				debug_opcode = "RTI";
				break;
			case 0x60:
				debug_opcode = "RTS";
				break;
			case 0xE9:
			case 0xE5:
			case 0xF5:
			case 0xED:
			case 0xFD:
			case 0xF9:
			case 0xE1:
			case 0xF1:
				debug_opcode = "SBC";
				break;
			case 0x38:
				debug_opcode = "SEC";
				break;
			case 0xF8:
				debug_opcode = "SED";
				break;
			case 0x78:
				debug_opcode = "SEI";
				break;
			case 0x85:
			case 0x95:
			case 0x8D:
			case 0x9D:
			case 0x99:
			case 0x81:
			case 0x91:
				debug_opcode = "STA";
				break;
			case 0x86:
			case 0x96:
			case 0x8E:
				debug_opcode = "STX";
				break;
			case 0x84:
			case 0x94:
			case 0x8C:
				debug_opcode = "STY";
				break;
			case 0xAA:
				debug_opcode = "TAX";
				break;
			case 0xA8:
				debug_opcode = "TAY";
				break;
			case 0xBA:
				debug_opcode = "TSX";
				break;
			case 0x8A:
				debug_opcode = "TXA";
				break;
			case 0x9A:
				debug_opcode = "TXS";
				break;
			case 0x98:
				debug_opcode = "TYA";
				break;
		}
		return debug_opcode + " " + debug_par.toUpperCase();// + "(" + Integer.toHexString(addressing(pos, instSize)) + ")";
	}

	protected String debugNumCycles(int pos)
	{
		int opcode = JEmu.platform.getRAM(pos);

		switch(opcode)
		{
			// ADC (add with carry)
			case 0x69: return "2";
			case 0x65: return "3";
			case 0x75: return "4";
			case 0x6D: return "4";
			case 0x7D: return "4";
			case 0x79: return "4";
			case 0x61: return "6";
			case 0x71: return "5";

			// AND (and with memory and A)
			case 0x29: return "2";
			case 0x25: return "3";
			case 0x35: return "4";
			case 0x2D: return "4";
			case 0x3D: return "4";
			case 0x39: return "4";
			case 0x21: return "6";
			case 0x31: return "5";

			// ASL (A shift left)
			case 0x0A: return "2";
			case 0x06: return "5";
			case 0x16: return "6";
			case 0x0E: return "6";
			case 0x1E: return "7";

			// BCC (branch on C)
			case 0x90: return "2 (+1)";

			// BCS (branch on carry set)
			case 0xB0: return "2 (+1)";

			// BEQ (branch on result zero)
			case 0xF0: return "2 (+1)";

			// BIT (test bits in memory with A)
			case 0x24: return "3";
			case 0x2C: return "4";

			// BMI (branch on result minus)
			case 0x30: return "2 (+1)";

			// BNE (branch on result not zero)
			case 0xD0: return "2 (+1)";

			// BPL (branch on result plus)
			case 0x10: return "2 (+1)";

			// BRK (force break)
			case 0x00: return "7";

			// BVC (branch on overflow clear)
			case 0x50: return "2 (+1)";

			// BVS (branch on overflow set)
			case 0x70: return "2 (+1)";

			// CLC (clear carry flag)
			case 0x18: return "2";

			// CLD (clear decimal mode)
			case 0xD8: return "2";

			// CLI (clear interrupt disable bit)
			case 0x58: return "2";

			// CLV (clear overflow flag)
			case 0xB8: return "2";

			// CMP (compare memory and A)
			case 0xC9: return "2";
			case 0xC5: return "2";
			case 0xD5: return "4";
			case 0xCD: return "4";
			case 0xDD: return "4";
			case 0xD9: return "4";
			case 0xC1: return "6";
			case 0xD1: return "5";

			// CPX (compare memory and X)
			case 0xE0: return "2";
			case 0xE4: return "3";
			case 0xEC: return "4";

			// CPY (compare memory and Y)
			case 0xC0: return "2";
			case 0xC4: return "3";
			case 0xCC: return "4";

			// DEC (decrement memory)
			case 0xC6: return "5";
			case 0xD6: return "6";
			case 0xCE: return "6";
			case 0xDE: return "7";

			// DEX (decrement X)
			case 0xCA: return "2";

			// DEY (decrement Y)
			case 0x88: return "2";
			
			// EOR (exclusive-or memory with A)
			case 0x49: return "2";
			case 0x45: return "3";
			case 0x55: return "4";
			case 0x40: return "4";
			case 0x5D: return "4";
			case 0x59: return "4";
			case 0x41: return "6";
			case 0x51: return "5";

			// INC (increment memory)
			case 0xE6: return "5";
			case 0xF6: return "6";
			case 0xEE: return "6";
			case 0xFE: return "7";

			// INX (increment X)
			case 0xE8: return "2";

			// INY (increment Y)
			case 0xC8: return "2";

			// JMP (jump)
			case 0x4C: return "3";
			case 0x6C: return "5";

			// JSR (jump to subroutine)
			case 0x20: return "6";

			// LDA (load A with memory)
			case 0xA9: return "2";
			case 0xA5: return "3";
			case 0xB5: return "4";
			case 0xAD: return "4";
			case 0xBD: return "4";
			case 0xB9: return "4";
			case 0xA1: return "6";
			case 0xB1: return "5";

			// LDX (load X with memory)
			case 0xA2: return "2";
			case 0xA6: return "3";
			case 0xB6: return "4";
			case 0xAE: return "4";
			case 0xBE: return "4";

			// LDY (load Y with memory)
			case 0xA0: return "2";
			case 0xA4: return "3";
			case 0xB4: return "4";
			case 0xAC: return "4";
			case 0xBC: return "4";

			// LSR (shift right)
			case 0x4A: return "2";
			case 0x46: return "5";
			case 0x56: return "6";
			case 0x4E: return "6";
			case 0x5E: return "7";

			// NOP (no operation)
			case 0xEA: return "2";

			// ORA (OR memory with A)
			case 0x09: return "2";
			case 0x05: return "3";
			case 0x15: return "4";
			case 0x0D: return "4";
			case 0x1D: return "4";
			case 0x19: return "4";
			case 0x01: return "6";
			case 0x11: return "5";

			// PHA (push A on stack)
			case 0x48: return "3";

			// PHA (push SP on stack)
			case 0x08: return "3";

			// PLA (push A from stack)
			case 0x68: return "4";

			// PLA (push A from stack)
			case 0x28: return "4";

			// ROL (rotate one bit left)
			case 0x2A: return "2";
			case 0x26: return "5";
			case 0x36: return "6";
			case 0x2E: return "6";
			case 0x3E: return "7";

			// ROR (rotate one bit right)
			case 0x6A: return "2";
			case 0x66: return "5";
			case 0x76: return "6";
			case 0x6E: return "6";
			case 0x7E: return "7";

			// RTI (return from interrupt)
			case 0x4D: return "6";

			// RTS (return from subroutine)
			case 0x60: return "6";

			// SBC (substract from A with carry)
			case 0xE9: return "2";
			case 0xE5: return "3";
			case 0xF5: return "4";
			case 0xED: return "4";
			case 0xFD: return "4";
			case 0xF9: return "4";
			case 0xE1: return "6";
			case 0xF1: return "5";

			// SEC (set carry flag)
			case 0x38: return "2";

			// SED (set decimal mode)
			case 0xF8: return "2";

		    // SEI (set interrupt disable status)
			case 0x78: return "2";

		    // STA (store A in memory)
			case 0x85: return "3";
			case 0x95: return "4";
			case 0x8D: return "4";
			case 0x9D: return "5";
			case 0x99: return "5";
			case 0x81: return "6";
			case 0x91: return "6";

		    // STX (store X in memory)
			case 0x86: return "3";
			case 0x96: return "4";
			case 0x8E: return "4";

		    // STY (store Y in memory)
			case 0x84: return "3";
			case 0x94: return "4";
			case 0x8C: return "4";
			
			// TAX (transfer A into X)
			case 0xAA: return "2";

			// TAY (transfer A into Y)
			case 0xA8: return "2";

			// TSX (transfer SP into X)
			case 0xBA: return "2";

			// TXA (transfer X into A)
			case 0x8A: return "2";

			// TXS (transfer X into SP)
			case 0x9A: return "2";

			// TYA (transfer Y into A)
			case 0x98: return "2";
		}
		return "?";
	}

	protected String registerName(int n)
	{
		switch(n)
		{
			case 0: return "A";
			case 1: return "X";
			case 2: return "Y";
		}
		return "";
	}

	protected int registerValue(int n)
	{
		switch(n)
		{
			case 0: return A;
			case 1: return X;
			case 2: return Y;
		}
		return 0;	
	}

	protected String flagName(int n)
	{
		switch(n)
		{
			case 0: return "S";
			case 1: return "O";
			case 2: return "B";
			case 3: return "D";
			case 4: return "I";
			case 5: return "Z";
			case 6: return "C";
		}
		return "";
	}

	protected boolean flagValue(int n)
	{
		switch(n)
		{
			case 0: return S;
			case 1: return O;
			case 2: return B;
			case 3: return D;
			case 4: return I;
			case 5: return Z;
			case 6: return C;
		}
		return false;
	}

	/*
	 * PRIVATE
	 */
	private int addressing(int pos, int instSize)
	{
		short lsrc = 0, hsrc = 0;
		short opcode = JEmu.platform.getRAM(pos);
		int src = 0, address;

		if(instSize > 1)
				lsrc = JEmu.platform.getRAM(pos + 1);
		if(instSize > 2)
				hsrc = JEmu.platform.getRAM(pos + 2);

		switch (opcode)
		{
			/* Implied: the opcode uses no parameter */
			case 0x00: /* BRK */
			case 0x4D: /* RTI */
			case 0x60: /* RTS */
			case 0x68: /* PLA */
			case 0x28: /* PLP */
			case 0x48: /* PHA */
			case 0x08: /* PHP */
			case 0x18: /* CLC */
			case 0xD8: /* CLD */
			case 0x58: /* CLI */
			case 0xB8: /* CLV */
			case 0xCA: /* DEX */
			case 0x88: /* DEY */
			case 0xE8: /* INX */
			case 0xC8: /* INY */
			case 0xEA: /* NOP */
			case 0x38: /* SEC */
			case 0xF8: /* SED */
			case 0x78: /* SEI */
			case 0xAA: /* TAX */
			case 0xA8: /* TAY */
			case 0xBA: /* TSX */
			case 0x8A: /* TXA */
			case 0x9A: /* TXS */
			case 0x98: /* TYA */
				break;

			/* Accumulator: it'll use the accumulator (A)
			 * as parameter. */
			case 0x0A: /* ASL */
			case 0x4A: /* LSR */
			case 0x2A: /* ROL */
			case 0x6A: /* ROR */
				src = A;
				break;

			/* Immediate: it'll pass an absoulte number
			 * as a parameter. */
			case 0x04: /* NOP */
			case 0x69: /* ADC */
			case 0x29: /* AND */
			case 0xC9: /* CMP */
			case 0xE0: /* CPX */
			case 0xC0: /* CPY */
			case 0x49: /* EOR */
			case 0xA9: /* LDA */
			case 0xA2: /* LDX */
			case 0xA0: /* LDY */
			case 0x09: /* ORA */
			case 0xE9: /* SBC */
				src = lsrc;
				break;

			/* Zero page: it'll use only one byte to access the
			 * addresses $0000-00FF from the memory. */
			case 0x06: /* ASL */
			case 0xC6: /* DEC */
			case 0xE6: /* INC */
			case 0x46: /* LSR */
			case 0x26: /* ROL */
			case 0x66: /* ROR */
			case 0x65: /* ADC */
			case 0x25: /* AND */
			case 0x24: /* BIT */
			case 0xC5: /* CMP */
			case 0xE4: /* CPX */
			case 0xC4: /* CPY */
			case 0x45: /* EOR */
			case 0xA5: /* LDA */
			case 0xA6: /* LDX */
			case 0xA4: /* LDY */
			case 0x05: /* ORA */
			case 0xE5: /* SBC */
			case 0x85: /* STA */
			case 0x86: /* STX */
			case 0x84: /* STY */
				address = lsrc;
				src = JEmu.platform.getRAM(address);
				break;

			/* Zero page, X: loads from the (zero page + X) */
			case 0x16: /* ASL */
			case 0xD6: /* DEC */
			case 0xF6: /* INC */
			case 0x56: /* LSR */
			case 0x36: /* ROL */
			case 0x76: /* ROR */
			case 0x75: /* ADC */
			case 0x35: /* AND */
			case 0xD5: /* CMP */
			case 0x55: /* EOR */
			case 0xB5: /* LDA */
			case 0xB4: /* LDY */
			case 0x15: /* ORA */
			case 0xF5: /* SBC */
			case 0x95: /* STA */
			case 0x96: /* STX */
			case 0x94: /* STY */
				address = lsrc + X;
				src = JEmu.platform.getRAM(address);
				break;

			/* Zero page, Y: loads from the (zero page + Y) */
			case 0xB6: /* LDX */
				address = lsrc + Y;
				src = JEmu.platform.getRAM(address);
				break;

			/* Absolute: it'll pass an absolute 16-bit memory
			 * address. */
			case 0x0E: /* ASL */
			case 0xCE: /* DEC */
			case 0xEE: /* INC */
			case 0x20: /* JSR */
			case 0x4E: /* LSR */
			case 0x2E: /* ROL */
			case 0x6E: /* ROR */
			case 0x6D: /* ADC */
			case 0x2D: /* AND */
			case 0x2C: /* BIT */
			case 0xCD: /* CMP */
			case 0xEC: /* CPX */
			case 0xCC: /* CPY */
			case 0x40: /* EOR */
			case 0xAD: /* LDA */
			case 0xAE: /* LDX */
			case 0xAC: /* LDY */
			case 0x0D: /* ORA */
			case 0xED: /* SBC */
			case 0x8D: /* STA */
			case 0x8E: /* STX */
			case 0x8C: /* STY */
			case 0x4C: /* JMP */
				address = ((lsrc)|((hsrc)<<8));
				src = JEmu.platform.getRAM(address);
				break;

			/* Absoulte, X: get the byte in the (absolute + X)
			 * position. */
			case 0x1E: /* ASL */
			case 0xDE: /* DEC */
			case 0xFE: /* INC */
			case 0x5E: /* LSR */
			case 0x3E: /* ROL */
			case 0x7E: /* ROR */
			case 0x9D: /* STA */
			case 0x7D: /* ADC */
			case 0x3D: /* AND */
			case 0xDD: /* CMP */
			case 0x5D: /* EOR */
			case 0xBD: /* LDA */
			case 0xBC: /* LDY */
			case 0x1D: /* ORA */
			case 0xFD: /* SBC */
				address = ((lsrc)|((hsrc)<<8)) + X;
				src = JEmu.platform.getRAM(address);
				break;
				
			/* Absolute, Y: get the byte in the (absolute + Y)
			 * position. */
			case 0x99: /* STA */
			case 0x79: /* ADC */
			case 0x39: /* AND */
			case 0xD9: /* CMP */
			case 0x59: /* EOR */
			case 0xB9: /* LDA */
			case 0xBE: /* LDY */
			case 0x19: /* ORA */
			case 0xF9: /* SBC */
				address = ((lsrc)|((hsrc)<<8)) + Y;
				src = JEmu.platform.getRAM(address);
				break;

			/* (Indirect, X): get the byte into the (zero page + X)
			 * and the following one and makes an adress Z. Takes 
			 * the byte from Z. */
			case 0x61: /* ADC */
			case 0x21: /* AND */
			case 0xC1: /* CMP */
			case 0x41: /* EOR */
			case 0xA1: /* LDA */
			case 0x01: /* ORA */
			case 0xE1: /* SBC */
			case 0x81: /* STA */
				address = ((lsrc+X)|((JEmu.platform.getRAM(lsrc+X+1))<<8));
				src = JEmu.platform.getRAM(address);
				break;

			/* (Indirect), Y: get the byte into the zero page and
			 * the following one and makes an adress Z. Takes the
			 * byte from (Z + Y). */
			case 0x91: /* STA */
			case 0x71: /* ADC */
			case 0x31: /* AND */
			case 0xD1: /* CMP */
			case 0x51: /* EOR */
			case 0xB1: /* LDA */
			case 0x11: /* ORA */
			case 0xF1: /* SBC */
				address = ((lsrc)|((lsrc+1)<<8)) + Y;
				src = JEmu.platform.getRAM(address);
				break;

			/* Relative: used in branches. Adds to IP if the
			 * highest bit of the parameter is 0, and substracts
			 * from IP if it's 1. */
			case 0x90: /* BCC */
			case 0xB0: /* BCS */
			case 0xF0: /* BEQ */
			case 0x30: /* BMI */
			case 0xD0: /* BNE */
			case 0x10: /* BPL */
			case 0x50: /* BVC */
			case 0x70: /* BVS */
				src = (pos + instSize - ((0x100 - lsrc) & 0xff));
				break;

			/* Indirect: it takes the byte that's into the given
			 * position of the memory and the next byte. */
			case 0x6C: /* JMP */
				address = ((lsrc)|((hsrc)<<8));
				src = (JEmu.platform.getRAM(address) | JEmu.platform.getRAM(address+1) << 8);
				break;
		}

		return src;
	}

}
