# DFA Visualizer
A visual DFA simulator for Automata Theory. Input a language condition → get a fully animated DFA with state transitions, live transition table, and step-by-step simulation. Built with pure Java and JavaFX, no external dependencies. 

<img width="1375" height="912" alt="image" src="https://github.com/user-attachments/assets/b4e94657-3e26-4b17-bdb8-26b3e4565249" /> 

## 📌 What is this?

The **DFA Visualizer** is a desktop application built with **pure Java and JavaFX** that automatically constructs and animates a Deterministic Finite Automaton (DFA) based on a language condition entered by the user.

Instead of drawing DFAs by hand, you can:
- Type a condition like `ends with abb`
- Enter an input string like `aabbabb`
- Watch the DFA get **built visually** and the string get **processed step by step**

Built as a learning tool for **Theory of Computation / Automata Theory** courses. 
## ✨ Features

| Feature | Description |
|--------|-------------|
| 🧠 **Condition-Based DFA Generation** | Auto-generates DFA from plain English conditions |
| 🎨 **Graph Visualization** | Draws states, transitions, self-loops, and start arrows |
| ▶️ **Step-by-Step Animation** | Watch each state transition highlighted in real time |
| 📊 **Live Transition Table** | Table updates and highlights the active cell during simulation |
| 📜 **Transition History Log** | Full log of every step taken during simulation |
| ⏯️ **Simulation Controls** | Play, Pause, Next, Previous, Reset |
| 🎚️ **Speed Control** | Slider to control animation speed (slow ↔ fast) |
| 🌙 **Dark Theme UI** | Clean Catppuccin Mocha dark theme throughout |

## 🖥️ Screenshots 
<img width="1372" height="911" alt="image" src="https://github.com/user-attachments/assets/db13eef4-d0f6-4973-9b35-340ea3be6d5e" /> 
<img width="1375" height="912" alt="image" src="https://github.com/user-attachments/assets/b4e94657-3e26-4b17-bdb8-26b3e4565249" /> 
<img width="1372" height="915" alt="image" src="https://github.com/user-attachments/assets/42aa014a-a485-4211-ac26-1f738680acc2" />

## 🗂️ Project Structure
```
src/
├── module-info.java
├── Phase1Test.java              ← Test DFA core logic
├── Phase2Test.java              ← Test DFA generator
│
├── model/
│   ├── State.java               ← DFA state (name, start, final flags)
│   ├── Transition.java          ← DFA transition (from, symbol, to)
│   └── DFA.java                 ← Full DFA structure + transition table
│
├── simulator/
│   └── DFASimulator.java        ← Step-by-step simulation engine
│
├── generator/
│   ├── DFAGenerator.java        ← Entry point — routes condition to builder
│   ├── EndsWithGenerator.java   ← KMP-based DFA for "ends with"
│   ├── StartsWithGenerator.java ← DFA for "starts with"
│   ├── ContainsGenerator.java   ← KMP-based DFA for "contains"
│   └── EvenOddGenerator.java    ← 2-state toggle DFA for even/odd count
│
├── utils/
│   └── GraphLayout.java         ← Smart state positioning (row/circular)
│
├── ui/
│   ├── MainApp.java             ← JavaFX entry point
│   ├── InputPanel.java          ← TOP: condition/string/alphabet inputs
│   ├── GraphPane.java           ← CENTER: DFA graph canvas
│   ├── TransitionTable.java     ← RIGHT: live transition table
│   └── ControlPanel.java        ← BOTTOM: controls + history log
│
└── resources/
    └── styles.css               ← Dark theme stylesheet
```

---

## ⚙️ Setup & Run

### Prerequisites
- Java JDK 17 or higher → [Download](https://adoptium.net)
- JavaFX SDK 17 or higher → [Download](https://openjfx.io)
- IntelliJ IDEA → [Download](https://www.jetbrains.com/idea)
### Steps

**1. Clone the repository**
```bash
git clone https://github.com/YOUR_USERNAME/dfa-visualizer.git
cd dfa-visualizer
```

**2. Open in IntelliJ IDEA**
- File → Open → select the project folder

**3. Add JavaFX Library**
- `Ctrl + Alt + Shift + S` → Libraries → `+` → Java
- Navigate to your JavaFX SDK `lib` folder → OK → Apply

**4. Set VM Options**
- Run → Edit Configurations → VM Options:
```
--module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics
```

**5. Set Main Class**
```
ui.MainApp
```

**6. Run!**
```
Shift + F10
```

--- 
## 🧩 How It Works
```
User Input
    │
    ▼
DFAGenerator.generate("ends with abb", "ab")
    │
    ▼
EndsWithGenerator  ──builds──▶  DFA (states + transitions)
    │
    ▼
GraphPane.drawDFA()  ──renders──▶  Visual graph on screen
    │
    ▼
DFASimulator.simulate()  ──produces──▶  List of SimulationSteps
    │
    ▼
ControlPanel  ──animates──▶  State highlights + Table updates
```
## 🎨 Color Scheme

| State | Color |
|-------|-------|
| Inactive | `#45475a` Gray |
| Current | `#f9e2af` Yellow + Glow |
| Visited | `#89b4fa` Blue |
| Final | `#a6e3a1` Green |
| Dead / Rejected | `#f38ba8` Red |

---

## 🛠️ Built With

- **Java 17+** — core DFA logic, simulation engine
- **JavaFX** — UI framework, graph drawing, animation
- **No external libraries** — everything built from scratch

## 📚 Concepts Demonstrated

- Deterministic Finite Automata (DFA) construction
- KMP failure function for pattern-matching DFAs
- JavaFX scene graph, Canvas, Timeline animation
- MVC-style separation (model / generator / simulator / ui)
- Graph layout algorithms (row, two-row, circular)

## 👩‍💻 Author 
Riya Mote 
-Github: riyayamp3 (https://github.com/riyayamp3)
