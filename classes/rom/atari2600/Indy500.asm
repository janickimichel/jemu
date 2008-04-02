   LIST OFF
; ***  I N D Y  5 0 0  ***
; Copyright 1977 Atari, Inc.
; Designer: Ed Riddle

; Analyzed, labeled and commented
;  by Glenn Saunders
; Last Update: Jan. 5, 2004
;  by Dennis Debro
; Last Update: Sep 14, 2004

   processor 6502

;
; NOTE: You must compile this with vcs.h version 105 or greater.
;
TIA_BASE_READ_ADDRESS = $30         ; set the read address base so this runs on
                                    ; the real VCS and compiles to the exact
                                    ; ROM image

   include vcs.h

   LIST ON

;===============================================================================
; A S S E M B L E R - S W I T C H E S
;===============================================================================

NTSC                    = 0
PAL                     = 1

COMPILE_VERSION         = NTSC      ; change this to compile for different
                                    ; regions

;============================================================================
; T I A - C O N S T A N T S
;============================================================================

HMOVE_L7          =  $70
HMOVE_L6          =  $60
HMOVE_L5          =  $50
HMOVE_L4          =  $40
HMOVE_L3          =  $30
HMOVE_L2          =  $20
HMOVE_L1          =  $10
HMOVE_0           =  $00
HMOVE_R1          =  $F0
HMOVE_R2          =  $E0
HMOVE_R3          =  $D0
HMOVE_R4          =  $C0
HMOVE_R5          =  $B0
HMOVE_R6          =  $A0
HMOVE_R7          =  $90
HMOVE_R8          =  $80

; values for NUSIZx:
ONE_COPY          = %000
TWO_COPIES        = %001
TWO_WIDE_COPIES   = %010
THREE_COPIES      = %011
DOUBLE_SIZE       = %101
THREE_MED_COPIES  = %110
QUAD_SIZE         = %111
MSBL_SIZE1        = %000000
MSBL_SIZE2        = %010000
MSBL_SIZE4        = %100000
MSBL_SIZE8        = %110000

; values for REFPx:
NO_REFLECT        = %0000
REFLECT           = %1000

; SWCHA joystick bits:
MOVE_RIGHT        = %01111111
MOVE_LEFT         = %10111111
MOVE_DOWN         = %00100000
MOVE_UP           = %00010000
NO_MOVE           = %11111111

; mask for SWCHB
BW_MASK           = %1000         ; black and white bit
SELECT_MASK       = %10
RESET_MASK        = %01

;============================================================================
; U S E R - C O N S T A N T S
;============================================================================

ROM_BASE_ADDRESS     = $F000

   IF COMPILE_VERSION = NTSC

VBLANK_TIME          = $24
YELLOW               = $16

   ELSE

VBLANK_TIME          = $23
YELLOW               = $26

   ENDIF

; color constancts
BLACK                = $00
WHITE                = $0E
ORANGE               = $20
BRICK_RED            = $30
RED                  = $40
PURPLE               = $50
BLUE_PURPLE          = $60
DK_BLUE              = $70
LT_BLUE              = $90
GREEN                = $D0
BROWN                = $E0

MAX_GAME_SELECTION   = 14

SELECT_DELAY                  = 30  ; number of frames to wait to check select
                                    ; switch

H_KERNEL             = 222
H_FONT               = 5
H_CAR                = 8
MAX_ROTATIONS        = 8

MAX_CAR_SPRITES      = H_CAR*MAX_ROTATIONS

MAX_COLOR_XOR        = 64

YMIN                 = 40
YMAX                 = 232

; game state values
GAME_RUNNING         = #255
GAME_OVER            = 0

TIME_TRIAL_SECONDS   = $60          ; BCD
TIME_TRIAL_REDUCTION = $01          ; BCD

MAX_RACE_LAPS        = $25          ; BCD
MAX_CRASH_AND_SCORE  = $50          ; BCD
MAX_TAG_SCORE        = $99          ; BCD

; game variation flags
MASK_TIME_TRIAL         = %01000000
MASK_GAME_COLORS        = %00111000
MASK_TRACK_NUMBER       = %00000111

GRAND_PRIX_TRACK        = %000
DEVIL_ELBOW_TRACK       = %001
CRASH_AND_SCORE_TRACK1  = %010
CRASH_AND_SCORE_TRACK2  = %011
SPRINT_TRACK            = %100

; game selection values
CRASH_AND_SCORE         = 4
TAG                     = 8
ICE_RACE                = 10

FINISH_LINE_X           = 85
;init game values
PLAYER1_STARTX1         = 134
PLAYER2_STARTX1         = 134
PLAYER1_STARTY1         = 192
PLAYER2_STARTY1         = 210
PLAYER1_START_DIR1      = REFLECT
PLAYER2_START_DIR2      = REFLECT
BALL_STARTY1            = 128
;these values used for "Crash and Score" and "Tag" games
PLAYER1_STARTX2         = 28
PLAYER2_STARTX2         = 136
PLAYER1_STARTY2         = 202
PLAYER2_STARTY2         = 68
PLAYER1_START_DIR2      = NO_REFLECT
PLAYER2_START_DIR2      = REFLECT
BALL_STARTY2            = 255

;values for pixel movement
VMOVE_0           = 8
VMOVE_U2          = VMOVE_0+2
VMOVE_U1          = VMOVE_0+1
VMOVE_D1          = VMOVE_0-1
VMOVE_D2          = VMOVE_0-2

;============================================================================
; Z P - V A R I A B L E S
;============================================================================
   SEG.U variables
   org $80

gameSelection              ds 1
gameVariation              ds 1
playerScores               ds 2
;--------------------------------------
player1Score               = playerScores
player2Score               = playerScores+1
scoreOffsets               ds 4
;--------------------------------------
lsbScoreOffsets            = scoreOffsets
player1LSBOffset           = lsbScoreOffsets
player2LSBOffset           = lsbScoreOffsets+1
;--------------------------------------
msbScoreOffsets            = scoreOffsets+2
player1MSBOffset           = msbScoreOffsets
player2MSBOffset           = msbScoreOffsets+1
scoreGraphics              ds 2
;--------------------------------------
scoreGraphic1              = scoreGraphics
scoreGraphic2              = scoreGraphics+1
playerVelocity             ds 2
;--------------------------------------
player1Velocity            = playerVelocity
player2Velocity            = playerVelocity+1
playerMotion               ds 2
;--------------------------------------
player1Motion              = playerMotion
player2Motion              = player1Motion+1
lapCheckPoints             ds 2
;--------------------------------------
player1CheckPoints         = lapCheckPoints
player2CheckPoints         = player1CheckPoints+1
colorXOR                   ds 1
playerRotation             ds 2
;--------------------------------------
player1Rotation            = playerRotation
player2Rotation            = player1Rotation+1
changeDirectionTimer       ds 2
;--------------------------------------
player1ChangeDirTimer      = changeDirectionTimer
player2ChangeDirTimer      = player1ChangeDirTimer+1
tagChasing                 ds 1     ; car being chased in TAG game
playfieldBallCollisions    ds 2
;--------------------------------------
player1PFBallCollisions    = playfieldBallCollisions
player2PFBallCollisions    = playfieldBallCollisions+1
missileVertPos             ds 1
playerFrameWait            ds 2
;--------------------------------------
player1FrameWait           = playerFrameWait
player2FrameWait           = player1FrameWait+1
volumeChannelValues        ds 2
;--------------------------------------
volumeChannel1             = volumeChannelValues
volumeChannel2             = volumeChannel1+1
temp                       ds 1
drivingControllerValues    ds 2
;--------------------------------------
player1DCValue             = drivingControllerValues
player2DCValue             = drivingControllerValues+1
tempStackPointer           ds 1
previousPlayerDirections   ds 2
;--------------------------------------
player1PrevDirections      = previousPlayerDirections
player2PrevDirections      = player1PrevDirections+1
gameState                  ds 1
hitPlayfieldFlag           ds 2
;--------------------------------------
player1HitPlayfield        = hitPlayfieldFlag
player2HitPlayfield        = player1HitPlayfield+1
carCollisionFlag           ds 1
currentScanline            ds 1
accelerationRate           ds 2
;--------------------------------------
player1AccelerationRate    = accelerationRate
player2AccelerationRate    = accelerationRate+1
previousDCValues           ds 2
;--------------------------------------
player1PrevDCValue         = previousDCValues
player2PrevDCValue         = player1PrevDCValue+1
difficultySwitchValue      ds 1
lastTagChaser              ds 1
playerVerticalMotion       ds 1
carGraphics                ds H_CAR*2
;--------------------------------------
player1Graphics            = carGraphics
player2Graphics            = player1Graphics+H_CAR

