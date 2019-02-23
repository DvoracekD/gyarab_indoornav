package cz.gyarab.nav.map;

public class StringFilter {

    public static CharSequence filter(CharSequence source){
        String original = source.toString();
        original = original.replace("Ě", "E");
        original = original.replace("Š", "S");
        original = original.replace("Č", "C");
        original = original.replace("Ř", "R");
        original = original.replace("Ž", "Z");
        original = original.replace("Ý", "Y");
        original = original.replace("Á", "A");
        original = original.replace("Í", "I");
        original = original.replace("É", "E");
        original = original.replace("Ú", "U");
        original = original.replace("Ů", "U");
        original = original.replace("Ď", "D");
        original = original.replace("Ť", "T");
        original = original.replace("Ň", "N");

        original = original.replace("ě", "e");
        original = original.replace("š", "s");
        original = original.replace("č", "c");
        original = original.replace("ř", "r");
        original = original.replace("ž", "z");
        original = original.replace("ý", "y");
        original = original.replace("á", "a");
        original = original.replace("í", "i");
        original = original.replace("é", "e");
        original = original.replace("ú", "u");
        original = original.replace("ů", "u");
        original = original.replace("ď", "d");
        original = original.replace("ť", "t");
        original = original.replace("ň", "n");

        return original;
    }

}
