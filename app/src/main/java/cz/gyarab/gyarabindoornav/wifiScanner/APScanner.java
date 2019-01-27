package cz.gyarab.gyarabindoornav.wifiScanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class APScanner {

    private static String aps = "FC:15:B4:BC:AB:29\t028/ředitelna; 1B.1.80\t" +
            "FC:15:B4:BC:AB:27\t028/chodba; 1B.13.80\t" +
            "FC:15:B4:BC:AB:0E\t031/hala; 12B.2.80\t" +
            "FC:15:B4:BC:D1:21\t032/klub; 12B.1.80\t" +
            "FC:15:B4:BC:AB:26\t106/P2; 7B.1.80\t" +
            "FC:15:B4:BC:AB:24\t126/chodba; 7B.2.80\t" +
            "FC:15:B4:BC:AB:1C\t126/chodba; 7B.3.80\t" +
            "FC:15:B4:BC:AB:0F\tT02/VT; 12B.3.80\t" +
            "14:58:D0:67:EF:15\t105/P3\t" +
            "14:58:D0:67:EF:14\t109/P1\t" +
            "FC:15:B4:BC:AB:13\t222/chodba; 8A.1.80\t" +
            "FC:15:B4:BC:AB:12\t222/chodba; 8A.2.80\t" +
            "FC:15:B4:BC:AB:19\t222/chodba; 8A.3.80\t" +
            "FC:15:B4:BC:AB:28\t222/chodba; 8A.4.80\t" +
            "FC:15:B4:BC:AB:25\t319/chodba; 10A.1.80\t" +
            "FC:15:B4:BC:AB:33\t314/knihovna; 10A.2.80 (via 11.8.A)\t" +
            "FC:15:B4:BC:AB:2A\t318/studovna; 10A.3.80 (via 11.7.B)\t" +
            "FC:15:B4:BC:AB:2B\t319/chodba; 10A.4.80\t" +
            "14:58:D0:67:EF:13\t'-17/serverovna\t";

    private static Map<String, String> dict;
    private static APScanner self;

    public static synchronized APScanner getInstance(){
        if(self == null){
            self = new APScanner();
        }
        return self;
    }

    public APScanner() {
        Scanner scanner = new Scanner(aps);
        scanner.useDelimiter("\t");

        dict = new HashMap<>();

        while (scanner.hasNext()){
            dict.put(scanner.next(), scanner.next());
        }
    }

    public Map<String, String> getMap(){
        return dict;
    }

}
