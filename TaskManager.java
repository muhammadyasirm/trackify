import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.*;
import java.time.LocalDate;

public class TaskManager {
    private final int userId;
    private VBox tasksContainer;
    private final String[] PRIORITIES = {"A", "B", "C", "D"};
    private final String[] CATEGORIES = {"Work", "Personal", "Study", "Home", "Other"};

    public TaskManager(int userId) {
        this.userId = userId;
    }

    public Parent createContent() {
        BorderPane root = new BorderPane();

        root.setStyle("-fx-background-color: #f1f5f9;");

        HBox header = createHeader();
        root.setTop(header);

        tasksContainer = new VBox(20);
        tasksContainer.setPadding(new Insets(20));
        tasksContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(tasksContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        loadTasksFromDatabase();

        HBox bottomBox = createBottomBox();
        root.setBottom(bottomBox);

        return root;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Tasks");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));

        header.getChildren().add(title);
        return header;
    }

    private HBox createBottomBox() {
        TextField taskInput = new TextField();
        taskInput.setPromptText("Enter your task here...");
        taskInput.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-padding: 10; -fx-font-size: 14; -fx-background-color: white;");
        taskInput.setPrefWidth(400);

        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(PRIORITIES);
        priorityComboBox.setValue("C");
        priorityComboBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14;");

        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(CATEGORIES);
        categoryComboBox.setValue("Other");
        categoryComboBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14;");

        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setPromptText("Select due date");
        dueDatePicker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14;");

        Button addTaskButton = new Button("Add Task");
        addTaskButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        addTaskButton.setOnMouseEntered(e -> addTaskButton.setStyle("-fx-background-color: #00838f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        addTaskButton.setOnMouseExited(e -> addTaskButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        addTaskButton.setOnAction(e -> {
            String taskText = taskInput.getText().trim();
            if (taskText.isEmpty()) {
                showAlert("Invalid Input", "Task description cannot be empty");
                return;
            }
            try {
                addTaskToDatabase(
                        taskText,
                        dueDatePicker.getValue() != null ? Date.valueOf(dueDatePicker.getValue()) : null,
                        null,
                        priorityComboBox.getValue(),
                        categoryComboBox.getValue()
                );
                loadTasksFromDatabase();
                taskInput.clear();
                dueDatePicker.setValue(null);
            } catch (SQLException ex) {
                showAlert("Database Error", "Error adding task: " + ex.getMessage());
            }
        });

        HBox bottomBox = new HBox(10, taskInput, priorityComboBox, categoryComboBox, dueDatePicker, addTaskButton);
        bottomBox.setPadding(new Insets(20));
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        return bottomBox;
    }

    private void loadTasksFromDatabase() {
        tasksContainer.getChildren().clear();
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM tasks WHERE user_ID = ? ORDER BY task_ID ASC")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    createTaskCard(
                            rs.getInt("task_ID"),
                            rs.getString("task"),
                            rs.getDate("dueDate"),
                            rs.getTime("dueTime"),
                            rs.getString("priority"),
                            rs.getString("category"),
                            rs.getBoolean("completed")
                    );
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error loading tasks: " + e.getMessage());
        }
    }

    private void addTaskToDatabase(String taskText, Date dueDate, Time dueTime, String priority, String category) throws SQLException {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO tasks (task, dueDate, dueTime, priority, category, completed, user_ID) VALUES (?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, taskText);
            stmt.setDate(2, dueDate);
            stmt.setTime(3, dueTime);
            stmt.setString(4, priority);
            stmt.setString(5, category);
            stmt.setBoolean(6, false);
            stmt.setInt(7, userId);
            stmt.executeUpdate();
        }
    }

    private void updateTaskInDatabase(int id, String taskText, Date dueDate, Time dueTime, String priority, String category, boolean completed) throws SQLException {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE tasks SET task = ?, dueDate = ?, dueTime = ?, priority = ?, category = ?, completed = ? WHERE task_ID = ? AND user_ID = ?")) {
            stmt.setString(1, taskText);
            stmt.setDate(2, dueDate);
            stmt.setTime(3, dueTime);
            stmt.setString(4, priority);
            stmt.setString(5, category);
            stmt.setBoolean(6, completed);
            stmt.setInt(7, id);
            stmt.setInt(8, userId);
            stmt.executeUpdate();
        }
    }

    private void removeTaskFromDatabase(int id) throws SQLException {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM tasks WHERE task_ID = ? AND user_ID = ?")) {
            stmt.setInt(1, id);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    private void createTaskCard(int id, String taskText, Date dueDate, Time dueTime, String priority, String category, boolean completed) {
        VBox taskCard = new VBox(15);
        taskCard.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2); -fx-padding: 20;");
        taskCard.setPrefWidth(600);

        Label taskLabel = new Label();
        taskLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        taskLabel.setWrapText(true);
        taskLabel.setMaxWidth(500);
        String previewText = taskText.trim();
        if (previewText.length() > 50) {
            previewText = previewText.substring(0, 50) + "...";
        }
        taskLabel.setText(previewText);
        if (completed) {
            taskLabel.setStyle("-fx-text-fill: #94a3b8; -fx-strikethrough: true;");
        } else {
            taskLabel.setStyle("-fx-text-fill: #1e293b;");
        }

        HBox actionBox = new HBox(10);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;");
        editButton.setOnMouseEntered(e -> editButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;"));
        editButton.setOnMouseExited(e -> editButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;"));
        editButton.setOnAction(e -> openEditDialog(id, taskText, dueDate, dueTime, priority, category, completed));

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #fecaca; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;"));
        deleteButton.setOnAction(e -> {
            try {
                removeTaskFromDatabase(id);
                loadTasksFromDatabase();
            } catch (SQLException ex) {
                showAlert("Database Error", "Error deleting task: " + ex.getMessage());
            }
        });

        actionBox.getChildren().addAll(editButton, deleteButton);
        taskCard.getChildren().addAll(taskLabel, actionBox);
        tasksContainer.getChildren().add(taskCard);
    }

    private String getPriorityStyle(String priority) {
        switch (priority) {
            case "A": return "-fx-background-color: #fee2e2; -fx-background-radius: 12; -fx-text-fill: #b91c1c;";
            case "B": return "-fx-background-color: #fef3c7; -fx-background-radius: 12; -fx-text-fill: #b45309;";
            case "C": return "-fx-background-color: #dcfce7; -fx-background-radius: 12; -fx-text-fill: #15803d;";
            default: return "-fx-background-color: #e0e7ff; -fx-background-radius: 12; -fx-text-fill: #4338ca;";
        }
    }

    private void openEditDialog(int id, String taskText, Date dueDate, Time dueTime, String priority, String category, boolean completed) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Task");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        Label taskLabel = new Label("Task:");
        taskLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        TextArea taskField = new TextArea(taskText);
        taskField.setWrapText(true);
        taskField.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1.5; -fx-font-size: 14; -fx-pref-height: 100;");

        Label dueDateLabel = new Label("Due Date:");
        dueDateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        DatePicker datePicker = new DatePicker();
        if (dueDate != null) datePicker.setValue(dueDate.toLocalDate());
        datePicker.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14;");

        Label priorityLabel = new Label("Priority:");
        priorityLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        ComboBox<String> priorityComboBox = new ComboBox<>();
        priorityComboBox.getItems().addAll(PRIORITIES);
        priorityComboBox.setValue(priority);
        priorityComboBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14;");

        Label categoryLabel = new Label("Category:");
        categoryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        ComboBox<String> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll(CATEGORIES);
        categoryComboBox.setValue(category);
        categoryComboBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-font-size: 14;");

        Label completedLabel = new Label("Completed:");
        completedLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        CheckBox completedCheckBox = new CheckBox();
        completedCheckBox.setSelected(completed);

        content.getChildren().addAll(
                taskLabel, taskField,
                dueDateLabel, datePicker,
                priorityLabel, priorityComboBox,
                categoryLabel, categoryComboBox,
                completedLabel, completedCheckBox
        );

        dialog.getDialogPane().setContent(content);
        taskField.requestFocus();

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == saveButtonType) {
                if (taskField.getText().trim().isEmpty()) {
                    showAlert("Invalid Input", "Task description cannot be empty");
                    return;
                }
                try {
                    updateTaskInDatabase(
                            id,
                            taskField.getText(),
                            datePicker.getValue() != null ? Date.valueOf(datePicker.getValue()) : null,
                            null,
                            priorityComboBox.getValue(),
                            categoryComboBox.getValue(),
                            completedCheckBox.isSelected()
                    );
                    loadTasksFromDatabase();
                } catch (SQLException ex) {
                    showAlert("Database Error", "Error updating task: " + ex.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void closeConnection() {
        // Connection is managed by DBconnection
    }
}