package io.rhiot.component.deviceio.i2c.driver;

public class BMP180Constants {

    // Device data
    public static final short BMP085_BUSID = 1;
    public static final short BMP085_DEVICEADDR = 0x77;

    public static final short BMP085_DEVICE_ID_ADDR = 0xD0;
    public static final short BMP085_DEVICE_ID = 0x55;

    // Calibration data
    public static final short BMP085_CONTROL = 0xF4;
    public static final byte BMP085_READPRESSURECMD = 0x34;
    public static final byte BMP085_READTEMPCMD = 0x2E;
    public static final short BMP085_ADCDATA = 0xF6;

    public static final int BMP085_CALIBRATION_START = 0xAA;
    public static final int BMP085_CALIBRATION_END = 0xBF;

    public static final int BMP085_CALIBRATION_AC1 = 0xAA;
    public static final int BMP085_CALIBRATION_AC2 = 0xAC;
    public static final int BMP085_CALIBRATION_AC3 = 0xAE;
    public static final int BMP085_CALIBRATION_AC4 = 0xB0;
    public static final int BMP085_CALIBRATION_AC5 = 0xB2;
    public static final int BMP085_CALIBRATION_AC6 = 0xB4;
    public static final int BMP085_CALIBRATION_B1 = 0xB6;
    public static final int BMP085_CALIBRATION_B2 = 0xB8;
    public static final int BMP085_CALIBRATION_MB = 0xBA;
    public static final int BMP085_CALIBRATION_MC = 0xBC;
    public static final int BMP085_CALIBRATION_MD = 0xBE;

}
