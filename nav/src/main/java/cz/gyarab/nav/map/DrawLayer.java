package cz.gyarab.nav.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawLayer extends AppCompatImageView {

    private Paint paint;

    public DrawLayer(Context context) {
        super(context);
        init();
    }

    public DrawLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setClickListeners();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.parseColor("#16A7F0"));
        paint.setStrokeWidth(200);
        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float[] points = {250,142,145,144,145,144,2154,1154};
        canvas.drawLines(points, paint);
    }

    /**
     * kontrola kliknutí na mapu a předání souřadnice kliknutí
     */
    public void setClickListeners(){
        final float[] lastXY = new float[2];

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    lastXY[0] = event.getX();
                    lastXY[1] = event.getY();
                }
                return false;
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] plan = MapAdapter.getPlanField(lastXY[0], lastXY[1]);
                Log.d("debug", "onTouch: "+ plan[0]+"   "+ plan[1]);
            }
        });
    }
}
