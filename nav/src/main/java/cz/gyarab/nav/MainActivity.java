package cz.gyarab.nav;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.otaliastudios.zoom.ZoomLayout;

import java.util.ArrayList;
import java.util.Set;

import cz.gyarab.nav.dijkstra.Graph;
import cz.gyarab.nav.map.DrawLayer;
import cz.gyarab.nav.map.DrawLayerViewModel;
import cz.gyarab.nav.map.GraphLoader;
import cz.gyarab.nav.modules.CompassArrow;
import cz.gyarab.nav.modules.CompassModule;
import cz.gyarab.nav.map.MapAdapter;
import cz.gyarab.nav.modules.MotionModule;
import cz.gyarab.nav.modules.SearchBar;

public class MainActivity extends AppCompatActivity {

    private MainViewModel viewModel;

    private CompassModule compassModule;
    private MotionModule motionModule;
    private CompassArrow compassArrow;
    private ZoomLayout zoomLayout;
    private ImageView routeOffButton;
    private AutoCompleteTextView textView;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    //ekvivalent 0.5m v pixelech na plánu
    public static int stepSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //požadavek o přístup k wifi
        requestPermission();

        //inicializace viewModelu aplikace, který udržuje veškerá data
        //Musí být před voláním super třídy
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zoomLayout = findViewById(R.id.zoomLayout);

        //inicializace šipky kompasu
        compassArrow = viewModel.getCompassArrow();
        compassArrow.setImage((ImageView) findViewById(R.id.compass_arrow));
        compassArrow.setOnMoveListener(new CompassArrow.onMoveListener() {
            @Override
            public void onMove() {
                centerCamera();
            }
        });

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
                doStep();
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
                if (compassArrow.checkFresh()) {
                    compassArrow.setPosition(width * 0.1f, height * 0.8f);
                }else{
                    compassArrow.refreshPosition();
                }
                //ekvivalent 0.5m v pixelech na plánu
                stepSize = width / 36 / 5;
            }
        });

        //temporary
        findViewById(R.id.step).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doStep();
                centerCamera();
            }
        });

        //temporary
        CheckBox checkBox = findViewById(R.id.use_location);
        checkBox.setChecked(true);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.useLocation = isChecked;
            }
        });

        //tlačítko pro smazání trasy
        final DrawLayer drawLayer = findViewById(R.id.map);
        routeOffButton = findViewById(R.id.route_off_button);
        hideRouteOffButton();
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

        //vyhledávací okno
        textView = findViewById(R.id.searchView);
        if (viewModel.getDijkstra() != null)
            setTextViewAdapter(viewModel.getDijkstra().getGraph().getNames().keySet());
        else
            viewModel.setGraphLoadedListener(new GraphLoader.GraphLoadedListener() {
                @Override
                public void onGraphLoaded(Graph graph) {
                    setTextViewAdapter(graph.getNames().keySet());
                }
            });

        final DrawLayerViewModel drawLayerViewModel = ViewModelProviders.of(this).get(DrawLayerViewModel.class);

        //poslouchá jestli byla vybrána odpověď
        SearchBar searchBar = findViewById(R.id.search_layout);
        searchBar.setViewModel(ViewModelProviders.of(this).get(SearchBar.SearchBarViewModel.class));
        searchBar.setListener(new SearchBar.OptionSelectedListener() {
            @Override
            public void onOptionSelected(String option) {
                drawLayerViewModel.updateFinish(viewModel.getDijkstra().getGraph().getNames().get(option));
                textView.clearFocus();
            }
        });

    }

    private void setTextViewAdapter(Set<String> namesList){
        ArrayList<String> namesArrayList = new ArrayList<>(namesList);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line, namesArrayList);
        textView.setAdapter(adapter);
    }

    /**
     * zacentruje pohled na současnou polohu
     */
    public void centerCamera(){
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

    private void doStep(){
        if (compassArrow.move(stepSize)){
            DrawLayerViewModel drawLayerViewModel = ViewModelProviders.of(this).get(DrawLayerViewModel.class);
            int x = MapAdapter.getPlanField(compassArrow.getX());
            int y = MapAdapter.getPlanField(compassArrow.getY());
            drawLayerViewModel.updateRoute(viewModel.getDijkstra().getGraph().getVertex(x, y));
        }
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

    /**
     * zdroj: https://developer.android.com/training/permissions/requesting
     */
    private void requestPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                permissionPopup();
            } else {
                // No explanation needed; request the permission
                permissionPopup();

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
    }

    private void permissionPopup(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }

    /**
     * zdroj: https://developer.android.com/training/permissions/requesting
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
