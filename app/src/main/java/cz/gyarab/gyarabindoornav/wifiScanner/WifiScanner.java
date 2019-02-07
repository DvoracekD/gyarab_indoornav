package cz.gyarab.gyarabindoornav.wifiScanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import cz.gyarab.gyarabindoornav.buildingScanner.SignalEntry;

public class WifiScanner {

    private WifiManager wifiManager;
    private List<ScanResult> results;
    private ArrayAdapter adapter;
    private ArrayList<String> arrayList;
    private Activity context;
    //private ArrayList<ScanResult> arrayList = new ArrayList<>();

    //MAC adresa hotspotu a patro
    /*static {
        accessPointMap = new HashMap<>();
        accessPointMap.put("60:e3:27:cd:0f:4f", 1);
        accessPointMap.put("e4:be:ed:31:0a:e4", 3);
        accessPointMap.put("10:fe:ed:93:22:07", 0);
    }*/

    public WifiScanner(Activity context, ListView listView) {

        this.context = context;

        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(context, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        //scanWifi(context);
    }

    public void scanWifi(Context context) {
        arrayList.clear();
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(context, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            List<SignalEntry> scanList = new ArrayList<>();
            context.unregisterReceiver(this);
            adapter.notifyDataSetChanged();

            for (int i = 1; i <= results.size(); i++) {
                ScanResult scanResult = results.get(i-1);

                if (!MainActivity.filterOn || scanResult.SSID.equals("GYM_ARABSKA") && MainActivity.filterOn)
                    arrayList.add(i +". " + scanResult.BSSID + " " + WifiManager.calculateSignalLevel(scanResult.level, 100) + "%");
                adapter.notifyDataSetChanged();
                scanList.add(new SignalEntry(scanResult.SSID, scanResult.BSSID, scanResult.level));
            }
            saveScan(scanList);
        };
    };

    private void saveScan(final List<SignalEntry> list){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (FileOutputStream fos = new FileOutputStream(context.getFileStreamPath("scan.tmp"))) {

                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(list);
                    context.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
