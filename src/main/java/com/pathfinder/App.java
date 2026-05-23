package com.pathfinder;

import com.pathfinder.database.DatabaseManager;
import com.pathfinder.graph.GraphManager;
import com.pathfinder.model.City;
import com.pathfinder.model.Edge;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class App extends Application {

    private DatabaseManager dbManager = new DatabaseManager();
    private GraphManager graphManager = new GraphManager();

    private Pane canvas = new Pane();

    // Dropdowns for Finding Path
    private ComboBox<City> sourceCombo = new ComboBox<>();
    private ComboBox<City> destCombo = new ComboBox<>();

    // Dropdowns for Connecting Cities
    private ComboBox<City> connectSourceCombo = new ComboBox<>();
    private ComboBox<City> connectDestCombo = new ComboBox<>();
    private TextField distanceInput = new TextField();

    private Map<Integer, City> cityMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // Canvas Setup (Dark UI)
        canvas.setStyle("-fx-background-color: #1e1e1e;");
        root.setCenter(canvas);

        // Control Panel Setup
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        sidebar.setPrefWidth(300);

        Label infoLabel = new Label("Double-click canvas to add a new city.");
        infoLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");

        Button addEdgeBtn = new Button("Add Connection");
        Button findPathBtn = new Button("Find Shortest Path");
        findPathBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        sidebar.getChildren().addAll(
                new Label("GRAPH CONTROLS"),
                new Separator(),
                infoLabel,
                new Separator(),
                new Label("Connect Cities:"),
                new Label("City 1:"), connectSourceCombo,
                new Label("City 2:"), connectDestCombo,
                new Label("Distance (km):"), distanceInput,
                addEdgeBtn,
                new Separator(),
                new Label("Pathfinding:"),
                new Label("From:"), sourceCombo,
                new Label("To:"), destCombo,
                findPathBtn);
        root.setRight(sidebar);

        // Event Handlers
        canvas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                promptForNewCity(e.getX(), e.getY());
            }
        });

        // Add Edge Logic using the new dedicated dropdowns
        addEdgeBtn.setOnAction(e -> {
            City c1 = connectSourceCombo.getValue();
            City c2 = connectDestCombo.getValue();
            if (c1 != null && c2 != null && !distanceInput.getText().isEmpty()) {
                try {
                    double dist = Double.parseDouble(distanceInput.getText());
                    dbManager.insertEdge(c1.getId(), c2.getId(), dist);
                    distanceInput.clear(); // Clear the text field after adding
                    loadGraphData(); // Refresh UI to show the new line
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Distance must be a valid number.");
                    alert.show();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select both cities and enter a distance.");
                alert.show();
            }
        });

        findPathBtn.setOnAction(e -> highlightShortestPath());

        // Initial Load
        loadGraphData();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Graph Pathfinder - Dijkstra Engine");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void promptForNewCity(double x, double y) {
        TextInputDialog dialog = new TextInputDialog("New City");
        dialog.setTitle("Add City");
        dialog.setHeaderText("Enter the name of the new city:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            dbManager.insertCity(name, x, y);
            loadGraphData();
        });
    }

    private void loadGraphData() {
        canvas.getChildren().clear();
        graphManager.clear();
        cityMap.clear();

        // Clear all combo boxes
        sourceCombo.getItems().clear();
        destCombo.getItems().clear();
        connectSourceCombo.getItems().clear();
        connectDestCombo.getItems().clear();

        List<City> cities = dbManager.getAllCities();
        List<Edge> edges = dbManager.getAllEdges();

        // Populate graph and all combo boxes
        for (City c : cities) {
            cityMap.put(c.getId(), c);
            graphManager.addNode(c.getId());

            sourceCombo.getItems().add(c);
            destCombo.getItems().add(c);
            connectSourceCombo.getItems().add(c);
            connectDestCombo.getItems().add(c);
        }

        // Draw edges first (so they stay behind nodes)
        for (Edge e : edges) {
            graphManager.addEdge(e);
            City src = cityMap.get(e.getSourceId());
            City tgt = cityMap.get(e.getTargetId());
            if (src != null && tgt != null) {
                drawEdgeLine(src, tgt, e.getDistance(), Color.GRAY, 2);
            }
        }

        // Draw nodes on top
        for (City c : cities) {
            drawCityNode(c);
        }
    }

    private void drawCityNode(City c) {
        Circle circle = new Circle(c.getX(), c.getY(), 12, Color.web("#4CAF50"));
        circle.setStroke(Color.WHITE);
        circle.setStrokeWidth(2);

        Text label = new Text(c.getX() - 15, c.getY() - 20, c.getName());
        label.setFill(Color.WHITE);

        canvas.getChildren().addAll(circle, label);
    }

    private void drawEdgeLine(City src, City tgt, double dist, Color color, int width) {
        Line line = new Line(src.getX(), src.getY(), tgt.getX(), tgt.getY());
        line.setStroke(color);
        line.setStrokeWidth(width);

        Text weight = new Text((src.getX() + tgt.getX()) / 2, (src.getY() + tgt.getY()) / 2 - 5, dist + " km");
        weight.setFill(Color.web("#FFC107"));

        canvas.getChildren().addAll(line, weight);
        line.toBack();
    }

    private void highlightShortestPath() {
        City start = sourceCombo.getValue();
        City end = destCombo.getValue();
        if (start == null || end == null)
            return;

        List<Integer> path = graphManager.getShortestPath(start.getId(), end.getId());

        // Redraw graph to clear previous highlights
        loadGraphData();

        if (path.isEmpty() || path.size() == 1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No path found between these cities.");
            alert.show();
            return;
        }

        // Draw green thick lines over the calculated path
        for (int i = 0; i < path.size() - 1; i++) {
            City c1 = cityMap.get(path.get(i));
            City c2 = cityMap.get(path.get(i + 1));
            drawEdgeLine(c1, c2, getDistanceBetween(c1.getId(), c2.getId()), Color.web("#00E676"), 5);
        }
    }

    private double getDistanceBetween(int id1, int id2) {
        return dbManager.getAllEdges().stream()
                .filter(e -> (e.getSourceId() == id1 && e.getTargetId() == id2))
                .map(Edge::getDistance)
                .findFirst().orElse(0.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}