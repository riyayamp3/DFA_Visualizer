package ui;

import generator.DFAGenerator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.DFA;

/**
 * InputPanel — TOP section of the UI.
 *
 * Contains:
 *   - Condition field  (e.g. "ends with abb")
 *   - Input string     (e.g. "aabbabb")
 *   - Alphabet field   (e.g. "ab")
 *   - [Generate DFA]   button
 *   - [Start Simulation] button
 *   - Status label
 */
public class InputPanel extends VBox {

    // ── Fields ───────────────────────────────────────────────────────────────
    private final TextField conditionField;
    private final TextField inputStringField;
    private final TextField alphabetField;
    private final Label     statusLabel;

    // ── References to other panels ───────────────────────────────────────────
    private final GraphPane       graphPane;
    private final TransitionTable transitionTable;
    private final ControlPanel    controlPanel;

    // ── Current DFA ──────────────────────────────────────────────────────────
    private DFA currentDFA;

    public InputPanel(GraphPane graphPane,
                      TransitionTable transitionTable,
                      ControlPanel controlPanel) {

        this.graphPane       = graphPane;
        this.transitionTable = transitionTable;
        this.controlPanel    = controlPanel;
        controlPanel.setVisualRefs(graphPane, transitionTable);

        // ── Title ────────────────────────────────────────────────────────────
        Label title = new Label("DFA Visualizer");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: #cdd6f4;");

        // ── Input fields ─────────────────────────────────────────────────────
        conditionField    = styledField("ends with abb", 280);
        inputStringField  = styledField("aabbabb",       180);
        alphabetField     = styledField("ab",             80);

        // ── Labels ───────────────────────────────────────────────────────────
        HBox row = new HBox(12,
            labeledField("Condition",    conditionField),
            labeledField("Input String", inputStringField),
            labeledField("Alphabet",     alphabetField)
        );
        row.setAlignment(Pos.CENTER_LEFT);

        // ── Buttons ──────────────────────────────────────────────────────────
        Button generateBtn  = styledButton("⚙  Generate DFA",    "#89b4fa");
        Button simulateBtn  = styledButton("▶  Start Simulation", "#a6e3a1");

        generateBtn.setOnAction(e -> onGenerate());
        simulateBtn.setOnAction(e -> onSimulate());

        HBox buttons = new HBox(10, generateBtn, simulateBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        // ── Status label ─────────────────────────────────────────────────────
        statusLabel = new Label("Enter a condition and click Generate DFA.");
        statusLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

        // ── Hint label ───────────────────────────────────────────────────────
        Label hint = new Label(
            "Conditions: ends with · starts with · contains · even/odd number of"
        );
        hint.setStyle("-fx-text-fill: #585b70; -fx-font-size: 11px;");

        // ── Layout ───────────────────────────────────────────────────────────
        HBox topRow = new HBox(20, title, row, buttons);
        topRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(topRow, statusLabel, hint);
        setSpacing(6);
        setPadding(new Insets(12, 16, 10, 16));
        setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 0 0 1 0;");
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void onGenerate() {
        String condition = conditionField.getText().trim();
        String alphabet  = alphabetField.getText().trim();

        if (condition.isEmpty() || alphabet.isEmpty()) {
            setStatus("⚠  Please fill in Condition and Alphabet.", "#fab387");
            return;
        }

        try {
            currentDFA = DFAGenerator.generate(condition, alphabet);
            graphPane.drawDFA(currentDFA);
            transitionTable.populate(currentDFA);
            controlPanel.setDFA(currentDFA, inputStringField.getText().trim());
            setStatus("✅  DFA generated for: \"" + condition + "\"", "#a6e3a1");
        } catch (IllegalArgumentException ex) {
            setStatus("❌  " + ex.getMessage(), "#f38ba8");
        }
    }

    private void onSimulate() {
        if (currentDFA == null) {
            setStatus("⚠  Generate a DFA first.", "#fab387");
            return;
        }
        String input = inputStringField.getText().trim();
        controlPanel.startSimulation(currentDFA, input);
        setStatus("▶  Simulating: \"" + input + "\"", "#89dceb");
    }

    private void setStatus(String msg, String color) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
    }

    // ── UI Helpers ───────────────────────────────────────────────────────────

    private TextField styledField(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setText(prompt);
        tf.setPrefWidth(width);
        tf.setStyle(
            "-fx-background-color: #313244;" +
            "-fx-text-fill: #cdd6f4;" +
            "-fx-prompt-text-fill: #585b70;" +
            "-fx-border-color: #45475a;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 5 8 5 8;"
        );
        return tf;
    }

    private VBox labeledField(String labelText, TextField field) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 11px;");
        VBox box = new VBox(3, lbl, field);
        return box;
    }

    private Button styledButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #313244;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 7 14 7 14;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: #1e1e2e;" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 7 14 7 14;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #313244;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 7 14 7 14;" +
            "-fx-font-size: 13px;" +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    public DFA getCurrentDFA() { return currentDFA; }
}
