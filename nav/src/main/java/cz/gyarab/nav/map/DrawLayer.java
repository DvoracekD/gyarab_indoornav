package cz.gyarab.nav.map;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.support.v7.widget.AppCompatImageView;

import java.util.LinkedList;

import cz.gyarab.nav.MainActivity;
import cz.gyarab.nav.dijkstra.Vertex;

public class DrawLayer extends AppCompatImageView {

    private DrawLayerViewModel viewModel;

    public final static int STROKE_SIZE = 100;

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

        final MainActivity activity = (MainActivity)getContext();
        viewModel = ViewModelProviders.of(activity).get(DrawLayerViewModel.class);
        viewModel.setMainViewModel(activity.getViewModel());
        setListeners();
        viewModel.setUpdateListener(new DrawLayerViewModel.ViewUpdateListener() {
            @Override
            public void update() {
                postInvalidate();
                activity.showRouteOffButton();
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!viewModel.canDraw())return;

        float[] points = viewModel.getPoints();
        if (points != null && points.length != 0){
            canvas.drawLines(points, viewModel.getLinePaint());
            //kolečka spojující jednotlivé linky polyline
            if (viewModel.getPath() == null) return;
            for (Vertex v : viewModel.getPath()){
                canvas.drawCircle(MapAdapter.getMapCoordinate(v.getX()), MapAdapter.getMapCoordinate(v.getY()), STROKE_SIZE/2f, viewModel.getCirclePaint());
            }
        }

    }

    /**
     * kontrola kliknutí na mapu a předání souřadnice kliknutí
     */
    public void setListeners(){

        setOnTouchListener(viewModel.getTouchListener());
        setOnClickListener(viewModel.getClickListener());

    }

    public void setCanDraw(boolean canDraw) {
        viewModel.setCanDraw(canDraw);
    }

}
