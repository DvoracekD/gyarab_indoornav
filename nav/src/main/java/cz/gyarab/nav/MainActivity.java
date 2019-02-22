package cz.gyarab.nav;

import android.arch.lifecycle.ViewModelProviders;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.otaliastudios.zoom.ZoomLayout;

import cz.gyarab.nav.dijkstra.DijkstraAlgorithm;
import cz.gyarab.nav.map.DrawLayer;
import cz.gyarab.nav.modules.CompassArrow;
import cz.gyarab.nav.modules.CompassModule;
import cz.gyarab.nav.map.MapAdapter;
import cz.gyarab.nav.modules.MotionModule;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    private CompassModule compassModule;
    private MotionModule motionModule;
    private CompassArrow compassArrow;
    private ZoomLayout zoomLayout;
    private ImageView routeOffButton;

    //ekvivalent 0.5m v pixelech na plánu
    public static int stepSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //inicializace viewModelu aplikace, který udržuje veškerá data
        //Musí být před voláním super třídy
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zoomLayout = findViewById(R.id.zoomLayout);

        //inicializace šipky kompasu
        compassArrow = viewModel.getCompassArrow();
        compassArrow.setImage((ImageView) findViewById(R.id.compass_arrow));

        //inicializace modulu kompasu
        compassModule = viewModel.getCompassModule();
        compassModule.setListener(new CompassModule.CompassListener() {
            @Override
            public void onNewAzimuth(float azimuth) {
                compassArrow.adjustArrow(azimuth);
            }
        });

        //inicializace modulu pohybu(detekce směru a trvání chůze)
        motionModule = viewModel.getMotionModule();
        motionModule.setListener(new MotionModule.MotionListener() {
            @Override
            public void onStep() {
                compassArrow.move(stepSize);
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
                //zadání výšky a šířky mapy v pixelech pro pozdější převody
                MapAdapter.init(width, height);
                //Po změně UI se vrátí na původní pozici
                compassArrow.refreshPosition();
                if (compassArrow.checkFresh()) {
                    compassArrow.setPosition(width * 0.1f, height * 0.8f);
                }
                centerCamera();
                //ekvivalent 0.5m v pixelech na plánu
                stepSize = width / 36 / 5;
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

        //temporary
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation expandIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.expand_in);
                findViewById(R.id.route_off_button).startAnimation(expandIn);
            }
        });

        //tlačítko pro smazání trasy
        final DrawLayer drawLayer = findViewById(R.id.map);
        routeOffButton = findViewById(R.id.route_off_button);
        if (viewModel.routeOffButtonHidden)hideRouteOffButton();
        routeOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawLayer.setCanDraw(false);
                drawLayer.postInvalidate();
                hideRouteOffButton();
            }
        });

        //tlačítko pro centrování mapy
        findViewById(R.id.my_location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation click = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.clicked);
                findViewById(R.id.my_location_button).startAnimation(click);
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
        motionModule.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        compassModule.stop();
        motionModule.stop();
    }

    public MainViewModel getViewModel() {
        return viewModel;
    }

    public CompassArrow getCompassArrow() {
        return compassArrow;
    }

    public void showRouteOffButton(){
        if (viewModel.routeOffButtonHidden){
            Animation expandIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.expand_in);
            routeOffButton.startAnimation(expandIn);
            routeOffButton.setVisibility(View.VISIBLE);
            viewModel.routeOffButtonHidden = false;
        }
    }

    public void hideRouteOffButton(){
        if (!viewModel.routeOffButtonHidden){
            Animation expandOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.expand_out);
            routeOffButton.startAnimation(expandOut);
            routeOffButton.setVisibility(View.INVISIBLE);
            viewModel.routeOffButtonHidden = true;
        }
    }

}
