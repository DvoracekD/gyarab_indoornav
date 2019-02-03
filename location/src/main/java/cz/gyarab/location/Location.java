package cz.gyarab.location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Paint;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

import cz.gyarab.location.dijkstra.DijkstraAlgorithm;
import cz.gyarab.location.dijkstra.Edge;
import cz.gyarab.location.dijkstra.Graph;
import cz.gyarab.location.dijkstra.MyLine;
import cz.gyarab.location.dijkstra.Vertex;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Location extends Application{

    private static MyPane[][] planes;
    private static MyCircle[][] radios;
    private static final int PLAN_WIDTH = 36;
    private static final int PLAN_HEIGHT = 25;
    //2,5 metru mezi kontrolními body
    private static final double DISTANCE_BETWEEN_POINTS = 2.5;
    private static Entry[][] entries = new Entry[PLAN_WIDTH][PLAN_HEIGHT];

    private final int SQUARE = 35;
    private final int FRAME_HEIGHT = PLAN_HEIGHT * SQUARE;
    private final int FRAME_WIDTH = PLAN_WIDTH * SQUARE;

    private AnchorPane stackPane;
    private GridPane gridPane;
    private Button activeButton;
    private ImageView imageView;

    private enum Mode{ADD_POINT, ADD_CONSTRAINT, ADD_SUB_VERTEXES, FIND_WAY}
    private Mode mode;
    private ComboBox<String> modeSelector;

    public static void main(String[] args) {
        readJson();
        launch(args);
        //readObject();
    }

    public static void readJson(){
        String input = "";
        try {
            Scanner scanner = new Scanner(new File("location/resources/data.json"));
            while (scanner.hasNext())
                input += scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        JSONArray xArrays = new JSONArray(input);
        for (int i = 0; i < xArrays.length(); i++) {
            JSONArray yArrays = (JSONArray)xArrays.get(i);
            for (int j = 0; j < yArrays.length(); j++) {
                JSONArray entryArray = (JSONArray)yArrays.get(j);
                ArrayList<SignalEntry> signalEntries = new ArrayList<>();
                for (int k = 0; k < entryArray.length(); k++) {
                    JSONObject entry = (JSONObject)entryArray.get(k);
                    signalEntries.add(new SignalEntry(entry.getString("SSID"), entry.getString("BSSID"), entry.getInt("signal")));
                }
                entries[i][j] = new Entry(signalEntries);
            }
        }
//        for (Object yArrays : xArrays) {
//            JSONArray YArray = (JSONArray)yArrays;
//            for (Object entries : YArray){
//                JSONArray Entries = (JSONArray)entries;
//                for (Object entry : Entries){
//                    JSONObject Entry = (JSONObject)entry;
//                    
//                }
//            }
//        };
    }

    public static void readObject(){

        try (FileInputStream fis = new FileInputStream("out.tmp")) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            entries = (Entry[][]) ois.readObject();
            if (entries == null)entries = new Entry[PLAN_WIDTH][PLAN_HEIGHT];
            System.out.println("Loaded");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox root = new VBox();
        modeSelector = new ComboBox();
        modeSelector.getItems().setAll("Algorithm", "Graph generator");
        modeSelector.setValue("Algorithm");
        modeSelector.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switch (newValue){
                    case "Algorithm":
                        setAlgorithmWorkspace();
                        break;
                    case "Graph generator":
                        setGraphWorkspace();
                        break;
                }
            }
        });
        final Button addPointButton = new Button("Add Point");
        addPointButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                updateGraph();
                if (activeButton != null)
                    activeButton.setStyle("");
                addPointButton.setStyle("-fx-background-color:lightgreen");
                activeButton = addPointButton;
                mode = Mode.ADD_POINT;
            }
        });
        final Button addConstraintButton = new Button("Add Constraint");
        addConstraintButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (activeButton != null)
                    activeButton.setStyle("");
                addConstraintButton.setStyle("-fx-background-color:lightgreen");
                activeButton = addConstraintButton;
                mode = Mode.ADD_CONSTRAINT;
            }
        });

        final Button addSubVertexes = new Button("Add SubVertexes");
        addSubVertexes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (activeButton != null)
                    activeButton.setStyle("");
                addSubVertexes.setStyle("-fx-background-color:lightgreen");
                activeButton = addSubVertexes;
                mode = Mode.ADD_SUB_VERTEXES;
            }
        });

        final Button findWayButton = new Button("Find Way");
        findWayButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (activeButton != null)
                    activeButton.setStyle("");
                findWayButton.setStyle("-fx-background-color:lightgreen");
                activeButton = findWayButton;
                mode = Mode.FIND_WAY;
                //findWay(graph.getVertexes().get(graph.getVertexes().size()-1),graph.getVertexes().get(0));
            }
        });

        final Button saveButton = new Button("Save");
        saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                saveObj(graph, "map_graph");
            }
        });


        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(addPointButton);
        toolBar.getItems().add(addConstraintButton);
        toolBar.getItems().add(addSubVertexes);
        toolBar.getItems().add(findWayButton);
        toolBar.getItems().add(saveButton);
        //mezere pro zarovnání v toolbaru
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        toolBar.getItems().add(spring);
        toolBar.getItems().add(modeSelector);
        root.getChildren().add(toolBar);

        imageView = new ImageView();
        imageView.setImage(new Image(new FileInputStream("location/resources/1NP.png")));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(FRAME_WIDTH);

        stackPane = new AnchorPane(imageView);

        radios = new MyCircle[PLAN_WIDTH][PLAN_HEIGHT];
        planes = new MyPane[PLAN_WIDTH][PLAN_HEIGHT];
        gridPane = new GridPane();

        //!!!
        //! setAlgorithmWorkspace();

        stackPane.getChildren().add(gridPane);
        gridPane.setAlignment(Pos.BOTTOM_LEFT);
        gridPane.setPadding(new Insets(0,0,5,10));
        ScrollPane scrollPane = new ScrollPane(stackPane);
        root.getChildren().add(scrollPane);
        //planes[23][16].setStyle("-fx-background-color: rgba( 0 , 255, 0 , 0.5 ) ");

        primaryStage.setTitle("MapEditor");
        primaryStage.setScene(new Scene(root, FRAME_WIDTH+10, FRAME_HEIGHT+80));
        primaryStage.show();

        modeSelector.setValue("Graph generator");
    }

    private void setAlgorithmWorkspace(){
        gridPane.getChildren().clear();
        gridPane.setGridLinesVisible(true);
        for (int i = 0; i < PLAN_HEIGHT; i++) {
            for (int j = 0; j < PLAN_WIDTH; j++) {
                final MyPane node = new MyPane(j, i);
                node.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        System.out.println("["+node.x+","+node.y+"]");
                        List<SignalEntry> list = entries[node.x][node.y].list;
                        Collections.sort(list, new Comparator<SignalEntry>() {
                            @Override
                            public int compare(SignalEntry o1, SignalEntry o2) {
                                return o1.getSSID().compareTo(o2.getSSID());
                            }
                        });
                        ArrayList<SignalEntry> newList = new ArrayList<>();
                        for (SignalEntry signalEntry : list){
                            if (signalEntry.getSSID().contains("GYM_ARABSKA")){
                                System.out.println(signalEntry);
                                newList.add(signalEntry);
                            }
                        }
                        Test.testEntry.list = newList;
                        updatePlanes();
                    }
                });
                node.setMaxSize(SQUARE, SQUARE);
                node.setMinSize(SQUARE, SQUARE);
                planes[j][i] = node;

                int difference = getDifference(j, i);
                if (entries[j][i].list.size() != 0){

                    //System.out.println("-fx-background-color: rgba( 0 , "+(255-(difference/255f *255f)) +", 0 , 0.5 ) ");
                    planes[j][i].setStyle("-fx-background-color: rgba( 0 , "+(255-(difference/255f *255f))+", 0 , 0.5 ) ");
                }
                node.getChildren().add(new Text(difference+""));
                StackPane.setAlignment(node, Pos.CENTER);

                gridPane.add(node, j, i);
            }
        }
    }

    private MyCircle constraintCircle;
    private Graph graph;

    private void setGraphWorkspace() {

        readGraph("map_graph");
        if (graph == null)
            graph = new Graph();
        gridPane.getChildren().clear();
        imageView.setOpacity(0.8);

        for (int i = 0; i < PLAN_HEIGHT; i++) {
            for (int j = 0; j < PLAN_WIDTH; j++) {
                //čtverec na pozadí
                final MyPane square = new MyPane(j, i);
                square.setMaxSize(SQUARE, SQUARE);
                square.setMinSize(SQUARE, SQUARE);
                planes[j][i] = square;
                gridPane.add(square, j, i);

                //kruh uprostred
                final MyCircle circle = new MyCircle(6, Color.TRANSPARENT);
                radios[j][i] = circle;
                circle.strokeProperty().set(Color.BLACK);
                GridPane.setHalignment(circle, HPos.CENTER);
                GridPane.setValignment(circle, VPos.CENTER);
                //GridPane.setMargin(radios[j][i], new Insets(0));
                gridPane.add(circle, j, i);

                //po kliknuti
                final int finalJ = j;
                final int finalI = i;
                final String coords = j+","+i;

                circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        switch (mode){
                            case ADD_POINT:
                                if (!event.isControlDown()){
                                    showDialog(circle, finalJ, finalI);
                                    Tooltip.install(circle, new Tooltip(circle.getVertex().getName() != null ? circle.getVertex().getName() : ""));
                                }
                                if (event.isControlDown()){
                                    graph.getVertexes().remove(circle.getVertex());
                                    circle.deleteVertex();
                                }
                                break;
                            case ADD_CONSTRAINT:
                                if (!event.isControlDown()){
                                    if (circle.getVertex() == null)break;
                                    if (constraintCircle == null) constraintCircle = circle;
                                    else {
                                        if (circle.equals(constraintCircle))break;
                                        Edge edge = new Edge(constraintCircle.getVertex(), circle.getVertex(), computeDistance(constraintCircle.getVertex(), circle.getVertex()));
                                        addEdge(circle, constraintCircle, edge);
                                        graph.getEdges().add(edge);
                                        constraintCircle = null;
                                    }
                                }
                                break;
                            case ADD_SUB_VERTEXES:
                                //při kliknutím pravým tlačítkem se zobrazí pripsane vrcholy
                                if (event.getButton().equals(MouseButton.SECONDARY)){
                                    revertColors();
                                    for (Vertex v : graph.getSubVertexes().get(coords)){
                                        MyCircle c = radios[v.getX()][v.getY()];
                                        coloredCircles.add(c);
                                        c.setColor(Color.YELLOW);
                                    }
                                    return;
                                }
                                //v prvním kole výber vrcholu
                                if (currentVertex == null){
                                    currentVertex = circle.getVertex();
                                    if (currentVertex != null){
                                        coloredCircles.add(circle);
                                        circle.setColor(Color.BLUE);
                                    }
                                }
                                //opětovným kliknutím na základní vrchol se výběr potrvdí
                                else if (currentVertex == circle.getVertex()){
                                    revertColoredCircles();
                                }
                                //do slovníku přidávám možné vrcholy do kterých lze z daného bodu přejít
                                else {
                                    String vertexName = coords;
                                    //Pokud je stisknuty control, zruší se vyběr daného bodu
                                    if (event.isControlDown()){
                                        ArrayList<Vertex> currentAdjVer = graph.getSubVertexes().get(vertexName);
                                        Vertex vertexOfCurrentCircle = circle.getVertex();
                                        //pokud se jedna o vrchol cesty
                                        if (vertexOfCurrentCircle != null && vertexOfCurrentCircle.equals(currentVertex)){
                                            revertColoredCircles();
                                        }
                                        //pokud se jedna o podrazeny vrchol
                                        else if(currentAdjVer != null){
                                            currentAdjVer.remove(currentVertex);
                                            circle.setColor(Color.TRANSPARENT);
                                        }
                                        return;
                                    }
                                    if (graph.getSubVertexes().get(vertexName)==null){
                                        graph.getSubVertexes().put(vertexName, new ArrayList<Vertex>());
                                    }
                                    graph.getSubVertexes().get(vertexName).add(currentVertex);
                                    circle.setColor(Color.ORANGE);
                                    coloredCircles.add(circle);
                                }
                                break;
                            //hledani nejkratsi cesty
                            case FIND_WAY:
                                Vertex vertex;
                                if (circle.getVertex()==null){
                                    vertex = new Vertex(finalJ, finalI, "");
                                    graph.getVertexes().add(vertex);
                                    for (Vertex v : graph.getSubVertexes().get(coords)){
                                        graph.getEdges().add(new Edge(vertex, v, computeDistance(vertex, v)));
                                    }
                                }
                                else vertex = circle.getVertex();

                                if (firstVertex == null){
                                    firstVertex = vertex;
                                    revertColors();
                                }
                                else {
                                    findWay(firstVertex, vertex);
                                    firstVertex = null;
                                }
                                break;
                        }
                    }
                });
            }
        }
        //updateGraph();
    }

    //první vrchol cesty
    Vertex firstVertex;

    //vrchol pro vytváření podvrcholů
    ArrayList<MyCircle> coloredCircles = new ArrayList<>();
    Vertex currentVertex;

    private void revertColoredCircles(){
        coloredCircles.get(0).setColor(Color.RED);
        currentVertex = null;
        for (int k = 1; k < coloredCircles.size(); k++) {
            coloredCircles.get(k).setColor(Color.TRANSPARENT);
        }
        coloredCircles = new ArrayList<>();
    }

    private void revertColors(){
        for (MyCircle c : coloredCircles)c.setPreviousColor();
        coloredCircles = new ArrayList<>();
    }

    private void addEdge(MyCircle circle, MyCircle constraintCircle, Edge edge){
        Bounds circleBounds = circle.localToParent(circle.getBoundsInLocal());
        Bounds constraintCircleBounds = constraintCircle.localToParent(constraintCircle.getBoundsInLocal());
        final MyLine line = new MyLine(
                (circleBounds.getMaxX()+circleBounds.getMinX())/2,
                (circleBounds.getMaxY()+circleBounds.getMinY())/2,
                (constraintCircleBounds.getMaxX()+constraintCircleBounds.getMinX())/2,
                (constraintCircleBounds.getMaxY()+constraintCircleBounds.getMinY())/2);
        //line.getStrokeDashArray().addAll(25d, 10d);
        line.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.isControlDown()){
                    stackPane.getChildren().remove(line);
                    graph.getEdges().remove(line.getEdge());
                }
            }
        });
        stackPane.getChildren().add(line);
        line.setEdge(edge);
    }

    private void updateGraph(){
        for (Vertex v : graph.getVertexes()){
            radios[v.getX()][v.getY()].setVertex(v);
        }
        for (Edge e : graph.getEdges()){
            addEdge(radios[e.getSource().getX()][e.getSource().getY()], radios[e.getDestination().getX()][e.getDestination().getY()], e);
        }
    }

    /**
     * spočítá skutečnou vzdálenost mezi dvěma uzly
     * @param node1
     * @param node2
     * @return
     */
    private double computeDistance(Vertex node1, Vertex node2){
        return Math.sqrt(Math.pow(node1.getX()-node2.getX(), 2) + Math.pow(node1.getY()-node2.getY(), 2))*DISTANCE_BETWEEN_POINTS;
    }

    private void showDialog(final MyCircle circle, final int x, final int y){
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Name of the node");
        dialog.setHeaderText("Enter the name of the node");
        //dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();

        //Po vložení jména vytvoří uzel grafu a přidá ho do seznamu
        result.ifPresent(new Consumer<String>() {
            @Override
            public void accept(String name) {
                Vertex vertex = new Vertex(x, y, name);
                //smaže starý uzel ze seznamu pokud je přepisován
                graph.getVertexes().remove(circle.getVertex());
                circle.setVertex(vertex);
                graph.getVertexes().add(vertex);
            }
        });
    }

    private void updatePlanes(){
        for (int i = 0; i < PLAN_HEIGHT; i++) {
            for (int j = 0; j < PLAN_WIDTH; j++) {
                int difference = getDifference(j, i);
                if (entries[j][i].list.size() != 0){

                    //System.out.println("-fx-background-color: rgba( 0 , "+(255-(difference/255f *255f)) +", 0 , 0.5 ) ");
                    planes[j][i].setStyle("-fx-background-color: rgba( 0 , "+(255-(difference/255f *255f))+", 0 , 0.5 ) ");
                }
                planes[j][i].getChildren().remove(0);
                planes[j][i].getChildren().add(new Text(difference+""));
            }
        }
    }

    public void readGraph(String name){

        try (FileInputStream fis = new FileInputStream(name)) {
            ObjectInputStream ois = new ObjectInputStream(fis);
            graph = (Graph) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void saveObj(Object object, String objName){
        try(FileOutputStream fos = new FileOutputStream(objName)){

            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(object);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getDifference(int x, int y){
        //pokud vidí víc AP nebo nějaké nevidí, získá 20 trestných bodů
        final int PENALTY = 50;
        int difference = 0;
        int used = 0;
        for (SignalEntry entry : entries[x][y].list){
            if (entry.getSSID().contentEquals("GYM_ARABSKA")) {
                //silá naskenovaného signálu
                int scanSignal = Test.getAPSig(entry.getSSID(), entry.getBSSID());
                //pokud bod vidí signál navíc
                if (scanSignal == 0)
                    difference += PENALTY;
                //přičte známku za jeden společný AP
                difference += Math.abs(scanSignal - entry.getSignal());
                used++;
            }
        }
        //pro ty AP, ktere byly naskenovany ale v referencnim bodu chybí
        if (entries[x][y].list.size() != 0)
            difference += ((Test.count - used)*PENALTY);
        return difference;
    }

    private void findWay(Vertex start, Vertex finish){
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
        dijkstra.execute(start);
        LinkedList<Vertex> path = dijkstra.getPath(finish);

        if (path==null){
            System.out.println("Nelze!");
            return;
        }

        for (Vertex vertex : path) {
            System.out.println(vertex);
            radios[vertex.getX()][vertex.getY()].setColor(Color.GREEN);
            coloredCircles.add(radios[vertex.getX()][vertex.getY()]);
        }
        System.out.println(dijkstra.getDistance(finish));
    }
}
