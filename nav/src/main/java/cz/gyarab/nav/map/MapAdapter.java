package cz.gyarab.nav.map;

public class MapAdapter {

    private static int mMap_width;
    private static int mMap_height;
    private static final int PLAN_WIDTH = 36;//počet polí, ne které je plán rozdělen
    private static final int PLAN_HEIGHT = 25;
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
        int[] res = {(int)x/onePlanUnit, (int)y/onePlanUnit};
        return res;
    }

}