frameCount                 ds 1
selectDebounce             ds 1
accelerationSoundTimer     ds 2
;--------------------------------------
player1AccelerationTimer   = accelerationSoundTimer
player2AccelerationTimer   = player1AccelerationTimer+1
playerGraphicsMask         ds 2
;--------------------------------------
player1GraphicMask         = playerGraphicsMask
player2GraphicMask         = playerGraphicsMask+1
initAccelerationRate       ds 1
initDecelerationRate       ds 1
maximumVelocity            ds 1
rotationDelay              ds 1
playerDelay                ds 2
;--------------------------------------
player1Delay               = playerDelay
player2Delay               = playerDelay+1
playerHorizPos             ds 2
;--------------------------------------
player1HorizPos            = playerHorizPos
player2HorizPos            = playerHorizPos+1

playerVertPos              ds 2
;--------------------------------------
player1VertPos             = playerVertPos
player2VertPos             = playerVertPos+1
playerDirections           ds 2
;--------------------------------------
player1Direction           = playerDirections
player2Direction           = playerDirections+1
ballVertPos                ds 1
missileHorizPos            ds 1     ; don't think this is actually used
playfieldPointers          ds 6
;--------------------------------------
pf0Pointer                 = playfieldPointers
pf1Pointer                 = pf0Pointer+2
pf2Pointer                 = pf1Pointer+2

;============================================================================
; R O M - C O D E
;============================================================================

   SEG Bank0
   org ROM_BASE_ADDRESS

Start
;
; Set up everything so the power up state is known.
;
   sei
   cld                              ; clear decimal mode
   lda #0
   ldx #0
.clearLoop
   sta VSYNC,x                      ; clear TIA
   sta SWCHA,x                      ; clear RIOT
   inx
   bne .clearLoop
   lda #MSBL_SIZE4
   sta NUSIZ0
   sta NUSIZ1
   ldx #$FF
   txs                              ; point the stack to the beginning
   jsr SetDisplayedGameSelection
   jsr EndGame
   jsr InitializeGameVariables
MainLoop
   jsr VerticalSync
   jsr LoadCarGraphics
   lda frameCount                   ; get the frame count
   and #$3F                         ; increment colorXOR ~ every 60 seconds
   bne .doGameCalculations
   inc colorXOR                     ; causes colors to cycle when game over
   lda gameState                    ; get the current game state
   beq .doGameCalculations          ; do game calculations if game over
   lda colorXOR
   cmp #MAX_COLOR_XOR               ; if colorXOR reaches it's max then
   beq .resetColorXOR               ; reset it's value
   bit gameVariation
   bvs .doGameCalculations
   ldx #1                           ; time trial timer is player 2 score
   jsr ReduceTimeTrialTimer
   bne .doGameCalculations
.resetColorXOR
   lda #0
   jsr SetVariablesToEndGame
.doGameCalculations
   lda gameState                    ; get the current game state
   beq .doneGameCalculations        ; skip game logic if game over
   jsr PlayGameSounds
   jsr ReadDrivingControllers
   jsr DeterminePlayerMotion
   jsr CheckIfPlayerHitTrack
   lda gameSelection                ; get the current game selection
   cmp #CRASH_AND_SCORE
   bcc GrandPrixGameLogic
   cmp #ICE_RACE
   bcs GrandPrixGameLogic
   cmp #TAG
   bcs TagGameLogic
CrashAndScoreGameLogic
   jsr CheckForCarCollision
   jsr CheckIfPlayerCaughtBox
   jmp .doneGameCalculations

GrandPrixGameLogic
   jsr CheckForCarCollision
   jsr CheckForCompletedLaps
   jmp .doneGameCalculations

TagGameLogic
   jsr CheckTagCollisions
   ldx tagChasing                   ; get the car number being chased
   lda frameCount
   and #2
   beq .doneGameCalculations
   lda #$FF                         ; turn car graphic on every other frame
   sta playerGraphicsMask,x
.doneGameCalculations
   jsr DoneGameCalculations
   jsr DisplayKernel
   jsr ReadConsoleSwitches
   jsr SetGameColors
   jmp MainLoop

VerticalSync
.waitTime
   lda INTIM
   bne .waitTime
   sta WSYNC

   IF COMPILE_VERSION = PAL

   lda #%11010010
   sta VBLANK                       ; disable TIA (D1 = 1)
   sta WSYNC
   sta WSYNC
   sta WSYNC

   ENDIF

   lda #22
   sta VSYNC                        ; start vertical sync (D1 = 1)
   sta TIM8T                        ; set vertical sync wait time
   inc frameCount
.vsyncWaitTime
   lda INTIM
   bne .vsyncWaitTime
   sta WSYNC
   sta VSYNC                        ; end vertical sync (D1 = 0)
   lda #VBLANK_TIME
   sta TIM64T                       ; set vertical blank wait time
   rts

DisplayKernel SUBROUTINE
   lda #0
   sta CXCLR                        ; clear all collisions
   sta currentScanline              ; set initial scan line count
   sta scoreGraphic1                ; clear score graphic data to avoid
   sta scoreGraphic2                ; bleeding from previous frame
   lda #%00000010                   ; set playfield to SCORE mode (i.e.
   sta CTRLPF                       ; player colors same as score)
   tsx
   stx tempStackPointer             ; save stack pointer
.waitTime
   lda INTIM
   bne .waitTime
   sta WSYNC
   sta VBLANK                       ; enable TIA (D1 = 0)

   IF COMPILE_VERSION = PAL

   ldy #24
.skip25Scanlines
   sta WSYNC
   dey
   bpl .skip25Scanlines

   ENDIF

.scoreKernelWait
   inc currentScanline        ; 5
   sta WSYNC
;--------------------------------------
   lda currentScanline        ; 3
   cmp #2                     ; 2
   bcc .scoreKernelWait       ; 2³
ScoreKernel
   sta WSYNC
