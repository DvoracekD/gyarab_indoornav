package cz.gyarab.nav.modules;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.widget.ImageView;

/**
 * Reprezentuje postavu uživatel na mapě (střelku kompasu)
 * Pro její správnou funkci musí mít nastevený ImageView image
 */
public class CompassArrow {

    private float currentAzimuth;
    private ImageView image;
    private boolean fresh;// značí že byl objekt právě vytovřen (aplikace se nachází v prvním cyklu, např. před prvním otočením)

    private float x;
    private float y;

    /**
     * Bezprostředně po vytvoření instance musí následovat nastavení ImageView
     * @param currentAzimuth prvotní natočení kompasu z intervalu <0;360> (0 směřuje na sever)
     */
    public CompassArrow(float currentAzimuth) {
        this.currentAzimuth = currentAzimuth;
        fresh = true;
        setXY(0,0);
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    private void setXY(float x, float y){
        this.x = x;
        this.y = y;
    }

    public void setPosition(float x, float y){
        image.setTranslationX(x - image.getLayoutParams().width/2f);
        image.setTranslationY(y - image.getLayoutParams().height/2f);
        setXY(x, y);
        //RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(image.getLayoutParams());
        //layoutParams.leftMargin = x - image.getLayoutParams().width/2;
        //layoutParams.topMargin = y - image.getLayoutParams().height/2;
        //image.setLayoutParams(layoutParams);
    }

    public void refreshPosition(){
        setPosition(x, y);
    }

    public void move(int stepSize){
        //normalizace na orientovaný uhel, tak jak je v matematice
        final double normalAngle = Math.toRadians((90 + currentAzimuth)%360);

        float newX = this.x - (int)(stepSize * Math.cos(normalAngle));
        float newY = this.y - (int)(stepSize * Math.sin(normalAngle));

        ObjectAnimator x = ObjectAnimator.ofFloat(image, "translationX", this.x, newX);
        ObjectAnimator y = ObjectAnimator.ofFloat(image, "translationY", this.y, newY);

        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(x, y);
        animSetXY.setDuration(300);
        animSetXY.start();

        setXY(newX, newY);
//        setPosition(x - (int)(stepSize * Math.cos(normalAngle)),
//                y - (int)(stepSize * Math.sin(normalAngle)));
    }

    public void adjustArrow(float azimuth) {

        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(image, "rotation", currentAzimuth, azimuth);
        currentAzimuth = azimuth;
        rotationAnimator.setDuration(500);
        rotationAnimator.start();
//        an = new RotateAnimation(-currentAzimuth, -azimuth,
//                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
//                0.5f);
//        currentAzimuth = azimuth;
//
//        an.setDuration(500);
//        an.setRepeatCount(0);
//        an.setFillAfter(true);
//
//        image.startAnimation(an);

    }

    public boolean checkFresh(){
        if (fresh){
            fresh = false;
            return true;
        }
        return false;
    }

    public float getWidth(){
        return image.getLayoutParams().width;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getCenterX() {
        return x+getWidth()/2;
    }

    public float getCenterY() {
        return y+getWidth()/2;
    }
}