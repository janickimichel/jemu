class m6502 extends CPU
{
	private int PC;

	// registers
	private int A, X, Y;

	// flags
	private boolean S, O, B, D, I, Z, C;

	// stack pointer
	private int SP;

	public void reset()
	{
		PC = 0xf000;
	}

	// 
	// execute one step
	//
	public int step()
	{
		int cycles = 0, bytes_left = 0, bytes_inst = 0;
		int opcode = 0;
		int lsrc = 0, hsrc = 0;
		int src = 0, address = 0;
		int temp;

		lastInstructionPointer = PC;

		do
		{
			switch(bytes_left)
			{
				case 0: 
					opcode = memory.get(PC);
		
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
							bytes_inst = 1;
							bytes_left = 1;
							break;
							
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
							bytes_inst = 2;
							bytes_left = 2;
							break;

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
							bytes_inst = 3;
							bytes_left = 3;
							break;
			
						/* Invalid operation */
						default:
							return 0;
					}
					lsrc = hsrc = 0; /* clear src variables */
					src = 0; 
					break;
				case 1:
					if(bytes_inst == 2)
						lsrc = memory.get(PC);
					else /* if == 3 */
						hsrc = memory.get(PC);
					break;
				case 2:
					lsrc = memory.get(PC);
					break;
			}
			
			/* advance position in the memory */
			PC++;
			
			bytes_left--;


		} while(bytes_left > 0);

		/* Adressing modes
		 *
		 * This will get the source to be used as a parameter to
		 * the instruction. The 6502 has quite a few adressing modes,
		 * so this switch will (hopefully) cover all of them */
		switch (opcode)
		{
			/* Implied: the opcode uses no parameter */
			case 0x00: /* BRK */
				cycles = 1; /* 7 cycles */
			case 0x4D: /* RTI */
			case 0x60: /* RTS */
				cycles += 2; /* 6 cycles */
			case 0x68: /* PLA */
			case 0x28: /* PLP */
				cycles += 1; /* 4 cycles */
			case 0x48: /* PHA */
			case 0x08: /* PHP */
				cycles += 1; /* 3 cycles */
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
				cycles += 2; /* 2 cycles */
				break;

			/* Accumulator: it'll use the accumulator (A)
			 * as parameter. */
			case 0x0A: /* ASL */
			case 0x4A: /* LSR */
			case 0x2A: /* ROL */
			case 0x6A: /* ROR */
				cycles = 2;
				src = A;
				break;

			/* Immediate: it'll pass an absoulte number
			 * as a parameter. */
			case 0x04: /* NOP */
				cycles = 1;
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
				cycles += 2;
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
				cycles = 2; /* 5 cycles */
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
				cycles += 3; /* 3 cycles */
				address = lsrc;
				src = memory.get(address);
				break;

			/* Zero page, X: loads from the (zero page + X) */
			case 0x16: /* ASL */
			case 0xD6: /* DEC */
			case 0xF6: /* INC */
			case 0x56: /* LSR */
			case 0x36: /* ROL */
			case 0x76: /* ROR */
				cycles = 2; /* 6 cycles */
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
				cycles += 4; /* 4 cycles */
				address = lsrc + X;
				src = memory.get(address);
				break;

			/* Zero page, Y: loads from the (zero page + Y) */
			case 0xB6: /* LDX */
				cycles = 4;
				address = lsrc + Y;
				src = memory.get(address);
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
				cycles = 2; /* 6 cycles */
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
				cycles += 1; /* 4 cycles */
			case 0x4C: /* JMP */
				cycles += 3; /* 3 cycles */
				address = ((lsrc)|((hsrc)<<8));
				src = memory.get(address);
				break;

			/* Absoulte, X: get the byte in the (absolute + X)
			 * position. */
			case 0x1E: /* ASL */
			case 0xDE: /* DEC */
			case 0xFE: /* INC */
			case 0x5E: /* LSR */
			case 0x3E: /* ROL */
			case 0x7E: /* ROR */
				cycles = 2; /* 7 cycles */
			case 0x9D: /* STA */
				cycles++; /* 5 cycles */
			case 0x7D: /* ADC */
			case 0x3D: /* AND */
			case 0xDD: /* CMP */
			case 0x5D: /* EOR */
			case 0xBD: /* LDA */
			case 0xBC: /* LDY */
			case 0x1D: /* ORA */
			case 0xFD: /* SBC */
				cycles += 4; /* 4 cycles */
				address = ((lsrc)|((hsrc)<<8)) + X;
				src = memory.get(address);
				break;
				
			/* Absolute, Y: get the byte in the (absolute + Y)
			 * position. */
			case 0x99: /* STA */
				cycles = 1; /* 5 cycles */
			case 0x79: /* ADC */
			case 0x39: /* AND */
			case 0xD9: /* CMP */
			case 0x59: /* EOR */
			case 0xB9: /* LDA */
			case 0xBE: /* LDY */
			case 0x19: /* ORA */
			case 0xF9: /* SBC */
				cycles += 4; /* 4 cycles */
				address = ((lsrc)|((hsrc)<<8)) + Y;
				src = memory.get(address);
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
				cycles = 6;
				address = ((lsrc+X)|((memory.get(lsrc+X+1))<<8));
				// address = FULL(dev_mem_get(lsrc+X), dev_mem_get(lsrc+X+1));
				src = memory.get(address);
				break;

			/* (Indirect), Y: get the byte into the zero page and
			 * the following one and makes an adress Z. Takes the
			 * byte from (Z + Y). */
			case 0x91: /* STA */
				cycles = 1; /* 6 cycles */
			case 0x71: /* ADC */
			case 0x31: /* AND */
			case 0xD1: /* CMP */
			case 0x51: /* EOR */
			case 0xB1: /* LDA */
			case 0x11: /* ORA */
			case 0xF1: /* SBC */
				cycles += 5; /* 5 cycles */
				address = ((lsrc)|((lsrc+1)<<8)) + Y;
				//address = FULL(dev_mem_get(lsrc),
				//			dev_mem_get(lsrc+1))+Y;
				src = memory.get(address);
				break;

			/* Relative: used in branches. Adds to PC if the
			 * highest bit of the parameter is 0, and substracts
			 * from PC if it's 1. */
			case 0x90: /* BCC */
			case 0xB0: /* BCS */
			case 0xF0: /* BEQ */
			case 0x30: /* BMI */
			case 0xD0: /* BNE */
			case 0x10: /* BPL */
			case 0x50: /* BVC */
			case 0x70: /* BVS */
				cycles = 2;
				src = (PC + bytes_inst - (0x100 - lsrc));
				// src = REL_ADDR(PC, lsrc);
				break;

			/* Indirect: it takes the byte that's into the given
			 * position of the memory and the next byte. */
			case 0x6C: /* JMP */
				cycles = 5;
				// src = FULL(lsrc,hsrc);
				src = ((lsrc)|((hsrc)<<8));
				// address = FULL(
				// 		dev_mem_get(address),
				//		dev_mem_get(address+1)
				//		);
				address = (memory.get(address) | memory.get(address+1) << 8);
				break;
		}

		/***************************
		 * Execute the instruction *
		 ***************************/
		switch (opcode)
		{

			/* *****
			 * ADC *  ->  add memory to accumulator with carry
			 *******/
			case 0x6D:
			case 0x61:
			case 0x65:
			case 0x69:
			case 0x71:
			case 0x75:
			case 0x79:
			case 0x7D:
				// TODO - review
				temp = src + A + ((C) ? 1 : 0);
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
				break;

			/* *****
			 * AND *  -> logical AND between A and memory
			 *******/
			case 0x21:
			case 0x25:
			case 0x29:
			case 0x2D:
			case 0x31:
			case 0x35:
			case 0x39:
			case 0x3D:
				A &= src;
				S = ((A & 0x80) > 0);
				Z = (A == 0);
				break;

			/* *****
			 * ASL *  -> Shift left one bit (memory or A)
			 *******/
			case 0x0A:
			case 0x06:
			case 0x16:
			case 0x0E:
			case 0x1E:
				C = ((src & 0x80) > 0);
				src <<= 1;
				src &= 0xFF;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				if (opcode == 0x0A)
					A = src;
				else
					memory.set(address,src,cycles);
				break;

			/* *****
			 * BCC *  -> Branch on carry clear (i.e., C == 0)
			 *******/
			case 0x90:
				if (!C)
				{
					PC = src;
					cycles++;
				}
				break;

			/* *****
			 * BCS *  -> Branch on carry set (i.e., C == 1)
			 *******/
			case 0xB0:
				if (C)
				{
					PC = src;
					cycles++;
				}
				break;

			/* *****
			 * BEQ *  -> Branch if equal (i.e., Z == 1)
			 *******/
			case 0xF0:
				if (Z)
				{
					PC = src;
					cycles++;
				}
				break;

			/* *****
			 * BIT *  -> Test bits in memory with A
			 *******/
			case 0x24:
			case 0x2C:
				S = ((src & 0x80) > 0);
				/* Copy bit 6 to OVERFLOW flag. */
				O = ((0x40 & src) > 0);
				Z = ((src & A) == 0);
				break;

			/* *****
			 * BMI *  -> Branch if minus (i.e., S == 1)
			 *******/
			case 0x30:
				if (S)
				{
					cycles++;
					PC = src;
				}
				break;

			/* *****
			 * BNE *  -> Branch if not equal (i.e., Z == 0)
			 *******/
			case 0xD0:
				if (!Z)
				{
					PC = src;
					cycles++;
				}
				break;
				
			/* *****
			 * BPL *  -> Branch if plus (i.e., S == 0)
			 *******/
			case 0x10:
				if (!S)
				{
					PC = src;
					cycles++;
				}
				break;

			/* *****
			 * BRK *  -> Force break
			 *******/
			case 0x00:
				/* push return address onto the stack */
				PUSH((PC >> 8) & 0xFF, cycles);
				PUSH(PC & 0xFF, cycles);
				B = true;
				PUSH(SP, cycles);
				I = true;
				PC = (memory.get(0xFFFE)|(memory.get(0xFFFF)<<8));
				break;

			/* *****
			 * BVC *  -> Branch on overflow clear
			 *******/
			case 0x50:
				if (!O)
				{
					PC = src;
					cycles++;
				}
				break;

			/* *****
			 * BVS *  -> Branch on overflow set
			 *******/
			case 0x70:
				if (O)
				{
					PC = src;
					cycles++;
				}
				break;

			/* *****
			 * CLC *  -> Clear carry flag
			 *******/
			case 0x18:
				C = false;
				break;

			/* *****
			 * CLD *  -> Clear decimal flag
			 *******/
			case 0xD8:
				D = false;
				break;

			/* *****
			 * CLI *  -> Clear interrupt flag
			 *******/
			case 0x58:
				I = false;
				break;

			/* *****
			 * CLV *  -> Clear overflow flag
			 *******/
			case 0xB8:
				O = false;
				break;

			/* *****
			 * CMP *  -> Compare memory with A
			 *******/
			case 0xC9:
			case 0xC5:
			case 0xD5:
			case 0xCD:
			case 0xDD:
			case 0xD9:
			case 0xC1:
			case 0xD1:
				src = A - src;
				C = (src < 0x100);
				S = ((src & 0x80) > 0);
				Z = ((src &= 0xff) == 0);
				break;

			/* *****
			 * CPX *  -> Compare memory with X
			 *******/
			case 0xE0:
			case 0xE4:
			case 0xEC:
				src = X - src;
				C = (src < 0x100);
				S = ((src & 0x80) > 0);
				Z = ((src &= 0xff) == 0);
				break;
				
			/* *****
			 * CPY *  -> Compare memory with Y
			 *******/
			case 0xC0:
			case 0xC4:
			case 0xCC:
				src = Y - src;
				C = (src < 0x100);
				S = ((src & 0x80) > 0);
				Z = ((src &= 0xff) == 0);
				break;

			/* *****
			 * DEC *  -> Decrement memory by one
			 *******/
			case 0xC6:
			case 0xD6:
			case 0xCE:
			case 0xDE:
				src = (src - 1) & 0xff;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				memory.set(address,src,cycles);
				break;

			/* *****
			 * DEX *  -> Decrement X by one
			 *******/
			case 0xCA:
				X = (X-1) & 0xFF;
				S = ((X & 0x80) > 0);
				Z = (X == 0);
				break;

			/* *****
			 * DEY *  -> Decrement Y by one
			 *******/
			case 0x88:
				Y = (Y-1) & 0xFF;
				S = ((Y & 0x80) > 0);
				Z = (Y == 0);
				break;

			/* *****
			 * EOR *  -> Exclusive OR memory with accumulator
			 *******/
			case 0x49:
			case 0x45:
			case 0x55:
			case 0x40:
			case 0x5D:
			case 0x59:
			case 0x41:
			case 0x51:
				src ^= A;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				A = src;
				break;

			/* *****
			 * INC *  -> Increment memory by one
			 *******/
			case 0xE6:
			case 0xF6:
			case 0xEE:
			case 0xFE:
				src = (src + 1) & 0xff;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				memory.set(address, src, cycles);
				break;

			/* *****
			 * INX *  -> Increment X by one
			 *******/
			case 0xE8:
				X = (X+1) & 0xFF;
				S = ((X & 0x80) > 0);
				Z = (X == 0);
				break;

			/* *****
			 * INY *  -> Increment Y by one
			 *******/
			case 0xC8:
				Y = (Y+1) & 0xFF;
				S = ((Y & 0x80) > 0);
				Z = (Y == 0);
				break;

			/* *****
			 * JMP *  -> Jump to a new location
			 *******/
			case 0x4C:
			case 0x6C:
				PC = address;
				break;

			/* *****
			 * JSR *  -> Jump and save return address
			 *******/
			case 0x20:
				PC--;
				/* push return address onto the stack */
				PUSH((PC >> 8) & 0xFF, cycles);
				PUSH(PC & 0xFF, cycles);
				PC = address;
				break;

			/* *****
			 * LDA *  -> Load A with memory
			 *******/
			case 0xA9:
			case 0xA5:
			case 0xB5:
			case 0xAD:
			case 0xBD:
			case 0xB9:
			case 0xA1:
			case 0xB1:
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				A = src;
				break;

			/* *****
			 * LDX *  -> Load X with memory
			 *******/
			case 0xA2:
			case 0xA6:
			case 0xB6:
			case 0xAE:
			case 0xBE:
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				X = src;
				break;

			/* *****
			 * LDY *  -> Load Y with memory
			 *******/
			case 0xA0:
			case 0xA4:
			case 0xB4:
			case 0xAC:
			case 0xBC:
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				Y = src;
				break;

			/* *****
			 * LSR *  -> Shift right one bit
			 *******/
			case 0x4A:
			case 0x46:
			case 0x56:
			case 0x4E:
			case 0x5E:
				C = ((src & 0x01) > 0);
				src >>= 1;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				if(opcode == 0x4A)
					A = src;
				else
					memory.set(address, src, cycles);
				break;

			/* *****
			 * NOP *  -> No operation
			 *******/
			case 0xEA:
			case 0x04:
				/* do nothing */
				break;

			/* *****
			 * ORA *  -> Memory Or A
			 *******/
			case 0x09:
			case 0x05:
			case 0x15:
			case 0x0D:
			case 0x1D:
			case 0x19:
			case 0x01:
			case 0x11:
				src |= A;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				A = src;
				break;

			/* *****
			 * PHA *  -> Push A onto stack
			 *******/
			case 0x48:
				src = A;
				PUSH(src, cycles);
				break;

			/* *****
			 * PHP *  -> Push SP onto stack
			 *******/
			case 0x08:
				src = SP;
				PUSH(src, cycles);
				break;

			/* *****
			 * PLA *  -> Pull A from stack
			 *******/
			case 0x68:
				src = PULL();
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				A = src;
				break;

			/* *****
			 * PLP *  -> Pull SP from stack
			 *******/
			case 0x28:
				src = PULL();
				SP = src;
				break;

			/* *****
			 * ROL *  -> Rotate one bit left
			 *******/
			case 0x2A:
			case 0x26:
			case 0x36:
			case 0x2E:
			case 0x3E:
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
					memory.set(address, src, cycles);
				break;

			/* *****
			 * ROR *  -> Rotate one bit right
			 *******/
			case 0x6A:
			case 0x66:
			case 0x76:
			case 0x6E:
			case 0x7E:
				if(C)
					src |= 0x100;
				C = ((src & 0x01) > 0);
				src >>= 1;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				if(opcode == 0x6A)
					A = src;
				else
					memory.set(address, src, cycles);
				break;

			/* *****
			 * RTI *  -> Return from interrupt
			 *******/
			case 0x4D:
				src = PULL();
				SP = src;
				src = PULL();
				src |= (PULL() << 8);
				PC = src;
				break;

			/* *****
			 * RTS *  -> Return from subroutine
			 *******/
			case 0x60:
				src = PULL();
				src += ((PULL()) << 8) + 1;
				PC = src;
				break;
				
			/* *****
			 * SBC *  -> Substract with borrow (carry)
			 *******/
			case 0xE9:
			case 0xE5:
			case 0xF5:
			case 0xED:
			case 0xFD:
			case 0xF9:
			case 0xE1:
			case 0xF1:
				temp = A - src - (C ? 0 : 1);
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
				break;

			/* *****
			 * SEC *  -> Set carry flag
			 *******/
			case 0x38:
				C = true;
				break;

			/* *****
			 * SED *  -> Set decimal mode
			 *******/
			case 0xF8:
				D = true;
				break;

			/* *****
			 * SEI *  -> Set interrupt disable status
			 *******/
			case 0x78:
				I = true;
				break;

			/* *****
			 * STA *  -> Store A into memory
			 *******/
			case 0x85:
			case 0x95:
			case 0x8D:
			case 0x9D:
			case 0x99:
			case 0x81:
			case 0x91:
				memory.set(address, A, cycles);
				break;

			/* *****
			 * STX *  -> Store X into memory
			 *******/
			case 0x86:
			case 0x96:
			case 0x8E:
				memory.set(address, X, cycles);
				break;

			/* *****
			 * STY *  -> Store Y into memory
			 *******/
			case 0x84:
			case 0x94:
			case 0x8C:
				memory.set(address, Y, cycles);
				break;

			/* *****
			 * TAX *  -> Transfer A to X
			 *******/
			case 0xAA:
				src = A;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				X = src;
				break;

			/* *****
			 * TAY *  -> Transfer A to Y
			 *******/
			case 0xA8:
				src = A;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				Y = src;
				break;

			/* *****
			 * TSX *  -> Transfer SP to X
			 *******/
			case 0xBA:
				src = SP;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				X = src;
				break;

			/* *****
			 * TXA *  -> Transfer X to A
			 *******/
			case 0x8A:
				src = X;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				A = src;
				break;

			/* *****
			 * TXS *  -> Transfer X to SP
			 *******/
			case 0x9A:
				src = X;
				SP = src;
				break;

			/* *****
			 * TYA *  -> Transfer Y to A
			 *******/
			case 0x98:
				src = Y;
				S = ((src & 0x80) > 0);
				Z = (src == 0);
				A = src;
				break;
		}

		return cycles;
	}

	//
	// returns the instruction pointer
	//
	public int instructionPointer()
	{
		return PC;
	}

	//
	// returns debug information for that opcode
	// 
	protected DebugValues debug(int pos)
	{
		DebugValues dv = new DebugValues();
		int bytes_left = 0;
		int opcode = 0;
		int lsrc = 0, hsrc = 0;
		int src = 0;
		int pc = pos;

		do
		{
			/* load next byte from memory */
			switch(bytes_left)
			{
				case 0: 
					opcode = memory.get(pos);
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
							bytes_left = 1;
							dv.n_opcodes = 1;
							break;
							
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
						case 0x04: /* NOP - undocumented */
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
							bytes_left = 2;
							dv.n_opcodes = 2;
							break;

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
							bytes_left = 3;
							dv.n_opcodes = 3;
							break;
						default:
							return null;
					}
					lsrc = hsrc = src = 0; /* clear src variables */
					break;
				case 1:
					if(dv.n_opcodes == 2)
						lsrc = memory.get(pos);
					else /* if == 3 */
						hsrc = memory.get(pos);
					break;
				case 2:
					lsrc = memory.get(pos);
					break;
			}
			
			/* advance position in the memory */
			pos++;
			
			bytes_left--;

		} while(bytes_left > 0);
		
		String debug_par = "", debug_opcode = "";
		
		switch (opcode)
		{
			/* Implied: the opcode uses no parameter */
			case 0x00: /* BRK */
				dv.cycles = 1; /* 7 cycles */
			case 0x4D: /* RTI */
			case 0x60: /* RTS */
				dv.cycles += 2; /* 6 cycles */
			case 0x68: /* PLA */
			case 0x28: /* PLP */
				dv.cycles += 1; /* 4 cycles */
			case 0x48: /* PHA */
			case 0x08: /* PHP */
				dv.cycles += 1; /* 3 cycles */
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
				dv.cycles += 2; /* 2 cycles */
				debug_par = " ";
				break;

			/* Accumulator: it'll use the accumulator (A)
			 * as parameter. */
			case 0x0A: /* ASL */
			case 0x4A: /* LSR */
			case 0x2A: /* ROL */
			case 0x6A: /* ROR */
				dv.cycles = 2;
				debug_par = "A";
				break;

			/* Immediate: it'll pass an absoulte number
			 * as a parameter. */
			case 0x04: /* NOP - undocumented! */
				dv.cycles = 1;
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
				dv.cycles += 2;
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
				dv.cycles = 2; /* 5 cycles */
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
				dv.cycles += 3; /* 3 cycles */
				debug_par = "$" + Integer.toHexString(lsrc);
				break;

			/* Zero page, X: loads from the (zero page + X) */
			case 0x16: /* ASL */
			case 0xD6: /* DEC */
			case 0xF6: /* INC */
			case 0x56: /* LSR */
			case 0x36: /* ROL */
			case 0x76: /* ROR */
				dv.cycles = 2; /* 6 cycles */
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
				dv.cycles += 4; /* 4 cycles */
				debug_par = "$" + Integer.toHexString(lsrc) + ",X";
				break;

			/* Zero page, Y: loads from the (zero page + Y) */
			case 0xB6: /* LDX */
				dv.cycles = 4;
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
				dv.cycles += 2; /* 6 cycles */
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
				dv.cycles += 1; /* 4 cycles */
			case 0x4C: /* JMP */
				dv.cycles += 3; /* 3 cycles */
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
				dv.cycles = 2; /* 7 cycles */
			case 0x9D: /* STA */
				dv.cycles++; /* 5 cycles */
			case 0x7D: /* ADC */
			case 0x3D: /* AND */
			case 0xDD: /* CMP */
			case 0x5D: /* EOR */
			case 0xBD: /* LDA */
			case 0xBC: /* LDY */
			case 0x1D: /* ORA */
			case 0xFD: /* SBC */
				dv.cycles += 4; /* 4 cycles */
				debug_par = "$" + Integer.toHexString(lsrc | (hsrc << 8)) + ",X";
				break;
				
			/* Absoulte, Y: get the byte in the (absolute + Y)
			 * position. */
			case 0x99: /* STA */
				dv.cycles = 1; /* 5 cycles */
			case 0x79: /* ADC */
			case 0x39: /* AND */
			case 0xD9: /* CMP */
			case 0x59: /* EOR */
			case 0xB9: /* LDA */
			case 0xBE: /* LDY */
			case 0x19: /* ORA */
			case 0xF9: /* SBC */
				dv.cycles += 4; /* 4 cycles */
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
				dv.cycles = 6;
				debug_par = "($" + Integer.toHexString(lsrc) + ",X)";
				break;

			/* (Indirect), Y: get the byte into the zero page and
			 * the following one and makes an adress Z. Takes the
			 * byte from (Z + Y). */
			case 0x91: /* STA */
				dv.cycles = 1; /* 6 cycles */
			case 0x71: /* ADC */
			case 0x31: /* AND */
			case 0xD1: /* CMP */
			case 0x51: /* EOR */
			case 0xB1: /* LDA */
			case 0x11: /* ORA */
			case 0xF1: /* SBC */
				dv.cycles += 5; /* 5 cycles */
				debug_par = "($" + Integer.toHexString(lsrc) + "),Y";
				break;

			/* Relative: used in branches. Adds to PC if the
			 * highest bit of the parameter is 0, and substracts
			 * from PC if it's 1. */
			case 0x90: /* BCC */
			case 0xB0: /* BCS */
			case 0xF0: /* BEQ */
			case 0x30: /* BMI */
			case 0xD0: /* BNE */
			case 0x10: /* BPL */
			case 0x50: /* BVC */
			case 0x70: /* BVS */
				dv.cycles = 2;
				debug_par = "$" + Integer.toHexString(pc + dv.n_opcodes - (0x100 - lsrc));
				break;

			/* Indirect: it takes the byte that's into the given
			 * position of the memory and the next byte. */
			case 0x6C: /* JMP */
				dv.cycles = 5;
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
		dv.instruction = debug_opcode + " " + debug_par;
		return dv;
	}

	protected Register[] registers()
	{
		Register[] r = new Register[4];
		r[0] = new Register("A", A);
		r[1] = new Register("X", X);
		r[2] = new Register("Y", Y);
		r[3] = new Register("SP", SP);
		return r;
	}

	protected Flag[] flags()
	{
		Flag[] f = new Flag[7];
		f[0] = new Flag("S", S);
		f[1] = new Flag("O", O);
		f[2] = new Flag("B", B);
		f[3] = new Flag("D", D);
		f[4] = new Flag("I", I);
		f[5] = new Flag("Z", Z);
		f[6] = new Flag("C", C);
		return f;
	}

	//
	// private methods (hacks)
	//
	private void PUSH(int b, int cycles) 
	{ 
		memory.set(SP+0x100, b ,cycles);
		SP--;
	}
	private int PULL()
	{
		return memory.get((++SP)+0x100);
	}

}
