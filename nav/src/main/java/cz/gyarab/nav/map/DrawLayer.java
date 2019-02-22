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

import java.util.LinkedList;

import cz.gyarab.nav.MainActivity;
import cz.gyarab.nav.dijkstra.DijkstraAlgorithm;
import cz.gyarab.nav.dijkstra.Vertex;

public class DrawLayer extends AppCompatImageView {

    private Paint linePaint;
    private Paint circlePaint;
    private float[] points;
    private LinkedList<Vertex> path;
    private final int STROKE_SIZE = 100;
    private boolean canDraw = true;

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

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        linePaint.setColor(Color.parseColor("#16A7F0"));
        linePaint.setStrokeWidth(STROKE_SIZE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.parseColor("#16A7F0"));
        circlePaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!canDraw)return;

        if (points != null && points.length != 0){
            canvas.drawLines(points, linePaint);
            //kolečka spojující jednotlivé linky polyline
            for (Vertex v : path){
                canvas.drawCircle(MapAdapter.getMapCoordinate(v.getX()), MapAdapter.getMapCoordinate(v.getY()), STROKE_SIZE/2f, circlePaint);
            }
        }

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

        //po kliknutí na místo v mapě
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO přenést na samostatné vlákno
                int[] plan = MapAdapter.getPlanField(lastXY[0], lastXY[1]);
                Log.d("debug", "onTouch: "+ plan[0]+"   "+ plan[1]);
                MainActivity activity = (MainActivity)getContext();
                DijkstraAlgorithm dijkstra = activity.getViewModel().getDijkstra();
                if (dijkstra == null){
                    Log.e("debug", "onClick: Dijkstra = null");
                    return;
                }

                //pozice uživatele na mapě
                Vertex start = dijkstra.getGraph().getVertex(
                        MapAdapter.getPlanField(activity.getCompassArrow().getCenterX()),
                        MapAdapter.getPlanField(activity.getCompassArrow().getCenterY()));
                //pozice kliknutí
                Vertex finish = dijkstra.getGraph().getVertex(plan[0],plan[1]);
                dijkstra.execute(start);
                path = dijkstra.getPath(finish);
                //okamžitě po vykreslení cesty se objekt restartuje
                activity.getViewModel().rebuildDijkstra();

                if (path==null){
                    System.out.println(start + " -> "+ finish);
                    System.out.println("Nelze!");
                    return;
                }

                fillPoints(path);

                for (Vertex vertex : path) {
                    System.out.println(vertex);
                }
                System.out.println(dijkstra.getDistance(finish));
                canDraw = true;
                activity.showRouteOffButton();
                postInvalidate();
            }
        });
    }

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

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
    }
}
