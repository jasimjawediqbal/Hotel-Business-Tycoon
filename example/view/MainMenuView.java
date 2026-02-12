package view;

import controller.GameController;
import javax.swing.*;
import java.awt.*;

public class MainMenuView extends JFrame {
    public MainMenuView(GameController controller) {
        setTitle("Echoes of the Closed Motel");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3,1));

        JButton start = new JButton("Start New Game");
        start.addActionListener(e -> {
            controller.startNewGame();
            dispose();
        });

        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));

        add(new JLabel("Echoes of the Closed Motel", SwingConstants.CENTER));
        add(start);
        add(exit);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
