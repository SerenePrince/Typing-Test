package typingtest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class TypingTest extends Application {

	private static final String WORDS_FILE = "words_alpha.txt"; // Path to the file containing words
	private static final int NUM_WORDS = 50; // Number of words to be displayed
	private static final Font FONT = Font.font("Roboto-Regular.tff", 18); // Font style and size

	private List<String> words; // List of words for the typing test
	private int currentWordIndex = 0; // Index of the current word to be typed
	private int correctWordCount = 0; // Number of correctly typed words
	private int totalCharactersTyped = 0; // Total characters typed by the user
	private boolean timerStarted = false; // Flag to indicate if the timer has started
	private long startTime = 0; // Start time for calculating WPM
	private TextFlow textFlow; // Display area for the words
	private TextField inputField; // TextField for user input
	private Button resetButton; // Button to reset the game

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		// Initialize UI components
		textFlow = new TextFlow();
		inputField = new TextField();
		inputField.setPadding(new javafx.geometry.Insets(5)); // Add padding for aesthetics

		resetButton = new Button("Reset");
		resetButton.getStyleClass().add("reset-button"); // Apply CSS styling
		resetButton.setOnAction(e -> resetGame()); // Reset game on button click

		// Set up key press event handler
		inputField.setOnKeyPressed(this::handleKeyPress);

		// Load words from file and display them
		words = loadWords();
		if (words != null) {
			displayWordsWithColors(words);
		}

		// Layout configuration
		HBox inputLayout = new HBox(10);
		inputLayout.getChildren().addAll(inputField, resetButton);
		HBox.setHgrow(inputField, Priority.ALWAYS); // Allow TextField to grow
		inputLayout.setAlignment(Pos.CENTER_LEFT); // Align elements to the left

		VBox layout = new VBox(10);
		layout.getChildren().addAll(textFlow, inputLayout);
		layout.setPadding(new javafx.geometry.Insets(10)); // Add padding to the layout

		// Set up the scene and apply CSS stylesheet
		Scene scene = new Scene(layout, 800, 250);
		scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // Load the CSS file
		primaryStage.setTitle("Typing Game");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * Load words from the specified file and return a random selection of
	 * NUM_WORDS.
	 */
	private List<String> loadWords() {
		try {
			List<String> allWords = Files.readAllLines(Paths.get(WORDS_FILE));
			Collections.shuffle(allWords); // Shuffle to get a random selection
			return allWords.subList(0, NUM_WORDS); // Return the first NUM_WORDS words
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Handle key press events in the input field. Start the timer on first key
	 * press and handle space and backspace events.
	 */
	private void handleKeyPress(KeyEvent event) {
		if (!timerStarted && !inputField.getText().isEmpty()) {
			startTime = System.nanoTime(); // Start the timer
			timerStarted = true;
		}

		if (event.getCode() != KeyCode.SPACE && event.getCode() != KeyCode.BACK_SPACE) {
			totalCharactersTyped++;
		}

		if (event.getCode() == KeyCode.SPACE) {
			String typedWord = inputField.getText().trim();
			totalCharactersTyped += typedWord.length(); // Add length of the typed word

			if (typedWord.equals(words.get(currentWordIndex))) {
				// Correct word
				correctWordCount++;
				updateWordDisplay(currentWordIndex, Color.GREEN);
			} else {
				// Incorrect word
				updateWordDisplay(currentWordIndex, Color.RED);
			}
			currentWordIndex++;
			inputField.clear();

			// Check if the test is complete
			if (currentWordIndex >= words.size()) {
				inputField.setDisable(true);
				displayAccuracy();
				long endTime = System.nanoTime();
				calculateAndDisplayWPM(endTime);
			}
		} else if (event.getCode() == KeyCode.BACK_SPACE) {
			// Reset border color when backspace is pressed
			changeBorderColor(Color.GRAY);
		}
	}

	/**
	 * Display the words with the appropriate color and styling.
	 */
	private void displayWordsWithColors(List<String> words) {
		textFlow.getChildren().clear();
		for (String word : words) {
			Text wordText = new Text(word + " ");
			wordText.setFill(Color.WHITE); // Default text color
			wordText.setFont(FONT);
			textFlow.getChildren().add(wordText);
		}
	}

	/**
	 * Update the display color of a specific word to indicate correctness.
	 * 
	 * @param index Index of the word to update
	 * @param color Color to apply
	 */
	private void updateWordDisplay(int index, Color color) {
		if (index < 0 || index >= words.size()) {
			return;
		}
		Text wordText = (Text) textFlow.getChildren().get(index);
		wordText.setFill(color);
	}

	/**
	 * Change the border color of the input field.
	 * 
	 * @param color Color to apply
	 */
	private void changeBorderColor(Color color) {
		inputField.setStyle("-fx-border-color: " + toRgbString(color) + ";");
	}

	/**
	 * Convert a Color object to an RGB string for CSS styling.
	 * 
	 * @param color Color to convert
	 * @return RGB string representation
	 */
	private String toRgbString(Color color) {
		return String.format("rgb(%d,%d,%d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255),
				(int) (color.getBlue() * 255));
	}

	/**
	 * Display the user's typing accuracy.
	 */
	private void displayAccuracy() {
		double accuracy = ((double) correctWordCount / NUM_WORDS) * 100;
		textFlow.getChildren().clear();
		Text accuracyText = new Text(String.format("Test completed!\nAccuracy: %.2f%%\n", accuracy));
		accuracyText.setFont(FONT);
		accuracyText.setFill(Color.WHITE);
		textFlow.getChildren().add(accuracyText);
	}

	/**
	 * Calculate and display Words Per Minute (WPM).
	 * 
	 * @param endTime Time in nanoseconds when the test ended
	 */
	private void calculateAndDisplayWPM(long endTime) {
		long timeElapsedNano = endTime - startTime; // Time elapsed in nanoseconds
		double timeElapsedMinutes = timeElapsedNano / (1_000_000_000.0 * 60); // Convert nanoseconds to minutes

		if (timeElapsedMinutes <= 0) {
			timeElapsedMinutes = 1; // Prevent division by zero or extremely small time values
		}

		// Number of words is total characters typed divided by 5 (standard average word
		// length)
		double wordsTyped = totalCharactersTyped / 5.0;
		double wpm = wordsTyped / timeElapsedMinutes; // Calculate WPM

		Text wpmText = new Text(String.format("Words Per Minute (WPM): %.2f", wpm));
		wpmText.setFont(FONT);
		wpmText.setFill(Color.WHITE);
		textFlow.getChildren().add(wpmText);
	}

	/**
	 * Reset the game to its initial state.
	 */
	private void resetGame() {
		currentWordIndex = 0;
		correctWordCount = 0;
		totalCharactersTyped = 0;
		timerStarted = false;
		inputField.clear();
		inputField.setDisable(false);

		words = loadWords();
		if (words != null) {
			displayWordsWithColors(words);
		}

		// Clear previous results but keep the words
		textFlow.getChildren().removeIf(node -> node instanceof Text && ((Text) node).getFill() != Color.WHITE);
	}
}