;--------------------------------------
   lda scoreGraphic1          ; 3         get the score graphic for display
   sta PF1                    ; 3 = @06
   ldy player1MSBOffset       ; 3
   lda NumberFonts,y          ; 4         read the number fonts
   and #$F0                   ; 2         mask the lower nybble
   sta scoreGraphic1          ; 3         save it in the score graphic
   ldy player1LSBOffset       ; 3
   lda NumberFonts,y          ; 4         read the number fonts
   and #$0F                   ; 2         mask the upper nybble
   ora scoreGraphic1          ; 3         or with score graphic to get LSB
   sta scoreGraphic1          ; 3         value
   lda scoreGraphic2          ; 3         get the score graphic for display
   sta PF1                    ; 3 = @39
   ldy player2MSBOffset       ; 3
   lda NumberFonts,y          ; 4         read the number fonts
   and #$F0                   ; 2         mask the lower nybble
   sta scoreGraphic2          ; 3         save it in the score graphic
   ldy player2LSBOffset       ; 3
   lda NumberFonts,y          ; 4         read the number fonts
   and #$0F                   ; 2         mask the upper nybble
   ora scoreGraphic2          ; 3         or with score graphic to get LSB
   sta scoreGraphic2          ; 3         value
   nop                        ; 2         waste 8 cycles to push to the next
   nop                        ; 2         scan line
   nop                        ; 2
   nop                        ; 2
;--------------------------------------
   inc currentScanline        ; 5 = @03   increment scan line count
   lda currentScanline        ; 3
   cmp #H_FONT+2+1            ; 2
   bcs GameKernel             ; 2³
   lda scoreGraphic1          ; 3
   sta PF1                    ; 3 = @16
   inc player1LSBOffset       ; 5
   inc player1MSBOffset       ; 5
   inc player2LSBOffset       ; 5
   inc player2MSBOffset       ; 5
   lda scoreGraphic2          ; 3
   sta PF1                    ; 3 = @42
   jmp ScoreKernel            ; 3

GameKernel
   lda #0                     ; 2
   sta PF1                    ; 3 = @16   clear PF1 register
   lda (VSYNC,x)              ; 6         waste 6 cycles :-)
   lda #32                    ; 2
   sta currentScanline        ; 3
   lda #%00000001             ; 2
   sta CTRLPF                 ; 3 = @32   reflect the playfield
.gameKernelLoop
   ldx #ENAM1                 ; 2
   txs                        ; 2         stack trick to enable missiles
   sec                        ; 2
   lda player1VertPos         ; 3         get player1's vertical position
   sbc currentScanline        ; 3         subtract current scan line
   and #$FE                   ; 2         graphics drawn every other line
   tax                        ; 2
   and player1GraphicMask     ; 3         used to make player blink in Tag
   beq .loadPlayer1Graphics   ; 2
   lda #0                     ; 2
   beq .drawPlayer1Car        ; 3

.loadPlayer1Graphics
   lda carGraphics,x          ; 4
.drawPlayer1Car
   sta WSYNC
;--------------------------------------
   sta GRP0                   ; 3 = @03
   clc                        ; 2
   lda missileVertPos         ; 3         get missile's vertical position
   sbc currentScanline        ; 3         subtract current scan line
   and #$F8                   ; 2         mask D2 - D0
   php                        ; 3         push Z status to stack to
   php                        ; 3         enable/disable M0 and M1
   lda currentScanline        ; 3         get the current scan line
   bpl .determinePFOffset     ; 2³
   eor #$F8                   ; 2
.determinePFOffset
   lsr                        ; 2         divide the value by 8 (each PF byte
   lsr                        ; 2         uses 8 scan lines)
   lsr                        ; 2
   tay                        ; 2         save for PF lookup
   inc player1LSBOffset       ; 5         probably here to waste 5 cycles
   nop                        ; 2
   lda currentScanline        ; 3         get the current scan line
   cmp ballVertPos            ; 3         compare with ball vertical position
   bcc .setupToDrawPlayer2    ; 2³        skip drawing finish line if less
   sta ENABL                  ; 3 = @52   draw finish line every other line
.setupToDrawPlayer2
   lda player2VertPos         ; 3         get player2's vertical position
   sec                        ; 2
   sbc currentScanline        ; 3         subtract current scan line
   ora #1                     ; 2         graphics drawn every other line
   tax                        ; 2
   and player2GraphicMask     ; 3         used to make player blink in Tag
   beq .loadPlayer2Graphics   ; 2³
   lda #0                     ; 2
   beq .drawPlayer2Car        ; 3

.loadPlayer2Graphics
   lda carGraphics,x          ; 4
.drawPlayer2Car
;--------------------------------------
   sta GRP1                   ; 3 = @01
   lda (pf0Pointer),y         ; 5         draw the race track playfield
   sta PF0                    ; 3 = @09
   lda (pf1Pointer),y         ; 5
   sta PF1                    ; 3 = @17
   lda (pf2Pointer),y         ; 5
   sta PF2                    ; 3 = @25
   clc                        ; 2
   lda currentScanline        ; 3
   adc #2                     ; 2         2LK
   sta currentScanline        ; 3
   cmp #H_KERNEL              ; 2
   bcc .gameKernelLoop        ; 2³
   ldx tempStackPointer       ; 3 = @42
   txs                        ; 2         restore stack pointer
   lda #$F0                   ; 2
   sta player1GraphicMask     ; 3         reset player graphic mask values
   sta player2GraphicMask     ; 3
   lda #0                     ; 2
   sta ENAM0                  ; 3 = @57
   sta ENAM1                  ; 3 = @60
   sta ENABL                  ; 3 = @63
   sta GRP0                   ; 3 = @66
   sta GRP1                   ; 3 = @69
   sta GRP0                   ; 3 = @72   VDEL'd
   sta PF2                    ; 3 = @75
;--------------------------------------
   sta PF1                    ; 3 = @02
   sta PF0                    ; 3 = @05

   IF COMPILE_VERSION = NTSC

   lda #%11010010             ; 2
   sta TIM8T                  ; 4 = @11
   sta VBLANK                 ; 3 = @14   disable TIA (D1 = 1)

   ELSE

   ldy #23
.skip24Scanlines
   sta WSYNC
   dey
   bpl .skip24Scanlines
   lda #%11010010
   sta TIM8T
   sta WSYNC

   ENDIF

   rts

ReadConsoleSwitches
   lda SWCHB                        ; read console switches
   lsr                              ; RESET now in carry
   bcs .checkForSelectSwitch
   lda #GAME_RUNNING
   sta gameState
   lda #0
   ldx #32
.clearGameRAM
   sta playerScores,x
   dex
   bpl .clearGameRAM
   lda frameCount                   ; get the current frame count
   and #1                           ; make the frame count between 0 and 1
   sta frameCount
   lda #TIME_TRIAL_SECONDS
   bit gameVariation                ; if this a one player game (Time Trial)
   bvs .skipTimeTrialSecondsSet     ; then don't set time trial seconds
   sta player2Score                 ; set time trial seconds
.skipTimeTrialSecondsSet
   lda #0                           ; not needed must have been left over
   rts

.checkForSelectSwitch
   lsr                              ; SELECT now in carry
   bcs .clearSelectDebounce
   lda selectDebounce               ; get select debounce value
   bne .reduceSelectDebounce        ; skip select if not 0
   lda #SELECT_DELAY
   sta selectDebounce               ; reset select debounce value
   lda gameSelection                ; get the current game selection
   cmp #MAX_GAME_SELECTION-1
   bcc .incrementGameSelection
   lda #-1                          ; set gameSelection to -1 so increment
   sta gameSelection                ; below sets it to 0
.incrementGameSelection
   inc gameSelection
   jsr EndGame
   sta player2Score                 ; clear player 2 score (a = 0)
