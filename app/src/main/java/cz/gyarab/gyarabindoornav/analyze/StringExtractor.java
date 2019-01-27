package cz.gyarab.gyarabindoornav.analyze;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cz.gyarab.gyarabindoornav.buildingScanner.Entry;
import cz.gyarab.gyarabindoornav.buildingScanner.MyRoundButton;
import cz.gyarab.gyarabindoornav.buildingScanner.Position;
import cz.gyarab.gyarabindoornav.buildingScanner.SignalEntry;

public class StringExtractor {
    public static String getStringContent(MyRoundButton activePosition, Entry entry){
        if (activePosition == null || entry == null)return "";
        entry.list.sort(new Comparator<SignalEntry>() {
            @Override
            public int compare(SignalEntry o1, SignalEntry o2) {
                return o2.getSSID().compareTo(o1.getSSID());
            }
        });
        String result = "";
        result+= "[" + activePosition.x +", "+activePosition.y+"]\n";
        for (SignalEntry signalEntry: entry.list) {
            result += signalEntry.getSSID() + ", " + signalEntry.getBSSID() + ", " + signalEntry.getSignal()+"\n";
        }
        result += "difference"+activePosition.getDiff() + "%\n";
        return result;
    }
}
