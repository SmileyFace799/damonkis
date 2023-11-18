package damonkis.view;

import damonkis.model.Monkeys;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Damonkis extends Application {

    public static void launch(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Damonkis B)");

        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);

        Label progressLabel = new Label();
        progressLabel.setTextAlignment(TextAlignment.CENTER);
        progressLabel.setMinHeight(100);
        root.getChildren().add(progressLabel);

        root.getChildren().add(new Label("HISTORICAL BREAKTHROUGHS:"));

        HBox historyContainer = new HBox();
        historyContainer.setAlignment(Pos.CENTER);

        root.getChildren().add(historyContainer);
        ScrollPane history = new DebugScrollPane();
        historyContainer.getChildren().add(history);

        TextFlow historyText = new TextFlow();
        history.setContent(historyText);

        primaryStage.setScene(new Scene(root, 500, 400));
        primaryStage.show();

        Monkeys monkeys = new Monkeys("Skibidi");
        monkeys.startTyping();

        Thread screenUpdateThread = new Thread(() -> {
            boolean monkeysFinished = false;
            while (!monkeysFinished) {
                monkeysFinished = monkeys.isFinished();
                List<Text> newHistoryText;
                synchronized (Monkeys.HISTORY_LOCK) {
                    newHistoryText = monkeys
                            .getNewHistory()
                            .stream()
                            .map(result -> {
                                Text text = new Text(StringFormatter.getResultString(result) + "\n");
                                if (result.first()) {
                                    text.setStyle("-fx-font-weight: bold;");
                                }
                                return text;
                            })
                            .toList();
                }
                monkeys.clearNewHistory();
                Platform.runLater(() -> {
                    progressLabel.setText(StringFormatter.getProgressString(monkeys));
                    historyText.getChildren().addAll(0, newHistoryText);
                });
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