SetDisplayedGameSelection
   sed
   clc
   lda gameSelection                ; get the current game selection
   tax                              ; move to x to look up game variation
   adc #1                           ; increase the value by 1 (BCD) to show
   sta player1Score                 ; on the screen
   cld
   lda GameVariationTable,x         ; set the game variation based on game
   sta gameVariation                ; selection
   rts

.clearSelectDebounce
   lda #0
   sta selectDebounce
   rts

.reduceSelectDebounce
   dec selectDebounce
   rts

SetGameColors
   ldy #24
   sec                              ; set carry to assume B/W mode
   lda SWCHB                        ; read the console switches
   and #BW_MASK                     ; mask to get B/W switch value
   beq .skipColorMode
   lda gameVariation                ; get the current game variation
   and #MASK_GAME_COLORS            ; mask to get the variation colors
   lsr                              ; divide the value by 2
   tay                              ; for the table offset
.skipColorMode
   ldx #3
.setColorsLoop
   lda GameColorTable,y
   bit gameState
   bvs .skipColorCycling
   eor colorXOR
.skipColorCycling
   bcc .setGameColors               ; set colors if in COLOR mode
   and #$0F                         ; mask the hue
.setGameColors
   sta COLUP0,x                     ; color the players, playfield, and
   dey                              ; background
   dex
   bpl .setColorsLoop
   lda SWCHB                        ; read console switches
   and #SELECT_MASK | RESET_MASK    ; keep the SELECT and RESET values
   eor #SELECT_MASK | RESET_MASK
   bne InitializeGameVariables      ; init game vars if either are pressed
   rts

InitializeGameVariables
   lda RaceTrackPF0LSBTable
   sta pf0Pointer
   lda #>RaceTrackGraphics          ; set the MSB of the race track pointers
   sta pf0Pointer+1
   sta pf1Pointer+1
   sta pf2Pointer+1
   lda gameVariation                ; get the game variation
   and #MASK_TRACK_NUMBER           ; mask values to get track number
   tay
   cmp #DEVIL_ELBOW_TRACK           ; set the PF1 pointers if this is not the
   bne .setTrackPF1Pointer          ; Devil's Elbow track
   lda RaceTrackPF0LSBTable+1       ; set the PF0 pointer for the Devil's
   sta pf0Pointer                   ; Elbow track
.setTrackPF1Pointer
   lda RaceTrackPF1OffsetTable,y
   tax
   lda RaceTrackPF1LSBTable,x
   sta pf1Pointer
   lda RaceTrackPF2LSBTable,y
   sta pf2Pointer
   ldx #6
   ldy #6
   lda gameVariation                ; get current game variation
   and #CRASH_AND_SCORE_TRACK1      ; if this is not a "Crash and Score" or
   beq .initVariablesLoop           ; "Tag" game then set game variables
   ldy #13
.initVariablesLoop
   lda InitializationTable,y
   sta playerHorizPos,x
   dey
   dex
   bpl .initVariablesLoop
   inx                              ; x = 0
   jsr CalculateHorizPosition       ; move player 1 horizontally
   inx
   lda player2HorizPos
   jsr CalculateHorizPosition       ; move player 2 horizontally
   ldx #4
   lda #FINISH_LINE_X
   jsr CalculateHorizPosition       ; move ball (finish line) horizontally
   sta WSYNC                        ; wait for next scan line
   sta HMOVE
   lda gameSelection                ; get the current game selection
   tay
   lda MomentumRateOffsetTable,y
   tay
   ldx #3
.setMomentumRateLoop
   lda MomentumRateTable,y
   sta initAccelerationRate,x
   dey
   dex
   bpl .setMomentumRateLoop
   rts

ReadDrivingControllers
   ldx #1                           ; start with player 2
   lda SWCHB                        ; read the console switches
   sta difficultySwitchValue        ; save to manipulate difficulty settings
   lda SWCHA                        ; read driving controller
   bit gameVariation                ; check for one player (time trial) game
   bvc .readPlayer1Controller
.setDrivingControllerValue
   and #3                           ; keep valid driving controller values
   sta drivingControllerValues,x
   lda INPT4,x                      ; read the joystick button
   bpl .accelerateCar               ; branch if fire button pressed
   lda accelerationRate,x           ; get the player's acceleration rate
   bne .reduceAccelerationRate      ; if not 0 then don't move
   lda playerVelocity,x             ; get the player's velocity
   beq .setPlayerRotation           ; branch if at rest
   dec playerVelocity,x             ; reduce the player's velocity
   lda initDecelerationRate
   sta accelerationRate,x           ; set to show car slowing down
   jsr InitPlayerMotion
   jmp .setPlayerRotation

.accelerateCar
   lda accelerationRate,x           ; don't change player's velocity until
   bne .reduceAccelerationRate      ; acceleration rate reaches 0
   lda maximumVelocity
   bit difficultySwitchValue        ; check the difficulty setting
   bmi .skipSpeedReduction          ; skip speed reduction if EXPERT setting
   sec
   sbc #2                           ; reduce init speed for NOVICE mode
.skipSpeedReduction
   cmp playerVelocity,x
   bcc .setPlayerRotation
   inc playerVelocity,x             ; increase player's velocity
   lda initAccelerationRate         ; reset the acceleration rate
   sta accelerationRate,x
   jsr InitPlayerMotion
.setPlayerRotation
   lda drivingControllerValues,x    ; get driving controller values
   asl                              ; multiply the value by 4 (i.e. shift
   asl                              ; values to D3 and D2)
   ora previousDCValues,x           ; add in the previous DC values
   tay                              ; set for RotationValueTable offset
   lda playerDirections,x           ; get the player's direction
   cmp previousPlayerDirections,x
   bne .readNewDirectionValues
   lda RotationValueTable,y
   sta playerRotation,x
.setPlayerDirections
   clc
   adc playerDirections,x           ; add in direction to manipulate REFLECT
   and #$0F                         ; only keep lower nybbles
   sta playerDirections,x
   tya
   lsr                              ; shift driving controller value back
   lsr                              ; into D1 and D0
   sta previousDCValues,x           ; to save for next frame
   jsr CheckToChangeDirectionPath
.readPlayer1Controller
   asl difficultySwitchValue        ; shift player 1 difficulty setting to D7
   lda SWCHA                        ; read driving controller
   lsr                              ; shift player 1 values to lower nybbles
   lsr
   lsr
   lsr
   dex
   beq .setDrivingControllerValue
   rts

.reduceAccelerationRate
   dec accelerationRate,x
   jmp .setPlayerRotation

.readNewDirectionValues
   lda RotationValueTable,y
   jmp .setPlayerDirections

InitPlayerMotion
   lda playerVelocity,x             ; get the player's velocity
   and #7
   tay
   lda PlayerMotionTable,y
   sta playerMotion,x
   rts

DeterminePlayerMotion
   ldx #1
.determineMotionLoop
   lda playerVelocity,x             ; get the player's velocity
   and #8
   beq .determineMovement
   jsr MoveCars
.determineMovement
   lda playerMotion,x               ; get current motion of player
   sec                              ; set carry for rotation
   bmi .skipCarryClear
   clc
.skipCarryClear
   rol                              ; rotate value left (i.e. D0 <- C <- D7)
   sta playerMotion,x               ; set new motion value
   bcc .nextPlayer                  ; don't move if playerMotion was positive
   jsr MoveCars
.nextPlayer
   dex
   beq .determineMotionLoop
   rts

