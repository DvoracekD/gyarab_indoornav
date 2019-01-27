package cz.gyarab.location;

import java.util.ArrayList;
import java.util.List;

public class Test {

    static Entry testEntry;
    static int count;

    static {
        ArrayList<SignalEntry> list = new ArrayList<>();
        switch ("16,18"){
            case "16,15":
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:f0", 52));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:e0", 30));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c2:90", 24));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c4:10", 15));
                break;
            case "5,21":
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c0:d0", 80));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c0:c0", 99));
                list.add(new SignalEntry("GYM_ARABSKA", "a0:d3:c1:b6:7d:70", 50));
                break;
            case "2,10":
                list.add(new SignalEntry("GYM_ARABSKA", "a0:d3:c1:b6:7d:70", 90));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c0:f0", 70));
                list.add(new SignalEntry("GYM_ARABSKA", "a0:d3:c1:b6:7d:60", 59));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c0:e0", 41));
                break;
            case "17,18":
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:e0", 99));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:f0", 99));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c2:90", 66));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c4:10", 66));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c2:30", 41));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c2:80", 37));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c4:00", 24));
                break;
            case "16,18":
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:e0", 81));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c2:90", 79));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c3:f0", 72));
                list.add(new SignalEntry("GYM_ARABSKA", "28:80:23:24:c4:10", 61));
                break;
        }
        count = list.size();
        testEntry = new Entry(list);
    }

    /**
     *
     * @param ssid
     * @param bssid
     * @return rozdíl v síle signálu (-1 pokud vůbec signál nezná)
     */
    public static int getAPSig(String ssid, String bssid){
        for (SignalEntry signalEntry : testEntry.list)
            if (signalEntry.getSSID().contentEquals(ssid) && signalEntry.getBSSID().contentEquals(bssid))
                return signalEntry.getSignal();
        //pokud neopsahuje dotazované AP
        return 0;
    }
}
