; Cave 1K
; Copyright 2003, Thomas Jentzsch
; Version 1.07

; free: 42 bytes

;TODOs
; o Explosions (better graphics)
; + High-Score (color change?)
; + walls
; + two height variables
; + change score color when new high score
; + show version number as initial highscore
; ? different scoring (lose points when colliding)
; + competition mode (non random), P0 = pro

; Difficulties:
; + faster scrolling cave
; + taller walls
; ? moving walls
; x helicopter further right
; ? cave moving up and down
; ? cave getting tighter


; Suggestions for 4K version:
; Power-Ups:
;  - Slow the game down
;  - Reduce length of obstacles
;  - Reduce spacing between obstacles
;  - Give you immunity from next 'x' obstacles
; ? helicopter left/right [~32 pixel)

    processor 6502
    include vcs.h


;===============================================================================
; A S S E M B L E R - S W I T C H E S
;===============================================================================

VERSION         = $107

BASED_ADR       = $f800

DEBUG           = 0         ; [0] (+ 1)
TEST            = 0         ; [0] (  0) for testing parameter at maximum difficulty
RANDOM          = 1         ; [1] (- 2) 0 used for debugging
ILLEGAL         = 0         ; [?] (- 2) allow illegal opcodes
EASY_GAME       = 1         ; [0] (+-0) use EASY_GAME = 1 for contest

; features:
Y_MOVE_CAVE     = 0         ; [0] (+36) move cave up and down (needs more ROM!)
X_MOVE_HELI     = 0         ; [0] (+??) allow horizontal helicopter movement
SINGLE_BLADE    = 1         ; [1] (  0) alternative heiicopter rotorblades
SCORE_COL       = 1         ; [1] (+ 8) different color fur current and high score
FRICTION        = 1         ; [1] (+24) enable friction (damn hard if disabled!)
SHOW_VERSION    = 1         ; [1] (+ 3) show version number as initial high score
COMP_MODE       = 1         ; [1] (+15) add non random variation (P0 difficulty)
FRICTION_MODE   = 0         ; [0] (+ 5) disable friction (P1 difficulty)


;===============================================================================
; C O N S T A N T S
;===============================================================================

SCREEN_W    = 160

BORDER_H    = 8
  IF Y_MOVE_CAVE
CENTER_H    = 133
  ELSE
CENTER_H    = 140
  ENDIF
KERNEL_H    = CENTER_H + BORDER_H*2
HELI_H      = 16

RAND_EOR_8  = $b2

SCORE_COLOR         = $0a               ; white
SCORE_COMP_COLOR    = $ca               ; green
HISCORE_COLOR       = $44               ; red
HISCORE_COMP_COLOR  = $66               ; magenta

; difficulty parameters:
HELI_MAX_X  = 62-9                      ; not used
HELI_X      = 8                         ; x-position of helicopter
HELI_Y      = KERNEL_H/2+HELI_H/2       ; initial y-position of helicopter

  IF EASY_GAME
SPEED_MIN   = 40                        ; 38/980; 40/940, 42/900, 48/760, 50/705
SPEED_MAX   = $55                       ; maximum cave scrolling speed

THRUST      = 24-3                      ;
GRAVITY     = THRUST*4/3                ;

WALL_MIN_X  = SCREEN_W-HELI_MAX_X-8-16  ; -4? minimum distance between two walls
WALL_MAX_X  = HELI_X+40                 ; no further delay after that distance

WALL_MIN_Y  = 40                        ; starting wall height
WALL_MAX_Y  = CENTER_H-HELI_H*7/2       ; maximum height of wall
  ELSE
SPEED_MIN   = 48                        ; 38/980; 40/940, 42/900, 48/760, 50/705
SPEED_MAX   = $60                       ; maximum cave scrolling speed

THRUST      = 27+3                      ; 27+3 (+3?)
GRAVITY     = THRUST*4/3-2              ; *4/3(*5/4?)

WALL_MIN_X  = SCREEN_W-HELI_MAX_X-8-12  ; -4? minimum distance between two walls
WALL_MAX_X  = HELI_X+40                 ; no further delay after that distance

WALL_MIN_Y  = 50                        ; starting wall height
WALL_MAX_Y  = CENTER_H-HELI_H*6/2       ; maximum height of wall
  ENDIF

HEIGHT_INC  = 2                         ; wall height increasement

;===============================================================================
; Z P - V A R I A B L E S
;===============================================================================

    SEG.U   variables
    ORG     $80

tmpVar      .byte
frameCnt    .byte
compMode    .byte

