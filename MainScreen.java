import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MainScreen {
    private User user;
    private BorderPane rightContent;
    private NotesManager notesManager;
    private TaskManager taskManager;

    public MainScreen(User user) {
        this.user = user;
        this.notesManager = new NotesManager(user.getUserId());
        this.taskManager = new TaskManager(user.getUserId());
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        // Sidebar
        VBox sidebar = new VBox(20);
        sidebar.setStyle("-fx-background-color: #1e293b; -fx-padding: 20;");
        sidebar.setPrefWidth(250);
        sidebar.setAlignment(Pos.TOP_CENTER);

        Label appTitle = new Label("Trackify");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        appTitle.setTextFill(Color.WHITE);

        Button notesButton = createSidebarButton("Notes");
        notesButton.setOnAction(e -> rightContent.setCenter(notesManager.createContent()));

        Button tasksButton = createSidebarButton("Tasks");
        tasksButton.setOnAction(e -> rightContent.setCenter(taskManager.createContent()));

        Button logoutButton = createSidebarButton("Logout");
        logoutButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: #b91c1c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        logoutButton.setOnAction(e -> {
            notesManager.closeConnection();
            taskManager.closeConnection();
            DBconnection.closeConnection();
            Log_reg logReg = new Log_reg();
            logReg.start(new Stage());
            logoutButton.getScene().getWindow().hide();
        });

        sidebar.getChildren().addAll(appTitle, notesButton, tasksButton, logoutButton);

        // Right Content Area
        rightContent = new BorderPane();
        rightContent.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 20;");
        rightContent.setCenter(notesManager.createContent()); // Default to Notes


        root.setLeft(sidebar);
        root.setCenter(rightContent);

        return new Scene(root, 1300, 700);
    }

    private Button createSidebarButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: #00838f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }
}