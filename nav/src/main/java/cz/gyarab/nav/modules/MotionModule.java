package cz.gyarab.nav.modules;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

/**
 * Třída zodpovědná za uživatelův pohyb po mapě
 */
public class MotionModule implements SensorEventListener {

    /**
     * Listener pohybu
     */
    public interface MotionListener{
        /**
         * spustí se při zaznamenání pohybu
         */
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
        if (listener != null)
            listener.onStep();
    }

    public void setListener(MotionListener listener) {
        this.listener = listener;
    }

    public void start(){
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