;---------------------------------------
; initialized pointers
scorePtr    ds 8
ptrHeli0    ds 2
ptrHeli1    ds 2
ptrHeliCol  ds 2
initPtrs    = scorePtr
NUM_PTRS    = . - initPtrs
;---------------------------------------

xPosCave    .byte
  IF Y_MOVE_CAVE
yPosCave    .byte
  ENDIF
colCave     .byte



;Lst    ds 2
;hWall0      = hWallLst
;hWall1      = hWallLst+1

; helicopter:
ySpeed      ds 2
ySpeedLo    = ySpeed
ySpeedHi    = ySpeed+1

; scores:
  IF SCORE_COL
colScore    .byte
  ENDIF
score       ds 2
scoreLo     = score
scoreHi     = score+1

;---------------------------------------
random      .byte               ;       random initialization
yHeli       ds 2
yHeliLo     = yHeli             ;       random initialization
; initialized variables start here:
initVars    = .-1
yHeliHi     = yHeli+1

; cave graphics:
PF0Lst      ds BORDER_H
PF1Lst      ds BORDER_H
PF2Lst      ds BORDER_H

speedCave   .byte
  IF X_MOVE_HELI
xHeli       .byte
  ELSE
xObjects    ds 5
xHeli0      = xObjects
xHeli1      = xObjects+1
  ENDIF
  IF SHOW_VERSION = 0
NUM_INITS   = . - initVars - 3
;---------------------------------------
  ENDIF
dummy       = xObjects+2        ;       missile 0 is not used!
; walls:
xWall0      = xObjects+3
xWall1      = xObjects+4
xWallLst    = xWall0
yWallLst    ds 2
yWall0      = yWallLst
yWall1      = yWallLst+1
hWallLst    ds 2
hWall0      = hWallLst
hWall1      = hWallLst+1
wallLst     = xWallLst

; only initialized on START:
mode        .byte
scoreMax    ds 2
scoreMaxLo  = scoreMax
scoreMaxHi  = scoreMax+1
  IF SHOW_VERSION
NUM_INITS   = . - initVars
;---------------------------------------
  ENDIF


;===============================================================================
; M A C R O S
;===============================================================================

  MAC DEBUG_BRK
    IF DEBUG
      brk                         ;
    ENDIF
  ENDM

  MAC BIT_B
    .byte   $24
  ENDM

  MAC BIT_W
    .byte   $2c
  ENDM

  MAC SLEEP
    IF {1} = 1
      ECHO "ERROR: SLEEP 1 not allowed !"
      END
    ENDIF
    IF {1} & 1
     IF ILLEGAL
      nop $00
     ELSE
      bit $00
     ENDIF
      REPEAT ({1}-3)/2
        nop
      REPEND
    ELSE
      REPEAT ({1})/2
        nop
      REPEND
    ENDIF
  ENDM

  MAC CHECKPAGE
    IF >. != >{1}
      ECHO ""
      ECHO "ERROR: different pages! (", {1}, ",", ., ")"
      ECHO ""
      ERR
    ENDIF
  ENDM


;===============================================================================
; R O M - C O D E
;===============================================================================
    SEG     Bank0

    ORG     BASED_ADR, 0

;***************************************************************
Kernel SUBROUTINE
;***************************************************************
; make sure that the C-flag is always set!

.skipDraw:
    lda     #0              ; 2
    sta.w   GRP1            ; 4
    sta     GRP0            ; 3
Wait14:
    sec                     ; 2
Wait12:
    rts                     ; 6 = 17


.skipDrawCtr:               ;10         @53
    jsr     .skipDraw       ;23
    beq     .contDrawCtr    ; 3         @03


DrawScreen:
.waitTim:
    ldy     INTIM
    bne     .waitTim
    sta     WSYNC
    sta     HMOVE
    sty     VBLANK
;---------------------------------------------------------------
  IF Y_MOVE_CAVE
    lda     yPosCave
    lsr
    ora     #$10
    tax
  ELSE
    ldx     #BORDER_H*2-1
  ENDIF
    jsr     DrawBorder      ;           @35

    ldy     #KERNEL_H       ; 2
    sta     WSYNC
;---------------------------------------
    ldx     #BORDER_H       ; 2
.loopTopKernel:             ;           @02
    lda     COLUPFTbl+7,x   ; 4
    ora     colCave         ; 3
    sta     COLUPF          ; 3         @12
    lda     PF0Lst-1,x      ; 4
    sta     PF0             ; 3         @19
    lda     PF1Lst-1,x      ; 4
    sta     PF1             ; 3         @26
    lda     PF2Lst-1,x      ; 4
    sta     PF2             ; 3 = 31    @33

    tya                     ; 2
    sbc     yHeliHi         ; 3
    adc     #HELI_H         ; 2
    bcs     .drawHeliTop    ; 2³

    jsr     .skipDraw       ;23
    beq     .contDrawTop    ; 3

