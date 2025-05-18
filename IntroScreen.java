import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class IntroScreen {

    public Scene createScene() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #f1f5f9;"); // Light gray background

        try {
            Image img1 = new Image("logo.png");
            ImageView vimg1 = new ImageView(img1);
            vimg1.setFitWidth(100);
            vimg1.setPreserveRatio(true);

            Text logoTxt = new Text("Trackify");
            logoTxt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 60));
            logoTxt.setFill(Color.web("#1e293b")); // Dark slate color


            HBox logoBox = new HBox(15);
            logoBox.getChildren().addAll(vimg1, logoTxt);
            logoBox.setAlignment(Pos.CENTER);
            logoBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 2);");
            root.getChildren().add(logoBox);
        } catch (Exception e) {
            System.err.println("Error loading logo image: " + e.getMessage());
            Text logoTxt = new Text("Trackify");
            logoTxt.setFont(Font.font("Segoe UI", FontWeight.BOLD, 60));
            logoTxt.setFill(Color.web("#1e293b"));
            root.getChildren().add(logoTxt);
        }

        return new Scene(root, 1300, 700);
    }
}