/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Bertrand Martel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.android.bluetooth.parrot;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import fr.bmartel.android.bluetooth.ICharacteristicListener;
import fr.bmartel.android.bluetooth.IDeviceInitListener;
import fr.bmartel.android.bluetooth.connection.BluetoothDeviceAbstr;
import fr.bmartel.android.bluetooth.connection.IBluetoothDeviceConn;
import fr.bmartel.android.bluetooth.parrot.history.HistoricFrames;
import fr.bmartel.android.bluetooth.parrot.history.HistoricStates;
import fr.bmartel.android.bluetooth.parrot.history.HistoricUtils;
import fr.bmartel.android.bluetooth.parrot.history.IHistoricFrame;
import fr.bmartel.android.bluetooth.parrot.history.RxStatus;
import fr.bmartel.android.bluetooth.parrot.history.TxStatus;
import fr.bmartel.android.utils.ByteUtils;

/**
 * Flower Power Bluetooth device management
 *
 * @author Bertrand Martel
 */
public class FlowerPowerDevice extends BluetoothDeviceAbstr implements IFlowerPowerDevice {

    private String TAG = FlowerPowerDevice.class.getName();

    /**
     * live measurement period in seconds
     */
    private int LIVE_MEASURE_PERIOD_DEFAULT = 15;

    private int entriesCount = 0;
    private int lastEntry = 0;

    private HistoricStates historicState = HistoricStates.NONE;
    private ArrayList<IHistoricFrame> historicFrameList = new ArrayList<>();
    private ArrayList<IHistoricFrame> tempHistoricFrameList = new ArrayList<>();

    private ArrayList<IFlowerPowerListener> flowerPowerListenerList = new ArrayList<>();

    private ArrayList<IDeviceInitListener> initListenerList = new ArrayList<>();

    private boolean historyRetrieving = false;

    private int historicFileLength = -1;
    private int historicBytesCount = -1;

    private boolean init = false;

    /**
     * battery value default set to -1
     */
    private int batteryValue = -1;

    private double light = -1;

    private double temperature = -1;

    private double moisture = -1;

    private double ec = -1;

    /**
     * led state default false
     */
    private boolean led_state = false;

    /**
     * unsigned int
     */
    private long flower_power_date = 0;

    private int last_move_date = 0;

    private int measurement_period = 0;

    private double sunlight = 0;
    private double airTemp = 0;
    private double soilTemp = 0;
    private double soilEC = 0;
    private double soilWc = 0;

    private FlowerPowerColor color = FlowerPowerColor.UNKNOWN;

    private String name = "";

