#include "../Node/Sense.h"

module SenseBaseC {
    uses {
        interface SplitControl as SerialControl;
        interface SplitControl as RadioControl;
        interface StdControl as CollectionControl;
        interface RootControl;
        interface Leds;
        interface Boot;
        interface AMSend;
        interface Receive;
        interface Packet;
    }
}

implementation {
    bool busy = FALSE;
    message_t msg;  
    SenseMsg* packet;
    
    event void Boot.booted() {
        call RadioControl.start();
    }
    
    event void RadioControl.startDone(error_t err) {
        if (err == SUCCESS) {
            call SerialControl.start();
        } else
            call RadioControl.start();
    }
    
    event void RadioControl.stopDone(error_t err) {/* NOT IMPLEMENTED */} 
    
    event void SerialControl.startDone(error_t err) {
        if (err == SUCCESS){
        	call CollectionControl.start();
            call RootControl.setRoot();
            call Leds.led0On();
        }    
        else 
            call SerialControl.start();
    }
    
    event void SerialControl.stopDone(error_t err) {/* NOT IMPLEMENTED */}
    
    event message_t* Receive.receive(message_t *bufmsg, void *payload, uint8_t len) {
        SenseMsg* newpkt = (SenseMsg*) payload;
        if (len == sizeof(SenseMsg) && !busy) {
            //call Leds.led1Toggle();
			call Leds.led1On();
            packet = (SenseMsg*) call Packet.getPayload(&msg, sizeof(SenseMsg));
            *packet = *newpkt;
            if (call AMSend.send(AM_BROADCAST_ADDR, &msg, sizeof(SenseMsg)) == SUCCESS) {
                busy = TRUE;
				call Leds.led2On();
			}
        }
        return bufmsg;
    }
    
    event void AMSend.sendDone(message_t* bufPtr, error_t error) {
        if (error == SUCCESS){
        	//call Leds.led2Toggle();
            busy = FALSE;
			call Leds.led2Off();
			call Leds.led1Off();
        }
  }
}