.drawHeliTop:
    lda     (ptrHeli1),y    ; 5
    sta     GRP1            ; 3
    lda     (ptrHeli0),y    ; 5
    sta     GRP0            ; 3
    lda     (ptrHeliCol),y  ; 5
    sta.w   COLUP0          ; 4
.contDrawTop:
    sta     COLUP1          ; 3 = 38    @71

    dey                     ; 2
    dex                     ; 2
;---------------------------------------
    bne     .loopTopKernel  ; 2³=  7

    stx     PF0             ; 3
    stx     PF1             ; 3
    stx     PF2             ; 3         @10

    jsr     Wait14          ;18
    lda     (ptrHeli0,x)    ; 6             x = 0!

    lda     colCave         ; 3
    eor     #$8a            ; 2             brightness of walls
    sta     COLUPF          ; 3              (bit 1 must be set, else change code!)
    tax                     ; 2 = 10

; *** center kernel loop ***
.loopCtrKernel:             ;           @40
    stx     COLUP1          ; 3 =  3    @43 je später desto weiter rechts kann der Heli fliegen
                            ;                aber desto größer wird der Mauer-Mindestabstand (@40..46)
    tya                     ; 2
    sbc     yHeliHi         ; 3
    adc     #HELI_H         ; 2
    bcc     .skipDrawCtr    ; 2³
    lda     (ptrHeli1),y    ; 5
    sta     GRP1            ; 3
    lda     (ptrHeli0),y    ; 5
    sta     GRP0            ; 3
    lda     (ptrHeliCol),y  ; 5
    sta     COLUP0          ; 3 = 33    @00
;---------------------------------------
    sta     COLUP1          ; 3 =  3    @03
.contDrawCtr:               ;

    tya                     ; 2
    sbc     yWall1          ; 3
    adc     hWall1          ; 3
    txa                     ; 2             bit 1 set!
    adc     #$ff            ; 2
    sta     ENABL           ; 3 = 15    @18

    tya                     ; 2
    sbc     yWall0          ; 3
    adc     hWall0          ; 3
    txa                     ; 2             bit 1 set!
    adc     #$ff            ; 2
    sta     ENAM1           ; 3 = 15    @33

    dey                     ; 2
    cpy     #BORDER_H       ; 2
    bne     .loopCtrKernel  ; 2³=  6/7

    ldx     #0              ; 2
.loopBtmKernel:             ;           @41
    tya                     ; 2
    sbc     yHeliHi         ; 3
    adc     #HELI_H         ; 2
    bcs     .drawHeliBtm    ; 2³
    jsr     .skipDraw       ;23
    beq     .contDrawBtm    ; 3         @00

.drawHeliBtm:
    lda     (ptrHeli1),y    ; 5
    sta     GRP1            ; 3
    lda     (ptrHeli0),y    ; 5
    sta     GRP0            ; 3
    lda     (ptrHeliCol),y  ; 5
    sta.w   COLUP0          ; 4         @00
.contDrawBtm:
;---------------------------------------
    sta     COLUP1          ; 3 = 38    @03

    lda     COLUPFTbl-1,y   ; 4
    ora     colCave         ; 3
    sta     COLUPF          ; 3         @13
    lda     PF0Lst,x        ; 4
    sta     PF0             ; 3         @20
    lda     PF1Lst,x        ; 4
    sta     PF1             ; 3         @27
    lda     PF2Lst,x        ; 4
    sta     PF2             ; 3 = 31    @34

    inx                     ; 2
    dey                     ; 2
    bne     .loopBtmKernel  ; 2³= 6/7

  CHECKPAGE Kernel

    sty     GRP0
    sty     GRP1

  IF Y_MOVE_CAVE
    ldx     #BORDER_H*4
    lda     yPosCave
    adc     #BORDER_H*2+1
    lsr
    tay
  ELSE
    ldx     #BORDER_H*2-1
  ENDIF
    jsr     DrawBorder
    sta     WSYNC
;---------------------------------------
  IF Y_MOVE_CAVE
    iny
  ENDIF
    sty     PF0
    sty     PF1
    sty     PF2
    iny                         ;           #%00000001 (two copies close)
    sty     NUSIZ0              ;
    sty     NUSIZ1              ;

    ldy     #DIGIT_HEIGHT
  IF SCORE_COL
    lda     colScore
    sta     COLUP0
    sta     COLUP1
  ELSE
    sty     COLUP0
    sty     COLUP1
  ENDIF

