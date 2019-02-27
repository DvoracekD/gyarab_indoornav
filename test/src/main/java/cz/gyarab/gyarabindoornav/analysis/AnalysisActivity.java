package cz.gyarab.gyarabindoornav.analysis;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cz.gyarab.gyarabindoornav.R;
import cz.gyarab.gyarabindoornav.buildingScanner.Entry;
import cz.gyarab.gyarabindoornav.buildingScanner.MyRoundButton;
import cz.gyarab.gyarabindoornav.buildingScanner.SignalEntry;

public class AnalysisActivity extends AppCompatActivity {

    private final int PLAN_WIDTH = 36;
    private final int PLAN_HEIGHT = 25;
    private  final String SSID = "GYM_ARABSKA";
    private MyRoundButton buttons[][] = new MyRoundButton[PLAN_WIDTH][PLAN_HEIGHT];
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

    /**
     * spustí se po obdržení výsledků skenu wifi
     */
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        results = wifiManager.getScanResults();
        context.unregisterReceiver(this);

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

        loadJSON("data.json");

        planCanvas = findViewById(R.id.plan_canvas);
        planCanvas.setRowCount(PLAN_HEIGHT);
        planCanvas.setColumnCount(PLAN_WIDTH);

    }

    /**
     * nastavuje barvu jednotlivým čtvercům podle rozdílu signálu od referenčního skenu
     */
    private void setRects(){
        for (int i = 0; i < PLAN_HEIGHT; i++){
            for (int j = 0; j < PLAN_WIDTH; j++) {

                if (buttons[j][i] == null){
                    buttons[j][i] = new MyRoundButton(this, j, i);
                    planCanvas.addView(buttons[j][i]);
                }
                int difference = getDifference(j, i);
                if (difference == 255)continue;
                float green = (1-(Math.abs(difference)/255f));
                if (difference > 255) green = 0;
                buttons[j][i].setBackgroundColor(Color.argb( 0.5f, 0f , green > 1 ? 1 : green, 0f ));
            }
        }
    }

    /**
     * Vypočítá o kolik se v součtu liší naskenované hodnoty od těch referenčních
     * @param x souřadnice aktuální pozice
     * @param y souřadnice aktuální pozice
     * @return celkový rozdíl od referenčních hodnot
     */
    private int getDifference(int x, int y){
        if (entries[x][y] == null)return 255;
        //na začátku je zkopírováno pole s výsledky scanu, aby se s ním mohlo pracovat
        ArrayList<ScanResult> scanResults = new ArrayList<>(results);
        int difference = 0;
        for (SignalEntry entry : entries[x][y].list){
            if (entry.getSSID().contains(SSID)) {
                //silá naskenovaného signálu
                int scanSignal = getAPSig(entry.getSSID(), entry.getBSSID(), scanResults);
                difference += Math.abs(scanSignal - entry.getRawSignal());//raw signal protoze v jsonu je ulozen v procentech
            }
        }
        //V seznamu naskenovaných wifi zbyly ty, které jsou navíc. Budou přičteny k rozdílu
        for (ScanResult result : scanResults){
            if (result.SSID.contains(SSID))
                difference += SignalEntry.getSignal(result.level);
        }

        return difference;
    }

    /**
     * Zjťuje, zdali je dotazovaná wifi v seznamu naskenovaných wifi. Pokud ano, ze seznamu ji smaže
     * @param ssid
     * @param bssid
     * @return rozdíl v síle signálu v procentech(0 pokud vůbec signál nezná)
     */
    public int getAPSig(String ssid, String bssid, ArrayList<ScanResult> scanResults){
        for (ScanResult signalEntry : scanResults)
            if (signalEntry.SSID.contentEquals(ssid) && signalEntry.BSSID.contentEquals(bssid)){
                scanResults.remove(signalEntry);
                return SignalEntry.getSignal(signalEntry.level);
            }
        //pokud neobsahuje dotazované AP
        return 0;
    }

    /**
     * Ukládá do Jsonu referenční tabulku hodnot
     * @throws JSONException
     */
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

    /**
     * načítá Json tabulku referenčních hodnot
     * @param name
     */
    private void loadJSON(String name){
        //nahrání json souboru
        String json = "";
        try (InputStream is = getAssets().open(name)) {
            json = new Scanner(is).useDelimiter("\\A").next();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JSONArray xJsonArray = new JSONArray(json);
            entries = new Entry[xJsonArray.length()][xJsonArray.getJSONArray(0).length()];

            for (int i = 0; i < xJsonArray.length(); i++) {
                JSONArray yJsonArray = xJsonArray.getJSONArray(i);
                for (int j = 0; j < yJsonArray.length(); j++) {
                    JSONArray jsonEntry = yJsonArray.getJSONArray(j);
                    ArrayList<SignalEntry> signalEntries = new ArrayList<>();
                    for (int k = 0; k < jsonEntry.length(); k++) {
                        JSONObject jsonSignalEntry =  jsonEntry.getJSONObject(k);
                        signalEntries.add(new SignalEntry(
                                jsonSignalEntry.getString("SSID"),
                                jsonSignalEntry.getString("BSSID"),
                                jsonSignalEntry.getInt("signal")));
                    }
                    if (jsonEntry.length()!= 0)entries[i][j] = new Entry(signalEntries);
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * ukládá jeden scan jako objekt do souboru
     */
    private void saveScan(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try (FileOutputStream fos = openFileOutput("scan.tmp", Context.MODE_PRIVATE)) {

                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(results);
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
