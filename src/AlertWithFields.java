
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

// *** VT2021, Inlämningsuppgift, del 2
// Grupp 096
// joas47

public class AlertWithFields extends Alert {

    private TextField nameField = new TextField();
    private TextField timeField = new TextField();

    public AlertWithFields() {
        super(AlertType.CONFIRMATION);
        GridPane gridPane = new GridPane();
        gridPane.addRow(0, new Label("Name: "), nameField);
        gridPane.addRow(1, new Label("Time: "), timeField);
        gridPane.setAlignment(Pos.CENTER);
        getDialogPane().setContent(gridPane);
    }

    public TextField getNameField() {
        return nameField;
    }

    public TextField getTimeField() {
        return timeField;
    }

    public String getName() {
        return nameField.getText();
    }

    public int getTime() {
        return Integer.parseInt(timeField.getText());
    }
}

