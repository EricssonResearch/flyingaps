import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * The main class that creates the GUI.
 */
public class Main extends Application {

    /**
     * The entry point for JavaFx Gui creation.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Drone Command");

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("drone_command.fxml"));
            Pane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
            primaryStage.setOnCloseRequest((e) -> {
                    Platform.exit();
                    System.exit(0);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The main method that is first to be called.
     */
    public static void main(String[] args) {
        launch(args);
    }
}