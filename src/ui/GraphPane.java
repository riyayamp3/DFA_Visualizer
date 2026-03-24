package ui;

import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import model.DFA;
import model.State;
import model.Transition;
import utils.GraphLayout;

import java.util.*;

/**
 * GraphPane — Phase 4 upgrade.
 *
 * Draws a fully polished DFA graph:
 *   States     → radial-gradient filled circles, double ring for final
 *   Transitions→ straight or curved arrows with arrowheads + symbol labels
 *   Self-loops → cubic curve arc above the state
 *   Start arrow→ dashed labelled arrow into start state
 *
 * Color scheme:
 *   Inactive  #45475a  Current   #f9e2af  Visited  #89b4fa
 *   Final     #a6e3a1  Dead      #f38ba8  Arrow    #89b4fa
 */
public class GraphPane extends Pane {

    // Constants
    public  static final double RADIUS       = 32;
    private static final double ARROW_LEN    = 13;
    private static final double ARROW_SPREAD = 0.38;
    private static final double CURVE_OFFSET = 55;
    private static final double LOOP_HEIGHT  = 48;
    private static final double LOOP_RX      = 22;

    // Colors
    public static final Color COL_INACTIVE = Color.web("#45475a");
    public static final Color COL_CURRENT  = Color.web("#f9e2af");
    public static final Color COL_VISITED  = Color.web("#89b4fa");
    public static final Color COL_FINAL    = Color.web("#a6e3a1");
    public static final Color COL_DEAD     = Color.web("#f38ba8");
    public static final Color COL_ARROW    = Color.web("#89b4fa");
    public static final Color COL_SYMBOL   = Color.web("#f9e2af");
    public static final Color COL_TEXT     = Color.web("#cdd6f4");
    public static final Color COL_BG       = Color.web("#1e1e2e");
    public static final Color COL_STROKE   = Color.web("#6c7086");

    // State tracking
    private final Map<String, double[]> positions  = new LinkedHashMap<>();
    private final Map<String, Circle>   circles    = new HashMap<>();
    private final Map<String, Circle>   innerRings = new HashMap<>();
    private DFA currentDFA;

    public GraphPane() {
        setStyle("-fx-background-color: #1e1e2e;");
        setMinSize(560, 420);
        showPlaceholder();

        widthProperty().addListener((o, ov, nv) -> { if (currentDFA != null) drawDFA(currentDFA); });
        heightProperty().addListener((o, ov, nv) -> { if (currentDFA != null) drawDFA(currentDFA); });
    }

    // ── Public API ───────────────────────────────────────────────────────────

    public void drawDFA(DFA dfa) {
        this.currentDFA = dfa;
        getChildren().clear();
        positions.clear();
        circles.clear();
        innerRings.clear();

        double w = getWidth()  > 0 ? getWidth()  : 600;
        double h = getHeight() > 0 ? getHeight() : 420;

        positions.putAll(GraphLayout.compute(dfa.getStates(), w, h));

        Map<String, List<Transition>> pairMap = groupByPair(dfa.getTransitions());

        // Draw transitions first (behind states)
        for (Map.Entry<String, List<Transition>> e : pairMap.entrySet()) {
            drawTransitionGroup(e.getValue(), pairMap);
        }

        // Draw states on top
        for (State s : dfa.getStates()) drawState(s);

        drawStartArrow(dfa.getStartState());
    }

    /** Mark a state as visited (blue) without making it the current state. */
    public void markVisited(String name) {
        highlightState(name, COL_VISITED);
    }

    public void highlightState(String name, Color color) {        Circle c = circles.get(name);
        if (c == null) return;
        c.setFill(buildGradient(color));
        DropShadow glow = new DropShadow(20, color);
        glow.setInput(new Glow(0.7));
        c.setEffect(glow);
        Circle ring = innerRings.get(name);
        if (ring != null) ring.setStroke(color.deriveColor(0, 1, 1.2, 1));
    }

    public void resetColors(DFA dfa) {
        for (State s : dfa.getStates()) {
            Circle c = circles.get(s.getName());
            if (c == null) continue;
            Color base = s.isFinalState() ? COL_FINAL : COL_INACTIVE;
            c.setFill(buildGradient(base));
            c.setEffect(defaultShadow());
            Circle ring = innerRings.get(s.getName());
            if (ring != null) ring.setStroke(COL_STROKE);
        }
    }

