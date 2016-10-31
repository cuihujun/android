package no.nordicsemi.android.nrftoolbox.pos;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.nrftoolbox.profile.BleManager;


import no.nordicsemi.android.nrftoolbox.parser.PulseOximeterMeasurementParser;
import no.nordicsemi.android.nrftoolbox.utility.DebugLogger;

public class SPO2Manager extends BleManager<SPO2Callbacks> {
    public final static UUID SPO2_SERVICE_UUID = UUID.fromString("00001822-0000-1000-8000-00805f9b34fb");
    private static final UUID SPO2_PLX_SPOT_CHARACTERISTIC_UUID = UUID.fromString("00002A5E-0000-1000-8000-00805f9b34fb");
    private static final UUID SPO2_PLX_CONTINIUOS_CHARACTERISTIC_UUID = UUID.fromString("00002A5F-0000-1000-8000-00805f9b34fb");

    private BluetoothGattCharacteristic mSPO2Characteristic;

    private static SPO2Manager managerInstance = null;

    /**
     * singleton implementation of HRSManager class
     */
    public static synchronized SPO2Manager getInstance(final Context context) {
        if (managerInstance == null) {
            managerInstance = new SPO2Manager(context);
        }
        return managerInstance;
    }

    public SPO2Manager(final Context context) {
        super(context);
    }

    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving notification, etc
     */
    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

        @Override
        protected Queue<Request> initGatt(final BluetoothGatt gatt) {
            final LinkedList<Request> requests = new LinkedList<>();
            requests.push(Request.newEnableIndicationsRequest(mSPO2Characteristic));
            return requests;
        }

        @Override
        protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SPO2_SERVICE_UUID);
            if (service != null) {
                mSPO2Characteristic = service.getCharacteristic(SPO2_PLX_CONTINIUOS_CHARACTERISTIC_UUID);
            }
            return mSPO2Characteristic != null;
        }

        @Override
        protected void onDeviceDisconnected() {
            mSPO2Characteristic = null;
        }

        @Override
        public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (mLogSession != null)
                Logger.a(mLogSession, PulseOximeterMeasurementParser.parse(characteristic));

            float spO2Value = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SINT16, 1);
            mCallbacks.onSPO2ValueReceived(spO2Value);
        }

        @Override
        public void onCharacteristicIndicated(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            if (mLogSession != null)
                Logger.a(mLogSession, PulseOximeterMeasurementParser.parse(characteristic));

            Integer spO2Value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
            float spO2ValueFloat = spO2Value.floatValue() / 10;
            mCallbacks.onSPO2ValueReceived(spO2ValueFloat);
        }

    };

}
