package no.nordicsemi.android.nrftoolbox.pos;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import android.view.ViewGroup;
import android.widget.TextView;

import org.achartengine.GraphicalView;

import java.util.Locale;
import java.util.UUID;

import no.nordicsemi.android.nrftoolbox.FindDevicesActivity;
import no.nordicsemi.android.nrftoolbox.R;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;
import no.nordicsemi.android.nrftoolbox.profile.BleProfileActivity;


/**
 * Pulse Oximeter Activity is the main Pulse Oximeter activity. It implements SPO2Callbacks to receive callbacks from SPO2Manager class. The activity supports portrait and landscape orientations. The activity
 * uses external library AChartEngine to show real time graph of Pulse Oximeter values.
 */
public class SPO2Activity extends BleProfileActivity implements SPO2Callbacks {
    @SuppressWarnings("unused")
    private final String TAG = "SPO2Activity";

    private final static String GRAPH_STATUS = "graph_status";
    private final static String GRAPH_COUNTER = "graph_counter";
    private final static String SPO2_VALUE = "SpO2_value";

    private final static double MAX_SPO2_VALUE = 100.0;
    private final static double MIN_POSITIVE_VALUE = 0.0;
    private final static int REFRESH_INTERVAL = 1000; // 1 second interval

    private Handler mHandler = new Handler();

    private boolean isGraphInProgress = false;

    private GraphicalView mGraphView;
    private LineGraphView mLineGraph;
    private TextView mSPO2ValueText;

    private float mSpO2Value = 0;
    private int mCounter = 0;

    @Override
    protected void onCreateView(final Bundle savedInstanceState) {
        setContentView(R.layout.activity_feature_spo2);
        setGUI();
    }

    private void setGUI() {
        mLineGraph = LineGraphView.getLineGraphView();
        mSPO2ValueText = (TextView) findViewById(R.id.text_spo2_value);
        showGraph();
    }

    private void showGraph() {
        mGraphView = mLineGraph.getView(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.graph_spo2);
        layout.addView(mGraphView);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            isGraphInProgress = savedInstanceState.getBoolean(GRAPH_STATUS);
            mCounter = savedInstanceState.getInt(GRAPH_COUNTER);
            mSpO2Value = savedInstanceState.getInt(SPO2_VALUE);

            if (isGraphInProgress)
                startShowGraph();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(GRAPH_STATUS, isGraphInProgress);
        outState.putInt(GRAPH_COUNTER, mCounter);
        outState.putFloat(SPO2_VALUE, mSpO2Value);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopShowGraph();
    }

    @Override
    protected int getLoggerProfileTitle() {
        return R.string.hrs_feature_title;
    }

    @Override
    protected int getAboutTextId() {
        return R.string.hrs_about_text;
    }

    @Override
    protected int getDefaultDeviceName() {
        return R.string.hrs_default_name;
    }

    @Override
    protected UUID getFilterUUID() {
        return SPO2Manager.SPO2_SERVICE_UUID;
    }

    private void updateGraph(final int spO2Value) {
        mCounter++;
        mLineGraph.addValue(new Point(mCounter, spO2Value));
        mGraphView.repaint();
    }

    private Runnable mRepeatTask = new Runnable() {
        @Override
        public void run() {
            if (mSpO2Value > 0)
                updateGraph(Math.round(mSpO2Value));
            if (isGraphInProgress)
                mHandler.postDelayed(mRepeatTask, REFRESH_INTERVAL);
        }
    };

    void startShowGraph() {
        isGraphInProgress = true;
        mRepeatTask.run();
    }

    void stopShowGraph() {
        isGraphInProgress = false;
        mHandler.removeCallbacks(mRepeatTask);
    }

    @Override
    protected BleManager<SPO2Callbacks> initializeManager() {
        final SPO2Manager manager = SPO2Manager.getInstance(getApplicationContext());
        manager.setGattCallbacks(this);
        manager.connect(FindDevicesActivity.d.device);
        return manager;
    }

    private void setSpO2ValueOnView(final float value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value >= MIN_POSITIVE_VALUE && value <= MAX_SPO2_VALUE) {
                    mSPO2ValueText.setText(String.format(Locale.US, "%2.1f", value));
                } else {
                    mSPO2ValueText.setText(R.string.not_available_value);
                }
            }
        });
    }


    @Override
    public void onServicesDiscovered(final boolean optionalServicesFound) {
        // this may notify user or show some views
    }

    @Override
    public void onDeviceReady() {
        startShowGraph();
    }

    @Override
    public void onSPO2ValueReceived(float value) {
        mSpO2Value = value;
        setSpO2ValueOnView(mSpO2Value);
    }

    @Override
    public void onDeviceDisconnected() {
        super.onDeviceDisconnected();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSPO2ValueText.setText(R.string.not_available_value);
                stopShowGraph();
            }
        });
    }

    @Override
    protected void setDefaultUI() {
        mSPO2ValueText.setText(R.string.not_available_value);
        clearGraph();
    }

    private void clearGraph() {
        mLineGraph.clearGraph();
        mGraphView.repaint();
        mCounter = 0;
        mSpO2Value = 0;
    }
}