    /**
     * @param conn
     */
    @SuppressLint("NewApi")
    public FlowerPowerDevice(IBluetoothDeviceConn conn) {
        super(conn);
        setCharacteristicListener(new ICharacteristicListener() {

            @Override
            public void onCharacteristicReadReceived(BluetoothGattCharacteristic charac) {

                if (charac.getUuid().toString().equals(FlowerPowerConst.SUNLIGHT_UUID)) {
                    double sunlight = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSunLightChange(sunlight);
                    }
                    FlowerPowerDevice.this.sunlight = sunlight;
                    Log.i(TAG, "sunlight : " + sunlight);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.SOIL_EC)) {
                    double soilElec = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSoilEcChange(soilElec);
                    }
                    FlowerPowerDevice.this.soilEC = soilElec;
                    Log.i(TAG,"soilEC : " + soilElec);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.SOIL_TEMP)) {
                    double soilTemp = ByteUtils.convertByteArrayToInt(ByteUtils.convertLeToBe(charac.getValue()));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSoilTempChange(soilTemp);
                    }
                    FlowerPowerDevice.this.soilTemp = soilTemp;
                    Log.i(TAG,"soilTemp : " + soilTemp);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.AIR_TEMP)) {
                    double airTemp = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onAirTempChange(airTemp);
                    }
                    FlowerPowerDevice.this.airTemp = airTemp;
                    Log.i(TAG,"airTemp : " + airTemp);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.SOIL_WC)) {
                    double soilWC = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSoilWcChange(soilWC);
                    }
                    FlowerPowerDevice.this.soilWc = soilWC;
                    Log.i(TAG,"soilWC : " + soilWC);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.BATTERY_CHARAC)) {
                    batteryValue = charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Log.i(TAG,"batteryValue : " + batteryValue);

                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onBatteryValueReceived(batteryValue);
                    }
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.DATE_CHARAC)) {
                    flower_power_date = (new Date().getTime()) - charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    Log.i(TAG,"flower_power_date : " + flower_power_date);
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onFlowerPowerClockReceived(flower_power_date);
                    }
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.LED_CHARAC)) {
                    if (charac.getValue()[0] == 0x00)
                        led_state = false;
                    else
                        led_state = true;
                    Log.i(TAG,"led_state : " + led_state);

                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onLedStateChange(led_state);
                    }

                } else if (charac.getUuid().toString().equals(FlowerPowerConst.LAST_MOVE_TIME_CHARAC)) {
                    last_move_date = charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    Log.i(TAG,"last_move_date : " + last_move_date);

                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onLastMoveChange(last_move_date);
                    }

                } else if (charac.getUuid().toString().equals(FlowerPowerConst.DEVICE_COLOR_CHARAC)) {
                    switch (charac.getValue()[0]) {
                        case 1:
                            color = FlowerPowerColor.BROWN;
                            break;
                        case 2:
                            color = FlowerPowerColor.ESMERALD;
                            break;
                        case 3:
                            color = FlowerPowerColor.LEMON;
                            break;
                        case 4:
                            color = FlowerPowerColor.GRAY_BROWN;
                            break;
                        case 5:
                            color = FlowerPowerColor.GRAY_GREEN;
                            break;
                        case 6:
                            color = FlowerPowerColor.CLASSIC_GREEN;
                            break;
                        case 7:
                            color = FlowerPowerColor.GRAY_BLUE;
                            break;
                    }
                    Log.i(TAG,"color : " + color);

                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onFlowerPowerColorChange(color);
                    }

                } else if (charac.getUuid().toString().equals(FlowerPowerConst.HISTORY_SERVICE_ENTRIES_NUMBER)) {

                    entriesCount=ByteUtils.convertByteArrayToInt(ByteUtils.convertLeToBe(charac.getValue()));

                } else if (charac.getUuid().toString().equals(FlowerPowerConst.HISTORY_SERVICE_LAST_ENTRY_INDEX)) {

                    lastEntry = ByteUtils.convertByteArrayToInt(ByteUtils.convertLeToBe(charac.getValue()));

                    if (lastEntry > 0 && entriesCount > 0) {
                        int index = lastEntry - 1 + 1;
                        getConn().writeCharacteristic(FlowerPowerConst.HISTORY_SERVICE, FlowerPowerConst.HISTORY_SERVICE_TRANSFER_START_INDEX, ByteUtils.convertLeToBe(ByteUtils.convertIntToByteArray(index)));
                    } else {
                        Log.e(TAG,"Error while setting transfer start index");
                    }

                }else if (charac.getUuid().toString().equals(FlowerPowerConst.UPLOAD_SERVICE_RX_STATUS)){

                }
                else if (charac.getUuid().toString().equals(FlowerPowerConst.UPLOAD_SERVICE_TX_STATUS)){

                }
                else if (charac.getUuid().toString().equals(FlowerPowerConst.HISTORY_SERVICE_TRANSFER_START_INDEX)) {

                } else if (charac.getUuid().toString().equals(FlowerPowerConst.DEVICE_NAME)) {
                    if (charac.getValue()!=null && charac.getValue().length > 0) {
                        try {
                            name = new String(charac.getValue(), "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                            flowerPowerListenerList.get(i).onFlowerPowerNameChange(name);
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicChangeReceived(BluetoothGattCharacteristic charac) {

                if (charac.getUuid().toString().equals(FlowerPowerConst.SUNLIGHT_UUID)) {
                    double sunlight = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSunLightChange(sunlight);
                    }
                    FlowerPowerDevice.this.sunlight = sunlight;
                    Log.i(TAG,"sunlight : " + sunlight);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.SOIL_EC)) {
                    double soilElec = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSoilEcChange(soilElec);
                    }
                    FlowerPowerDevice.this.soilEC = soilElec;
                    Log.i(TAG,"soilEC : " + soilElec);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.SOIL_TEMP)) {
                    double soilTemp = ByteUtils.convertByteArrayToInt(ByteUtils.convertLeToBe(charac.getValue()));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSoilTempChange(soilTemp);
                    }
                    FlowerPowerDevice.this.soilTemp = soilTemp;
                    Log.i(TAG,"soilTemp : " + soilTemp);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.AIR_TEMP)) {
                    double airTemp = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onAirTempChange(airTemp);
                    }
                    FlowerPowerDevice.this.airTemp = airTemp;
                    Log.i(TAG,"airTemp : " + airTemp);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.SOIL_WC)) {
                    double soilWC = ByteUtils.convertByteArrayToInt((ByteUtils.convertLeToBe(charac.getValue())));
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onSoilWcChange(soilWC);
                    }
                    FlowerPowerDevice.this.soilWc = soilWC;
                    Log.i(TAG,"soilWC : " + soilWC);
                } else if (charac.getUuid().equals(FlowerPowerConst.BATTERY_CHARAC)) {
                    batteryValue = charac.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onBatteryValueReceived(batteryValue);
                    }
                    FlowerPowerDevice.this.batteryValue = batteryValue;
                    Log.i(TAG,"batteryValue : " + batteryValue);
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.UPLOAD_SERVICE_TX_STATUS)) {
                    //set state to RECEIVING if TRANSFERRING is received
                    Log.i(TAG,"RECEIVED UPLOAD_SERVICE_TX_STATUS NOTIFICATION");

                    if (charac.getValue().length > 0 && charac.getValue()[0] == TxStatus.getTxValue(TxStatus.states.TRANSFERRING) && historicState == HistoricStates.STANDBY) {
                        historicState = HistoricStates.FIRST_TX_BUFFER_FRAME;
                        Log.i(TAG,"RECEIVED TRANSFERRING");
                    } else if (charac.getValue().length > 0 && charac.getValue()[0] == TxStatus.getTxValue(TxStatus.states.TRANSFERRING) && historicState == HistoricStates.RECEIVING) {
                        Log.i(TAG,"RECEIVED TRANSFERRING during reception. Continue to stack...");
                    } else if (charac.getValue().length > 0) {
                        if (TxStatus.getTxState(charac.getValue()[0]) == TxStatus.states.WAITING_ACK) {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    getConn().writeCharacteristic(FlowerPowerConst.UPLOAD_SERVICE, FlowerPowerConst.UPLOAD_SERVICE_RX_STATUS, new byte[]{RxStatus.getRxValue(RxStatus.states.ACK)});
                                }
                            }).start();

                        }
                    }
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.UPLOAD_SERVICE_TX_BUFFER)) {

                    //set state to RECEIVING if TRANSFERRING is received
                    Log.i(TAG,"RECEIVED UPLOAD_SERVICE_TX_BUFFER NOTIFICATION");

                    if (charac.getValue().length > 0 && historicState == HistoricStates.FIRST_TX_BUFFER_FRAME) {

                        Log.i(TAG,"RECEIVED FIRST_TX_BUFFER_FRAME");

                        int fileLengthTemp = HistoricUtils.getFileLength(charac.getValue());
                        if (fileLengthTemp != -1) {

                            historicFileLength = fileLengthTemp;
                            Log.i(TAG,"TOTAL LENGTH EXPECTED : " + historicFileLength + " => setting to RECEIVING state");
                            historicState = HistoricStates.RECEIVING;

                            //clear historic list now that we already had first tx buffer frame
                            historicFrameList.clear();
                            tempHistoricFrameList.clear();

                            //set counter to 0
                            historicBytesCount = 0;
                        } else {
                            System.err.println("Error historic file length is inconsistent");
                            historicState = HistoricStates.NONE;
                        }
                    } else if (charac.getValue().length > 0 && historicState == HistoricStates.RECEIVING) {
                        IHistoricFrame frame = new HistoricFrames(charac.getValue());

                        boolean aleadyReceived = HistoricUtils.isAlreadyReceived(tempHistoricFrameList, frame);

                        if (frame.getIndex() != 0 && !aleadyReceived) {
                            tempHistoricFrameList.add(frame);

                        } else if (frame.getIndex() == 0) {
                            ArrayList<IHistoricFrame> historyList = HistoricUtils.rearrangeIndex(tempHistoricFrameList);
                            for (int i = 0; i < historyList.size(); i++) {
                                historicFrameList.add(historyList.get(i));
                            }

                            tempHistoricFrameList.clear();
                            tempHistoricFrameList.add(frame);
                        }

                        if (!aleadyReceived) {
                            historicBytesCount += frame.getPayload().length;

                            for (int i = 0; i  < flowerPowerListenerList.size();i++)
                            {
                                Log.i(TAG,"PROGRESS CHANGE : " + ((int)historicBytesCount*100/historicFileLength));

                                flowerPowerListenerList.get(i).onFullHistoryProgressChange(((int)historicBytesCount*100/historicFileLength));
                            }
                        }

                        Log.i(TAG,"historicBytesCount => " + historicBytesCount + " et total : " + historicFileLength);

                        if (historicBytesCount > historicFileLength) {
                            ArrayList<IHistoricFrame> historyList = HistoricUtils.rearrangeIndex(tempHistoricFrameList);
                            for (int i = 0; i < historyList.size(); i++) {
                                historicFrameList.add(historyList.get(i));
                            }
                            tempHistoricFrameList.clear();

                            Log.i(TAG,"COMPLETE HISTORIC DATA HAS BEEN RETRIEVED.WE HAVE NOW " + historicFrameList.size() + " frames stacked");

                            historyRetrieving = false;
                            for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                                flowerPowerListenerList.get(i).onFullHistoryDataReceived(historicFrameList);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCharacteristicWriteReceived(BluetoothGattCharacteristic charac) {
                if (charac.getUuid().toString().equals(FlowerPowerConst.LIVE_MEASUREMENT_PERIOD)) {
                    if (charac.getValue().length > 0) {
                        measurement_period = charac.getValue()[0] & 0xFF;

                        for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                            flowerPowerListenerList.get(i).onMeasurementPeriod(measurement_period);
                        }
                    }
                    if (!init) {
                        //trigger init callback
                        //TODO : change this init method to thread pool management
                        init = true;
                        for (int i = 0; i < initListenerList.size(); i++) {
                            initListenerList.get(i).onInit();
                        }
                    }
                } else if (charac.getUuid().toString().equals(FlowerPowerConst.LED_CHARAC)) {
                    if (charac.getValue().length > 0 && charac.getValue()[0] == 0x00)
                        led_state = false;
                    else
                        led_state = true;

                    for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                        flowerPowerListenerList.get(i).onLedStateChange(led_state);
                    }

                } else if (charac.getUuid().toString().equals(FlowerPowerConst.DEVICE_NAME)) {
                    if (charac.getValue().length > 0) {
                        name = charac.getStringValue(0);

                        for (int i = 0; i < flowerPowerListenerList.size(); i++) {
                            flowerPowerListenerList.get(i).onFlowerPowerNameChange(name);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void init() {

        Log.i(TAG,"initializing Flower Power live measurement");

        readFlowerPowerName();
        readBatteryState();
        readFlowerPowerDate();
        readFlowerPowerColor();
        readLedState();
        readSunlight();
        readSoilEc();
        readSoilTemp();
        readAirTemp();
        readSoilWc();

        //getConn().readCharacteristic(FlowerPowerConst.BATTERY_SERVICE, FlowerPowerConst.BATTERY_CHARAC);

        //getConn().readCharacteristic(FlowerPowerConst.DATE_SERVICE, FlowerPowerConst.DATE_CHARAC);

        //getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.LED_CHARAC);

        //getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.LAST_MOVE_TIME_CHARAC);

        //getConn().readCharacteristic(FlowerPowerConst.DEVICE_COLOR_SERVICE, FlowerPowerConst.DEVICE_COLOR_CHARAC);

        readFlowerPowerName();

        enableNotification(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SUNLIGHT_UUID);

        enableNotification(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SOIL_EC);

        enableNotification(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SOIL_TEMP);

        enableNotification(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.AIR_TEMP);

        enableNotification(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SOIL_WC);

        getConn().writeCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.LIVE_MEASUREMENT_PERIOD, ByteUtils.convertIntToByteArray(LIVE_MEASURE_PERIOD_DEFAULT));


        getConn().readCharacteristic(FlowerPowerConst.HISTORY_SERVICE, FlowerPowerConst.HISTORY_SERVICE_TRANSFER_START_INDEX);

        getConn().readCharacteristic(FlowerPowerConst.HISTORY_SERVICE, FlowerPowerConst.HISTORY_SERVICE_ENTRIES_NUMBER);

        getConn().readCharacteristic(FlowerPowerConst.HISTORY_SERVICE,FlowerPowerConst.HISTORY_SERVICE_LAST_ENTRY_INDEX);

        //startHistoricDataRetrievalProcess();
    }

    /**
     * switch led state
     *
     * @param state
     */
    public void setOnOff(boolean state) {
        byte[] data = new byte[]{0x00};

        if (state) {
            data = new byte[]{0x01};
        }
        getConn().writeCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.LED_CHARAC, data);

    }

    /**
     * start history retrieval process
     */
    public void startHistoricDataRetrievalProcess() {

        if (!historyRetrieving) {
            historyRetrieving = true;
        }
        //historic retrieval procedure
        //submit to notification for tx upload service
        enableNotification(FlowerPowerConst.UPLOAD_SERVICE, FlowerPowerConst.UPLOAD_SERVICE_TX_STATUS);
        enableNotification(FlowerPowerConst.UPLOAD_SERVICE, FlowerPowerConst.UPLOAD_SERVICE_TX_BUFFER);

        getConn().writeCharacteristic(FlowerPowerConst.UPLOAD_SERVICE, FlowerPowerConst.UPLOAD_SERVICE_RX_STATUS, new byte[]{RxStatus.getRxValue(RxStatus.states.RECEIVING)});

        historicState = HistoricStates.STANDBY;
        historicFileLength = 0;
        historicBytesCount = 0;
        historicFrameList.clear();
        tempHistoricFrameList.clear();
        //send RECEIVING as rx status
    }

    @Override
    public void addListener(IFlowerPowerListener listener) {
        flowerPowerListenerList.add(listener);
    }

    @Override
    public void startHistoryRetrievalProcedure() {

        //this call must be asynchronous => no blocking in the UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                startHistoricDataRetrievalProcess();
            }
        }).start();
    }

    @Override
    public void setLedOnOff(boolean ledState) {
        final boolean ledTemp = ledState;
        //this call must be asynchronous => no blocking in the UI thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                setOnOff(ledTemp);
            }
        }).start();
    }

    @Override
    public void setMeasurementPeriod(int period) {
        final int periodTemp = period;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().writeCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.LIVE_MEASUREMENT_PERIOD, ByteUtils.convertIntToByteArray(periodTemp));
            }
        }).start();
    }

    @Override
    public void setFlowerPowerName(String name) {
        final String deviceName = name;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().writeCharacteristic(FlowerPowerConst.DEVICE_COLOR_SERVICE, FlowerPowerConst.DEVICE_NAME, deviceName.getBytes());
            }
        }).start();
    }

    @Override
    public void readFlowerPowerName() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.DEVICE_COLOR_SERVICE, FlowerPowerConst.DEVICE_NAME);
            }
        }).start();
    }

    @Override
    public void readBatteryState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.BATTERY_SERVICE, FlowerPowerConst.BATTERY_CHARAC);
            }
        }).start();
    }

    @Override
    public void readFlowerPowerDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.DATE_SERVICE, FlowerPowerConst.DATE_CHARAC);
            }
        }).start();
    }

    @Override
    public void readFlowerPowerColor() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.DEVICE_COLOR_SERVICE, FlowerPowerConst.DEVICE_COLOR_CHARAC);
            }
        }).start();
    }

    @Override
    public void readLedState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.LED_CHARAC);
            }
        }).start();
    }

    @Override
    public void readSunlight() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SUNLIGHT_UUID);
            }
        }).start();
    }

    @Override
    public void readSoilEc() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SOIL_EC);
            }
        }).start();
    }

    @Override
    public void readSoilTemp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SOIL_TEMP);
            }
        }).start();
    }

    @Override
    public void readAirTemp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.AIR_TEMP);
            }
        }).start();
    }

    @Override
    public void readSoilWc() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getConn().readCharacteristic(FlowerPowerConst.LIVE_SERVICE_UUID, FlowerPowerConst.SOIL_WC);
            }
        }).start();
    }

    @Override
    public String getFlowerPowerName() {
        return name;
    }

    @Override
    public double getSunlight() {
        return sunlight;
    }

    @Override
    public double getAirTemp() {
        return airTemp;
    }

    @Override
    public double getSoilTemp() {
        return soilTemp;
    }

    @Override
    public double getSoilEC() {
        return soilEC;
    }

    @Override
    public double getWc() {
        return soilWc;
    }

    @Override
    public long getFlowerPowerDate() {
        return flower_power_date;
    }

    @Override
    public int getBaterry() {
        return batteryValue;
    }

    @Override
    public FlowerPowerColor getColor() {
        return color;
    }

    @Override
    public int getMeasurementPeriod() {
        return measurement_period;
    }

    @Override
    public boolean isRetrievingHistory() {
        return historyRetrieving;
    }

    @Override
    public boolean isInit() {
        return init;
    }

    @Override
    public void addInitListener(IDeviceInitListener listener) {
        initListenerList.add(listener);
    }
}