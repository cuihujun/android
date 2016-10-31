package no.nordicsemi.android.nrftoolbox;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import java.util.*;

import no.nordicsemi.android.nrftoolbox.scanner.DeviceListAdapter;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import no.nordicsemi.android.nrftoolbox.scanner.ExtendedBluetoothDevice;


public class FindDevicesActivity extends AppCompatActivity {

    private final String TAG = "FindDevicesActivity";
    private final String mDeviceName = "Zephyr Health Sensor";
    private FindDevicesAdapter mAdapter;
    private GridView mGridView;
    private Button mScanButton;
    private boolean mIsScanning = false;
    private final Handler mHandler = new Handler();
    private final static long SCAN_DURATION = 5000;
    public static ExtendedBluetoothDevice d;

    private View mPermissionRationale;

    private final static int REQUEST_PERMISSION_REQ_CODE = 100; // any 8-bit number


    private Random r = new Random();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        ArrayList<String> MOBILE_OS = new ArrayList<String>();
//        for (int i = 0; i < 20; i++) {
//            int i1 = r.nextInt(10000);
//            String s = String.valueOf(i1);
//            MOBILE_OS.add(s);
//        }

        setContentView(R.layout.activity_find_devices);

        // ensure that Bluetooth exists
        if (!ensureBLEExists())
            finish();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);

        mScanButton = (Button) findViewById(R.id.scan_button);
        mScanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScan();
            }
        });

        mGridView = (GridView) findViewById(R.id.grid);

        mGridView.setAdapter(mAdapter = new FindDevicesAdapter(this));

        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v,
                                    int position, long id) {
                stopScan();
                d = (ExtendedBluetoothDevice) mAdapter.getItem(position);

                final Intent intent = new Intent(FindDevicesActivity.this, FeaturesActivity.class);

                startActivity(intent);
            }
        });

        final View dialogView = LayoutInflater.from(this).inflate(R.layout.fragment_device_selection, null);

        mPermissionRationale = dialogView.findViewById(R.id.permission_rationale); // this is not null only on API23+

        startScan();

     }

    private void onDeviceSelected(final BluetoothDevice device, final String name) {
        DebugLogger.d(TAG, "Found device " + device.getAddress() + " Name = " + name);
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now we may proceed with scanning.
                    startScan();
                } else {
                    mPermissionRationale.setVisibility(View.VISIBLE);
                    Toast.makeText(this, R.string.no_required_permission, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }


    private void startScan() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) && mPermissionRationale.getVisibility() == View.GONE) {
                mPermissionRationale.setVisibility(View.VISIBLE);
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
            }
            return;
        }

        // Hide the rationale message, we don't need it anymore.
        if (mPermissionRationale != null)
            mPermissionRationale.setVisibility(View.GONE);

        if (mIsScanning) {
            return;
        }
        mAdapter.clearDevices();
        mScanButton.setText(R.string.scanner_action_cancel);

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build();
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setDeviceName(mDeviceName).build());
        scanner.startScan(filters, settings, scanCallback);

        mIsScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsScanning) {
                    stopScan();
                }
            }
        }, SCAN_DURATION);
    }

    /**
     * Stop scan if user tap Cancel button
     */
    private void stopScan() {
        if (mIsScanning) {
            mScanButton.setText(R.string.scanner_action_scan);

            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);

            mIsScanning = false;
        }
    }

    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            // do nothing
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            mAdapter.update(results);
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // should never be called
        }
    };

        private boolean ensureBLEExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}