MoveCars
   inc playerDelay,x
   sta HMCLR                        ; clear all horizontal motion
   lda previousPlayerDirections,x
   sec
   sbc #2
   and #3
   bne LF364
   lda playerDelay,x
   and #3
   beq .leaveSubroutine
LF364:
   lda playerDelay,x
   and #1
   beq LF36C
   lda #$10
LF36C:
   ora previousPlayerDirections,x
   tay
   lda PlayerPixelMovementTable,y
   sta HMP0,x                          ; set player's fine horizontal motion
   and #$0F                            ; mask fine horizontal motion to get
   sec                                 ; vertical motion values
   sbc #VMOVE_0
   sta playerVerticalMotion            ; D7 means the player is moving down
   clc
   adc playerVertPos,x                 ; add value to vertical position
   sta playerVertPos,x                 ; negative value will move player down
   bit playerVerticalMotion            ; check how player is moving vertically
   bmi .playerMovingDown
   cmp #YMAX
   bcc .setPlayerVerticalPos
   lda #YMIN+6
   bne .setPlayerVerticalPos

.playerMovingDown
   cmp #YMIN
   bcs .setPlayerVerticalPos
   lda #YMAX-11
.setPlayerVerticalPos
   sta playerVertPos,x
   sta VDELP0,x
   lda PlayerPixelMovementTable,y
   lsr                              ; move fine horizontal motion to lower
   lsr                              ; nybbles
   lsr
   lsr
   cmp #8
   bcc .setPlayerHorizontalPos
   ora #$F0                         ; make the value negative
   clc                              ; values are -1 <= a <= 1
.setPlayerHorizontalPos
   adc playerHorizPos,x             ; add value to horizontal position
   sta playerHorizPos,x             ; negative value will move player left
   sta WSYNC                        ; wait for next scan line
   sta HMOVE                        ; move all objects horizontally
.leaveSubroutine
   rts

CheckToChangeDirectionPath SUBROUTINE
   lda playerVelocity,x             ; get the player's velocity
   cmp rotationDelay                ; if less than rotationDelay then
   bcc .resetPreviousDirections     ; reset previous directions
   lda changeDirectionTimer,x       ; if not 0 then player not allowed
   bne .reduceChangeDirectionTimer  ; change direction path
   lda playerDirections,x           ; if the player's directions didn't
   cmp previousPlayerDirections,x   ; change then leave
   beq .leaveSubroutine
   lda playerRotation,x
   bmi .reducePreviousDirections
   inc previousPlayerDirections,x
.setPreviousDirections
   lda previousPlayerDirections,x
   and #$0F                         ; only keep lower nybbles
   sta previousPlayerDirections,x
   lda gameSelection                ; get the current game selection
   cmp #ICE_RACE
   lda playerVelocity,x             ; get the player's velocity
   bcs .moveCarAcrossIce
   lsr                              ; divide the value by 2
.setChangeDirectionTimer
   sta changeDirectionTimer,x
.leaveSubroutine
   rts

.moveCarAcrossIce
   asl                              ; multiply player's velocity by 2
   bne .setChangeDirectionTimer
.reducePreviousDirections
   dec previousPlayerDirections,x
   jmp .setPreviousDirections

.resetPreviousDirections
   lda playerDirections,x
   sta previousPlayerDirections,x
   rts

.reduceChangeDirectionTimer
   dec changeDirectionTimer,x
   rts

LoadCarGraphics
   lda frameCount                   ; get the current frame count
   and #1                           ; alternate between player 1 and 2 each
   tax                              ; frame
   lda playerDirections,x           ; get the player's direction
   sta REFP0,x                      ; set the player's reflect state
   asl                              ; multiply the value by 8 (i.e number
   asl                              ; of rotations)
   asl
   cmp #MAX_CAR_SPRITES-1
   clc
   bmi .setCarGraphicsOffset
   sec
   eor #$47
.setCarGraphicsOffset
   tay
   stx temp                         ; not needed as x is never restored
   txa
   eor #$0E
   tax
.loadGraphicsLoop
   txa
   and #1
   beq .readCarGraphics
   bit gameVariation                ; if this is not a time trial game then
   bvs .readCarGraphics             ; read the graphics from ROM
   lda #0                           ; clear the second player car
   beq .setCarGraphics

.readCarGraphics
   lda CarGraphics,y
.setCarGraphics
   sta carGraphics,x
   bcc .readNextByte
   dey                              ; compensate for offset being out of
   dey                              ; range
.readNextByte
   iny
   dex
   dex
   bpl .loadGraphicsLoop
   rts

CheckForCompletedLaps SUBROUTINE
   lda frameCount                   ; get the current frame count
   and #1                           ; check player collisions on alternating
   tax                              ; frames
   lda playfieldBallCollisions,x    ; read saved collisions
   asl                              ; shift player/ball collisions to D7
   bpl SavePlayerBallCollisions     ; if ball not save current collision
   lda playerVertPos,x              ; get the player's vertical position
   cmp #128
   lda #0
   bcs .setVerticalCheckPoint
   lda #1
.setVerticalCheckPoint
   sta temp
   lda playerHorizPos,x             ; get the player's horizontal position
   cmp #205
   lda temp
   bcc .setLapCheckPoint
   ora #2
.setLapCheckPoint
   tay
   lda LapCheckPointTable,y
   ora lapCheckPoints,x             ; add in the check point value
   sta lapCheckPoints,x
   cmp #15                          ; if player didn't make full lap around
   bne .leaveSubroutine             ; then leave
   lda CXP0FB,x                     ; read collision register
   and #$40                         ; get player ball collision
   beq .setBallCollision            ; set ball collision if ball not hit
   lda playfieldBallCollisions,x
   bmi .leaveSubroutine             ; leave if the player hit playfield
   lda #%11000110
   sta playfieldBallCollisions,x
   sta playerHorizPos,x
   lda #0
   sta lapCheckPoints,x             ; clear the check point flags
   jsr IncrementScore
   cmp #MAX_RACE_LAPS
   beq EndGame
.leaveSubroutine
   rts

SavePlayerBallCollisions
   lda CXP0FB,x                     ; read collision register
   and #$40                         ; get player ball collision
   sta playfieldBallCollisions,x
   rts

.setBallCollision
   lda #$40
   sta playfieldBallCollisions,x
   rts

EndGame
   lda #0
   sta missileVertPos
SetVariablesToEndGame
   sta gameState                    ; set game state to GAME_OVER
   sta AUDV0                        ; turn off all sounds
   sta AUDV1
   sta colorXOR                     ; clear colorXOR
   sta frameCount                   ; clear frameCount
   rts

IncrementScore
   lda #0
   sta colorXOR                     ; clear colorXOR
   sed
   clc
   lda playerScores,x               ; get the player's score
   adc #$01                         ; increment the score by 1
.setPlayerScore
   sta playerScores,x
   cld                              ; clear decimal mode
   rts

ReduceTimeTrialTimer
   sed                              ; set to decimal mode
   lda player2Score                 ; get the time trial timer
   sec
   sbc #TIME_TRIAL_REDUCTION        ; reduce the timer
   jmp .setPlayerScore

CheckIfPlayerCaughtBox SUBROUTINE
   sta HMCLR                        ; clear all horizontal movements
   lda lapCheckPoints
   beq .setMissileInitPosition
   lda frameCount                   ; get the current frame count
   and #1                           ; alternate between player 1 and 2 each
   tax                              ; frame
   lda CXM0FB,x                     ; read the missile/playfield collisions
   bmi .calculateNewRandomPosition  ; branch if the missile hit the playfield
   lda playerFrameWait,x
   bne .reducePlayersWaitTime
   lda CXM0P,x                      ; read the player/missile collisions
   asl                              ; move player/missile collision to D7
   bpl .leaveSubroutine
   jsr PlayerCaughtBox
