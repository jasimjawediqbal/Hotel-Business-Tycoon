package view;

import controller.GameController;
import model.*;
import javax.swing.*;
import java.awt.*;

public class HotelPanel extends JPanel {
    private GameController controller;
    private GameState state;

    public HotelPanel(GameController controller, GameState state) {
        this.controller = controller;
        this.state = state;
        refresh();
    }

    public void refresh() {
        removeAll();
        setLayout(new GridLayout(2,3));
        for(Room room : state.hotel.getRooms()) {
            JButton btn = new JButton("<html>Room " + room.id + "<br>" + room.status + "</html>");
            btn.addActionListener(e -> controller.roomAction(room));
            add(btn);
        }
        revalidate();
        repaint();
    }
}
