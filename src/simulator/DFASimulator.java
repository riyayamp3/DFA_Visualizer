package simulator;

import model.DFA;
import model.State;
import model.Transition;

import java.util.ArrayList;
import java.util.List;

/**
 * DFASimulator processes an input string through a DFA step by step.
 *
 * After calling simulate(), you can access:
 *   - getSteps()        → ordered list of SimulationStep objects
 *   - isAccepted()      → whether the string was accepted
 *   - printLog()        → console output for debugging
 */
public class DFASimulator {

    private final DFA dfa;
    private final String inputString;

    private final List<SimulationStep> steps = new ArrayList<>();
    private boolean accepted = false;

    public DFASimulator(DFA dfa, String inputString) {
        this.dfa = dfa;
        this.inputString = inputString;
    }

    // ─── Simulation ──────────────────────────────────────────────────────────

    /**
     * Runs the full simulation.
     * Populates steps list and sets the accepted flag.
     */
    public void simulate() {
        steps.clear();
        accepted = false;

        State currentState = dfa.getStartState();
        if (currentState == null) {
            System.out.println("[ERROR] DFA has no start state.");
            return;
        }

        // Record the initial state (before reading any symbol)
        steps.add(new SimulationStep(
            currentState.getName(),
            '\0',           // no symbol yet
            currentState.getName(),
            "START"
        ));

        for (int i = 0; i < inputString.length(); i++) {
            char symbol = inputString.charAt(i);
            String fromName = currentState.getName();
            String toName = dfa.getNextStateName(fromName, symbol);

            if (toName == null) {
                // Dead / trap state – string rejected immediately
                steps.add(new SimulationStep(fromName, symbol, null, "DEAD"));
                accepted = false;
                return;
            }

            steps.add(new SimulationStep(fromName, symbol, toName, "MOVE"));
            currentState = dfa.getStateByName(toName);
        }

        // Check acceptance
        accepted = currentState.isFinalState();
        steps.add(new SimulationStep(
            currentState.getName(),
            '\0',
            currentState.getName(),
            accepted ? "ACCEPT" : "REJECT"
        ));
    }

    // ─── Console Log ─────────────────────────────────────────────────────────

    public void printLog() {
        System.out.println("=== Simulation Log ===");
        System.out.println("Input : \"" + inputString + "\"");
        System.out.println();

        for (SimulationStep step : steps) {
            switch (step.getType()) {
                case "START":
                    System.out.println("Start state : " + step.getFromState());
                    break;
                case "MOVE":
                    System.out.println("  " + step.getFromState()
                            + " --" + step.getSymbol() + "--> "
                            + step.getToState());
                    break;
                case "DEAD":
                    System.out.println("  " + step.getFromState()
                            + " --" + step.getSymbol() + "--> [DEAD STATE]");
                    System.out.println("\nResult : REJECTED (no transition)");
                    break;
                case "ACCEPT":
                    System.out.println("\nResult : ✅ ACCEPTED  (ended in final state "
                            + step.getFromState() + ")");
                    break;
                case "REJECT":
                    System.out.println("\nResult : ❌ REJECTED  (ended in non-final state "
                            + step.getFromState() + ")");
                    break;
            }
        }
        System.out.println("======================\n");
    }

    // ─── Getters ─────────────────────────────────────────────────────────────

    public List<SimulationStep> getSteps() { return steps; }

    public boolean isAccepted() { return accepted; }

    public String getInputString() { return inputString; }

    // ─── Inner Class : SimulationStep ─────────────────────────────────────────

    /**
     * Captures a single step during DFA execution.
     */
    public static class SimulationStep {
        private final String fromState;
        private final char symbol;
        private final String toState;     // null if dead state
        private final String type;        // START | MOVE | DEAD | ACCEPT | REJECT

        public SimulationStep(String fromState, char symbol, String toState, String type) {
            this.fromState = fromState;
            this.symbol = symbol;
            this.toState = toState;
            this.type = type;
        }

        public String getFromState() { return fromState; }
        public char getSymbol()      { return symbol; }
        public String getToState()   { return toState; }
        public String getType()      { return type; }

        @Override
        public String toString() {
            if (type.equals("MOVE")) {
                return fromState + " --" + symbol + "--> " + toState;
            }
            return "[" + type + "] " + fromState;
        }
    }
}
