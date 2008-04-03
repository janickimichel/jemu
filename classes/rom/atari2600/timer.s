	processor 6502

	include vcs.h

	org $F000

Start
	lda #15
	sta TIM64T

Here
	lda INTIM
	jmp Here

	org $fffc
	.word Start
	.word Start
