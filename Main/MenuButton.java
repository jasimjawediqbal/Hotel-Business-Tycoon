import javax.swing.*;
import java.awt.*;

class MenuButton extends JButton {
    public MenuButton(String text) {
        super(text);
        setFocusPainted(false);
        setFont(new Font("SansSerif", Font.BOLD, 18));
        setBackground(new Color(70, 130, 180));
        setForeground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                setBackground(new Color(100, 150, 200));
            }

            public void mouseExited(java.awt.event.MouseEvent e) {
                setBackground(new Color(70, 130, 180));
            }
        });
    }
}