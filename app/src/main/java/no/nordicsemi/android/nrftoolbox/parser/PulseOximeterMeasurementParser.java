package no.nordicsemi.android.nrftoolbox.parser;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by jbrennan on 8/16/16.
 */
public class PulseOximeterMeasurementParser {
    public static String parse(final BluetoothGattCharacteristic characteristic) {
        final StringBuilder builder = new StringBuilder();

        int offset = 0;
        final int flags = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
        offset += 1;

        // create and fill the new record
        final float spO2 = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
        builder.append("SpO2: ").append(spO2);
        offset += 2;

        final float pulseRate = characteristic.getFloatValue(BluetoothGattCharacteristic.FORMAT_SINT16, offset);
        builder.append("\nPulse Rate: ").append(pulseRate);

        return builder.toString();
    }

}
