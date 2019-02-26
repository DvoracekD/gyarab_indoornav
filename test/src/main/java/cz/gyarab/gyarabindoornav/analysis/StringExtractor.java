package cz.gyarab.gyarabindoornav.analysis;

import java.util.Comparator;

import cz.gyarab.gyarabindoornav.buildingScanner.Entry;
import cz.gyarab.gyarabindoornav.buildingScanner.MyRoundButton;
import cz.gyarab.gyarabindoornav.buildingScanner.SignalEntry;

public class StringExtractor {

    /**
     * Vrátí informace o daném bodě
     * @param activePosition daný čtverec
     * @param entry naskenované hodnoty
     * @return
     */
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
        return result;
    }
}
