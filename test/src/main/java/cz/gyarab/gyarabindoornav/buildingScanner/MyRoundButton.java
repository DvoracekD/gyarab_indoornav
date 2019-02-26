package cz.gyarab.gyarabindoornav.buildingScanner;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.RadioButton;

public class MyRoundButton extends android.support.v7.widget.AppCompatRadioButton {

    public int x;
    public int y;

    public MyRoundButton(Context context) {
        super(context);
    }

    public MyRoundButton(Context context, int x, int y) {

        super(context);
        this.x = x;
        this.y = y;

    }

}
