package com.example.blebeacon;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private static final int PERMISSION_REQUEST = 101;
    // Indoor free-space approximation from the original technical report (n=2).
    private static final double PATH_LOSS_EXPONENT = 2.0;
    private BluetoothLeScanner scanner;
    private boolean scanning = false;
    private TextView status, devices;
    private Button start, stop;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<String, String> observed = new LinkedHashMap<>();

    private final ScanCallback callback = new ScanCallback() {
        @Override public void onScanResult(int callbackType, ScanResult result) {
            if (result == null || result.getDevice() == null) return;
            int rssi = result.getRssi();
            int tx = result.getScanRecord() == null ? Integer.MIN_VALUE : result.getScanRecord().getTxPowerLevel();
            // Android uses Integer.MIN_VALUE for unknown advertising TX power.
            String estimate = tx == Integer.MIN_VALUE ? "Unavailable (TxPower not advertised)" :
                    String.format(Locale.US, "%.2f m (approx.)", estimateDistance(tx, rssi));
            String address = result.getDevice().getAddress();
            observed.put(address, "Device: " + address + "\nRSSI: " + rssi + " dBm\nTxPower: " +
                    (tx == Integer.MIN_VALUE ? "Unknown" : tx + " dBm") + "\nDistance: " + estimate);
            renderResults();
        }
        @Override public void onScanFailed(int errorCode) {
            status.setText("Scan failed (code " + errorCode + ")");
            stopScanning();
        }
    };

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);
        TextView heading = new TextView(this);
        heading.setText("BLE Beacon Explorer"); heading.setTextSize(24);
        root.addView(heading);
        TextView explanation = new TextView(this);
        explanation.setText("Discover nearby BLE advertisers and estimate proximity when TX power is available. Estimates are not reliable navigation measurements.");
        explanation.setPadding(0, pad / 2, 0, pad / 2); root.addView(explanation);
        start = new Button(this); start.setText("SEARCH BEACON"); root.addView(start);
        stop = new Button(this); stop.setText("STOP CALCULATING"); stop.setEnabled(false); root.addView(stop);
        status = new TextView(this); status.setText("Ready to scan"); status.setPadding(0, pad / 2, 0, pad / 2); root.addView(status);
        ScrollView scroll = new ScrollView(this); devices = new TextView(this); devices.setText("No devices detected yet.");
        scroll.addView(devices); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        start.setOnClickListener(v -> requestThenScan());
        stop.setOnClickListener(v -> stopScanning());
    }

    private void requestThenScan() {
        String[] permissions = Build.VERSION.SDK_INT >= 31
                ? new String[]{Manifest.permission.BLUETOOTH_SCAN}
                : new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(permissions, PERMISSION_REQUEST); return;
            }
        }
        startScanning();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST) return;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) { status.setText("Bluetooth scan permission denied."); return; }
        }
        if (grantResults.length > 0) startScanning();
    }

    @SuppressWarnings("MissingPermission")
    private void startScanning() {
        BluetoothManager manager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            status.setText("Enable Bluetooth in system settings, then try again."); return;
        }
        scanner = adapter.getBluetoothLeScanner();
        if (scanner == null) { status.setText("BLE scanner unavailable."); return; }
        observed.clear(); renderResults();
        try {
            scanner.startScan(callback);
            scanning = true; start.setEnabled(false); stop.setEnabled(true);
            status.setText("Scanning for BLE devices...");
        } catch (SecurityException e) { status.setText("Bluetooth permission missing."); }
    }

    @SuppressWarnings("MissingPermission")
    private void stopScanning() {
        if (scanning && scanner != null) {
            try { scanner.stopScan(callback); } catch (SecurityException ignored) { }
        }
        scanning = false; start.setEnabled(true); stop.setEnabled(false);
        if (!status.getText().toString().startsWith("Scan failed")) status.setText("Scan stopped.");
    }

    private void renderResults() {
        if (observed.isEmpty()) { devices.setText("No devices detected yet."); return; }
        StringBuilder sb = new StringBuilder();
        for (String item : observed.values()) sb.append(item).append("\n\n");
        devices.setText(sb.toString());
    }

    static double estimateDistance(int txPowerDbm, int rssiDbm) {
        return Math.pow(10.0, (txPowerDbm - rssiDbm) / (10.0 * PATH_LOSS_EXPONENT));
    }

    @Override protected void onStop() { stopScanning(); super.onStop(); }
}
