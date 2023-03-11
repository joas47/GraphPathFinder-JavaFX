
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

// *** VT2021, Inlämningsuppgift, del 2
// Grupp 096
// joas47

public class PathFinder extends Application {

    private static final String SAVE_FILE_NAME = "europa.graph";
    private static final String IMAGE_FILE_NAME = "file:europa.gif";
    private static final int SCENE_WIDTH = 600;
    private static final int SCENE_HEIGHT = 80;
    private static final int BUTTON_SPACING = 10;
    private static final int NODE_TEXT_Y_OFFSET = 24;
    private static final int NODE_TEXT_FONT_SIZE = 15;
    private static final int CONNECTION_LINE_WIDTH = 3;
    private static final int SCENE_WIDTH_OFFSET = 15;
    private static final int SCENE_HEIGHT_OFFSET = 100;
    private static final int BUTTON_PADDING = 8;

    private BorderPane bp = new BorderPane();
    private Scene scene;
    private Stage primaryStage;
    private Button newPlaceBt;
    private Image image;
    private Pane pane = new Pane();
    private ImageView imageView = new ImageView();

    private ListGraph<GraphNode> listGraph = new ListGraph<>();

    private GraphNode selectedNode1;
    private GraphNode selectedNode2;

    private boolean unsavedChanges = true;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // MenuBar
        Menu menu = new Menu("File");
        menu.setId("menuFile");

        MenuItem newMap = new MenuItem("New Map");
        newMap.setId("menuNewMap");
        newMap.setOnAction(new NewMapHandler());

        MenuItem open = new MenuItem("Open");
        open.setId("menuOpenFile");
        open.setOnAction(new OpenHandler());

        MenuItem save = new MenuItem("Save");
        save.setId("menuSaveFile");
        save.setOnAction(new SaveHandler());

        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setId("menuSaveImage");
        saveImage.setOnAction(new SaveImageHandler());

        MenuItem exit = new MenuItem("Exit");
        exit.setId("menuExit");
        exit.setOnAction(new ExitHandler());

        menu.getItems().addAll(newMap, open, save, saveImage, exit);
        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu");
        menuBar.getMenus().add(menu);
        VBox menuVbox = new VBox(menuBar);

        // Buttons
        Button findPathBt = new Button("Find Path");
        findPathBt.setId("btnFindPath");
        findPathBt.setOnAction(new FindPathHandler());

        Button showConnectionBt = new Button("Show Connection");
        showConnectionBt.setId("btnShowConnection");
        showConnectionBt.setOnAction(new ShowConnectionHandler());

        newPlaceBt = new Button("New Place");
        newPlaceBt.setId("btnNewPlace");
        newPlaceBt.setOnAction(new NewPlaceHandler());

        Button newConnectionBt = new Button("New Connection");
        newConnectionBt.setId("btnNewConnection");
        newConnectionBt.setOnAction(new NewConnectionHandler());

        Button changeConnectionBt = new Button("Change Connection");
        changeConnectionBt.setId("btnChangeConnection");
        changeConnectionBt.setOnAction(new ChangeConnectionHandler());

        HBox buttonsHBox = new HBox(findPathBt, showConnectionBt, newPlaceBt,
                newConnectionBt, changeConnectionBt);
        buttonsHBox.setPadding(new Insets(BUTTON_PADDING));
        buttonsHBox.setSpacing(BUTTON_SPACING);
        buttonsHBox.setAlignment(Pos.TOP_CENTER);

        // BorderPane
        bp.setTop(menuVbox);
        bp.setCenter(buttonsHBox);
        bp.setBottom(pane);
        pane.setId("outputArea");

