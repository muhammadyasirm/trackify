import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Optional;

public class Log_reg extends Application {
    private UserDAO userDAO = new UserDAO();
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Trackify - Login");
        showLoginScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private void showLoginScene() {
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(20));
        formContainer.setMaxWidth(550);
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label title = new Label("Login to Trackify");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));

        TextField emailInput = new TextField();
        emailInput.setPromptText("Enter your email");
        emailInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14;");

        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Enter your password");
        passInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14;");

        Button loginButton = new Button("Login");
        loginButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #00838f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        loginButton.setOnAction(e -> {
            try {
                Optional<User> user = userDAO.loginUser(emailInput.getText(), passInput.getText());
                if (user.isPresent()) {
                    showMainApplication(user.get());
                } else {
                    showAlert("Login Failed", "Invalid email or password");
                }
            } catch (SQLException ex) {
                showAlert("Database Error", "Error connecting to database: " + ex.getMessage());
            }
        });

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register");
        registerLink.setStyle("-fx-text-fill: #00acc1; -fx-font-size: 14;");
        registerLink.setOnAction(e -> showRegisterScene());

        formContainer.getChildren().addAll(title, emailInput, passInput, loginButton, registerLink);

        StackPane root = new StackPane(formContainer);
        root.setStyle("-fx-background-color: #f1f5f9;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 1300, 700);
        primaryStage.setScene(scene);
    }

    private void showRegisterScene() {
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(20));
        formContainer.setMaxWidth(550);
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label title = new Label("Register for Trackify");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));

        TextField userInput = new TextField();
        userInput.setPromptText("Enter username");
        userInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14;");

        TextField emailInput = new TextField();
        emailInput.setPromptText("Enter your email");
        emailInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14;");

        PasswordField passInput = new PasswordField();
        passInput.setPromptText("Enter your password");
        passInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14;");

        Button registerButton = new Button("Register");
        registerButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #00838f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        registerButton.setOnAction(e -> {
            try {
                if (userInput.getText().isEmpty() || emailInput.getText().isEmpty() || passInput.getText().isEmpty()) {
                    showAlert("Registration Failed", "All fields are required");
                } else if (userDAO.emailExists(emailInput.getText())) {
                    showAlert("Registration Failed", "Email already exists");
                } else {
                    boolean registered = userDAO.registerUser(
                            userInput.getText(),
                            emailInput.getText(),
                            passInput.getText()
                    );
                    if (registered) {
                        showAlert("Registration Successful", "You can now login");
                        showLoginScene();
                    } else {
                        showAlert("Registration Failed", "Error creating account");
                    }
                }
            } catch (SQLException ex) {
                showAlert("Database Error", "Error connecting to database: " + ex.getMessage());
            }
        });

        Hyperlink loginLink = new Hyperlink("Already have an account? Login");
        loginLink.setStyle("-fx-text-fill: #00acc1; -fx-font-size: 14;");
        loginLink.setOnAction(e -> showLoginScene());

        formContainer.getChildren().addAll(title, userInput, emailInput, passInput, registerButton, loginLink);

        StackPane root = new StackPane(formContainer);
        root.setStyle("-fx-background-color: #f1f5f9;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 1300, 700);
        primaryStage.setScene(scene);
    }

    private void showMainApplication(User user) {
        MainScreen mainScreen = new MainScreen(user);
        primaryStage.setScene(mainScreen.createScene());
        primaryStage.setTitle("Trackify - Welcome, " + user.getUsername());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}