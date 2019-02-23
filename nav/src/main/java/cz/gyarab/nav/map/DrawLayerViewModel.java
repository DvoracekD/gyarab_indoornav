package cz.gyarab.nav.map;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;

import cz.gyarab.nav.MainViewModel;
import cz.gyarab.nav.dijkstra.DijkstraAlgorithm;
import cz.gyarab.nav.dijkstra.Vertex;

public class DrawLayerViewModel extends AndroidViewModel {

    private MainViewModel mainViewModel;

    //objekty paint
    private Paint circlePaint;
    private Paint linePaint;

    //data pro vykreslení
    private float[] points;
    private LinkedList<Vertex> path;
    private boolean canDraw = true;
    //aktuální cíl cesty
    private Vertex finish;
    //poslední pozice hráče
    private Vertex start;

    //listeners
    private MapTouchListener touchListener;
    private MapClickListener clickListener;
    private ViewUpdateListener updateListener;

    public interface ViewUpdateListener {
        void update();
    }

    public DrawLayerViewModel(@NonNull Application application) {
        super(application);
        initPaints();
        points = new float[0];
        path = new LinkedList<>();

        //inicializace listeneru
        touchListener = new MapTouchListener();
        clickListener = new MapClickListener();
    }

    private void initPaints(){
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setColor(Color.parseColor("#16A7F0"));
        linePaint.setStrokeWidth(DrawLayer.STROKE_SIZE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.parseColor("#16A7F0"));
        circlePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    /**
     * ze seznamu vrcholů vytvoří pole floatů pro android metoru vytvářející polyline
     * výsledné pole obsahuje čtveřice floatů (x1, y1, x2, y2) představující počátečný a konečný bod jedné liny
     * @param path
     */
    private void fillPoints(LinkedList<Vertex> path){

        points = new float[path.size()*4-4];

        //první bod cesty
        points[0] = MapAdapter.getMapCoordinate(path.get(0).getX());
        points[1] = MapAdapter.getMapCoordinate(path.get(0).getY());

        int pointsI = 2;
        for (int i = 1; i < path.size()-1; i++){
            points[pointsI] = MapAdapter.getMapCoordinate(path.get(i).getX());
            points[pointsI+1] = MapAdapter.getMapCoordinate(path.get(i).getY());
            points[pointsI+2] = MapAdapter.getMapCoordinate(path.get(i).getX());
            points[pointsI+3] = MapAdapter.getMapCoordinate(path.get(i).getY());
            pointsI+=4;
        }

        //poslední bod
        points[pointsI] = MapAdapter.getMapCoordinate(path.get(path.size()-1).getX());
        points[pointsI+1] = MapAdapter.getMapCoordinate(path.get(path.size()-1).getY());

    }

    public void updateFinish(Vertex finish){
        if (start == null){

            start = mainViewModel.getDijkstra().getGraph().getVertex(
                    MapAdapter.getPlanField(mainViewModel.getCompassArrow().getCenterX()),
                    MapAdapter.getPlanField(mainViewModel.getCompassArrow().getCenterY()));
        }
        executeDijkstra(start, finish);
    }

    public void updateRoute(Vertex source){

        if (finish != null)
            executeDijkstra(source, finish);

    }

    private void executeDijkstra(Vertex start, Vertex finish){
        //Získání předvytvořeného Dijkstrova algoritmu
        DijkstraAlgorithm dijkstra = mainViewModel.getDijkstra();
        if (dijkstra == null) {
            Log.e("debug", "onClick: Dijkstra = null");
            return;
        }

        dijkstra.execute(start);//spuštění algoritmu
        path = dijkstra.getPath(finish); //získání výsledné cesty
        //okamžitě po vykreslení cesty se objekt restartuje
        mainViewModel.rebuildDijkstra();

        if (path == null) {
            System.out.println(start + " -> " + finish);
            System.out.println("Nelze!");
            return;
        }

        fillPoints(path);

        System.out.println(dijkstra.getDistance(finish));
        canDraw = true;

        //překreslí obraz
        if (updateListener != null)
            updateListener.update();
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    public Paint getCirclePaint() {
        return circlePaint;
    }

    public float[] getPoints() {
        return points;
    }

    public LinkedList<Vertex> getPath() {
        return path;
    }

    public MapTouchListener getTouchListener() {
        return touchListener;
    }

    public MapClickListener getClickListener() {
        return clickListener;
    }

    public void setUpdateListener(ViewUpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public boolean canDraw(){
        return canDraw;
    }

    public void setMainViewModel(MainViewModel mainViewModel) {
        this.mainViewModel = mainViewModel;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }

    //souřadnice posledního dotyku na mapu
    private float[] lastXY = new float[2];

    /**
     * Metoda OnClick() nemá možnost získat souřadnice kliknutí, proto používá souřadnice posledního dotyku
     */
    private class MapTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastXY[0] = event.getX();
                lastXY[1] = event.getY();
            }
            return false;
        }
    }

    /**
     * Listener, který se spustí při kliknutí na mapu
     */
    private class MapClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //TODO přenést na samostatné vlákno
            //získání souřadnic kliknutí
            int[] plan = MapAdapter.getPlanField(lastXY[0], lastXY[1]);
            Log.d("debug", "onTouch: " + plan[0] + "   " + plan[1]);
            //Získání předvytvořeného Dijkstrova algoritmu
            DijkstraAlgorithm dijkstra = mainViewModel.getDijkstra();
            if (dijkstra == null) {
                Log.e("debug", "onClick: Dijkstra = null");
                return;
            }

            //Výběr počátečního a konečného vrcholu
            //pozice uživatele na mapě
            start = dijkstra.getGraph().getVertex(
                    MapAdapter.getPlanField(mainViewModel.getCompassArrow().getCenterX()),
                    MapAdapter.getPlanField(mainViewModel.getCompassArrow().getCenterY()));
            //pozice kliknutí
            finish = dijkstra.getGraph().getVertex(plan[0], plan[1]);

            executeDijkstra(start, finish);
        }
    }
}
