package no.nordicsemi.android.nrftoolbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import no.nordicsemi.android.nrftoolbox.scanner.DeviceListAdapter;
import no.nordicsemi.android.nrftoolbox.scanner.ExtendedBluetoothDevice;

public class FindDevicesAdapter extends DeviceListAdapter {

    public FindDevicesAdapter(Context context) {
        super(context);
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        final int availableCount = mListValues.size();
        return availableCount;
    }

    @Override
    public Object getItem(int position) {
        return mListValues.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {

            gridView = new View(mContext);

            gridView = inflater.inflate(R.layout.babies, null);

            TextView textView = (TextView) gridView.findViewById(R.id.label);

            final ExtendedBluetoothDevice device = (ExtendedBluetoothDevice) getItem(position);

            textView.setText(device.device.getAddress());

            ImageView baby = (ImageView) gridView .findViewById(R.id.baby);

            baby.setImageResource(R.drawable.neonatal);

        } else {
            gridView = (View) convertView;
        }

        return gridView;
    }
}