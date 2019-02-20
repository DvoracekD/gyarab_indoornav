package cz.gyarab.nav;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.otaliastudios.zoom.ZoomLayout;

import cz.gyarab.nav.compass.CompassArrow;
import cz.gyarab.nav.compass.CompassModule;
import cz.gyarab.nav.map.MapAdapter;

public class MainActivity extends AppCompatActivity {

    private CompassModule compassModule;
    private MotionModule motionModule;
    private CompassArrow compassArrow;
    private ZoomLayout zoomLayout;

    //ekvivalent 0.5m v pixelech na plánu
    public static int stepSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zoomLayout = findViewById(R.id.zoomLayout);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //inicializace šipky kompasu
        compassArrow = new CompassArrow(findViewById(R.id.compass_arrow), 0);

        //inicializace kompasu
        compassModule = new CompassModule(sensorManager);
        compassModule.setListener(new CompassModule.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                compassArrow.adjustArrow(azimuth);
            }
        });

        //Když je vytvořený layout, nastaví se pozice šipky kompasu
        final ConstraintLayout map = findViewById(R.id.map_layout);
        map.getViewTreeObserver().addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                map.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = map.getMeasuredWidth();
                int height = map.getMeasuredHeight();
                MapAdapter.init(width, height);
                compassArrow.setPosition(width/2, height/2);
                centerCamera();
                //ekvivalent 0.5m v pixelech na plánu
                stepSize = width / 36 / 5;
            }
        });

        //inicializace modulu pohybu(detekce směru trvání chůze)
        motionModule = new MotionModule(sensorManager);
        motionModule.setListener(new MotionModule.MotionListener() {
            @Override
            public void onStep() {
                compassArrow.move(stepSize);
            }
        });

        //temporary
        findViewById(R.id.step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compassArrow.move(MainActivity.stepSize);
                centerCamera();
            }
        });

        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //tlačítko pro centrování mapy
        findViewById(R.id.my_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centerCamera();
            }
        });

    }

    /**
     * zacentruje pohled na současnou polohu
     */
    private void centerCamera(){
        float zoom = 3;
        zoomLayout.moveTo(zoom,
                -compassArrow.getX()+zoomLayout.getWidth()*zoom/2f,
                -compassArrow.getY()+zoomLayout.getHeight()*zoom/2f, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        compassModule.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassModule.stop();
    }
}
