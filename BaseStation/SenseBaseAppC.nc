#include "../Node/Sense.h"

configuration SenseBaseAppC {}

implementation {
    components MainC, LedsC, SenseBaseC as App;
    components ActiveMessageC as AM;
    components SerialActiveMessageC as SerialAM;
    components new TimerMilliC() as Timer;
    components CollectionC;
	components DelugeC;
    
    App.RadioControl -> AM;
    App.SerialControl -> SerialAM;
    App.CollectionControl -> CollectionC;
    App.RootControl -> CollectionC;
    App.Leds -> LedsC;
    App.Boot -> MainC;
    App.AMSend -> SerialAM.AMSend[AM_SENSEMSG];
    App.Receive -> CollectionC.Receive[AM_SENSEMSG];
    App.Packet -> SerialAM;
}
