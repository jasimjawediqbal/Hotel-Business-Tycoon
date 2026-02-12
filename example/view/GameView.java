package view;

import controller.GameController;
import model.*;
import javax.swing.*;
import java.awt.*;

public class GameView extends JFrame {
    private GameController controller;
    private GameState state;
    private HotelPanel hotelPanel;

    public GameView(GameController controller, GameState state) {
        this.controller = controller;
        this.state = state;

        setTitle("Hotel Management");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        hotelPanel = new HotelPanel(controller, state);
        add(hotelPanel, BorderLayout.CENTER);

        JButton nextDay = new JButton("End Day");
        nextDay.addActionListener(e -> controller.endDay());
        add(nextDay, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void refresh(GameState state) {
        hotelPanel.refresh();
    }
}
