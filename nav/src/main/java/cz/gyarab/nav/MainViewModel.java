package cz.gyarab.nav;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.otaliastudios.zoom.ZoomLayout;

import cz.gyarab.nav.compass.CompassArrow;
import cz.gyarab.nav.compass.CompassModule;

public class MainViewModel extends AndroidViewModel {

    Application app;
    private CompassModule compassModule;
    private MotionModule motionModule;
    private CompassArrow compassArrow;
    private ZoomLayout zoomLayout;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.app = application;
    }
}
