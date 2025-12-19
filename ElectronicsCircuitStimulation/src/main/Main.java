package main;

import controller.CircuitController;
import model.CircuitModel;
import view.CircuitPanel;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Electronics Circuit Simulation");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1195, 710);
            frame.setLocationRelativeTo(null);

            // 1. Initialize Model
            CircuitModel model = new CircuitModel();
            
            // 2. Initialize View
            CircuitPanel view = new CircuitPanel(model);
            
            // 3. Initialize Controller (Injects logic into Model and View)
            new CircuitController(model, view);

            frame.add(view);
            frame.setVisible(true);
        });
    }
}