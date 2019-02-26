package cz.gyarab.location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

import cz.gyarab.location.customViews.MyCircle;
import cz.gyarab.location.customViews.MyPane;
import cz.gyarab.location.dijkstra.DijkstraAlgorithm;
import cz.gyarab.location.dijkstra.Edge;
import cz.gyarab.location.dijkstra.Graph;
import cz.gyarab.location.dijkstra.MyLine;
import cz.gyarab.location.dijkstra.Vertex;
import cz.gyarab.location.signal.Entry;
import cz.gyarab.location.signal.SignalEntry;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
    }

    /**
     * Načtení json souboru do pole oběktů @Entry
     */
    private static void readJson(){
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

        //Inicializace tlačítek v horní liště
        final Button addPointButton = new Button("Add Point");
        addPointButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                updateGraph();
                setNewActiveButton(addPointButton);
                mode = Mode.ADD_POINT;
            }
        });
        final Button addConstraintButton = new Button("Add Constraint");
        addConstraintButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setNewActiveButton(addConstraintButton);
                mode = Mode.ADD_CONSTRAINT;
            }
        });

        final Button addSubVertexes = new Button("Add SubVertices");
        addSubVertexes.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setNewActiveButton(addSubVertexes);
                mode = Mode.ADD_SUB_VERTEXES;
            }
        });

        final Button findWayButton = new Button("Find Way");
        findWayButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setNewActiveButton(findWayButton);
                mode = Mode.FIND_WAY;
            }
        });

        final Button setNamesButton = new Button("Set Names");
        setNamesButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                setNewActiveButton(setNamesButton);
                mode = Mode.ADD_POINT;
                for (int i = 0; i < PLAN_HEIGHT; i++) {
                    for (int j = 0; j < PLAN_WIDTH; j++) {
                        MyCircle circle = radios[j][i];
                        if (circle.getVertex() != null && !circle.getVertex().getName().equals("")){
                            circle.setColor(Color.LIGHTSKYBLUE);
                            coloredCircles.add(circle);
                        }
                    }
                }
            }
        });

        final Button saveButton = new Button("Save");
        saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                GraphJson.saveJson(graph);
            }
        });


        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(addPointButton);
        toolBar.getItems().add(addConstraintButton);
        toolBar.getItems().add(addSubVertexes);
        toolBar.getItems().add(findWayButton);
        toolBar.getItems().add(setNamesButton);
        toolBar.getItems().add(saveButton);

        //mezere pro zarovnání mezi tlačítky a nabídkou režimů v toolbaru
        Region spring = new Region();
        HBox.setHgrow(spring, Priority.ALWAYS);
        toolBar.getItems().add(spring);
        toolBar.getItems().add(modeSelector);
        root.getChildren().add(toolBar);

        imageView = new ImageView();
        imageView.setImage(new Image(new FileInputStream("location/resources/1NP.png")));
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(FRAME_WIDTH);

        //Nastavení stylů
        stackPane = new AnchorPane(imageView);

        radios = new MyCircle[PLAN_WIDTH][PLAN_HEIGHT];
        planes = new MyPane[PLAN_WIDTH][PLAN_HEIGHT];
        gridPane = new GridPane();
        stackPane.getChildren().add(gridPane);
        gridPane.setAlignment(Pos.BOTTOM_LEFT);
        gridPane.setPadding(new Insets(0,0,5,10));
        ScrollPane scrollPane = new ScrollPane(stackPane);
        root.getChildren().add(scrollPane);


        primaryStage.setTitle("MapEditor");
        primaryStage.setScene(new Scene(root, FRAME_WIDTH+10, FRAME_HEIGHT+80));
        primaryStage.show();

        modeSelector.setValue("Graph generator");
    }

    /**
     * označí nové aktivní tlačítko, které se zbarvý zeleně
     * @param newActiveButton
     */
    private void setNewActiveButton(Button newActiveButton){
        if (activeButton != null)
            activeButton.setStyle("");
        newActiveButton.setStyle("-fx-background-color:lightgreen");
        activeButton = newActiveButton;
    }

    /**
     * Nastavení režimu algoritmus - vizualizace pracovní verze algortmu hledání polohy
     */
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

    /**
     * nastavení režimu Graf - vytvážení grafu mapy
     */
    private void setGraphWorkspace() {

        graph = GraphJson.readJson();
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
                                    if (graph.getSubVertexes().get(coords)== null){
                                        System.out.println("Chybí subvertexy");
                                        return;
                                    }
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
                                        //pokud se jedna o podřazeny vrchol
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
        updateGraph();
    }

    //první vrchol cesty
    Vertex firstVertex;

    //vrchol pro vytváření podvrcholů
    ArrayList<MyCircle> coloredCircles = new ArrayList<>();
    Vertex currentVertex;

    /**
     * Vrátí barvy obarveným vrcholům
     */
    private void revertColoredCircles(){
        coloredCircles.get(0).setColor(Color.RED);
        currentVertex = null;
        for (int k = 1; k < coloredCircles.size(); k++) {
            coloredCircles.get(k).setColor(Color.TRANSPARENT);
        }
        coloredCircles = new ArrayList<>();
    }

    /**
     * Vrátí barvy obarveným vrcholům
     */
    private void revertColors(){
        for (MyCircle c : coloredCircles)c.setPreviousColor();
        coloredCircles = new ArrayList<>();
    }

    /**
     * Zobrazí klikatelnou hranu mezi dvěma vrcholy
     * @param circle první vrchol
     * @param constraintCircle druhý vrchol
     * @param edge Interní reprezentace hrany pro Dijkstrův algoritmus
     */
    private void addEdge(MyCircle circle, MyCircle constraintCircle, Edge edge){
        Bounds circleBounds = circle.localToParent(circle.getBoundsInLocal());
        Bounds constraintCircleBounds = constraintCircle.localToParent(constraintCircle.getBoundsInLocal());
        final MyLine line = new MyLine(
                (circleBounds.getMaxX()+circleBounds.getMinX())/2,
                (circleBounds.getMaxY()+circleBounds.getMinY())/2,
                (constraintCircleBounds.getMaxX()+constraintCircleBounds.getMinX())/2,
                (constraintCircleBounds.getMaxY()+constraintCircleBounds.getMinY())/2);
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

    /**
     * zobrazí všechny vrcholy a hrany v paměti na plánu
     */
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
     * @param node1 vrchol 1
     * @param node2 vrchol 2
     * @return vzdálenost mezi vrcholy
     */
    private double computeDistance(Vertex node1, Vertex node2){
        return Math.sqrt(Math.pow(node1.getX()-node2.getX(), 2) + Math.pow(node1.getY()-node2.getY(), 2))*DISTANCE_BETWEEN_POINTS;
    }

    /**
     * Zobrazí okno přidání nového vrchlu, respektive změna jména vrcholu
     * @param circle tlačítko (čtverec), ke kterému má být vytvořen vrchol
     * @param x souřadnice čtverce v jednotkách plánku (počet čtverců od levého horního rohu)
     * @param y souřacnice čtverce v jednotkách plánku (počet čtverců od levého horního rohu)
     */
    private void showDialog(final MyCircle circle, final int x, final int y){
        //pokud kroužek nemá přiřazený vrchol, defaultní hodnota jména bude ""
        String name = circle.getVertex() == null ? "" : circle.getVertex().getName();
        TextInputDialog dialog = new TextInputDialog(name);
        dialog.setTitle("Name of the node");
        dialog.setHeaderText("Enter the name of the node");
        //dialog.setContentText("Please enter your name:");

        Optional<String> result = dialog.showAndWait();

        //Po vložení jména vytvoří uzel grafu a přidá ho do seznamu
        result.ifPresent(new Consumer<String>() {
            @Override
            public void accept(String name) {
                if (circle.getVertex()==null){
                    Vertex vertex = new Vertex(x, y, name);
                    circle.setVertex(vertex);
                    graph.getVertexes().add(vertex);
                }
                //pokud již uzel existoval, změní jméno
                else {
                    circle.getVertex().setName(name);
                }
            }
        });
    }

    /**
     * nastaví barvu čtverce dle rozdílu od referenčníh hodnot
     */
    private void updatePlanes(){
        for (int i = 0; i < PLAN_HEIGHT; i++) {
            for (int j = 0; j < PLAN_WIDTH; j++) {
                int difference = getDifference(j, i);
                if (entries[j][i].list.size() != 0){
                    if (difference > 255)planes[j][i].setStyle("-fx-background-color: rgba( 0 , 0, 0 , 0.1 ) ");
                    else
                    planes[j][i].setStyle("-fx-background-color: rgba( 0 , "+(255-(difference/255f *255f))+", 0 , 0.5 ) ");
                }
                planes[j][i].getChildren().remove(0);
                planes[j][i].getChildren().add(new Text(difference+""));
            }
        }
    }

    /**
     * algoritmus pro získání rozdílu hodnot (1. zkušební verze)
     * @param x
     * @param y
     * @return
     */
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

    /**
     * spoučtí Dijkstru a hledá nejkratší cestu mezi dměma vloženými vrcholy, kterou zobrazí na mapě pomocí zelených vrcholů
     * @param start počáteční vrchol
     * @param finish cílový vrchol
     */
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
