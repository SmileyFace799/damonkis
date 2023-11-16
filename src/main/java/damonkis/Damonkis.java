package damonkis;

import damonkis.model.Monkeys;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class Damonkis extends Application {

    public static void launch(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Damonkis B)");

        FlowPane root = new FlowPane();
        root.setAlignment(Pos.CENTER);

        Label progressLabel = new Label();
        progressLabel.setTextAlignment(TextAlignment.CENTER);
        root.getChildren().add(progressLabel);

        primaryStage.setScene(new Scene(root, 500, 300));
        primaryStage.show();

        Monkeys monkeys = new Monkeys("Henrik");
        monkeys.startTyping();

        Thread screenUpdateThread = new Thread(() -> {
            boolean stringFound = false;
            while (!stringFound) {
                stringFound = monkeys.getClosestResult().match() == 1;
                Platform.runLater(() -> progressLabel.setText(monkeys.getProgress()));
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        screenUpdateThread.setDaemon(true);
        screenUpdateThread.start();
    }
}
