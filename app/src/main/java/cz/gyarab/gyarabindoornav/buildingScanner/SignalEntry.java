package cz.gyarab.gyarabindoornav.buildingScanner;

import android.net.wifi.WifiManager;

import java.io.Serializable;

public class SignalEntry implements Serializable{
    private String SSID;
    private String BSSID;
    private int signal;

    public SignalEntry(String SSID, String BSSID, int signal) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.signal = signal;
    }

    public String getSSID() {
        return SSID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public int getSignal() {
        return WifiManager.calculateSignalLevel(signal, 100);
    }

    public static int getSignal(int signal){
        return WifiManager.calculateSignalLevel(signal, 100);
    }

    public int getRawSignal(){
        return signal;
    }
}