.loopDigits:
  IF ILLEGAL
    sta     WSYNC
;---------------------------------------
    lda     (scorePtr),y    ; 5
    sta     GRP1            ; 3         @08
    lda     (scorePtr+4),y  ; 5
    sta     GRP0            ; 3         @16
    lax     (scorePtr+6),y  ; 5
    lda     (scorePtr+2),y  ; 5
    sta     GRP1            ; 3         @29
    stx     GRP0            ; 3         @32
    dey                     ; 2
  ELSE
    lda     (scorePtr),y    ; 5
    sta     WSYNC
;---------------------------------------
    sta     GRP1            ; 3         @03
    lda     (scorePtr+4),y  ; 5
    sta     GRP0            ; 3         @11
    lda     (scorePtr+6),y  ; 5
    tax                     ; 2
    lda     (scorePtr+2),y  ; 5
    dey                     ; 2
    sta     GRP1            ; 3         @28
    stx     GRP0            ; 3         @31
  ENDIF
    bne     .loopDigits

    sty     GRP1
    sty     GRP0
; DrawSceen

;---------------------------------------------------------------
OverScan SUBROUTINE
;---------------------------------------------------------------
    lda     #36-5
    sta     TIM64T

    asl     INPT4
    lda     mode
    beq     .gameRunning
    bpl     .contExplosion
    bcs     .stopExplosion
  IF SHOW_VERSION
    ldy     #NUM_INITS-1+2-3
  ELSE
    ldx     #scoreMaxLo     ; 2         number of resetted values+1
  ENDIF
    brk                     ;           y=0!

.gameRunning:
; ***** collisions: *****
    lda     CXP0FB
    ora     CXP1FB
    and     #$c0
    beq     .skipCollisions

    jsr     CheckHigh
    bcc     .skipHigh
    lda     scoreHi
    stx     scoreMaxLo
    sta     scoreMaxHi
.skipHigh:
    dec     ptrHeliCol+1
    dec     ptrHeli0+1
    dec     ptrHeli1+1
.contExplosion:
    inc     mode
.stopExplosion:
    bne     .skipRunning
    DEBUG_BRK

.skipCollisions:

; ***** vertical helicopter movement: *****
; throttle:
    ldx     #<ySpeed
    lda     #THRUST         ;
    bcc     .doButton
    lda     #-GRAVITY       ;       negative gravity 33% larger!
    dey
.doButton:
    jsr     Add16

  IF FRICTION
    asl
    ldy     #-1
    bcc     .posSpeed
    iny
.posSpeed:
   IF FRICTION_MODE
    bit     SWCHB
    bmi     .skipFriction
   ENDIF
; friction (the faster, the more):
; (factor = ySpeed / 8)
    lda     ySpeedLo
    and     #%11100000
    asl
    eor     ySpeedHi
    and     #%11100000
    eor     ySpeedHi
;    and     #%00011111
;    sta     tmpVar
;    lda     ySpeedLo
;    and     #%11100000
;    asl
;    ora     tmpVar
    rol
    rol
    rol
    eor     #$ff
    jsr     Add16
   IF FRICTION_MODE
.skipFriction:
   ENDIF
  ENDIF

    tay
    ldx     #<yHeli
    lda     ySpeedLo
    jsr     Add16

; ***** x-move cave *****
    lda     xPosCave
    clc
    adc     speedCave
    sta     xPosCave
    bcs     .moveCave
.skipRunning:
    jmp     .skipMove

.moveCave:
; ***** increase score: *****
    lda     #0
    tay
    ldx     #<scoreLo
    sed
    jsr     Add16NC         ;           C = 1!
    cld
; y = 0; C = 0

    ldx     #BORDER_H
.loop:
    lda     PF0Lst-1,x
    and     #$10
    cmp     #$10
    ror     PF2Lst-1,x
    rol     PF1Lst-1,x
    ror     PF0Lst-1,x
    dex
    bne     .loop

; ***** handle walls *****
    inx                     ; 2         x = 1!
.loopMove:
; move wall:
    lda     xWallLst,x
    sec
    sbc     #4
    bcs     .okWall
    tya                     ;           a = 0!
    sta     yWallLst,x
.okWall:
    sta     xWallLst,x
.nextWall:
    dex
    bpl     .loopMove

