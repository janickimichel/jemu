	processor 6502
	include "vcs.h"
	include "macro.h"

	seg
	org $F000

Reset
StartOfFrame

	; Start of vertical blank processing
	lda #0
	sta VBLANK
	lda #2
	sta VSYNC

	; 3 scanlines of VSYNCH signal...
	sta WSYNC
	sta WSYNC
	sta WSYNC
	lda #0
	sta VSYNC          

	; 37 scanlines of vertical blank...
	REPEAT 36
		sta WSYNC
	REPEND

	lda #$ff
	sta COLUP0
	sta WSYNC

	; 192 scanlines of picture...
	ldx #0
	REPEAT 10
		sta WSYNC
	REPEND

	; show square
	lda #$ff
	sta GRP0
	sta WSYNC

	; put square on position
	nop
	lda #0
	sta $fffe
	rol $fffe,x
	rol 
	sta RESP0
	sta WSYNC

	REPEAT 18
		sta WSYNC
	REPEND

	; hide square
	lda #0
	sta GRP0

	REPEAT 162
		sta WSYNC
	REPEND

	lda #%01000010
	sta VBLANK                     ; end of screen - enter blanking

	; 30 scanlines of overscan...
	REPEAT 30
		sta WSYNC
	REPEND

	jmp StartOfFrame

	ORG $FFFA

	.word Reset          ; NMI
	.word Reset          ; RESET
	.word Reset          ; IRQ
