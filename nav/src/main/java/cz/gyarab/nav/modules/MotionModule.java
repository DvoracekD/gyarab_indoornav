package cz.gyarab.nav.modules;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class MotionModule implements SensorEventListener {

    public interface MotionListener{
        void onStep();
    }

    private SensorManager sensorManager;
    private MotionListener listener;
    private Sensor sensor;

    public MotionModule(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
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

    public void start(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
