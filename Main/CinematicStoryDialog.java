import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

class CinematicStoryDialog extends JDialog {
    private final JTextPane narrativeArea = new JTextPane();
    private final JLabel footerLabel = new JLabel("Press Enter to continue", SwingConstants.CENTER);
    private final JPanel choicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
    private final PopupOverlayPanel popupOverlay = new PopupOverlayPanel();
    private final StoryPhotoPanel photoPanel = new StoryPhotoPanel();

    private final String[] scenes;
    private final String[] sceneImageFiles;
    private final String[] choices;
    private final String completionPopupText;
    private int sceneIndex = 0;
    private int selectedChoice = -1;
    private Timer popupTimer;
    private float popupProgress = 0f;
    private int popupFrame = 0;
    private boolean popupActive = false;

    public static void showSequence(Window owner, String title, String... scenes) {
        CinematicStoryDialog dialog = new CinematicStoryDialog(owner, title, scenes, null, null, null);
        dialog.setVisible(true);
    }

    public static void showSequenceWithImages(Window owner, String title, String[] scenes, String[] sceneImageFiles) {
        CinematicStoryDialog dialog = new CinematicStoryDialog(owner, title, scenes, sceneImageFiles, null, null);
        dialog.setVisible(true);
    }

    public static void showSequenceWithPopup(Window owner, String title, String popupText, String... scenes) {
        CinematicStoryDialog dialog = new CinematicStoryDialog(owner, title, scenes, null, null, popupText);
        dialog.setVisible(true);
    }

    public static void showSequenceWithPopupAndImages(Window owner, String title, String popupText, String[] scenes,
            String[] sceneImageFiles) {
        CinematicStoryDialog dialog = new CinematicStoryDialog(owner, title, scenes, sceneImageFiles, null, popupText);
        dialog.setVisible(true);
    }

    public static int showChoice(Window owner, String title, String[] scenes, String... choices) {
        CinematicStoryDialog dialog = new CinematicStoryDialog(owner, title, scenes, null, choices, null);
        dialog.setVisible(true);
        return dialog.selectedChoice;
    }

    public static int showChoiceWithImages(Window owner, String title, String[] scenes, String[] sceneImageFiles,
            String... choices) {
        CinematicStoryDialog dialog = new CinematicStoryDialog(owner, title, scenes, sceneImageFiles, choices, null);
        dialog.setVisible(true);
        return dialog.selectedChoice;
    }

    private CinematicStoryDialog(Window owner, String title, String[] scenes, String[] sceneImageFiles, String[] choices,
            String completionPopupText) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.scenes = scenes == null ? new String[0] : scenes;
        this.sceneImageFiles = sceneImageFiles;
        this.choices = choices;
        this.completionPopupText = completionPopupText;