    // ── State Drawing ────────────────────────────────────────────────────────

    private void drawState(State s) {
        double[] p = positions.get(s.getName());
        double x = p[0], y = p[1];
        Color base = s.isFinalState() ? COL_FINAL : COL_INACTIVE;

        Circle circle = new Circle(x, y, RADIUS);
        circle.setFill(buildGradient(base));
        circle.setStroke(COL_STROKE);
        circle.setStrokeWidth(2);
        circle.setEffect(defaultShadow());
        getChildren().add(circle);
        circles.put(s.getName(), circle);

        // Double ring for final state
        if (s.isFinalState()) {
            Circle inner = new Circle(x, y, RADIUS - 7);
            inner.setFill(Color.TRANSPARENT);
            inner.setStroke(COL_STROKE);
            inner.setStrokeWidth(1.5);
            getChildren().add(inner);
            innerRings.put(s.getName(), inner);
        }

        // Label
        Text lbl = new Text(s.getName());
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setFill(COL_TEXT);
        double lw = lbl.getLayoutBounds().getWidth();
        double lh = lbl.getLayoutBounds().getHeight();
        lbl.setX(x - lw / 2);
        lbl.setY(y + lh / 4);
        getChildren().add(lbl);
    }

    // ── Transition Drawing ───────────────────────────────────────────────────

    private Map<String, List<Transition>> groupByPair(List<Transition> list) {
        Map<String, List<Transition>> map = new LinkedHashMap<>();
        for (Transition t : list) {
            String key = t.getFromState().getName() + "::" + t.getToState().getName();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
        }
        return map;
    }

    private void drawTransitionGroup(List<Transition> group,
                                      Map<String, List<Transition>> allPairs) {
        String from = group.get(0).getFromState().getName();
        String to   = group.get(0).getToState().getName();

        // Build symbol label e.g. "a,b"
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < group.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(group.get(i).getSymbol());
        }
        String sym = sb.toString();

        if (from.equals(to)) { drawSelfLoop(from, sym); return; }

        double[] fp = positions.get(from);
        double[] tp = positions.get(to);
        if (fp == null || tp == null) return;

