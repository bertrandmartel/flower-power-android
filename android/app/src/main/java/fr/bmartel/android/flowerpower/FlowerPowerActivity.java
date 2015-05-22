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

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.bmartel.android.bluetooth.BluetoothCustomManager;
import fr.bmartel.android.bluetooth.IBluetoothManagerEventListener;
import fr.bmartel.android.bluetooth.IScanListListener;
import fr.bmartel.android.bluetooth.shared.ActionFilterGatt;
import fr.bmartel.android.bluetooth.shared.ISharedActivity;
import fr.bmartel.android.bluetooth.shared.StableArrayAdapter;

/**
 * Flower Power bluetooth device management main activity
 *
 * @author Bertrand Martel
 */
public class FlowerPowerActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * debug tag
     */
    private String TAG = this.getClass().getName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * define if bluetooth is enabled on device
     */
    private final static int REQUEST_ENABLE_BT = 1;

    /**
     * Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * list of device to display
     */
    private ListView device_list_view = null;

    /**
     * current index of connecting device item in device list
     */
    private int list_item_position = 0;

    private FlowerPowerBtService currentService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flower_power);

        final ProgressBar progress_bar = (ProgressBar) findViewById(R.id.scanningProgress);

        if (progress_bar != null)
            progress_bar.setEnabled(false);

        final Button button_stop_scanning = (Button) findViewById(R.id.stop_scanning_button);

        if (button_stop_scanning != null)
            button_stop_scanning.setEnabled(false);

        final TextView scanText = (TextView) findViewById(R.id.scanText);

        if (scanText != null)
            scanText.setText("");

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth Smart is not supported on your device", Toast.LENGTH_SHORT).show();
            finish();
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        button_stop_scanning.setEnabled(false);

        final Button button_find_accessory = (Button) findViewById(R.id.scanning_button);

        button_stop_scanning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentService.isScanning()) {
                    currentService.stopScan();

                    if (progress_bar != null) {
                        progress_bar.setEnabled(false);
                        progress_bar.setVisibility(View.GONE);
                    }

                    if (scanText != null)
                        scanText.setText("");

                    if (button_stop_scanning != null)
                        button_stop_scanning.setEnabled(false);
                }
            }
        });

        button_find_accessory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                triggerNewScan();
            }
        });

        Intent intent = new Intent(this, FlowerPowerBtService.class);

        // bind the service to current activity and create it if it didnt exist before
        startService(intent);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
    }

    /**
     * trigger a BLE scan
     */
    public void triggerNewScan() {
        Button button_stop_scanning = (Button) findViewById(R.id.stop_scanning_button);
        ProgressBar progress_bar = (ProgressBar) findViewById(R.id.scanningProgress);
        TextView scanText = (TextView) findViewById(R.id.scanText);

        if (button_stop_scanning != null && progress_bar != null && scanText != null) {
            if (!currentService.isScanning()) {

                Toast.makeText(FlowerPowerActivity.this, "Looking for new accessories", Toast.LENGTH_SHORT).show();

                if (button_stop_scanning != null)
                    button_stop_scanning.setEnabled(true);

                if (progress_bar != null) {
                    progress_bar.setEnabled(true);
                    progress_bar.setVisibility(View.VISIBLE);
                }

                if (scanText != null)
                    scanText.setText("Scanning ...");

                //start scan so clear list view
                currentService.getListViewAdapter().clear();
                currentService.getListViewAdapter().notifyDataSetChanged();

                currentService.clearListAdapter();

                currentService.scanLeDevice(true);
            } else {
                Toast.makeText(FlowerPowerActivity.this, "Scanning already engaged...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
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

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            if (ActionFilterGatt.ACTION_GATT_CONNECTED.equals(action)) {


            } else if (ActionFilterGatt.ACTION_GATT_DISCONNECTED.equals(action)) {
                device_list_view.getChildAt(list_item_position).setBackgroundColor(Color.TRANSPARENT);
                invalidateOptionsMenu();

            } else if (ActionFilterGatt.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {

                System.out.println("A device has been successfully initialized !!!");

                device_list_view.getChildAt(list_item_position).setBackgroundColor(Color.GREEN);
                invalidateOptionsMenu();

                //service has been discovered on device => you can address directly the device


                ArrayList<String> actionsStr = intent.getStringArrayListExtra("");
                if (actionsStr.size() > 0) {
                    try {
                        JSONObject mainObject = new JSONObject(actionsStr.get(0));
                        if (mainObject.has("address") && mainObject.has("flowerPowerName") && mainObject.has("deviceName")) {

                            System.out.println("Setting for device = > " + mainObject.getString("address") + " - " + mainObject.getString("flowerPowerName") + " - " + mainObject.getString("deviceName"));

                            Intent intentFlower = new Intent(FlowerPowerActivity.this, FlowerPowerDeviceActivity.class);
                            intentFlower.putExtra("deviceAddr", mainObject.getString("address"));
                            intentFlower.putExtra("flowerPowerName", mainObject.getString("flowerPowerName"));
                            intentFlower.putExtra("deviceName", mainObject.getString("deviceName"));
                            startActivity(intentFlower);

                            /*
                            if (btManager.getConnectionList().get(mainObject.getString("address")).getDevice() instanceof IFlowerPowerDevice) {
                                IFlowerPowerDevice flowerPower = (IFlowerPowerDevice) btManager.getConnectionList().get(mainObject.getString("address")).getDevice();


                            }
                            */
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            } else if (ActionFilterGatt.ACTION_DATA_AVAILABLE.equals(action)) {

                if (intent.getStringArrayListExtra("STATUS") != null) {
                    ArrayList<String> values = intent.getStringArrayListExtra("STATUS");

                }
            }
        }
    };

    /**
     * Manage Bluetooth Service lifecycle
     */
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            System.out.println("Connected to service");

            currentService = ((FlowerPowerBtService.LocalBinder) service).getService();

            currentService.addScanListListener(new IScanListListener() {
                @Override
                public void onItemAddedInList(final BluetoothDevice device) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            currentService.getListViewAdapter().add(device);
                            currentService.getListViewAdapter().notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onNotifyChangeInList() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentService.getListViewAdapter().notifyDataSetChanged();
                        }
                    });
                }
            });
            currentService.addEventListener(new IBluetoothManagerEventListener() {

                @Override
                public void onBluetoothAdapterNotEnabled() {
                    //beware of Android SDK used on this Android device
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }

                @Override
                public void onEndOfScan() {

                    final Button button_stop_scanning = (Button) findViewById(R.id.stop_scanning_button);
                    final ProgressBar progress_bar = (ProgressBar) findViewById(R.id.scanningProgress);
                    final TextView scanText = (TextView) findViewById(R.id.scanText);

                    Toast.makeText(FlowerPowerActivity.this, "End of scanning...", Toast.LENGTH_SHORT).show();
                    if (button_stop_scanning != null)
                        button_stop_scanning.setEnabled(false);
                    if (progress_bar != null)
                        progress_bar.setEnabled(false);
                    if (scanText != null)
                        scanText.setText("");

                }

                @Override
                public void onStartOfScan() {
                    //Toast.makeText(FlowerPower.this, "Start of scanning...", Toast.LENGTH_SHORT).show();
                }

            });

            device_list_view = (ListView) findViewById(R.id.listView);

            final ArrayList<BluetoothDevice> list = new ArrayList<>();

            currentService.setListViewAdapter(new StableArrayAdapter(FlowerPowerActivity.this, R.layout.new_device_layout, list, currentService));

            device_list_view.setAdapter(currentService.getListViewAdapter());

            device_list_view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // selected item
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            device_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int position, long id) {

                    final ProgressBar progress_bar = (ProgressBar) findViewById(R.id.scanningProgress);
                    final TextView scanText = (TextView) findViewById(R.id.scanText);

                    if (progress_bar != null) {
                        progress_bar.setEnabled(false);
                        progress_bar.setVisibility(View.GONE);
                    }

                    if (scanText != null)
                        scanText.setText("");

                /*stop scanning*/
                    if (currentService.isScanning()) {

                        currentService.stopScan();
                    }
                /*connect to bluetooth gatt server on the device*/
                    String deviceAddress = currentService.getListViewAdapter().getItem(position).getAddress();

                    list_item_position = position;

                    currentService.connect(deviceAddress, FlowerPowerActivity.this);
                }
            });
            triggerNewScan();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

    };

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.flower_power, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_flower_power, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((FlowerPowerActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }


    /**
     * add filter to intent to receive notification from bluetooth service
     *
     * @return intent filter
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionFilterGatt.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ActionFilterGatt.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ActionFilterGatt.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ActionFilterGatt.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
