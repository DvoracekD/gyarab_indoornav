package cz.gyarab.gyarabindoornav.buildingScanner;

import java.io.Serializable;
import java.util.ArrayList;

public class Entry implements Serializable{

    public ArrayList<SignalEntry> list;

    public Entry(ArrayList<SignalEntry> list) {
        this.list = list;
    }

    public Entry(SignalEntry signalEntry) {
        list = new ArrayList<>();
        list.add(signalEntry);
    }

}
