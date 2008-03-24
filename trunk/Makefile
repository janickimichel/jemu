JAVAC = javac -O -d classes/ -cp classes -g -cp $(CLASSPATH)

#
# General
#
all: \
	jemu/JEmu.java \
	jemu/CPU.java  \
	jemu/Device.java \
	jemu/Video.java \
	jemu/BreakPoints.java \
	jemu/MemoryMaps.java \
	\
	platform/Atari2600.java \
	\
	cpu/MOS6502.java \
	video/TIA1A.java \
	devices/PIA6532.java
	$(JAVAC) $^

#
# Clean
#
clean:
	cmd /C del /q classes\\*.class
