#ifndef SENSE_H
#define SENSE_H

enum {
    TIMER_PERIOD_MILLI = 20500,
	PERIODICITY_MULTIPLIER = 1,
    AM_SENSEMSG = 1
};

typedef nx_struct senseMsg {
    nx_uint16_t nodeid;
	nx_uint16_t Voltage_data;
	nx_uint16_t AccelX_data;
	nx_uint16_t AccelY_data;
	nx_int16_t Intersema_data[2];
	nx_uint16_t Temp_data;
	nx_uint16_t Hum_data;
	nx_uint16_t VisLight_data;
	nx_uint16_t InfLight_data;
} SenseMsg;

#endif
