package view;

import javax.swing.*;
import java.awt.*;
import model.GameState;

public class EndingView extends JFrame {

    // Constructor for simple ending
    public EndingView(String message) {
        this(message, null);
    }

    // Constructor with GameState to show stats
    public EndingView(String message, GameState state) {
        setTitle("Game Over");
        setSize(450, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String displayText = "<b>" + message + "</b><br>";

        if(state != null) {
            displayText += "Days survived: " + state.day + "<br>";
            displayText += "Money: $" + state.money + "<br>";
            displayText += "Reputation: " + state.reputation + "<br>";
            displayText += "Staff: " + state.staffList.size() + "<br>";
        }

        JLabel label = new JLabel("<html><center>" + displayText + "</center></html>", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);

        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));
        add(exit, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
