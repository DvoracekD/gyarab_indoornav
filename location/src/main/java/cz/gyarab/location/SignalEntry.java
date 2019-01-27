package cz.gyarab.location;

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
        return signal;
    }

    @Override
    public String toString() {
        return "SignalEntry{" +
                "SSID='" + SSID + '\'' +
                ", BSSID='" + BSSID + '\'' +
                ", signal=" + signal +
                '}';
    }
}
