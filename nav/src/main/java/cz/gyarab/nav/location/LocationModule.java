package cz.gyarab.nav.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import cz.gyarab.nav.map.MapAdapter;

/**
 * lazy singleton třída pro skenování wifi signálu
 */
public class LocationModule {

    private static LocationModule instance;
    private WifiManager wifiManager;
    private Context context;
    private boolean live = true;

    private List<ScanResult> results;
    private Entry entries[][] = new Entry[MapAdapter.PLAN_WIDTH][MapAdapter.PLAN_HEIGHT];
    private  final String SSID = "GYM_ARABSKA";
    private int minDiff = Integer.MAX_VALUE;
    private int[] minDiffCoords = new int[2];

    private ScannedListener listener;
    public interface ScannedListener {
        void onScanned(int[] maxDiffCoords);
    }

    public static synchronized LocationModule getInstance(Context context){
        if(instance == null){
            instance = new LocationModule(context);
        }
        return instance;
    }

    private LocationModule(final Context context){
        wifiManager = (WifiManager) context.getApplicationContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.context = context;

        if (!wifiManager.isWifiEnabled()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
                }
            });

            wifiManager.setWifiEnabled(true);
        }

        loadJSON("reference_data.json");

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (live){
                    scanWifi(context);
                    //rozestupy mezi jednotlivými skeny (10s) - uspora beterie
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "location_thread").start();
    }

    public void scanWifi(final Context context) {
        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();

    }

    /**
     * spustí se po obdržení výsledků skenu wifi
     */
    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            context.unregisterReceiver(this);
            setRects();
        }
    };

    /**
     * hledá referenční čtverec s nejmenším rozdílem
     */
    private void setRects(){
        for (int i = 0; i < MapAdapter.PLAN_HEIGHT; i++){
            for (int j = 0; j < MapAdapter.PLAN_WIDTH; j++) {
                int difference = getDifference(j, i);
                if (difference < minDiff){
                    minDiff = difference;
                    minDiffCoords[0] = j;
                    minDiffCoords[1] = i;
                }
            }
        }
        //změní polohu uživatele
        System.out.println("změna");
        if (listener!=null)
            listener.onScanned(minDiffCoords);

        minDiff = Integer.MAX_VALUE;
    }

    /**
     * Vypočítá o kolik se v součtu liší naskenované hodnoty od těch referenčních
     * @param x souřadnice aktuální pozice
     * @param y souřadnice aktuální pozice
     * @return celkový rozdíl od referenčních hodnot
     */
    private int getDifference(int x, int y){
        if (entries[x][y] == null)return Integer.MAX_VALUE;
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
    private int getAPSig(String ssid, String bssid, ArrayList<ScanResult> scanResults){
        for (ScanResult signalEntry : scanResults)
            if (signalEntry.SSID.contentEquals(ssid) && signalEntry.BSSID.contentEquals(bssid)){
                scanResults.remove(signalEntry);
                return SignalEntry.getSignal(signalEntry.level);
            }
        //pokud neobsahuje dotazované AP
        return 0;
    }

    /**
     * načítá Json tabulku referenčních hodnot
     * @param name
     */
    private void loadJSON(String name){
        //nahrání json souboru
        String json = "";
        try (InputStream is = context.getAssets().open(name)) {
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

    public void setLive(boolean live) {
        this.live = live;
    }

    public void setListener(ScannedListener listener) {
        this.listener = listener;
    }
}
