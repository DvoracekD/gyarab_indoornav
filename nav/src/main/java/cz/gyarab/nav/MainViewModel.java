package cz.gyarab.nav;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import cz.gyarab.nav.dijkstra.DijkstraAlgorithm;
import cz.gyarab.nav.dijkstra.Graph;
import cz.gyarab.nav.map.GraphLoader;
import cz.gyarab.nav.location.LocationModule;
import cz.gyarab.nav.map.MapAdapter;
import cz.gyarab.nav.modules.MotionModule;
import cz.gyarab.nav.modules.CompassArrow;
import cz.gyarab.nav.modules.CompassModule;

public class MainViewModel extends AndroidViewModel {

    private CompassArrow compassArrow;
    private CompassModule compassModule;
    private MotionModule motionModule;
    private LocationModule locationModule;
    private Graph mGraph;
    private DijkstraAlgorithm dijkstra;
    boolean routeOffButtonHidden = false;
    private boolean enableLocation = true;
    //temporery
    boolean useLocation = true;

    private GraphLoader.GraphLoadedListener graphLoadedListener;

    public MainViewModel(@NonNull Application application) {
        super(application);
        SensorManager sensorManager = (SensorManager) application.getSystemService(Context.SENSOR_SERVICE);

        compassArrow = new CompassArrow(0);
        compassModule = new CompassModule(sensorManager);
        motionModule = new MotionModule(sensorManager);

        //nastaví otočení plánu od azimutu 0
        compassModule.setAzimuthFix(60);

        //modul location
        if (enableLocation) {
            locationModule = LocationModule.getInstance(getApplication());
            locationModule.setListener(new LocationModule.ScannedListener() {
                @Override
                public void onScanned(int[] minDiffCoords) {
                    if (useLocation)compassArrow.setPositionAnimated(MapAdapter.getMapCoordinate(minDiffCoords[0]), MapAdapter.getMapCoordinate(minDiffCoords[1]));
                }
            });
        }

        //vytvoření instance GraphLoaderu
        GraphLoader graphLoader = new GraphLoader(getApplication());
        //po nahrání grafu ze souboru je spuštěna metoda listeneru
        graphLoader.setListener(new GraphLoader.GraphLoadedListener() {
            @Override
            public void onGraphLoaded(Graph graph) {
                //logika po nahrání grafu
                mGraph = graph;
                dijkstra = new DijkstraAlgorithm(graph);

                //zpráva pro hlavní aktivitu
                graphLoadedListener.onGraphLoaded(graph);
            }
        });
        graphLoader.execute("map_graph.json");

    }

    public void disableLocation(){
        enableLocation = false;
    }

    public void setGraphLoadedListener(GraphLoader.GraphLoadedListener graphLoadedListener) {
        this.graphLoadedListener = graphLoadedListener;
    }

    public MotionModule getMotionModule() {
        return motionModule;
    }

    public CompassModule getCompassModule() {
        return compassModule;
    }

    public CompassArrow getCompassArrow() {
        return compassArrow;
    }

    public void rebuildDijkstra(){
        dijkstra = new DijkstraAlgorithm(mGraph);
    }

    public DijkstraAlgorithm getDijkstra() {
        return dijkstra;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        locationModule.setLive(false);
        compassArrow = null;
        compassModule = null;
        motionModule = null;
    }

}
