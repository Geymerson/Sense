#include "Sense.h"
#include <string.h>
#include <Timer.h>

module SenseC {
	uses {
		interface Boot;
		interface SplitControl as Control;
		interface StdControl as CollectionControl;
		interface Send;
		interface Leds;
		interface Timer<TMilli>;
		interface Intersema;
		interface Read<uint16_t> as X_Axis;
		interface Read<uint16_t> as Y_Axis;
		interface Read<uint16_t> as Temperature;
		interface Read<uint16_t> as Humidity;
		interface Read<uint16_t> as Voltage;
		interface Read<uint8_t> as VisibleLight;
		interface Read<uint8_t> as InfraredLight;
	}
}

implementation {
	message_t message;
	bool busy = FALSE;
	uint8_t clockCounter = 1;
	uint16_t AccelX_data, AccelY_data, Temp_data, Hum_data, VisLight_data, Voltage_data;
	int16_t Intersema_data[2];
	
	event void Boot.booted() {
        call Control.start();
    }
    
    event void Control.startDone(error_t err) {
        if (err == SUCCESS) {
            call Timer.startPeriodic(TIMER_PERIOD_MILLI);
            call CollectionControl.start();
            call Leds.led0On();
        } else
            call Control.start();
    }
    
    event void Control.stopDone(error_t err) {/* NOT IMPLEMENTED */}
    
    event void Timer.fired() {
		if(clockCounter >= PERIODICITY_MULTIPLIER) {
			call Voltage.read();
			clockCounter = 1;
		}
		else {
			clockCounter++;
		}
    }
    
    event void Voltage.readDone(error_t err, uint16_t data) {
    	Voltage_data = data;
    	call X_Axis.read();
    }
    
    event void X_Axis.readDone(error_t err, uint16_t data){
		AccelX_data = data;
		call Y_Axis.read();
	}
	
	event void Y_Axis.readDone(error_t err, uint16_t data){
		AccelY_data = data;
		call Intersema.read();
	}
	
	event void Intersema.readDone(error_t err, int16_t* data){
		Intersema_data[0] = data[0];
		Intersema_data[1] = data[1];
		call Temperature.read();
	}
	
	event void Temperature.readDone(error_t err, uint16_t data){
		Temp_data = data;
		call Humidity.read();
	}
	
	event void Humidity.readDone(error_t err, uint16_t data){
		Hum_data = data;
		call VisibleLight.read();
	}
	
	event void VisibleLight.readDone(error_t err, uint8_t data){
		VisLight_data = data;
		call InfraredLight.read();
	}
	
	event void InfraredLight.readDone(error_t err, uint8_t data){
		SenseMsg* packet = (SenseMsg*)(call Send.getPayload(&message, sizeof(SenseMsg)));
		packet->nodeid = TOS_NODE_ID;
		packet->Voltage_data = Voltage_data;
		packet->AccelX_data = AccelX_data;
		packet->AccelY_data = AccelY_data;
		packet->Intersema_data[0] = Intersema_data[0];
		packet->Intersema_data[1] = Intersema_data[1];
		packet->Temp_data =Temp_data;
		packet->Hum_data = Hum_data;
		packet->VisLight_data = VisLight_data;
		packet->InfLight_data = data;
		if(!busy) {
			if (call Send.send(&message, sizeof(SenseMsg)) == SUCCESS) {
				busy = TRUE;
				call Leds.led2On();
			}
		}
	}
    
    event void Send.sendDone(message_t* bufPtr, error_t error) {
        if (error == SUCCESS){
            //call Leds.led2Toggle();
            busy = FALSE;
			call Leds.led2Off();
        }
  	}
}