        setTitle(title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(true);
        if (owner != null) {
            setBounds(owner.getBounds());
        } else {
            setSize(980, 640);
            setLocationRelativeTo(null);
        }

        JPanel root = new JPanel(null);
        root.setBackground(Color.BLACK);
        setContentPane(root);

        narrativeArea.setEditable(false);
        narrativeArea.setOpaque(false);
        narrativeArea.setBorder(null);
        narrativeArea.setFocusable(false);
        narrativeArea.setHighlighter(null);
        narrativeArea.setForeground(new Color(236, 236, 236));
        narrativeArea.setFont(new Font("Serif", Font.PLAIN, 30));

        footerLabel.setForeground(new Color(160, 160, 160));
        footerLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        choicePanel.setOpaque(false);

        photoPanel.setOpaque(false);
        photoPanel.setVisible(false);

        root.add(narrativeArea);
        root.add(footerLabel);
        root.add(choicePanel);
        root.add(photoPanel);
        root.add(popupOverlay);
        popupOverlay.setVisible(false);

        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutComponents();
            }
        });

        setupKeyBindings(root);
        layoutComponents();
        showScene(0);
    }

    private void setupKeyBindings(JComponent root) {
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "advanceScene");
        actionMap.put("advanceScene", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                advanceScene();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "closeDialog");
        actionMap.put("closeDialog", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void layoutComponents() {
        int width = getContentPane().getWidth();
        int height = getContentPane().getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        int sideMargin = Math.max(64, width / 11);
        int noteWidth = 230;
        int noteHeight = 230;
        int noteX = width - noteWidth - 28;
        int noteY = height - noteHeight - 58;
        photoPanel.setBounds(noteX, noteY, noteWidth, noteHeight);

        int rightReserved = photoPanel.isVisible() ? (noteWidth + 58) : sideMargin;
        int textWidth = Math.max(260, width - sideMargin - rightReserved);
        int textHeight = Math.max(200, (int) (height * 0.55));
        int textY = Math.max(42, (height - textHeight) / 2 - 40);
        narrativeArea.setBounds(sideMargin, textY, textWidth, textHeight);

        int footerY = height - 42;
        footerLabel.setBounds(0, footerY, width, 20);

        int choiceY = height - 110;
        choicePanel.setBounds(0, choiceY, width, 40);
        popupOverlay.setBounds(0, 0, width, height);
    }

    private void showScene(int index) {
        if (index < 0 || index >= scenes.length) {
            return;
        }
        sceneIndex = index;
        setCenteredStoryText(scenes[index]);
        updateSceneImage(index);
        layoutComponents();

        boolean lastScene = sceneIndex == scenes.length - 1;
        if (lastScene && hasChoices()) {
            showChoiceButtons();
            footerLabel.setText("Choose a path");
        } else {
            choicePanel.removeAll();
            choicePanel.setVisible(false);
            if (lastScene) {
                footerLabel.setText("Press Enter to close");
                startPopupIfNeeded();
            } else {
                footerLabel.setText("Press Enter to continue");
                stopPopup();
            }
        }

        choicePanel.revalidate();
        choicePanel.repaint();
    }

    private void updateSceneImage(int index) {
        if (sceneImageFiles == null || index < 0 || index >= sceneImageFiles.length) {
            photoPanel.setVisible(false);
            return;
        }
        String imageName = sceneImageFiles[index];
        if (imageName == null || imageName.trim().isEmpty()) {
            photoPanel.setVisible(false);
            return;
        }

        File imageFile = resolveAssetFile(imageName);
        if (!imageFile.exists()) {
            photoPanel.setVisible(false);
            return;
        }
        Image image = new ImageIcon(imageFile.getAbsolutePath()).getImage();
        photoPanel.setSceneImage(image);
        photoPanel.setVisible(true);
        photoPanel.repaint();
    }

    private File resolveAssetFile(String fileName) {
        File local = new File(fileName);
        if (local.exists()) {
            return local;
        }
        return new File("..", fileName);
    }

    private boolean hasChoices() {
        return choices != null && choices.length > 0;
    }

    private void showChoiceButtons() {
        choicePanel.removeAll();
        choicePanel.setVisible(true);
        for (int i = 0; i < choices.length; i++) {
            final int choiceIndex = i;
            JButton button = new JButton(choices[i]);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 13));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(75, 120, 170));
            button.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(new Color(130, 170, 215), 1, true),
                    new EmptyBorder(8, 16, 8, 16)));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(92, 142, 198));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(75, 120, 170));
                }
            });
            button.addActionListener(e -> {
                selectedChoice = choiceIndex;
                dispose();
            });
            choicePanel.add(button);
        }
    }

    private void advanceScene() {
        if (sceneIndex >= scenes.length - 1) {
            if (!hasChoices()) {
                dispose();
            }
            return;
        }
        showScene(sceneIndex + 1);
    }

    private void startPopupIfNeeded() {
        if (completionPopupText == null || completionPopupText.trim().isEmpty() || popupActive) {
            return;
        }
        popupActive = true;
        popupProgress = 0f;
        popupFrame = 0;
        popupOverlay.setVisible(true);
        popupTimer = new Timer(30, e -> {
            popupFrame++;
            if (popupProgress < 1f) {
                popupProgress = Math.min(1f, popupProgress + 0.06f);
            }
            popupOverlay.repaint();
        });
        popupTimer.start();
    }

    private void stopPopup() {
        popupActive = false;
        popupProgress = 0f;
        popupFrame = 0;
        if (popupTimer != null) {
            popupTimer.stop();
            popupTimer = null;
        }
        popupOverlay.setVisible(false);
    }

    @Override
    public void dispose() {
        stopPopup();
        super.dispose();
    }

    private void setCenteredStoryText(String text) {
        StyledDocument doc = narrativeArea.getStyledDocument();
        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, StyleConstants.ALIGN_CENTER);
        StyleConstants.setForeground(attrs, new Color(236, 236, 236));
        try {
            doc.remove(0, doc.getLength());
            doc.insertString(0, text, attrs);
            doc.setParagraphAttributes(0, doc.getLength(), attrs, false);
            narrativeArea.setCaretPosition(0);
        } catch (BadLocationException ex) {
            narrativeArea.setText(text);
        }
    }

    private class PopupOverlayPanel extends JPanel {
        PopupOverlayPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!popupActive || completionPopupText == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            float pulse = 1f + 0.05f * (float) Math.sin(popupFrame * 0.22f);
            float scale = (0.72f + 0.28f * popupProgress) * pulse;
            int boxW = Math.max(280, (int) (380 * scale));
            int boxH = Math.max(72, (int) (96 * scale));
            int x = (w - boxW) / 2;
            int y = (h - boxH) / 2 + 110;

            int shadowAlpha = (int) (120 * popupProgress);
            g2.setColor(new Color(0, 0, 0, shadowAlpha));
            g2.fillRoundRect(x + 5, y + 6, boxW, boxH, 20, 20);

            int fillAlpha = (int) (205 * popupProgress);
            g2.setColor(new Color(18, 18, 18, fillAlpha));
            g2.fillRoundRect(x, y, boxW, boxH, 18, 18);

            int borderAlpha = (int) (230 * popupProgress);
            g2.setColor(new Color(255, 215, 120, borderAlpha));
            g2.setStroke(new BasicStroke(2.2f));
            g2.drawRoundRect(x, y, boxW, boxH, 18, 18);

            int fontSize = Math.max(20, (int) (28 * scale));
            Font font = new Font("Serif", Font.BOLD, fontSize);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            String text = completionPopupText;
            int tx = x + (boxW - fm.stringWidth(text)) / 2;
            int ty = y + (boxH + fm.getAscent()) / 2 - 6;
            g2.setColor(new Color(255, 236, 180, Math.min(255, (int) (255 * popupProgress))));
            g2.drawString(text, tx, ty);
            g2.dispose();
        }
    }

    private static class StoryPhotoPanel extends JPanel {
        private Image sceneImage;

        public void setSceneImage(Image sceneImage) {
            this.sceneImage = sceneImage;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (sceneImage == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            int panelW = getWidth();
            int panelH = getHeight();
            int frameW = (int) (panelW * 0.9);
            int frameH = (int) (panelH * 0.9);
            int frameX = (panelW - frameW) / 2;
            int frameY = (panelH - frameH) / 2;

            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(frameX + 6, frameY + 8, frameW, frameH, 16, 16);
            g2.setColor(new Color(250, 245, 232));
            g2.fillRoundRect(frameX, frameY, frameW, frameH, 14, 14);
            g2.setColor(new Color(194, 167, 120));
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawRoundRect(frameX, frameY, frameW, frameH, 14, 14);

            int pad = 11;
            int photoX = frameX + pad;
            int photoY = frameY + pad;
            int photoW = frameW - pad * 2;
            int photoH = frameH - pad * 2;

            Shape oldClip = g2.getClip();
            g2.setClip(photoX, photoY, photoW, photoH);
            int imageW = Math.max(1, sceneImage.getWidth(null));
            int imageH = Math.max(1, sceneImage.getHeight(null));
            double scale = Math.max((double) photoW / imageW, (double) photoH / imageH);
            int drawW = (int) Math.round(imageW * scale);
            int drawH = (int) Math.round(imageH * scale);
            int drawX = photoX + (photoW - drawW) / 2;
            int drawY = photoY + (photoH - drawH) / 2;
            g2.drawImage(sceneImage, drawX, drawY, drawW, drawH, null);
            g2.setClip(oldClip);
            g2.dispose();
        }
    }

}