.leaveSubroutine
   rts

PlayerCaughtBox
   lda #8
   sta playerFrameWait,x
   jsr IncrementScore
   cmp #MAX_CRASH_AND_SCORE
   beq .endGame
   lda frameCount
CalculateNewMissilePosition
   and #$7F                         ; make the value stay lower than 127
   adc #64                          ; increment the value by 64 (a <= 192)
   sta missileVertPos               ; set the missile's new vertical position
   eor carGraphics,x                ; XOR with player graphics for randomness
   and #$7F                         ; make the value stay loser than 127
   adc #16                          ; increment the value by 16 (a <= 143)
MoveMissilesHorizontally
   sta missileHorizPos
   sta lapCheckPoints
   ldx #2                           ; set missile 0 offset
   pha                              ; save value to stack
   jsr CalculateHorizPosition       ; position missile 0 horizontally
   inx
   pla                              ; pull value off the stack
   jsr CalculateHorizPosition       ; position missile 1 horizontally
   sta WSYNC                        ; wait for next scan line
   sta HMOVE
   rts

.calculateNewRandomPosition
   lda frameCount                   ; get the current frame count
   adc #10                          ; increment by 10
   jmp CalculateNewMissilePosition

.reducePlayersWaitTime
   dec playerFrameWait,x
   rts

.endGame
   lda #0
   jsr SetVariablesToEndGame
   rts

.setMissileInitPosition
   lda #132
   sta missileVertPos
   lda #86
   bne MoveMissilesHorizontally

PlayGameSounds
   lda frameCount                   ; get the current frame count
   and #1                           ; alternate between player 1 and 2 each
   tax                              ; frame
   stx temp
   lda volumeChannelValues,x        ; get the volume values
   bne LF533                        ; branch if not reached 0
   lda playerFrameWait,x
   bne LF52F
   lda playerVelocity,x             ; get the player's velocity
   lsr                              ; divide the value by 2
   tay
   bne .resetAccelerationSoundTimer
   lda accelerationSoundTimer,x
   beq .setIdleEngineSound
   dec accelerationSoundTimer,x
   jmp .setVolume

.setIdleEngineSound
   ldy #6
   bne .setVolume

.resetAccelerationSoundTimer
   lda #63
   sta accelerationSoundTimer,x
.setVolume
   lda AudioVolumeTable,y
   sta AUDV0,x
.setFrequencyAndChannels
   lda AudioFrequencyTable,y
   ora temp
   sta AUDF0,x
   lda AudioChannelTable,y
   sta AUDC0,x
   rts

LF52F:
   ldy #7
   bne .setVolume

LF533:
   ldy #8
   bne .setFrequencyAndChannels

CalculateHorizPosition
   clc
   adc #49
   pha                              ; push value to stack for later
   lsr                              ; shift top nybble to lower nybble
   lsr
   lsr
   lsr
   tay                              ; save the value
   pla                              ; get the object's x position
   and #$0F                         ; mask upper nybble
   sty temp                         ; save coarse value for later
   clc
   adc temp                         ; add in coarse value (A = C + F)
   cmp #14
   bcc .skipSubtractions
   sec
   sbc #14                          ; subtract 14
   iny                              ; and increment coarse value
.skipSubtractions
   cmp #8                           ; make sure hasn't gone pass min x value
   eor #$0F
   bcs .skipFineIncrement
   adc #1                           ; increment fine motion value
   dey                              ; reduce coarse value
.skipFineIncrement
   iny                              ; increment coarse value
   asl                              ; move fine motion value to upper nybble
   asl
   asl
   asl
   sty WSYNC                        ; wait for next scan line
.coarseMoveLoop
   dey
   bne .coarseMoveLoop
   sta RESP0,x                      ; set object's coarse position
   sta HMP0,x                       ; set object's fine motion
   rts

CheckIfPlayerHitTrack SUBROUTINE
   lda frameCount                   ; get the current frame count
   and #1                           ; alternate between player 1 and 2 each
   tax                              ; frame
   lda CXP0FB,x                     ; read collision register
   bpl .clearPlayfieldHitFlag       ; branch if didn't hit playfield
   lda gameVariation                ; get current game variation
   and #CRASH_AND_SCORE_TRACK1      ; if this is a "Crash and Score" or
   bne .slowCarDown                 ; "Tag" game then branch
   lda hitPlayfieldFlag,x           ; get the playfield hit state
   bne .slowCarDown
   inc hitPlayfieldFlag,x           ; increase playfield hit state
   lda #0
   sta playerVelocity,x             ; stop player's car
   jsr InitPlayerMotion
   lda #15
   sta volumeChannelValues,x        ; set audio volume to the max
   sta AUDV0,x
.leaveSubroutine
   rts

.clearPlayfieldHitFlag
   lda #0
   sta hitPlayfieldFlag,x
   rts

.slowCarDown
   lda #2
   cmp playerVelocity,x
   bcs .leaveSubroutine
   lsr playerVelocity,x             ; divide player velocity by 2
   jsr InitPlayerMotion
   rts

CheckForCarCollision SUBROUTINE
   lda CXPPMM                       ; read collision register
   bpl .clearCarCollisionFlag       ; branch if cars didn't collide
   lda carCollisionFlag             ; collsion recorded last frame
   bne .leaveSubroutine
   inc carCollisionFlag
   lda frameCount                   ; get the current frame count
   and #1                           ; alternate between player 1 and 2 each
   tax                              ; frame
   inc playerDirections,x           ; change the player's direction
   inc previousPlayerDirections,x
   lsr playerVelocity,x             ; divide player's velocity by 2
   jsr InitPlayerMotion
   txa                              ; move player index to a
   eor #1                           ; EOR with 1 to change to other player
   tax
   dec playerDirections,x           ; change player's direction
   dec previousPlayerDirections,x
   lsr playerVelocity,x             ; divide player's velocity by 2
   jsr InitPlayerMotion
   lda #15
   sta volumeChannel1               ; set volume registers to the max
   sta volumeChannel2
   sta AUDV0
   sta AUDV1
.leaveSubroutine
   rts

.clearCarCollisionFlag
   lda #0
   sta carCollisionFlag
   rts

CheckTagCollisions SUBROUTINE
   lda player1FrameWait
   ora player2FrameWait
   bne .waitTimeNotDone
   lda CXPPMM                       ; read collision register
   bmi .playerTagged                ; branch if cars collided
   lda frameCount                   ; increment the player's score ~ every 60
   and #$3F                         ; seconds
   bne .leaveSubroutine
   ldx tagChasing                   ; get the car number being chased
   jsr IncrementScore               ; increment score until car is tagged
   cmp #MAX_TAG_SCORE
   beq .endGame
.leaveSubroutine
   rts

.endGame
   jsr EndGame
   rts

.waitTimeNotDone
   lda #0
   ldx lastTagChaser                ; get the last chaser
   sta playerVelocity,x             ; stop player's car
   jsr InitPlayerMotion
   dec playerFrameWait,x            ; reduce the player's wait time
   rts

.playerTagged
   lda tagChasing                   ; get the player number being chased
   sta lastTagChaser                ; save as last chaser
   tax
   eor #1                           ; make other player the chased
   sta tagChasing
   lda #$3F                         ; set tagged player to be able move
   sta playerFrameWait,x            ; again in ~60 seconds
   rts

