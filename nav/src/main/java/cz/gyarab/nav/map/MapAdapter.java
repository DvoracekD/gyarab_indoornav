package cz.gyarab.nav.map;

/**
 * nabízí statický přepočet mezi souřadnicemi na obrazovce v pixelech a souřadnicemi v síti referenčních čtverců.
 */
public class MapAdapter {

    private static int mMap_width;
    private static int mMap_height;
    public static final int PLAN_WIDTH = 36;//počet polí, ne které je plán rozdělen
    public static final int PLAN_HEIGHT = 25;
    //počet pixelů na jeden díl plánu
    private static int onePlanUnit;

    private MapAdapter() {

    }

    public static void init(int map_width, int map_height){
        mMap_width = map_width;
        mMap_height = map_height;

        onePlanUnit = map_width/PLAN_WIDTH;
    }


    public static int[] getPlanField(float x, float y){
        return new int[]{(int)x/onePlanUnit, (int)y/onePlanUnit};
    }

    public static float[] getMapCoordinates(int x, int y){
        return new float[]{(float)(x*onePlanUnit+onePlanUnit/2), (float)(y*onePlanUnit+onePlanUnit/2)};
    }

    /**
     * čtverce na pixely
     * @param coord
     * @return
     */
    public static float getMapCoordinate(int coord){
        return coord*onePlanUnit+onePlanUnit/2f;
    }

    /**
     * pixely na čtverce
     * @param coord
     * @return
     */
    public static int getPlanField(float coord){
        return (int)coord/onePlanUnit;
    }

}
