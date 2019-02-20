package cz.gyarab.nav.dijkstra;

public class GraphLoader {

    private static GraphLoader instance;

    private GraphLoader(){



    }

    public static synchronized GraphLoader getInstance(){
        if(instance == null){
            instance = new GraphLoader();
        }
        return instance;
    }

}