DoneGameCalculations
   lda frameCount                   ; get the current frame count
   and #1                           ; alternate between player 1 and 2 each
   tax                              ; frame
   lda volumeChannelValues,x
   beq CalculateScoreOffsets        ; skip volume reduction if already 0
   sec
   sbc #1                           ; reduce volumn
   sta AUDV0,x
   sta volumeChannelValues,x
CalculateScoreOffsets
   ldx #1
.scoreOffsetLoop
   lda playerScores,x               ; get the player's score
   and #$0F                         ; mask off the upper nybbles
   sta temp                         ; save the value for later
   asl                              ; shift the value left to multiply by 4
   asl
   clc                              ; add in original so it's multiplied by 5
   adc temp                         ; [i.e. x * 5 = (x * 4) + x]
   sta lsbScoreOffsets,x
   lda playerScores,x
   and #$F0                         ; mask off the lower nybbles
   lsr                              ; divide the value by 4
   lsr
   sta temp                         ; save the value for later
   lsr                              ; divide the value by 16
   lsr
   clc                              ; add in original so it's multiplied by
   adc temp                         ; 5/16 [i.e. 5x/16 = (x / 16) + (x / 4)]
   sta msbScoreOffsets,x
   dex
   beq .scoreOffsetLoop
   rts

NumberFonts
zero
   .byte $0E ; |....XXX.|
   .byte $0A ; |....X.X.|
   .byte $0A ; |....X.X.|
   .byte $0A ; |....X.X.|
   .byte $0E ; |....XXX.|
one
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
two
   .byte $EE ; |XXX.XXX.|
   .byte $22 ; |..X...X.|
   .byte $EE ; |XXX.XXX.|
   .byte $88 ; |X...X...|
   .byte $EE ; |XXX.XXX.|
three
   .byte $EE ; |XXX.XXX.|
   .byte $22 ; |..X...X.|
   .byte $66 ; |.XX..XX.|
   .byte $22 ; |..X...X.|
   .byte $EE ; |XXX.XXX.|
four
   .byte $AA ; |X.X.X.X.|
   .byte $AA ; |X.X.X.X.|
   .byte $EE ; |XXX.XXX.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
five
   .byte $EE ; |XXX.XXX.|
   .byte $88 ; |X...X...|
   .byte $EE ; |XXX.XXX.|
   .byte $22 ; |..X...X.|
   .byte $EE ; |XXX.XXX.|
six
   .byte $EE ; |XXX.XXX.|
   .byte $88 ; |X...X...|
   .byte $EE ; |XXX XXX.|
   .byte $AA ; |X.X.X.X.|
   .byte $EE ; |XXX.XXX.|
seven
   .byte $EE ; |XXX.XXX.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
   .byte $22 ; |..X...X.|
eight
   .byte $EE ; |XXX.XXX.|
   .byte $AA ; |X.X.X.X.|
   .byte $EE ; |XXX.XXX.|
   .byte $AA ; |X.X.X.X.|
   .byte $EE ; |XXX.XXX.|
nine
   .byte $EE ; |XXX.XXX.|
   .byte $AA ; |X.X.X.X.|
   .byte $EE ; |XXX.XXX.|
   .byte $22 ; |..X...X.|
   .byte $EE ; |XXX.XXX.|

GameVariationTable
   .byte %01001000   ;Grand Prix Track
   .byte %00001000   ;Grand Prix Track (time trial)
   .byte %11010001   ;Devil's Elbow Track
   .byte %10010001   ;Devil's Elbow Track (time trial)
; crash and score
   .byte %01011010   ;I Track
   .byte %00011010   ;I Track
   .byte %11011011   ;II Track
   .byte %10011011   ;II Track
; tag
   .byte %01100010   ;Barrier Chase Track
   .byte %11100011   ;Motor Hunt Track
; ice race
   .byte %01101100   ;Sprint Race Track
   .byte %00101100   ;Sprint Race Track (time trial)
   .byte %11101000   ;Rally Track
   .byte %10101000   ;Rally Track (time trial)

PlayerPixelMovementTable
   .byte HMOVE_R1|VMOVE_0
   .byte HMOVE_R1|VMOVE_D1
   .byte HMOVE_R1|VMOVE_D2
   .byte HMOVE_0 |VMOVE_D2
   .byte HMOVE_0 |VMOVE_D2
   .byte HMOVE_0 |VMOVE_D2
   .byte HMOVE_L1|VMOVE_D2
   .byte HMOVE_L1|VMOVE_D1

   .byte HMOVE_L1|VMOVE_0
   .byte HMOVE_L1|VMOVE_U1
   .byte HMOVE_L1|VMOVE_U2
   .byte HMOVE_0 |VMOVE_U2
   .byte HMOVE_0 |VMOVE_U2
   .byte HMOVE_0 |VMOVE_U2
   .byte HMOVE_R1|VMOVE_U2
   .byte HMOVE_R1|VMOVE_U1

   .byte HMOVE_R1|VMOVE_0
   .byte HMOVE_R1|VMOVE_D1
   .byte HMOVE_R1|VMOVE_D2
   .byte HMOVE_R1|VMOVE_D2
   .byte HMOVE_0 |VMOVE_D2
   .byte HMOVE_L1|VMOVE_D2
   .byte HMOVE_L1|VMOVE_D2
   .byte HMOVE_L1|VMOVE_D1

   .byte HMOVE_L1|VMOVE_0
   .byte HMOVE_L1|VMOVE_U1
   .byte HMOVE_L1|VMOVE_U2
   .byte HMOVE_L1|VMOVE_U2
   .byte HMOVE_0 |VMOVE_U2
   .byte HMOVE_R1|VMOVE_U2
   .byte HMOVE_R1|VMOVE_U2
   .byte HMOVE_R1|VMOVE_U1

PlayerMotionTable
   .byte $00,$01,$11,$25,$55,$DA,$EE,$EF

RaceTrackPF1OffsetTable
   .byte 0,3,0,2,1

LapCheckPointTable
   .byte 8,1,4,2

RaceTrackPF0LSBTable
   .byte <RaceTrackGraphics-4,<RaceTrackGraphics-3

RaceTrackPF1LSBTable
   .byte <GrandPrixPF1Graphics,<SprintTrackPF1Graphics
   .byte <CrashAndScorePF1Graphics_2,<DevilElbowPF1Graphics

RaceTrackPF2LSBTable
   .byte <GrandPrixPF2Graphics,<DevilElbowPF2Graphics
   .byte <CrashAndScorePF2Graphics_1,<CrashAndScorePF2Graphics_2
   .byte <SprintTrackPF2Graphics

MomentumRateOffsetTable
   .byte 3,3,11,11,3,3,11
   .byte 11,3,11,7,7,15,15

AudioFrequencyTable
   .byte $0E,$08,$06,$04,$1A,$18,$0F,$00,$37

AudioChannelTable
   .byte $02,$02,$02,$02,$07,$07,$02,$0A,$08

AudioVolumeTable
   .byte $0C,$0C,$0C,$09,$06,$08,$06,$0A

