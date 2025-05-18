import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Trackify");
        primaryStage.setResizable(false);

        Image img = new Image("OIP.jpg");
        primaryStage.getIcons().add(img);

        // Show intro screen
        IntroScreen introScreen = new IntroScreen();
        primaryStage.setScene(introScreen.createScene());

        //login screen after 3 seconds
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> {
            Log_reg logReg = new Log_reg();
            logReg.start(primaryStage);
        });
        pause.play();

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}