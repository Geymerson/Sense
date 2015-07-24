COMPONENT=SenseAppC
SENSORBOARD=mts400
BOOTLOADER=tosboot

CLEAN_EXTRA=*.class SenseMsg.java
CFLAGS += -I$(TOSDIR)/lib/net \
          -I$(TOSDIR)/lib/net/le \
          -I$(TOSDIR)/lib/net/ctp
BUILD_EXTRA_DEPS += SenseMsg.class

SenseMsg.class: SenseMsg.java
	javac SenseMsg.java

SenseMsg.java:
	mig java -target=micaz -java-classname=SenseMsg Sense.h senseMsg -o $@

include $(MAKERULES)
