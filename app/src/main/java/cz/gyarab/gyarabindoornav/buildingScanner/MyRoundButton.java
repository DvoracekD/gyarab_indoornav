package cz.gyarab.gyarabindoornav.buildingScanner;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class MyRoundButton extends android.support.v7.widget.AppCompatRadioButton {

    public int x;
    public int y;
    private int diff;

    public MyRoundButton(Context context) {
        super(context);
    }

    public MyRoundButton(Context context, int x, int y) {

        super(context);
        this.x = x;
        this.y = y;

    }

    public int getDiff() {
        return diff;
    }

    public void setDiff(int diff) {
        this.diff = diff;
    }
}
