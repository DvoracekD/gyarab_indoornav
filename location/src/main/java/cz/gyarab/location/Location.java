package cz.gyarab.location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Paint;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    private static Circle[][] radios;
    private static final int PLAN_WIDTH = 36;
    private static final int PLAN_HEIGHT = 25;
    private static Entry[][] entries = new Entry[PLAN_WIDTH][PLAN_HEIGHT];

    private final int SQUARE = 35;
    private final int FRAME_HEIGHT = PLAN_HEIGHT * SQUARE;
    private final int FRAME_WIDTH = PLAN_WIDTH * SQUARE;

    private GridPane gridPane;
    private Button activeButton;
    private ImageView imageView;

    private enum Mode{ADD_POINT, ADD_CONSTRAINT}
    private Mode mode;

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
        ComboBox<String> modeSelector = new ComboBox();
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

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().add(addPointButton);
        toolBar.getItems().add(addConstraintButton);
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

        StackPane stackPane = new StackPane(imageView);

        radios = new Circle[PLAN_WIDTH][PLAN_HEIGHT];
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

        //set graph workspace
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

    private Circle constraintCircle;

    private void setGraphWorkspace() {
        gridPane.getChildren().clear();
        imageView.setOpacity(0.8);

        for (int i = 0; i < PLAN_HEIGHT; i++) {
            for (int j = 0; j < PLAN_WIDTH; j++) {
                //čtverec na pozadí
                final MyPane node = new MyPane(j, i);
                node.setMaxSize(SQUARE, SQUARE);
                node.setMinSize(SQUARE, SQUARE);
                planes[j][i] = node;
                gridPane.add(node, j, i);

                //kruh uprostred
                final Circle circle = new Circle(6, Color.TRANSPARENT);
                radios[j][i] = circle;
                circle.strokeProperty().set(Color.BLACK);
                GridPane.setHalignment(circle, HPos.CENTER);
                GridPane.setValignment(circle, VPos.CENTER);
                //GridPane.setMargin(radios[j][i], new Insets(0));
                gridPane.add(circle, j, i);

                //po kliknuti
                final int finalJ = j;
                final int finalI = i;
                circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        switch (mode){
                            case ADD_POINT:
                                if (!event.isControlDown()){
                                    circle.setFill(Color.RED);
                                }
                                if (event.isControlDown()){
                                    circle.setFill(Color.TRANSPARENT);
                                }
                                break;
                            case ADD_CONSTRAINT:
                                if (!event.isControlDown()){
                                    if (constraintCircle == null)constraintCircle = circle;
                                    else {
                                        gridPane.add(new Line(constraintCircle.getCenterX(), constraintCircle.getCenterY(), circle.getCenterX(), circle.getCenterY()), finalJ, finalI);
                                        constraintCircle = null;
                                    }
                                }
                                if (event.isControlDown()){
                                    circle.setFill(Color.TRANSPARENT);
                                }
                        }
                    }
                });
            }
        }
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
}
