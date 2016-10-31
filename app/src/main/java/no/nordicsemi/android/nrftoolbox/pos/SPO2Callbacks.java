package no.nordicsemi.android.nrftoolbox.pos;

import no.nordicsemi.android.nrftoolbox.profile.BleManagerCallbacks;

public interface SPO2Callbacks extends BleManagerCallbacks {

    /**
     * Called when new Heart Rate value has been obtained from the sensor
     *
     * @param value
     *            the new value
     */
    public void onSPO2ValueReceived(float value);
}