CarGraphics
   .byte $EE ; |XXX.XXX.|
   .byte $EE ; |XXX.XXX.|
   .byte $44 ; |.X...X..|
   .byte $7F ; |.XXXXXXX|
   .byte $7F ; |.XXXXXXX|
   .byte $44 ; |.X...X..|
   .byte $EE ; |XXX.XXX.|
   .byte $EE ; |XXX.XXX.|

   .byte $18 ; |...XX...|
   .byte $D8 ; |XX.XX...|
   .byte $CB ; |XX..X.XX|
   .byte $5E ; |.X.XXXX.|
   .byte $7E ; |.XXXXXX.|
   .byte $64 ; |.XX..X..|
   .byte $36 ; |..XX.XX.|
   .byte $36 ; |..XX.XX.|

   .byte $30 ; |..XX....|
   .byte $32 ; |..XX..X.|
   .byte $CC ; |XX..XX..|
   .byte $DC ; |XX.XXX..|
   .byte $3B ; |..XXX.XX|
   .byte $33 ; |..XX..XX|
   .byte $0C ; |....XX..|
   .byte $0C ; |....XX..|

   .byte $04 ; |.....X..|
   .byte $CC ; |XX..XX..|
   .byte $F8 ; |XXXXX...|
   .byte $1F ; |...XXXXX|
   .byte $DB ; |XX.XX.XX|
   .byte $F0 ; |XXXX....|
   .byte $3E ; |..XXXXX.|
   .byte $06 ; |.....XX.|

   .byte $18 ; |...XX...|
   .byte $DB ; |XX.XX.XX|
   .byte $FF ; |XXXXXXXX|
   .byte $DB ; |XX.XX.XX|
   .byte $18 ; |...XX...|
   .byte $DB ; |XX.XX.XX|
   .byte $FF ; |XXXXXXXX|
   .byte $C3 ; |XX....XX|

   .byte $20 ; |..X.....|
   .byte $33 ; |..XX..XX|
   .byte $1F ; |...XXXXX|
   .byte $F8 ; |XXXXX...|
   .byte $DB ; |XX.XX.XX|
   .byte $0F ; |....XXXX|
   .byte $7C ; |.XXXXX..|
   .byte $60 ; |.XX.....|

   .byte $0C ; |....XX..|
   .byte $4C ; |.X..XX..|
   .byte $33 ; |..XX..XX|
   .byte $3B ; |..XXX.XX|
   .byte $DC ; |XX.XXX..|
   .byte $CC ; |XX..XX..|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|

   .byte $18 ; |...XX...|
   .byte $1B ; |...XX.XX|
   .byte $D3 ; |XX.X..XX|
   .byte $7A ; |.XXXX.X.|
   .byte $3E ; |..XXXXX.|
   .byte $26 ; |..X..XX.|
   .byte $6C ; |.XX.XX..|
   .byte $6C ; |.XX.XX..|

RaceTrackGraphics
   .byte $F0 ; |XXXX....|
   .byte $F0 ; |XXXX....|
   .byte $70 ; |.XXX....|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
GrandPrixPF1Graphics
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
   .byte $30 ; |..XX....|
   .byte $F0 ; |XXXX....|
   .byte $FF ; |XXXXXXXX|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $01 ; |.......X|
SprintTrackPF1Graphics
   .byte $01 ; |.......X|
   .byte $01 ; |.......X|
   .byte $01 ; |.......X|
   .byte $01 ; |.......X|
   .byte $FF ; |XXXXXXXX|
   .byte $00 ; |........|
CrashAndScorePF1Graphics_2
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $03 ; |......XX|
   .byte $03 ; |......XX|
DevilElbowPF1Graphics
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $FF ; |XXXXXXXX|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $03 ; |......XX|
GrandPrixPF2Graphics
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $FF ; |XXXXXXXX|
   .byte $E0 ; |XXX.....|
   .byte $C0 ; |XX......|
   .byte $80 ; |X.......|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $01 ; |.......X|
DevilElbowPF2Graphics
   .byte $03 ; |......XX|
   .byte $07 ; |.....XXX|
   .byte $FF ; |XXXXXXXX|
   .byte $FF ; |XXXXXXXX|
   .byte $FF ; |XXXXXXXX|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $FF ; |XXXXXXXX|
   .byte $E0 ; |XXX.....|
CrashAndScorePF2Graphics_1
   .byte $C0 ; |XX......|
   .byte $80 ; |X.......|
   .byte $80 ; |X.......|
   .byte $80 ; |X.......|
   .byte $F0 ; |XXXX....|
   .byte $00 ; |........|
CrashAndScorePF2Graphics_2
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $80 ; |X.......|
   .byte $80 ; |X.......|
SprintTrackPF2Graphics
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $FF ; |XXXXXXXX|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $00 ; |........|
   .byte $07 ; |.....XXX|
   .byte $FF ; |XXXXXXXX|

InitializationTable
   .byte PLAYER1_STARTX1,PLAYER2_STARTX1
   .byte PLAYER1_STARTY1,PLAYER2_STARTY1
   .byte PLAYER1_START_DIR1,PLAYER2_START_DIR2
   .byte BALL_STARTY1

   .byte PLAYER1_STARTX2,PLAYER2_STARTX2
   .byte PLAYER1_STARTY2,PLAYER2_STARTY2
   .byte PLAYER1_START_DIR2,PLAYER2_START_DIR2
   .byte BALL_STARTY2

MomentumRateTable
   .byte 15,7,8,5
   .byte 31,15,8,3
   .byte 8,4,10,15
   .byte 24,10,8,3

RotationValueTable
   .byte $00,$FF,$01,$00,$01,$00,$00,$FF,$FF,$00,$00,$01,$00,$01,$FF

GameColorTable
   .byte BLACK,GREEN+10,BRICK_RED+10,ORANGE+7
   .byte DK_BLUE+4,PURPLE+10,LT_BLUE+8,BRICK_RED+6
   .byte BROWN+4,DK_BLUE+10,BROWN+8,ORANGE+10
   .byte BRICK_RED+3,BROWN+10,LT_BLUE+10
   .byte RED+6
   .byte BLACK,YELLOW,BLUE_PURPLE+6,LT_BLUE+8
; Black and White colors
   .byte BLACK+9,WHITE+1,BLACK,BLACK+8,BLACK+10

   IF COMPILE_VERSION = NTSC
;
; The following bytes are not used. Maybe they were left over from another
; game. It does look like something out of Breakout.
;
   .byte $11 ;|...X...X|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $05 ;|.....X.X|
   .byte $22 ;|..X...X.|
   .byte $44 ;|.X...X..|
   .byte $11 ;|...X...X|
   .byte $11 ;|...X...X|
   .byte $11 ;|...X...X|
   .byte $55 ;|.X.X.X.X|
   .byte $11 ;|...X...X|
   .byte $55 ;|.X.X.X.X|
   .byte $11 ;|...X...X|
   .byte $05 ;|.X.X.X.X|
   .byte $22 ;|..X...X.|
   .byte $77 ;|.XXX.XXX|
   .byte $33 ;|..XX..XX|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $11 ;|...X...X|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $05 ;|.....X.X|
   .byte $22 ;|..X...X.|
   .byte $11 ;|...X...X|
   .byte $11 ;|...X...X|
   .byte $55 ;|.X.X.X.X|
   .byte $44 ;|.X...X..|
   .byte $44 ;|.X...X..|
   .byte $11 ;|...X...X|
   .byte $55 ;|.X.X.X.X|
   .byte $55 ;|.X.X.X.X|
   .byte $07 ;|.....XXX|
   .byte $22 ;|..X...X.|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $55 ;|.X.X.X.X|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|
   .byte $77 ;|.XXX.XXX|

   ENDIF

   .org ROM_BASE_ADDRESS + 2048 - 4, 0 ; 2K ROM
   .word Start                      ; RESET vector
   .word Start                      ; BRK vector