; sort walls:
; (wall #1 is always *left* of wall #0)
    ldy     xWall1          ; 3
    lda     xWall0
    beq     .skipSwap
    tya
    beq     .doSwap
    cmp     xWall0
    bcc     .skipSwap
.doSwap:
; works only if stored consecutive!
    ldx     #6
.loopSwap
    lda     wallLst-1,x
    ldy     wallLst-2,x
    sta     wallLst-2,x
    sty     wallLst-1,x
    dex
    dex
    bne     .loopSwap
.skipSwap:

; ***** create new walls: *****
    lda     yWall0
    bne     .skipNewWall
;    ldy     xWall1
    cpy     #WALL_MIN_X         ;       too early for 2nd wall?
    bcs     .skipNewWall        ;        yes, skip

; NextRandom SUBROUTINE
  IF COMP_MODE
    bit     compMode
  ENDIF
    lda     random              ; 3
    lsr                         ; 2
    bcc     .skipEor            ; 2³
    eor     #RAND_EOR_8         ; 2
.skipEor:                       ;
    sta     random              ; 3

    cpy     #WALL_MAX_X         ;       maximum delay for a new wall?

  IF RANDOM
   IF COMP_MODE
    bvs     .skipRandom
   ENDIF
    eor     yHeliLo             ; 3     ySpeedLo
.skipRandom;
  ENDIF

    bcc     .contNew            ;        yes, don't wait any longer
    bmi     .skipNewWall        ;        no, 50% wait
.contNew:
; minimum: hWall + BORDER_H + 3 (+1)
; maximum: CENTER_H + BORDER_H + 1
    asl
    lsr
;    and     #$7f
    adc     #BORDER_H+HEIGHT_INC+2
    adc     hWall0
    cmp     #CENTER_H+BORDER_H+2
    bcs     .skipNewWall

; create minimum vertical distance between walls:
;  |y_new - y_old|*2 + h_old > HEIGHT
    tay
    sbc     yWall1
    bcs     .posDiff
    eor     #$ff
.posDiff:
    asl
    bcs     .createNew
    adc     hWall1
    cmp     #CENTER_H
    bcc     .skipNewWall
.createNew

; finally, create new wall:
    sty     yWall0
    lda     #SCREEN_W-2
    sta     xWall0

; increase wall height:
    lda     hWall0
    cmp     #WALL_MAX_Y
    bcs     .skipNewWall
    adc     #HEIGHT_INC
    sta     hWall0
.skipNewWall:

  IF Y_MOVE_CAVE
; move the cave up and down:
    lda     yPosCave
    lsr
    bcc     .moveUp
    bne     .moveDown
.moveUp:
    cmp     #7
    bcs     .moveDown
    adc     #2
.moveDown:
    rol
    sbc     #1
    sta     yPosCave
; 18 bytes
  ENDIF

; ***** end of cave movement *****
.skipMove:
    inc     frameCnt

;.skipRunning:
.waitTim:
    ldx     INTIM
    bne     .waitTim
    rts
; Overscan


;***************************************************************
DrawBorder SUBROUTINE
;***************************************************************
  IF Y_MOVE_CAVE
.min  = tmpVar

    sty     .min
.loop:
    dex                     ; 2
    sta     WSYNC
    txa                     ; 2
    and     #$0f
    tay
    lda     COLUPFTbl,y     ; 4
    ora     colCave         ; 3
    sta     COLUPF          ; 3
    ldy     #$ff            ; 2
    sty     PF0             ; 3     @17
    sty     PF1             ; 3
    sty     PF2             ; 3
    cpx     .min
    bne     .loop           ; 2³
  ELSE
.loop:
    sta     WSYNC
    lda     COLUPFTbl,x     ; 4
    ora     colCave         ; 3
    sta     COLUPF          ; 3
    lda     #$ff            ; 2
    sta     PF0             ; 3     @17
    sta     PF1             ; 3
    sta     PF2             ; 3
    dex                     ; 2
    bpl     .loop           ; 2³
    sec                     ; 2
  ENDIF
    rts                     ; 6     @35
; y = 0, C=1
; DrawBorder


;***************************************************************
CheckHigh SUBROUTINE
;***************************************************************
; carry doesn't matter!
  IF ILLEGAL
    lax     scoreLo
  ELSE
    lda     scoreLo
    tax
  ENDIF
    sbc     scoreMaxLo
    lda     scoreHi
    sbc     scoreMaxHi
    rts


;***************************************************************
Start SUBROUTINE
;***************************************************************
    cld
  IF SHOW_VERSION
    ldy     #NUM_INITS-1+2
Restart:
    ldx     #scoreMaxLo
  ELSE
    ldx     #0
    ldy     #-1
Restart:
  ENDIF
    lda     #0
.clearLoop:
    dex
    txs
    pha
    bne     .clearLoop
  IF SHOW_VERSION = 0
    sty     mode
  ENDIF

  IF COMP_MODE
    lda     SWCHB
    sta     compMode
  ENDIF

;---------------------------------------------------------------
GameInit SUBROUTINE
;---------------------------------------------------------------
  IF SHOW_VERSION
.loopInit:
    lda     InitTbl-2,y
    sta     initVars-2,y
    dey
  ELSE
    ldx     #NUM_INITS-1+2
.loopInit:
    lda     InitTbl-2,x
    sta     initVars-2,x
    dex
  ENDIF
    bpl     .loopInit

    ldx     #NUM_PTRS-1
    lda     #>Zero
.loopPtr:
    sta     initPtrs,x
    dex
    bne     .loopPtr

;---------------------------------------------------------------
MainLoop:
;---------------------------------------------------------------

;---------------------------------------------------------------
VerticalBlank SUBROUTINE
;---------------------------------------------------------------
    ldy     #3
    sty     VSYNC
.waitSync:
    lda     #%00010000
    sta     CTRLPF              ;       define width of ball and
    sta     NUSIZ1              ;        missile 1 (used for walls)
    sta     NUSIZ0              ;        missile 0 (used for walls)
    ldx     #44-1-4
    dey
    sta     WSYNC
    bpl     .waitSync
    sta     VSYNC
    stx     TIM64T

;---------------------------------------------------------------
GameCalc SUBROUTINE
;---------------------------------------------------------------
; still a lot of free time here

; position all objects:
    sec                     ; 2
    ldx     #5              ; 2
.loopObjects:
    sta     WSYNC
    lda     xObjects-1,x    ; 4
WaitObject:
    sbc     #$0f            ; 2
    bcs     WaitObject      ; 2³

  CHECKPAGE WaitObject

    eor     #$07            ; 2
    asl                     ; 2
    asl                     ; 2
    asl                     ; 2
    asl                     ; 2
    sta.wx  RESP0-1,x       ; 5     @23!
    sta     HMP0-1,x        ; 4
    dex                     ; 2
    bne     .loopObjects    ; 2³

; setup helicopter pointers:
    lda     frameCnt
    bit     INPT4
    bpl     .doubleSpeed
    lsr
.doubleSpeed:
    lsr
    lsr
    lda     #<Heli1a+HELI_H+1
    bcc     .heli0
    adc     #HELI_H*2-1     ;           C=1!
.heli0:
;    sec
    sbc     yHeliHi
    sta     ptrHeli0
    adc     #HELI_H-1
    sta     ptrHeli1
    lda     #<HeliCol+HELI_H+1
;    sec
    sbc     yHeliHi
    sta     ptrHeliCol

; setup score pointers:
    iny                         ;           y=0!
    jsr     CheckHigh
  IF SCORE_COL
    ldx     #HISCORE_COLOR
   IF COMP_MODE
    bit     compMode
    bvc     .skipCompHi
    ldx     #HISCORE_COMP_COLOR
.skipCompHi:
   ENDIF
  ENDIF
    lda     mode
    beq     .currentScore       ;           running
    lda     frameCnt            ;           stopped
    bpl     .hiScore
    clc
.currentScore:
    bcs     .currentScoreHi
  IF SCORE_COL
    ldx     #SCORE_COLOR
   IF COMP_MODE
    bvc     .skipComp
    ldx     #SCORE_COMP_COLOR
.skipComp:
   ENDIF
  ENDIF
    BIT_W
.hiScore:
    ldy     #<(scoreMax-score)
.currentScoreHi:
  IF SCORE_COL
    stx     colScore
  ENDIF
    ldx     #2+1
.loopScore:
    dex
    sty     tmpVar
    lda     score,y
    pha
    lsr
    lsr
    lsr
    lsr
    tay
    lda     DigitTbl,y
    sta     scorePtr,x
    pla
    and     #$0f
    tay
    lda     DigitTbl,y
    sta     scorePtr+4,x
    ldy     tmpVar
    iny
    dex
    bpl     .loopScore

; make explosion sound:
    lda     mode
    beq     .gameRunning
    cmp     #$20
    bcc     .explosion
    inx
    bcs     .silence

.gameRunning:
; ***** speed-up the cave *****
    lda     frameCnt
    asl
    bne     .skipAccel
    bcs     .skipAddColor
    lda     colCave
;    clc
    adc     #$10
    sta     colCave
.skipAddColor:

; accelerate scrolling by ~4.7%:
    lda     speedCave
    lsr
    lsr
    lsr
    lsr
    lsr
    adc     speedCave
    cmp     #SPEED_MAX
    bcc     .speedOk
    lda     #SPEED_MAX
.speedOk:
    sta     speedCave
.skipAccel:

; make helicopter noise:
    lda     frameCnt        ; 3
    and     #$07            ; 2
    bit     INPT4           ; 3
    bmi     .lowNoise       ; 2³
    ora     #$04            ; 2
.lowNoise:
    asl
    adc     #$14            ; 2
    ldx     #$08            ; 2         $02/$08
.explosion:
    sta     AUDF1           ; 3
    stx     AUDC1           ; 3
.silence:
    stx     AUDV1           ; 3
;GameCalc

    jsr     DrawScreen
;    jsr     OverScan
    jmp     MainLoop

;SetDigitPtr:
;    tay
;    lda     DigitTbl,y
;    sta     scorePtr,x
;    ldy     tmpVar
;    dex
;    dex
;    rts


;***************************************************************
Add16 SUBROUTINE
;***************************************************************
    clc
Add16NC:
    adc     $00,x
    sta     $00,x
    tya
    adc     $01,x
    sta     $01,x
    rts

CodeEnd:


;===============================================================================
; R O M - T A B L E S
;===============================================================================

  IF >. != >(BASED_ADR + $300)
    align 256
  ENDIF

Y SET . - CodeEnd

DigitTbl:
    .byte   #<Zero-1,  #<One-1,   #<Two-1,   #<Three-1, #<Four-1
    .byte   #<Five-1,  #<Six-1,   #<Seven-1, #<Eight-1, #<Nine-1

DIGIT_HEIGHT = 9

One:
    .byte   %00000000
    .byte   %00000100
    .byte   %00000100
    .byte   %00000100
Seven:
    .byte   %00000000
    .byte   %00000100
    .byte   %00000100
    .byte   %00000100
Four:
    .byte   %00000000
    .byte   %00000100
    .byte   %00000100
    .byte   %00000100
Zero:
    .byte   %01111000
    .byte   %10000100
    .byte   %10000100
    .byte   %10000100
    .byte   %00000000
    .byte   %10000100
    .byte   %10000100
    .byte   %10000100
Three:
    .byte   %01111000
    .byte   %00000100
    .byte   %00000100
    .byte   %00000100
Nine:
    .byte   %01111000
    .byte   %00000100
    .byte   %00000100
    .byte   %00000100
Eight:
    .byte   %01111000
    .byte   %10000100
    .byte   %10000100
    .byte   %10000100
Six:
    .byte   %01111000
    .byte   %10000100
    .byte   %10000100
    .byte   %10000100
Two:
    .byte   %01111000
    .byte   %10000000
    .byte   %10000000
    .byte   %10000000
Five:
    .byte   %01111000
    .byte   %00000100
    .byte   %00000100
    .byte   %00000100
    .byte   %01111000
    .byte   %10000000
    .byte   %10000000
    .byte   %10000000
    .byte   %01111000

  CHECKPAGE One

    .byte   "(C)2003 T.Jentzsch"

DigitEnd:

    ORG     BASED_ADR + $3fc - NUM_INITS - BORDER_H*2 - HELI_H*5 + 1, $68
Y SET Y + . - DigitEnd

InitTbl:
    .byte   HELI_Y                  ; yHeliHi

; cave border graphics, copied into PFxLst:
    .byte   %00010000               ; PF0
    .byte   %00110000
    .byte   %01110000
    .byte   %11110000
    .byte   %11110000
    .byte   %11110000
    .byte   %11110000
    .byte   %11110000

    .byte   %00000000               ; PF1
    .byte   %00000000
    .byte   %00000000
    .byte   %00000000
    .byte   %10000000
    .byte   %11000001
    .byte   %11100011
    .byte   %11110111

    .byte   %00000000               ; PF2
    .byte   %10000000
    .byte   %11000000
    .byte   %11100000
    .byte   %11110001
    .byte   %11111011
    .byte   %11111111
    .byte   %11111111

  IF TEST
    .byte   SPEED_MAX               ; speedCave
  ELSE
    .byte   SPEED_MIN-1             ; speedCave (is increased at start of game)
  ENDIF
;  IF X_MOVE_HELI
;    .byte   HELI_X                 ; xHeli
;  ELSE
    .byte   HELI_X+8                ; xHeli0
    .byte   HELI_X                  ; xHeli1
;  ENDIF
  IF SHOW_VERSION
    .byte   0                       ; dummy
    .byte   0                       ; xWall0
    .byte   0                       ; xWall1
    .byte   0                       ; yWall0
    .byte   0                       ; yWall1
   IF TEST
    .byte   WALL_MAX_Y              ; hWall0
    .byte   WALL_MAX_Y              ; hWall1
   ELSE
    .byte   WALL_MIN_Y-HEIGHT_INC   ; hWall0
    .byte   WALL_MIN_Y-HEIGHT_INC/2 ; hWall1
   ENDIF
    .byte   -1                      ; mode
    .byte   <VERSION                ; scoreLoMax
    .byte   >VERSION                ; scoreHiMax
  ENDIF

  IF <. < KERNEL_H-1
    ORG (. & (BASED_ADR + $300)) + KERNEL_H-1
  ENDIF

  IF SINGLE_BLADE
Heli1a:
    .byte   %11111000
    .byte   %10010100
    .byte   %10010000
    .byte   %11111000
    .byte   %11111110
    .byte   %11111111
    .byte   %11001111
    .byte   %10000111
    .byte   %00000110
    .byte   %00001000
    .byte   %10010000
    .byte   %11100000
    .byte   %11000000
    .byte   %10000000
    .byte   %11000000
    .byte   %11000000
Heli1b:
    .byte   %00000011
    .byte   %00000000
    .byte   %00000000
    .byte   %00000001
    .byte   %00000011
    .byte   %00000011
    .byte   %00000111
    .byte   %00000111
    .byte   %11001111
    .byte   %11111011
    .byte   %11111111
    .byte   %11000011
    .byte   %10000001
    .byte   %00000000
    .byte   %00000000
    .byte   %01111111
Heli0a:
    .byte   %11111000
    .byte   %10010100
    .byte   %10010000
    .byte   %11111000
    .byte   %11111110
    .byte   %11111111
    .byte   %11001111
    .byte   %10000111
    .byte   %00000110
    .byte   %00001000
    .byte   %10010000
    .byte   %11100000
    .byte   %11000000
    .byte   %10000000
    .byte   %11111111
    .byte   %10000000
Heli0b:
    .byte   %00000011
    .byte   %00000000
    .byte   %00000000
    .byte   %00000001
    .byte   %00000011
    .byte   %00000011
    .byte   %00000111
    .byte   %00000111
    .byte   %10001111
    .byte   %11111011
    .byte   %11111111
    .byte   %11000011
    .byte   %10000001
    .byte   %00000000
    .byte   %00000001
    .byte   %00000001
  ELSE
Heli1a:
    .byte   %11111000
    .byte   %10010100
    .byte   %10010000
    .byte   %11111000
    .byte   %11111110
    .byte   %11111111
    .byte   %11001111
    .byte   %10000111
    .byte   %00000110
    .byte   %00001000
    .byte   %10010000
    .byte   %11100000
    .byte   %11000000
    .byte   %10000000
    .byte   %11111111
    .byte   %10000000
Heli1b:
    .byte   %00000011
    .byte   %00000000
    .byte   %00000000
    .byte   %00000001
    .byte   %00000011
    .byte   %00000011
    .byte   %00000111
    .byte   %00000111
    .byte   %11001111
    .byte   %11111011
    .byte   %11111111
    .byte   %11000011
    .byte   %10000001
    .byte   %00000000
    .byte   %00000000
    .byte   %01111111
Heli0a:
    .byte   %11111000
    .byte   %10010100
    .byte   %10010000
    .byte   %11111000
    .byte   %11111110
    .byte   %11111111
    .byte   %11001111
    .byte   %10000111
    .byte   %00000110
    .byte   %00001000
    .byte   %10010000
    .byte   %11100000
    .byte   %11000000
    .byte   %10000000
    .byte   %10000000
    .byte   %11111111
Heli0b:
    .byte   %00000011
    .byte   %00000000
    .byte   %00000000
    .byte   %00000001
    .byte   %00000011
    .byte   %00000011
    .byte   %00000111
    .byte   %00000111
    .byte   %10001111
    .byte   %11111011
    .byte   %11111111
    .byte   %11000011
    .byte   %10000001
    .byte   %00000000
    .byte   %01111111
    .byte   %00000000
  ENDIF

COLUPFTbl = . - 1
;    .byte   $00,
    .byte        $02, $04, $06, $08, $0a, $0c, $0e
    .byte   $0e, $0c, $0a, $08, $06, $04, $02, $00

HeliCol:
    .byte   $2c
    .byte   $2a
    .byte   $26
    .byte   $82
    .byte   $84
    .byte   $86
    .byte   $2c
    .byte   $42
    .byte   $44
    .byte   $46
    .byte   $48
    .byte   $44
    .byte   $40
    .byte   $26
    .byte   $08
    .byte   $08


;Y SET $3fc - Y - . + InitTbl
  ECHO "*** Free ", Y, " bytes ***"

    org $fffc, 0
    .word   Start
    .word   Restart