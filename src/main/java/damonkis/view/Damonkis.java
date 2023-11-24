package damonkis.view;

import damonkis.model.Monkeys;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Damonkis extends Application {
	private Monkeys monkeys;
	private BorderPane root;

	private VBox wordPromptScreen;

	private VBox typingScreen;
	private Thread updateTypingScreenThread;

	public static void launch(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Damonkis B)");
		this.root = new BorderPane();

		makeWordPromptScreen();
		makeTypingScreen();

		setScreen(wordPromptScreen);

		primaryStage.setScene(new Scene(root, 500, 400));
		primaryStage.show();
	}

	private void setScreen(Node screenRoot) {
		root.setCenter(screenRoot);
	}

	private void makeWordPromptScreen() {
		this.wordPromptScreen = new VBox(20);
		wordPromptScreen.setAlignment(Pos.CENTER);

		wordPromptScreen.getChildren().add(new Label("What should the monkeys type?"));

		HBox wordBox = new HBox(10);
		wordBox.setAlignment(Pos.CENTER);
		wordPromptScreen.getChildren().add(wordBox);

		TextField wordInput = new TextField();
		String allowedCharactersRegex = String.format("[^%s]", Monkeys.LEGAL_CHARACTERS);
		wordInput.setTextFormatter(new TextFormatter<>(change -> {
			if (change.isContentChange()) {
				change.setText(change.getControlNewText().replaceAll(allowedCharactersRegex, ""));
				change.setRange(0, change.getControlText().length());
			}
			return change;
		}));
		wordBox.getChildren().add(wordInput);

		Button startTypingButton = new Button("Start typing");
		startTypingButton.setOnAction(ae -> {
			monkeys = new Monkeys(wordInput.getText());
			monkeys.startTyping();
			updateTypingScreenThread.start();
			setScreen(typingScreen);
		});
		wordBox.getChildren().add(startTypingButton);
	}

	private void makeTypingScreen() {
		this.typingScreen = new VBox();
		typingScreen.setAlignment(Pos.CENTER);

		Label progressLabel = new Label();
		progressLabel.setTextAlignment(TextAlignment.CENTER);
		progressLabel.setMinHeight(100);
		typingScreen.getChildren().add(progressLabel);

		typingScreen.getChildren().add(new Label("HISTORICAL BREAKTHROUGHS:"));

		HBox historyContainer = new HBox();
		historyContainer.setAlignment(Pos.CENTER);
		typingScreen.getChildren().add(historyContainer);

		ScrollPane history = new DebugScrollPane();
		historyContainer.getChildren().add(history);

		TextFlow historyText = new TextFlow();
		history.setContent(historyText);

		this.updateTypingScreenThread = new Thread(() -> {
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
		updateTypingScreenThread.setDaemon(true);
	}
}