        // Scene
        scene = new Scene(bp, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setTitle("PathFinder");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new WindowCloseHandler());
        primaryStage.show();
    }

    private Alert unsavedChangesAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Unsaved changes, continue anyway?");
        alert.setTitle("Warning!");
        alert.setHeaderText(null);
        return alert;
    }

    private void errorAlert(String s) {
        Alert timeAlert = new Alert(Alert.AlertType.ERROR, s);
        timeAlert.setTitle("Error!");
        timeAlert.setHeaderText(null);
        timeAlert.showAndWait();
    }

    private void insertNode(String name, double x, double y) {
        GraphNode node = new GraphNode(x, y, name);
        node.setOnMouseClicked(new NodeClickHandler());
        node.setId(name);
        Text text = new Text(x, y + NODE_TEXT_Y_OFFSET, name);
        text.setFont(Font.font("Arial", FontWeight.BOLD, NODE_TEXT_FONT_SIZE));
        text.setDisable(true);
        pane.getChildren().addAll(node, text);
        listGraph.add(node);
    }

    private void insertConnection(String from, String to, String by, int weight) {
        Set<GraphNode> nodes = listGraph.getNodes();
        GraphNode fromNode = null;
        GraphNode toNode = null;
        for (GraphNode graphNode : nodes) {
            if (graphNode.getName().equals(from)) {
                fromNode = graphNode;
            } else if (graphNode.getName().equals(to)) {
                toNode = graphNode;
            }
        }
        Line connection = new Line(Objects.requireNonNull(fromNode).getCenterX(),
                fromNode.getCenterY(), Objects.requireNonNull(toNode).getCenterX(),
                toNode.getCenterY());
        connection.setStrokeWidth(CONNECTION_LINE_WIDTH);
        connection.setDisable(true);
        if (listGraph.getEdgeBetween(fromNode, toNode) == null) {
            listGraph.connect(fromNode, toNode, by, weight);
            pane.getChildren().add(connection);
        }
    }

    private void resetGraph() {
        selectedNode1 = null;
        selectedNode2 = null;
        Set<GraphNode> set = listGraph.getNodes();
        for (GraphNode graphNode : set) {
            listGraph.remove(graphNode);
        }
        unsavedChanges = false;
    }

    private AlertWithFields getAlertWithFields() {
        AlertWithFields alert = new AlertWithFields();
        alert.setTitle("Connection");
        alert.setHeaderText("Connection from " + selectedNode1.getName() + " to "
                + selectedNode2.getName());
        return alert;
    }

    private class WindowCloseHandler implements EventHandler<WindowEvent> {
        @Override
        public void handle(WindowEvent windowEvent) {
            if (pane.getChildren().size() != 0 && unsavedChanges) {
                Alert alert = unsavedChangesAlert();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.CANCEL)) {
                    windowEvent.consume();
                }
            } else {
                Platform.exit();
            }
        }
    }

    private class ShowConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                try {
                    if (listGraph.getEdgeBetween(selectedNode1, selectedNode2) != null) {
                        AlertWithFields alert = getAlertWithFields();
                        alert.getNameField().setText(
                                listGraph.getEdgeBetween(selectedNode1, selectedNode2).getName());
                        alert.getTimeField().setText(String.valueOf(
                                listGraph.getEdgeBetween(selectedNode1, selectedNode2).getWeight()));
                        alert.getNameField().setEditable(false);
                        alert.getTimeField().setEditable(false);
                        alert.showAndWait();
                    } else {
                        errorAlert("No connections between places exist!");
                    }
                } catch (NoSuchElementException e) {
                    errorAlert("Two places must be selected!");
                }
            }
        }
    }

    private class NewConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                try {
                    if (listGraph.getEdgeBetween(selectedNode1, selectedNode2) == null) {
                        AlertWithFields alert = getAlertWithFields();
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                            if (!alert.getName().isBlank()) {
                                insertConnection(selectedNode1.getName(), selectedNode2.getName(),
                                        alert.getName(), alert.getTime());
                                unsavedChanges = true;
                            } else {
                                errorAlert("Name-field can't be empty!");
                            }
                        }
                    } else {
                        errorAlert("Connection already exists between locations!");
                    }
                } catch (NoSuchElementException e) {
                    errorAlert("Two places must be selected!");
                } catch (NumberFormatException e) {
                    errorAlert("Time-field must contain number!");
                } catch (IllegalArgumentException e) {
                    errorAlert("Time can't be negative!");
                }
            }
        }
    }

    private class ChangeConnectionHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                try {
                    if (listGraph.getEdgeBetween(selectedNode1, selectedNode2) != null) {
                        AlertWithFields alert = getAlertWithFields();
                        alert.getNameField().setText(listGraph.getEdgeBetween(selectedNode1, selectedNode2).getName());
                        alert.getNameField().setEditable(false);
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                            listGraph.setConnectionWeight(selectedNode1, selectedNode2, alert.getTime());
                            unsavedChanges = true;
                        }
                    } else {
                        errorAlert("No connection between places exist!");
                    }
                } catch (NoSuchElementException e) {
                    errorAlert("Two places must be selected!");
                } catch (NumberFormatException e) {
                    errorAlert("Time-field must contain number!");
                } catch (IllegalArgumentException e) {
                    errorAlert("Time can't be negative!");
                }
            }
        }
    }

    private class SaveHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                try {
                    FileWriter writer = new FileWriter(SAVE_FILE_NAME);
                    writer.write(IMAGE_FILE_NAME + '\n');
                    Set<GraphNode> nodes = listGraph.getNodes();
                    for (Iterator<GraphNode> iterator = nodes.iterator(); iterator.hasNext(); ) {
                        GraphNode node = iterator.next();
                        writer.write(node.getName() + ";" + node.getCenterX() + ";" + node.getCenterY());
                        if (iterator.hasNext()) {
                            writer.write(";");
                        } else {
                            writer.write('\n');
                        }
                    }
                    for (GraphNode node : nodes) {
                        Set<Edge<GraphNode>> edges = listGraph.getEdgesFrom(node);
                        for (Edge<GraphNode> edge : edges) {
                            writer.write(node.getName() + ";" + edge.getDestination()
                                    + ";" + edge.getName() + ";" + edge.getWeight() + '\n');
                        }
                    }
                    writer.close();
                    unsavedChanges = false;
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }

    private class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                if (selectedNode1 != null && selectedNode2 != null) {
                    if (listGraph.pathExists(selectedNode1, selectedNode2)) {
                        List<Edge<GraphNode>> path = listGraph.getPath(selectedNode1, selectedNode2);
                        StringBuilder string = new StringBuilder();
                        int totalTime = 0;
                        for (Edge<GraphNode> edge : path) {
                            string.append(edge.toString()).append('\n');
                            totalTime += edge.getWeight();
                        }
                        string.append("Total ").append(totalTime);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        TextArea textArea = new TextArea();
                        textArea.setEditable(false);
                        textArea.setText(string.toString());
                        alert.setHeaderText("The path from " + selectedNode1.getName()
                                + " to " + selectedNode2.getName() + ":");
                        alert.getDialogPane().setContent(textArea);
                        alert.showAndWait();
                    } else {
                        errorAlert("No path exists between" + selectedNode1.getName()
                                + " and " + selectedNode2.getName());
                    }
                } else {
                    errorAlert("Two places must be selected!");
                }
            }
        }
    }

    private class ExitHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0 && unsavedChanges) {
                Alert alert = unsavedChangesAlert();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                    Platform.exit();
                }
            } else {
                Platform.exit();
            }
        }
    }

    private class NewMapHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() == 0) {
                newMap();
            } else if (unsavedChanges) {
                Alert alert = unsavedChangesAlert();
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                    newMap();
                }
            } else {
                newMap();
            }
        }

        private void newMap() {
            resetGraph();
            if (image == null) {
                image = new Image(IMAGE_FILE_NAME);
            }
            addImageToBP();
        }
    }

    private void addImageToBP() {
        imageView.setImage(image);
        pane.getChildren().clear();
        pane.getChildren().add(imageView);
        primaryStage.setWidth(image.getWidth() + SCENE_WIDTH_OFFSET);
        primaryStage.setHeight(image.getHeight() + SCENE_HEIGHT_OFFSET);
        bp.setBottom(pane);
    }

    private class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                WritableImage writeableImage = bp.getBottom().snapshot(
                        new SnapshotParameters(), null);
                File capture = new File("capture.png");
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(writeableImage, null), "png", capture);
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
                    alert.showAndWait();
                }
            }
        }
    }

    private class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (pane.getChildren().size() != 0) {
                scene.setCursor(Cursor.CROSSHAIR);
                newPlaceBt.setDisable(true);
                bp.getBottom().setOnMouseClicked(new MouseClickHandler());
            }
        }

        private class MouseClickHandler implements EventHandler<MouseEvent> {
            @Override
            public void handle(MouseEvent mouseEvent) {
                double x = mouseEvent.getX();
                double y = mouseEvent.getY();
                TextInputDialog dialog = new TextInputDialog();
                dialog.setContentText("Name of place:");
                dialog.setTitle("Name");
                dialog.setHeaderText(null);
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent() && !result.get().isBlank()) {
                    String name = result.get();
                    insertNode(name, x, y);
                    unsavedChanges = true;
                } else {
                    errorAlert("Name-field can't be empty!");
                }
                newPlaceBt.setDisable(false);
                bp.getBottom().setOnMouseClicked(null);
                scene.setCursor(Cursor.DEFAULT);
            }
        }
    }

    private class OpenHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                if (pane.getChildren().size() != 0 && unsavedChanges) {
                    Alert alert = unsavedChangesAlert();
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get().equals(ButtonType.OK)) {
                        openGraph();
                    }
                } else {
                    openGraph();
                }
            } catch (FileNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No such file!");
                alert.setTitle("File not found");
                alert.setHeaderText(null);
                alert.showAndWait();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "IO-error " + e.getMessage());
                alert.showAndWait();
            }
        }

        private void openGraph() throws IOException {
            resetGraph();
            FileReader infile = new FileReader("europa.graph");
            BufferedReader in = new BufferedReader(infile);
            String line;
            line = in.readLine();
            image = new Image(line);
            addImageToBP();

            // GraphNodes
            line = in.readLine();
            String[] split = line.split(";");
            for (int i = 0; i < split.length; i += 3) {
                String name = split[i];
                double x = Double.parseDouble(split[i + 1]);
                double y = Double.parseDouble(split[i + 2]);
                insertNode(name, x, y);
            }
            // Connections
            for (line = in.readLine(); line != null; line = in.readLine()) {
                String[] split2 = line.split(";");
                String from = split2[0];
                String to = split2[1];
                String by = split2[2];
                int weight = Integer.parseInt(split2[3]);
                insertConnection(from, to, by, weight);
            }
        }
    }

    private class NodeClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent mouseEvent) {
            GraphNode n = (GraphNode) mouseEvent.getSource();
            if (selectedNode1 == null) {
                if (selectedNode2 != null) {
                    selectedNode1 = selectedNode2;
                    selectedNode2 = n;
                } else {
                    selectedNode1 = n;
                    n.toggleMarked();
                }
            } else if (selectedNode2 == null && n != selectedNode1) {
                selectedNode2 = n;
                n.toggleMarked();
            } else if (selectedNode1 == n) {
                if (selectedNode2 != null) {
                    selectedNode1 = selectedNode2;
                    n.toggleMarked();
                    selectedNode2 = null;
                } else {
                    selectedNode1 = null;
                    n.toggleMarked();
                }
            } else if (selectedNode2 == n) {
                selectedNode2 = null;
                n.toggleMarked();
            }
        }
    }
}
