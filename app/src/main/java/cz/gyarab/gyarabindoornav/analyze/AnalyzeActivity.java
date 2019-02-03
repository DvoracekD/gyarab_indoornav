package cz.gyarab.gyarabindoornav.analyze;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.JsonWriter;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import cz.gyarab.gyarabindoornav.R;
import cz.gyarab.gyarabindoornav.buildingScanner.Entry;
import cz.gyarab.gyarabindoornav.buildingScanner.MyRoundButton;
import cz.gyarab.gyarabindoornav.buildingScanner.SignalEntry;

public class AnalyzeActivity extends AppCompatActivity {

    private final int PLAN_WIDTH = 36;
    private final int PLAN_HEIGHT = 25;
    private MyRoundButton buttons[][] = new MyRoundButton[PLAN_WIDTH][PLAN_HEIGHT];
    private MyRoundButton activePosition;
    private Entry entries[][] = new Entry[PLAN_WIDTH][PLAN_HEIGHT];
    private List<ScanResult> results;
    private WifiManager wifiManager;
    private GridLayout planCanvas;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.scan_menu_item:
                scanWifi(this);
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

//        entries[activePosition.x][activePosition.y] = null;
//        activePosition.setBackgroundColor(Color.GREEN);
//
//        for (ScanResult scanResult : results) {
//
//            if (/*scanResult.SSID.equals("GYM_ARABSKA")*/ true){
//                if (entries[activePosition.x][activePosition.y] == null)
//                    entries[activePosition.x][activePosition.y] = new Entry(new SignalEntry(scanResult.SSID, scanResult.BSSID, scanResult.level));
//                else
//                    entries[activePosition.x][activePosition.y].list.add(new SignalEntry(scanResult.SSID, scanResult.BSSID, scanResult.level));
//            }
//            //adapter.notifyDataSetChanged();
//        }
        Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show();
        setRects();
        }
    };

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

        planCanvas = findViewById(R.id.plan_canvas);
        planCanvas.setRowCount(PLAN_HEIGHT);
        planCanvas.setColumnCount(PLAN_WIDTH);

//        for (int i = 0; i < PLAN_HEIGHT; i++){
//            for (int j = 0; j < PLAN_WIDTH; j++) {
//                if (entries[j][i] != null){
//                    int difference = 0;
//                    for (SignalEntry signalEntry : entries[j][i].list){
//                        if (signalEntry.getSSID().contentEquals("GYM_ARABSKA"))
//                            difference += (Test.getAPSig(signalEntry.getSSID(), signalEntry.getBSSID()));
//                    }
//                    buttons[j][i].setDiff(difference);
//                }
//            }
//        }
    }

    private void setRects(){
        for (int i = 0; i < PLAN_HEIGHT; i++){
            for (int j = 0; j < PLAN_WIDTH; j++) {

                buttons[j][i] = new MyRoundButton(this, j, i);
                planCanvas.addView(buttons[j][i]);
                int difference = getDifference(j, i);
                if (difference == 255)continue;
                float green = (1-(Math.abs(difference)/255f));
                if (difference > 255) green = 0;
                buttons[j][i].setBackgroundColor(Color.argb( 0.5f, 0f , green > 1 ? 1 : green, 0f ));

//                if (entries[j][i]!=null)buttons[j][i].setBackgroundColor(Color.argb(127, 0, 255, 0));
//                buttons[j][i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//                    @Override
//                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                        if (isChecked){
//                            activePosition = (MyRoundButton) buttonView;
//                            Toast.makeText(getApplicationContext(), "[" + activePosition.x +", "+activePosition.y+"]", Toast.LENGTH_SHORT).show();
//                            for (int k = 0; k < PLAN_WIDTH; k++) {
//                                for (int l = 0; l < PLAN_HEIGHT; l++) {
//                                    buttons[k][l].setChecked(false);
//                                }
//                            }
//                            Log.d("entry", StringExtractor.getStringContent(activePosition, entries[activePosition.x][activePosition.y]));
//                            buttonView.setChecked(true);
//                        }
//                    }
//                });
            }
        }
    }

    private int getDifference(int x, int y){
        if (entries[x][y] == null)return 255;
        //pokud vidí víc AP nebo nějaké nevidí, získá 20 trestných bodů
        final int PENALTY = 50;
        int difference = 0;
        int used = 0;
        for (SignalEntry entry : entries[x][y].list){
            if (entry.getSSID().contains("GYM_ARABSKA")) {
                //silá naskenovaného signálu
                int scanSignal = getAPSig(entry.getSSID(), entry.getBSSID());
                //pokud bod vidí signál navíc
                if (scanSignal == 0)
                    difference += PENALTY;
                //přičte známku za jeden společný AP
                difference += Math.abs(scanSignal - entry.getSignal());
                used++;
            }
        }
        //pro ty AP, ktere byly naskenovany ale v referencnim bodu chybí
        if (entries[x][y].list.size() != 0){
            int usableSignals = 0;
            for (SignalEntry e : entries[x][y].list)
                if (e.getSSID().contains("GYM_ARABSKA"))
                    usableSignals++;
            difference += ((usableSignals - used)*PENALTY);
        }
        return difference;
    }

    /**
     *
     * @param ssid
     * @param bssid
     * @return rozdíl v síle signálu (-1 pokud vůbec signál nezná)
     */
    public int getAPSig(String ssid, String bssid){
        for (ScanResult signalEntry : results)
            if (signalEntry.SSID.contentEquals(ssid) && signalEntry.BSSID.contentEquals(bssid))
                return signalEntry.level;
        //pokud neopsahuje dotazované AP
        return 0;
    }

    public void saveJson() throws JSONException {
        JSONArray xJsonArray = new JSONArray();
        for (int i=0; i<PLAN_WIDTH; i++){
            JSONArray yJsonArray = new JSONArray();
            for (int j =0; j<PLAN_HEIGHT; j++){
                JSONArray entryJsonArray = new JSONArray();
                if (entries[i][j] != null) {
                    for (SignalEntry signalEntry : entries[i][j].list) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("SSID", signalEntry.getSSID());
                        jsonObject.put("BSSID", signalEntry.getBSSID());
                        jsonObject.put("signal", signalEntry.getSignal());
                        entryJsonArray.put(jsonObject);
                    }
                }
                yJsonArray.put(entryJsonArray);
            }
            xJsonArray.put(yJsonArray);
        }
        String result = xJsonArray.toString();
        try {
            OutputStreamWriter out = new OutputStreamWriter(openFileOutput("data.json", Context.MODE_PRIVATE));
            out.write(xJsonArray.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

//    private void saveState(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try (FileOutputStream fos = openFileOutput("out.tmp", Context.MODE_PRIVATE)) {
//
//                    ObjectOutputStream oos = new ObjectOutputStream(fos);
//                    oos.writeObject(entries);
//                    runOnUiThread(new Runnable() {
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();
//    }
}
