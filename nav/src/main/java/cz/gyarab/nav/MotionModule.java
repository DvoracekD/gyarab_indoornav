package cz.gyarab.nav;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import cz.gyarab.nav.compass.CompassArrow;

public class MotionModule implements SensorEventListener {

    public interface MotionListener{
        void onStep();
    }

    private MotionListener listener;
    private Sensor sensor;

    public MotionModule(SensorManager sensorManager) {
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d("debug", "onTrigger: STEP");
        //compassArrow.move(MainActivity.stepSize);
        if (listener != null)
            listener.onStep();
    }

    public void setListener(MotionListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
