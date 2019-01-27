package cz.gyarab.gyarabindoornav.analyze;

import java.util.ArrayList;

import cz.gyarab.gyarabindoornav.buildingScanner.Entry;
import cz.gyarab.gyarabindoornav.buildingScanner.SignalEntry;

public class Test {
    static Entry testEntry;

    static {
        //[16, 15]
        ArrayList<SignalEntry> list = new ArrayList<>();
        list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3", 52));
        list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:e0", 30));
        list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c2:90", 24));
        list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c4:10", 15));
        testEntry = new Entry(list);
    }

    public static int getAPSig(String ssid, String bssid){
        for (SignalEntry signalEntry : testEntry.list)
            if (signalEntry.getSSID().contentEquals(ssid) && signalEntry.getBSSID().contentEquals(bssid))
                return signalEntry.getSignal();
        return 0;
    }
}