        boolean bidir = allPairs.containsKey(to + "::" + from);
        if (bidir) drawCurvedArrow(fp, tp, sym);
        else       drawStraightArrow(fp, tp, sym);
    }

    private void drawStraightArrow(double[] fp, double[] tp, String sym) {
        double angle = Math.atan2(tp[1] - fp[1], tp[0] - fp[0]);
        double sx = fp[0] + RADIUS * Math.cos(angle);
        double sy = fp[1] + RADIUS * Math.sin(angle);
        double ex = tp[0] - RADIUS * Math.cos(angle);
        double ey = tp[1] - RADIUS * Math.sin(angle);

        Line line = new Line(sx, sy, ex, ey);
        line.setStroke(COL_ARROW);
        line.setStrokeWidth(2);
        getChildren().add(line);

        drawArrowHead(ex, ey, angle);
        drawSymbolLabel((sx + ex) / 2, (sy + ey) / 2, sym, angle, -14);
    }

    private void drawCurvedArrow(double[] fp, double[] tp, String sym) {
        double angle = Math.atan2(tp[1] - fp[1], tp[0] - fp[0]);
        double sx = fp[0] + RADIUS * Math.cos(angle);
        double sy = fp[1] + RADIUS * Math.sin(angle);
        double ex = tp[0] - RADIUS * Math.cos(angle);
        double ey = tp[1] - RADIUS * Math.sin(angle);

        // Curve upward
        double perpX = -Math.sin(angle) * CURVE_OFFSET;
        double perpY =  Math.cos(angle) * CURVE_OFFSET;
        double cpx = (sx + ex) / 2 + perpX;
        double cpy = (sy + ey) / 2 + perpY;

        QuadCurve curve = new QuadCurve(sx, sy, cpx, cpy, ex, ey);
        curve.setFill(Color.TRANSPARENT);
        curve.setStroke(COL_ARROW);
        curve.setStrokeWidth(2);
        getChildren().add(curve);

        double endAngle = Math.atan2(ey - cpy, ex - cpx);
        drawArrowHead(ex, ey, endAngle);
        drawSymbolLabel(cpx, cpy, sym, angle, -10);
    }

    private void drawSelfLoop(String name, String sym) {
        double[] p = positions.get(name);
        if (p == null) return;
        double x = p[0], y = p[1];

        double topY  = y - RADIUS - LOOP_HEIGHT;
        double startX = x - LOOP_RX;
        double startY = y - RADIUS + 4;
        double endX   = x + LOOP_RX;
        double endY   = y - RADIUS + 4;

        CubicCurve loop = new CubicCurve(
            startX, startY,
            x - LOOP_RX * 2, topY,
            x + LOOP_RX * 2, topY,
            endX, endY
        );
        loop.setFill(Color.TRANSPARENT);
        loop.setStroke(COL_ARROW);
        loop.setStrokeWidth(2);
        getChildren().add(loop);

        drawArrowHead(endX, endY, Math.PI / 2 + 0.35);

        Text label = symbolText(sym);
        label.setX(x - label.getLayoutBounds().getWidth() / 2);
        label.setY(topY - 5);
        getChildren().add(label);
    }

    private void drawArrowHead(double x, double y, double angle) {
        double ax1 = x - ARROW_LEN * Math.cos(angle - ARROW_SPREAD);
        double ay1 = y - ARROW_LEN * Math.sin(angle - ARROW_SPREAD);
        double ax2 = x - ARROW_LEN * Math.cos(angle + ARROW_SPREAD);
        double ay2 = y - ARROW_LEN * Math.sin(angle + ARROW_SPREAD);
        Polygon head = new Polygon(x, y, ax1, ay1, ax2, ay2);
        head.setFill(COL_ARROW);
        head.setStroke(Color.TRANSPARENT);
        getChildren().add(head);
    }

    private void drawSymbolLabel(double mx, double my,
                                  String sym, double angle, double normalOffset) {
        Text t = symbolText(sym);
        double perpX = -Math.sin(angle) * normalOffset;
        double perpY =  Math.cos(angle) * normalOffset;
        double lw    = t.getLayoutBounds().getWidth();
        t.setX(mx - lw / 2 + perpX);
        t.setY(my + perpY);
        getChildren().add(t);
    }

    private void drawStartArrow(State start) {
        if (start == null) return;
        double[] p = positions.get(start.getName());
        if (p == null) return;
        double x = p[0], y = p[1];

        Line line = new Line(x - RADIUS - 52, y, x - RADIUS - 1, y);
        line.setStroke(COL_FINAL);
        line.setStrokeWidth(2);
        line.getStrokeDashArray().addAll(6.0, 3.0);
        getChildren().add(line);

        drawArrowHead(x - RADIUS, y, 0);

        Text t = new Text("start");
        t.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 11));
        t.setFill(Color.web("#a6adc8"));
        t.setX(x - RADIUS - 50);
        t.setY(y - 8);
        getChildren().add(t);
    }

    // ── Visual Helpers ───────────────────────────────────────────────────────

    private Text symbolText(String s) {
        Text t = new Text(s);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        t.setFill(COL_SYMBOL);
        t.setEffect(new DropShadow(4, COL_BG));
        return t;
    }

    private RadialGradient buildGradient(Color base) {
        return new RadialGradient(
            0, 0, 0.35, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, base.deriveColor(0, 1, 1.3, 1)),
            new Stop(1, base.deriveColor(0, 1, 0.65, 1))
        );
    }

    private DropShadow defaultShadow() {
        return new DropShadow(8, Color.web("#11111b"));
    }

    private void showPlaceholder() {
        Text t = new Text("Generate a DFA to see the graph here.");
        t.setFont(Font.font("Segoe UI", FontPosture.ITALIC, 14));
        t.setFill(Color.web("#585b70"));
        widthProperty().addListener((o, ov, nv) ->
            t.setX(nv.doubleValue() / 2 - t.getLayoutBounds().getWidth() / 2));
        heightProperty().addListener((o, ov, nv) ->
            t.setY(nv.doubleValue() / 2));
        getChildren().add(t);
    }
}
