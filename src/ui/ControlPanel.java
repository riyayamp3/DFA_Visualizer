package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import model.DFA;
import model.State;
import simulator.DFASimulator;
import simulator.DFASimulator.SimulationStep;

import java.util.List;

/**
 * ControlPanel — BOTTOM section of the UI.
 *
 * Contains:
 *   - Play / Pause / Next Step / Prev Step / Reset buttons
 *   - Speed slider
 *   - Result label (ACCEPTED / REJECTED)
 *   - Transition history log
 *
 * Drives the JavaFX Timeline animation.
 */
public class ControlPanel extends VBox {

    // ── Sub-panels ────────────────────────────────────────────────────────────
    private GraphPane       graphPane;
    private TransitionTable transitionTable;

    // ── Simulation state ─────────────────────────────────────────────────────
    private DFA                   currentDFA;
    private List<SimulationStep>  steps;
    private int                   stepIndex = 0;
    private Timeline              timeline;

    // ── Controls ─────────────────────────────────────────────────────────────
    private final Button   playBtn;
    private final Button   pauseBtn;
    private final Button   nextBtn;
    private final Button   prevBtn;
    private final Button   resetBtn;
    private final Slider   speedSlider;
    private final Label    resultLabel;
    private final TextArea logArea;

    // ── Back-reference set by InputPanel ─────────────────────────────────────
    public ControlPanel() {

        setStyle("-fx-background-color: #181825; -fx-border-color: #313244; -fx-border-width: 1 0 0 0;");
        setPadding(new Insets(10, 14, 10, 14));
        setSpacing(6);

        // ── Buttons ──────────────────────────────────────────────────────────
        playBtn  = ctrlButton("▶  Play",      "#a6e3a1");
        pauseBtn = ctrlButton("⏸  Pause",     "#f9e2af");
        nextBtn  = ctrlButton("⏭  Next",      "#89b4fa");
        prevBtn  = ctrlButton("⏮  Prev",      "#89dceb");
        resetBtn = ctrlButton("↺  Reset",     "#f38ba8");

        pauseBtn.setDisable(true);

        playBtn.setOnAction(e  -> onPlay());
        pauseBtn.setOnAction(e -> onPause());
        nextBtn.setOnAction(e  -> onNext());
        prevBtn.setOnAction(e  -> onPrev());
        resetBtn.setOnAction(e -> onReset());

        // ── Speed slider ─────────────────────────────────────────────────────
        Label speedLabel = new Label("Speed:");
        speedLabel.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 12px;");

        speedSlider = new Slider(200, 2000, 800);
        speedSlider.setShowTickLabels(false);
        speedSlider.setPrefWidth(130);
        speedSlider.setStyle("-fx-control-inner-background: #313244;");

        Label slowLabel = new Label("Slow");
        Label fastLabel = new Label("Fast");
        slowLabel.setStyle("-fx-text-fill: #585b70; -fx-font-size: 10px;");
        fastLabel.setStyle("-fx-text-fill: #585b70; -fx-font-size: 10px;");

        HBox speedBox = new HBox(6, speedLabel, slowLabel, speedSlider, fastLabel);
        speedBox.setAlignment(Pos.CENTER_LEFT);

        // ── Result label ─────────────────────────────────────────────────────
        resultLabel = new Label("");
        resultLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        resultLabel.setStyle("-fx-text-fill: #cdd6f4;");

        // ── Log area ─────────────────────────────────────────────────────────
        Label logTitle = new Label("Transition History");
        logTitle.setStyle("-fx-text-fill: #a6adc8; -fx-font-size: 11px;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(70);
        logArea.setStyle(
            "-fx-control-inner-background: #2a2a3e;" +
            "-fx-text-fill: #cdd6f4;" +
            "-fx-font-family: 'Consolas'; -fx-font-size: 12px;" +
            "-fx-border-color: #313244;" +
            "-fx-background-color: #2a2a3e;"
        );
        logArea.setPromptText("Transition steps will appear here...");

        // ── Assemble ─────────────────────────────────────────────────────────
        HBox btnRow = new HBox(8, playBtn, pauseBtn, nextBtn, prevBtn, resetBtn, speedBox, resultLabel);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(btnRow, logTitle, logArea);
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /** Inject graph and table references after construction. */
    public void setVisualRefs(GraphPane gp, TransitionTable tt) {
        this.graphPane       = gp;
        this.transitionTable = tt;
    }

    /** Called by InputPanel after generating a DFA. */
    public void setDFA(DFA dfa, String inputString) {
        this.currentDFA = dfa;
        onReset();
        DFASimulator sim = new DFASimulator(dfa, inputString);
        sim.simulate();
        this.steps = sim.getSteps();
    }

    /** Called by InputPanel's "Start Simulation" button. */
    public void startSimulation(DFA dfa, String inputString) {
        DFASimulator sim = new DFASimulator(dfa, inputString);
        sim.simulate();
        this.currentDFA = dfa;
        this.steps = sim.getSteps();
        stepIndex = 0;
        logArea.clear();
        resultLabel.setText("");
        onPlay();
    }

    // ── Button Handlers ──────────────────────────────────────────────────────

    private void onPlay() {
        if (steps == null || steps.isEmpty()) return;
        if (timeline != null) timeline.stop();

        // Speed: slider value = milliseconds per step (inverted: higher = slower)
        double ms = 2200 - speedSlider.getValue(); // 200ms–2000ms

        timeline = new Timeline(new KeyFrame(
            Duration.millis(ms),
            e -> advanceStep()
        ));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        playBtn.setDisable(true);
        pauseBtn.setDisable(false);
    }

    private void onPause() {
        if (timeline != null) timeline.pause();
        playBtn.setDisable(false);
        pauseBtn.setDisable(true);
    }

    private void onNext() {
        if (timeline != null) timeline.pause();
        playBtn.setDisable(false);
        pauseBtn.setDisable(true);
        advanceStep();
    }

    private void onPrev() {
        if (timeline != null) timeline.pause();
        if (stepIndex > 1) {
            stepIndex--;
            replayUpTo(stepIndex);
        }
    }

    private void onReset() {
        if (timeline != null) timeline.stop();
        stepIndex = 0;
        logArea.clear();
        resultLabel.setText("");
        playBtn.setDisable(false);
        pauseBtn.setDisable(true);

        if (currentDFA != null && graphPane != null) {
            graphPane.resetColors(currentDFA);
        }
        if (transitionTable != null) {
            transitionTable.resetHighlight();
        }
    }

    // ── Step Logic ───────────────────────────────────────────────────────────

    private void advanceStep() {
        if (steps == null || stepIndex >= steps.size()) {
            if (timeline != null) timeline.stop();
            playBtn.setDisable(false);
            pauseBtn.setDisable(true);
            return;
        }

        SimulationStep step = steps.get(stepIndex);
        applyStep(step);
        stepIndex++;

        // Stop at end
        if (stepIndex >= steps.size()) {
            if (timeline != null) timeline.stop();
            playBtn.setDisable(false);
            pauseBtn.setDisable(true);
        }
    }

    private void applyStep(SimulationStep step) {
        switch (step.getType()) {
            case "START":
                appendLog("▶ Start: " + step.getFromState());
                if (graphPane != null) {
                    graphPane.resetColors(currentDFA);
                    graphPane.highlightState(step.getFromState(), GraphPane.COL_CURRENT);
                }
                break;
            case "MOVE":
                appendLog(step.getFromState() + " --" + step.getSymbol() + "--> " + step.getToState());
                if (graphPane != null) {
                    graphPane.markVisited(step.getFromState());
                    graphPane.highlightState(step.getToState(), GraphPane.COL_CURRENT);
                }
                if (transitionTable != null) {
                    transitionTable.highlight(step.getFromState(), step.getSymbol());
                }
                break;
            case "DEAD":
                appendLog("💀 Dead state reached from " + step.getFromState() + " on '" + step.getSymbol() + "'");
                if (graphPane != null) graphPane.highlightState(step.getFromState(), GraphPane.COL_DEAD);
                showResult(false);
                break;
            case "ACCEPT":
                appendLog("✅ Accepted at: " + step.getFromState());
                if (graphPane != null) graphPane.highlightState(step.getFromState(), GraphPane.COL_FINAL);
                showResult(true);
                break;
            case "REJECT":
                appendLog("❌ Rejected at: " + step.getFromState());
                if (graphPane != null) graphPane.highlightState(step.getFromState(), GraphPane.COL_DEAD);
                showResult(false);
                break;
        }
    }

    private void replayUpTo(int targetIndex) {
        logArea.clear();
        resultLabel.setText("");
        for (int i = 0; i < targetIndex && i < steps.size(); i++) {
            applyStep(steps.get(i));
        }
    }

    private void appendLog(String text) {
        logArea.appendText(text + "\n");
    }

    private void showResult(boolean accepted) {
        if (accepted) {
            resultLabel.setText("✅  ACCEPTED");
            resultLabel.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 14px; -fx-font-weight: bold;");
        } else {
            resultLabel.setText("❌  REJECTED");
            resultLabel.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }

    // ── Button Factory ───────────────────────────────────────────────────────

    private Button ctrlButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: #313244;" +
            "-fx-text-fill: " + color + ";" +
            "-fx-border-color: " + color + ";" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 5 12 5 12;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;"
        );
        return btn;
    }
}
