import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

import java.sql.*;

public class NotesManager {
    private final int userId;
    private ObservableList<Note> notes = FXCollections.observableArrayList();
    private FlowPane cardsContainer;

    public NotesManager(int userId) {
        this.userId = userId;
    }

    public Parent createContent() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f1f5f9;");

        HBox header = createHeader();
        mainLayout.setTop(header);

        cardsContainer = new FlowPane();
        cardsContainer.setPadding(new Insets(20));
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setPrefWrapLength(1000);

        loadNotes();
        updateNotesDisplay();

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        mainLayout.setCenter(scrollPane);

        return mainLayout;
    }

    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15));
        header.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Notes");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#1e293b"));

        Button addButton = new Button("+ New Note");
        addButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #00838f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #00acc1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        addButton.setOnAction(e -> openNoteEditor(null));

        header.getChildren().addAll(title, addButton);
        return header;
    }

    private void openNoteEditor(Note existingNote) {
        Stage editorStage = new Stage();
        BorderPane editorLayout = new BorderPane();
        editorLayout.setStyle("-fx-background-color: white;");

        HTMLEditor htmlEditor = new HTMLEditor();
        if (existingNote != null) {
            htmlEditor.setHtmlText(existingNote.getContent());
        }

        Button saveButton = new Button("Save & Close");
        saveButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;");
        saveButton.setOnMouseEntered(e -> saveButton.setStyle("-fx-background-color: #4338ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        saveButton.setOnMouseExited(e -> saveButton.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-background-radius: 8; -fx-padding: 10 20;"));
        saveButton.setOnAction(e -> {
            String content = htmlEditor.getHtmlText();
            if (content.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid Input");
                alert.setContentText("Note content cannot be empty");
                alert.showAndWait();
                return;
            }

            try {
                if (existingNote == null) {
                    saveNoteToDatabase(content);
                } else {
                    updateNoteInDatabase(existingNote.getId(), content);
                    existingNote.setContent(content);
                }
                updateNotesDisplay();
                editorStage.close();
            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Error saving note: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        HBox buttonBar = new HBox(saveButton);
        buttonBar.setPadding(new Insets(15));
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        buttonBar.setStyle("-fx-background-color: #f8fafc;");

        editorLayout.setCenter(htmlEditor);
        editorLayout.setBottom(buttonBar);

        Scene editorScene = new Scene(editorLayout, 800, 600);
        editorStage.setTitle(existingNote == null ? "New Note" : "Edit Note");
        editorStage.setScene(editorScene);
        editorStage.show();
    }

    private void loadNotes() {
        notes.clear();
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT Notes_ID, Notes FROM notes WHERE user_ID = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(new Note(rs.getInt("Notes_ID"), rs.getString("Notes")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading notes: " + e.getMessage());
        }
    }

    private void saveNoteToDatabase(String content) throws SQLException {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO notes (user_ID, Notes, lastModified) VALUES (?, ?, CURRENT_TIMESTAMP)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, content);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        notes.add(new Note(generatedKeys.getInt(1), content));
                    }
                }
            }
        }
    }

    private void updateNoteInDatabase(int noteId, String content) throws SQLException {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE notes SET Notes = ?, lastModified = CURRENT_TIMESTAMP WHERE Notes_ID = ? AND user_ID = ?")) {
            stmt.setString(1, content);
            stmt.setInt(2, noteId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    private void deleteNoteFromDatabase(int noteId) throws SQLException {
        try (Connection conn = DBconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM notes WHERE Notes_ID = ? AND user_ID = ?")) {
            stmt.setInt(1, noteId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            notes.removeIf(note -> note.getId() == noteId);
        }
    }

    private void updateNotesDisplay() {
        cardsContainer.getChildren().clear();
        for (Note note : notes) {
            cardsContainer.getChildren().add(createNoteCard(note));
        }
    }

    private VBox createNoteCard(Note note) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        card.setPrefWidth(250);
        card.setMinHeight(200);
        card.setPadding(new Insets(15));

        Label preview = new Label();
        preview.setWrapText(true);
        preview.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        String plainText = note.getContent().replaceAll("<[^>]*>", "").replaceAll(" ", " ").trim();
        if (plainText.length() > 100) {
            plainText = plainText.substring(0, 100) + "...";
        }
        preview.setText(plainText);

        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;");
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-background-color: #fecaca; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; -fx-padding: 8 15;"));
        deleteButton.setOnAction(e -> {
            try {
                deleteNoteFromDatabase(note.getId());
                updateNotesDisplay();
            } catch (SQLException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setContentText("Error deleting note: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        HBox buttonContainer = new HBox(deleteButton);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        card.getChildren().addAll(preview, buttonContainer);
        card.setOnMouseClicked(e -> openNoteEditor(note));

        return card;
    }

    public void closeConnection() {
        // Connection is managed by DBconnection
    }

    private static class Note {
        private final int id;
        private String content;

        public Note(int id, String content) {
            this.id = id;
            this.content = content;
        }

        public int getId() { return id; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}