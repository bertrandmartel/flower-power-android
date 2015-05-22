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
package fr.bmartel.android.flowerpower;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import fr.bmartel.android.bluetooth.parrot.FlowerPowerColor;
import fr.bmartel.android.bluetooth.parrot.IFlowerPowerDevice;
import fr.bmartel.android.bluetooth.parrot.IFlowerPowerListener;
import fr.bmartel.android.bluetooth.parrot.history.IHistoricFrame;

/**
 * Flower Power device description activity
 *
 * @author Bertrand Martel
 */
public class FlowerPowerDeviceActivity extends ActionBarActivity {

    private FlowerPowerBtService currentService = null;

    private String address = "";

    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_device);

        Intent intent = getIntent();
        address = intent.getStringExtra("deviceAddr");
        String flowerPowerName = intent.getStringExtra("flowerPowerName");
        String deviceName = intent.getStringExtra("deviceName");

        setTitle(deviceName.trim() + " [ " + address + " ] ");


        Intent intentMain = new Intent(this, FlowerPowerBtService.class);

        // bind the service to current activity and create it if it didnt exist before
        startService(intentMain);
        bindService(intentMain, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * Manage Bluetooth Service lifecycle
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName name, IBinder service) {

            System.out.println("Connected to service");

            currentService = ((FlowerPowerBtService.LocalBinder) service).getService();

            if (currentService.getConnectionList().get(address) != null) {
                if (currentService.getConnectionList().get(address).getDevice() instanceof IFlowerPowerDevice) {
                    final IFlowerPowerDevice device = (IFlowerPowerDevice) currentService.getConnectionList().get(address).getDevice();

                    final SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy hh:mm");

                    final ToggleButton ledButton = (ToggleButton) findViewById(R.id.ledButton);
                    final TextView sunlightTxt = (TextView) findViewById(R.id.sunlightValue);
                    final TextView airTempTxt = (TextView) findViewById(R.id.airTemperatureValue);
                    final TextView soilTempTxt = (TextView) findViewById(R.id.soilTemperatureValue);
                    final TextView soilECTxt = (TextView) findViewById(R.id.soilECValue);
                    final TextView soilWcTxt = (TextView) findViewById(R.id.soilWcValue);
                    final TextView batteryTxt = (TextView) findViewById(R.id.batteryValue);
                    final TextView colorTxt = (TextView) findViewById(R.id.colorValue);
                    final TextView flowerPowerDate = (TextView) findViewById(R.id.flowerPowerDateValue);
                    final EditText measurementPeriodTxt = (EditText) findViewById(R.id.measurementPeriodValue);

                    final Button readDateBtn = (Button) findViewById(R.id.readDate);
                    final Button validatePeriodValueBtn = (Button) findViewById(R.id.validatePeriodValue);
                    final Button readColorBtn = (Button) findViewById(R.id.readColor);
                    final Button readBatteryBtn = (Button) findViewById(R.id.readBattery);
                    final Button readWcValueBtn = (Button) findViewById(R.id.readWcValue);
                    final Button readEcValue = (Button) findViewById(R.id.readEcValue);
                    final Button readAirTemp = (Button) findViewById(R.id.readAirTemp);
                    final Button readSoilTemp = (Button) findViewById(R.id.readSoilTemp);
                    final Button readSunlight = (Button) findViewById(R.id.readSunlight);

                    final Button historyButton = (Button) findViewById(R.id.historyButton);


                    readWcValueBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readSoilWc();
                                }
                            });
                        }
                    });
                    readEcValue.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readSoilEc();
                                }
                            });
                        }
                    });

                    readAirTemp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readAirTemp();
                                }
                            });
                        }
                    });
                    readSoilTemp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readSoilTemp();
                                }
                            });
                        }
                    });
                    readSunlight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readSunlight();
                                }
                            });
                        }
                    });

                    historyButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    progress = new ProgressDialog(FlowerPowerDeviceActivity.this);
                                    progress.setMessage("Retrieving historic ...");
                                    progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    progress.setIndeterminate(false);
                                    progress.setMax(100);
                                    progress.show();
                                    progress.setProgress(0);

                                    device.startHistoryRetrievalProcedure();
                                }
                            });
                        }
                    });

                    readDateBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readFlowerPowerDate();
                                }
                            });
                        }
                    });

                    validatePeriodValueBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.setMeasurementPeriod(Integer.parseInt(measurementPeriodTxt.getText().toString()));
                                }
                            });
                        }
                    });

                    readColorBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readFlowerPowerColor();
                                }
                            });
                        }
                    });

                    readBatteryBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    device.readBatteryState();
                                }
                            });
                        }
                    });

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            sunlightTxt.setText(String.valueOf(device.getSunlight()));
                            airTempTxt.setText(String.valueOf(device.getAirTemp()));
                            soilTempTxt.setText(String.valueOf(device.getSoilTemp()));
                            soilECTxt.setText(String.valueOf(device.getSoilEC()));
                            soilWcTxt.setText(String.valueOf(device.getWc()));
                            batteryTxt.setText(String.valueOf(device.getBaterry()));
                            colorTxt.setText(String.valueOf(device.getColor()));
                            flowerPowerDate.setText(String.valueOf(dt.format(new Date(device.getFlowerPowerDate()))));
                            measurementPeriodTxt.setText(String.valueOf(device.getMeasurementPeriod()));
                        }
                    });

                    device.addListener(new IFlowerPowerListener() {
                        @Override
                        public void onSunLightChange(final double value) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    sunlightTxt.setText(String.valueOf(value));
                                }
                            });
                        }

                        @Override
                        public void onSoilEcChange(final double soilEC) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    soilECTxt.setText(String.valueOf(soilEC));
                                }
                            });
                        }

                        @Override
                        public void onSoilTempChange(final double temp) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    soilTempTxt.setText(String.valueOf(temp));
                                }
                            });
                        }

                        @Override
                        public void onAirTempChange(final double temp) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    airTempTxt.setText(String.valueOf(temp));
                                }
                            });
                        }

                        @Override
                        public void onSoilWcChange(final double wc) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    soilWcTxt.setText(String.valueOf(wc));
                                }
                            });
                        }

                        @Override
                        public void onFullHistoryDataReceived(ArrayList<IHistoricFrame> frameList) {
                            System.out.println("Complete history has been retrieved => " + frameList.size() + " frames");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progress != null)
                                    {
                                        progress.hide();
                                        progress.setProgress(0);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onFullHistoryProgressChange(final int progressChange) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (progress != null) {
                                        System.out.println("set progress to " + progressChange);

                                        progress.setProgress(progressChange);
                                    }
                                }});
                        }

                        @Override
                        public void onBatteryValueReceived(final int batteryValuePercent) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    batteryTxt.setText(String.valueOf(batteryValuePercent));
                                }
                            });
                        }

                        @Override
                        public void onFlowerPowerClockReceived(final long date) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    flowerPowerDate.setText(String.valueOf(dt.format(new Date(date))));
                                }
                            });
                        }

                        @Override
                        public void onLedStateChange(final boolean ledState) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ledButton.setChecked(ledState);
                                }
                            });
                        }

                        @Override
                        public void onLastMoveChange(long date) {

                        }

                        @Override
                        public void onFlowerPowerColorChange(final FlowerPowerColor color) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    colorTxt.setText(String.valueOf(color));
                                }
                            });
                        }

                        @Override
                        public void onMeasurementPeriod(final int measurement_period) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    measurementPeriodTxt.setText(String.valueOf(measurement_period));
                                }
                            });
                        }

                        @Override
                        public void onFlowerPowerNameChange(final String name) {

                        }
                    });
                    System.out.println("identified flower power continuing ....");


                    if (ledButton != null) {
                        ledButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                device.setLedOnOff(ledButton.isChecked());
                            }
                        });
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //unregister receiver on pause
        //unregisterReceiver(mGattUpdateReceiver);

        try {
            // unregister receiver or you will have strong exception
            unbindService(mServiceConnection);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
