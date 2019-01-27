package cz.gyarab.gyarabindoornav.buildingScanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import cz.gyarab.gyarabindoornav.R;

public class PlanScannerActivity extends AppCompatActivity {

    private final int PLAN_WIDTH = 36;
    private final int PLAN_HEIGHT = 25;
    private MyRoundButton buttons[][] = new MyRoundButton[PLAN_WIDTH][PLAN_HEIGHT];
    private MyRoundButton activePosition;
    private Entry entries[][] = new Entry[PLAN_WIDTH][PLAN_HEIGHT];
    private WifiManager wifiManager;
    private List<ScanResult> results;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan_menu_item:
                scanWifi(this);
                return true;
            case R.id.content_menu_item:
                FragmentManager fragmentManager = getSupportFragmentManager();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_scanner);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        try (FileInputStream fis = new FileInputStream(getFileStreamPath("out.tmp"))) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            entries = (Entry[][]) ois.readObject();
            if (entries == null)entries = new Entry[PLAN_WIDTH][PLAN_HEIGHT];
            Toast.makeText(getApplicationContext(), "Loaded", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        GridLayout planCanvas = findViewById(R.id.plan_canvas);
        planCanvas.setRowCount(PLAN_HEIGHT);
        planCanvas.setColumnCount(PLAN_WIDTH);
        planCanvas.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                return false;
            }
        });

        for (int i = 0; i < PLAN_HEIGHT; i++){
            for (int j = 0; j < PLAN_WIDTH; j++) {
                buttons[j][i] = new MyRoundButton(this, j, i);
                planCanvas.addView(buttons[j][i]);
                if (entries[j][i]!=null)buttons[j][i].setBackgroundColor(Color.argb(0.5f,0f,1f,0f));
                buttons[j][i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked){
                            activePosition = (MyRoundButton) buttonView;
                            Toast.makeText(getApplicationContext(), "[" + activePosition.x +", "+activePosition.y+"]", Toast.LENGTH_SHORT).show();
                            for (int k = 0; k < PLAN_WIDTH; k++) {
                                for (int l = 0; l < PLAN_HEIGHT; l++) {
                                    buttons[k][l].setChecked(false);
                                }
                            }
                            buttonView.setChecked(true);
                        }
                    }
                });
            }
        }
    }


    public void scanWifi(Context context) {
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(context, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            context.unregisterReceiver(this);

            entries[activePosition.x][activePosition.y] = null;
            activePosition.setBackgroundColor(Color.GREEN);

            for (ScanResult scanResult : results) {

                if (/*scanResult.SSID.equals("GYM_ARABSKA")*/ true){
                    if (entries[activePosition.x][activePosition.y] == null)
                        entries[activePosition.x][activePosition.y] = new Entry(new SignalEntry(scanResult.SSID, scanResult.BSSID, scanResult.level));
                    else
                        entries[activePosition.x][activePosition.y].list.add(new SignalEntry(scanResult.SSID, scanResult.BSSID, scanResult.level));
                }
                //adapter.notifyDataSetChanged();
            }
            Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
            saveState();
        };
    };

    private void saveState(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (FileOutputStream fos = openFileOutput("out.tmp", Context.MODE_PRIVATE)) {

                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(entries);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
