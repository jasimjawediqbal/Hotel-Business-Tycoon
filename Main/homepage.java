import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Random;

// ===================== MODEL =====================
class SettingsModel {
    private boolean soundOn = true;
    private int volume = 70; // 0-100%

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }
}

// ===================== VIEW =====================
class BackgroundPanel extends JPanel implements ActionListener {
    private static final int WINDOW_ROWS = 5;
    private static final int WINDOW_COLS = 5;
    private static final int STAR_COUNT = 80;
    private static final int CLOUD_COUNT = 5;
    private static final int CONFETTI_COUNT = 24;

    private final Timer timer;
    private final Random rand = new Random();
    private final boolean[] windowLights = new boolean[WINDOW_ROWS * WINDOW_COLS];

    private final float[] starX = new float[STAR_COUNT];
    private final float[] starY = new float[STAR_COUNT];
    private final int[] starSize = new int[STAR_COUNT];
    private final float[] starPhase = new float[STAR_COUNT];

    private final float[] cloudX = new float[CLOUD_COUNT];
    private final float[] cloudY = new float[CLOUD_COUNT];
    private final float[] cloudSpeed = new float[CLOUD_COUNT];
    private final float[] cloudScale = new float[CLOUD_COUNT];

    private final float[] confettiX = new float[CONFETTI_COUNT];
    private final float[] confettiY = new float[CONFETTI_COUNT];
    private final float[] confettiSpeed = new float[CONFETTI_COUNT];
    private final int[] confettiSize = new int[CONFETTI_COUNT];
    private final Color[] confettiColor = new Color[CONFETTI_COUNT];

    private float cycle = 0f;
    private final float cycleSpeed = 0.0007f; // ~24s per cycle at 60fps
    private float carX1 = 0f;
    private float carX2 = 200f;
    private float roadOffset = 0f;
    private float titlePulse = 0f;
    private int frameCount = 0;

    public BackgroundPanel() {
        setPreferredSize(new Dimension(900, 600));
        setLayout(null);
        timer = new Timer(16, this); // ~60fps
        timer.start();

        for (int i = 0; i < windowLights.length; i++)
            windowLights[i] = rand.nextBoolean();

        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = rand.nextFloat();
            starY[i] = rand.nextFloat() * 0.55f;
            starSize[i] = 1 + rand.nextInt(3);
            starPhase[i] = rand.nextFloat() * (float) (Math.PI * 2);
        }

        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudX[i] = rand.nextInt(900) - 200;
            cloudY[i] = 40 + rand.nextInt(140);
            cloudSpeed[i] = 0.3f + rand.nextFloat() * 0.7f;
            cloudScale[i] = 0.6f + rand.nextFloat() * 0.8f;
        }

        for (int i = 0; i < CONFETTI_COUNT; i++) {
            confettiX[i] = rand.nextFloat();
            confettiY[i] = rand.nextFloat();
            confettiSpeed[i] = 0.3f + rand.nextFloat() * 0.8f;
            confettiSize[i] = 3 + rand.nextInt(4);
            confettiColor[i] = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));
        }
    }

    public void stopAnimation() {
        timer.stop();
    }

    public void startAnimation() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int width = getWidth();
        int height = getHeight();

        float day = (float) (0.5 - 0.5 * Math.cos(cycle * Math.PI * 2));
        float night = 1f - day;

        // Sky gradient
        Color dayTop = new Color(135, 206, 235);
        Color dayBottom = new Color(180, 225, 255);
        Color nightTop = new Color(10, 14, 40);
        Color nightBottom = new Color(25, 30, 70);
        Color skyTop = blendColors(nightTop, dayTop, day);
        Color skyBottom = blendColors(nightBottom, dayBottom, day);
        g2.setPaint(new GradientPaint(0, 0, skyTop, 0, height, skyBottom));
        g2.fillRect(0, 0, width, height);

        // Stars with glow
        if (night > 0.05f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f * night));
            for (int i = 0; i < STAR_COUNT; i++) {
                float twinkle = 0.4f + 0.6f * (float) Math.sin(starPhase[i] + frameCount * 0.05f);
                int alpha = 80 + (int) (160 * twinkle);
                g2.setColor(new Color(255, 255, 255, alpha));
                int x = (int) (starX[i] * width);
                int y = (int) (starY[i] * height);
                g2.fillOval(x, y, starSize[i], starSize[i]);
            }
            g2.setComposite(AlphaComposite.SrcOver);
        }

        // Sun/Moon
        float orbitRadius = width * 0.45f;
        float orbitCenterX = width * 0.5f;
        float orbitCenterY = height * 0.95f;
        float angle = (float) (cycle * Math.PI * 2 - Math.PI / 2);
        float orbX = orbitCenterX + (float) Math.cos(angle) * orbitRadius;
        float orbY = orbitCenterY + (float) Math.sin(angle) * orbitRadius * 0.6f;

        // Sun
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, day));
        int sunSize = (int) (60 + 10 * day);
        g2.setPaint(new RadialGradientPaint(
                orbX, orbY, sunSize * 0.5f,
                new float[] { 0f, 1f },
                new Color[] { new Color(255, 230, 120), new Color(255, 180, 60, 0) }));
        g2.fillOval((int) (orbX - sunSize / 2), (int) (orbY - sunSize / 2), sunSize, sunSize);

        // Moon
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, night));
        int moonSize = 45;
        g2.setColor(new Color(220, 230, 255));
        g2.fillOval((int) (orbX - moonSize / 2), (int) (orbY - moonSize / 2), moonSize, moonSize);
        g2.setColor(new Color(180, 190, 220, 140));
        g2.fillOval((int) (orbX - moonSize / 4), (int) (orbY - moonSize / 3), moonSize / 3, moonSize / 3);
        g2.setComposite(AlphaComposite.SrcOver);

        // Clouds with subtle depth
        float cloudAlpha = 0.3f + 0.5f * day;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cloudAlpha));
        for (int i = 0; i < CLOUD_COUNT; i++)
            drawCloud(g2, cloudX[i], cloudY[i], cloudScale[i]);
        g2.setComposite(AlphaComposite.SrcOver);

        // Hotel with more detailed facade
        int hotelWidth = width / 3;
        int hotelHeight = height / 2;
        int hotelX = width / 2 - hotelWidth / 2;
        int hotelY = height / 2 - hotelHeight / 2;
        drawHotelDetailed(g2, hotelX, hotelY, hotelWidth, hotelHeight, night);

        // Road + sidewalk
        int roadY = hotelY + hotelHeight + 28;
        int roadH = height - roadY;
        g2.setColor(new Color(50, 52, 58));
        g2.fillRect(0, roadY, width, roadH);

        g2.setColor(new Color(220, 220, 220, 200));
        int stripeY = roadY + roadH / 2 - 3;
        for (int x = -(int) roadOffset; x < width; x += 70)
            g2.fillRect(x, stripeY, 35, 6);

        int walkY = roadY - 18;
        g2.setColor(new Color(170, 170, 175));
        g2.fillRect(0, walkY, width, 18);
        g2.setColor(new Color(150, 150, 155));
        for (int x = 0; x < width; x += 40)
            g2.drawLine(x, walkY, x + 20, walkY + 18);

        // Cars with shadows
        drawCar(g2, carX1, roadY + roadH / 2 - 22, new Color(220, 70, 70));
        drawCar(g2, carX2, roadY + roadH / 2 + 8, new Color(70, 140, 220));

        // Confetti subtle
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            int x = (int) (confettiX[i] * width);
            int y = (int) (confettiY[i] * height);
            g2.setColor(confettiColor[i]);
            g2.fillOval(x, y, confettiSize[i], confettiSize[i]);
        }

        // Title glow
        String title = "HOTEL TYCOON";
        g2.setFont(new Font("Serif", Font.BOLD, 48));
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        int titleX = width / 2 - titleWidth / 2;
        int titleY = 80;

        float glow = 0.5f + 0.5f * (float) Math.sin(titlePulse);
        g2.setColor(new Color(0, 0, 0, 140));
        g2.drawString(title, titleX + 2, titleY + 2);
        g2.setColor(new Color(255, 215, 120, 160 + (int) (60 * glow)));
        g2.drawString(title, titleX, titleY);

        g2.dispose();
    }

    private void drawCloud(Graphics2D g2, float x, float y, float scale) {
        int w = (int) (140 * scale);
        int h = (int) (60 * scale);
        g2.setPaint(new GradientPaint((int) x, (int) y, new Color(255, 255, 255, 180), (int) x + w, (int) y + h,
                new Color(255, 255, 255, 50)));
        g2.fillOval((int) x, (int) y, w, h);
        g2.fillOval((int) (x + w * 0.2f), (int) (y - h * 0.3f), (int) (w * 0.6f), (int) (h * 0.7f));
        g2.fillOval((int) (x + w * 0.5f), (int) (y - h * 0.2f), (int) (w * 0.5f), (int) (h * 0.6f));
    }

    private void drawCar(Graphics2D g2, float x, int y, Color body) {
        int w = 50;
        int h = 18;
        // shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect((int) x + 3, y + h - 2 + 3, w, h / 3, 8, 8);
        // body
        g2.setColor(body);
        g2.fillRoundRect((int) x, y, w, h, 8, 8);
        g2.fillRoundRect((int) x + 8, y - 10, 32, 14, 8, 8);
        g2.setColor(new Color(180, 220, 255, 180));
        g2.fillRect((int) x + 16, y - 6, 14, 6);
        g2.setColor(Color.BLACK);
        g2.fillOval((int) x + 6, y + h - 2, 10, 10);
        g2.fillOval((int) x + w - 16, y + h - 2, 10, 10);
    }

    private void drawHotelDetailed(Graphics2D g2, int x, int y, int w, int h, float night) {
        int depth = Math.max(18, w / 8);

        // Building shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRoundRect(x + depth + 8, y + 8, w, h, 10, 10);

        // Side wall for depth
        Polygon side = new Polygon();
        side.addPoint(x + w, y);
        side.addPoint(x + w + depth, y + depth / 2);
        side.addPoint(x + w + depth, y + h + depth / 2);
        side.addPoint(x + w, y + h);
        g2.setPaint(
                new GradientPaint(x + w, y, new Color(175, 150, 120), x + w + depth, y + h, new Color(140, 120, 95)));
        g2.fillPolygon(side);

        // Main facade
        g2.setPaint(new GradientPaint(x, y, new Color(230, 205, 170), x, y + h, new Color(185, 160, 125)));
        g2.fillRoundRect(x, y, w, h, 10, 10);

        // Roof and parapet
        g2.setColor(new Color(155, 135, 110));
        g2.fillRect(x - 2, y - 16, w + 4, 16);
        g2.setColor(new Color(130, 110, 90));
        g2.fillRect(x - 4, y - 22, w + 8, 8);

        // Vertical pilasters
        g2.setColor(new Color(200, 175, 145));
        for (int i = 1; i <= 3; i++) {
            int px = x + i * w / 4 - 6;
            g2.fillRoundRect(px, y + 8, 10, h - 16, 6, 6);
        }

        // Hotel sign with glow
        int signW = (int) (w * 0.62f);
        int signH = 28;
        int signX = x + w / 2 - signW / 2;
        int signY = y - 44;
        g2.setColor(new Color(70, 55, 40));
        g2.fillRoundRect(signX, signY, signW, signH, 10, 10);
        g2.setColor(new Color(255, 230, 160, (int) (180 + 60 * night)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(signX + 2, signY + 2, signW - 4, signH - 4, 8, 8);
        g2.setFont(new Font("Serif", Font.BOLD, 18));
        String boardName = "Hotel Taj";
        int boardX = signX + (signW - g2.getFontMetrics().stringWidth(boardName)) / 2;
        g2.drawString(boardName, boardX, signY + 20);

        // Windows with frames and reflections
        int rows = WINDOW_ROWS;
        int cols = WINDOW_COLS;
        int gap = 8;
        int winW = (w - (cols + 1) * gap) / cols;
        int winH = (h - (rows + 1) * gap) / rows;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int idx = r * cols + c;
                int wx = x + gap + c * (winW + gap);
                int wy = y + gap + r * (winH + gap);
                boolean lit = windowLights[idx] && night > 0.15f;
                Color base = lit ? new Color(255, 220, 140) : new Color(60, 65, 80);
                g2.setColor(new Color(90, 80, 70));
                g2.fillRoundRect(wx - 2, wy - 2, winW + 4, winH + 4, 6, 6);
                g2.setPaint(new GradientPaint(wx, wy, base.brighter(), wx + winW, wy + winH, base.darker()));
                g2.fillRoundRect(wx, wy, winW, winH, 4, 4);
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawLine(wx + 3, wy + 3, wx + winW - 4, wy + winH - 6);
                g2.setColor(new Color(0, 0, 0, 60));
                g2.drawLine(wx + winW / 2, wy + 2, wx + winW / 2, wy + winH - 2);
                g2.drawLine(wx + 2, wy + winH / 2, wx + winW - 2, wy + winH / 2);
            }
        }

        // Entrance canopy and doors
        int doorW = w / 5;
        int doorH = h / 4;
        int doorX = x + w / 2 - doorW / 2;
        int doorY = y + h - doorH;

        g2.setColor(new Color(120, 95, 70));
        g2.fillRoundRect(doorX - 20, doorY - 12, doorW + 40, 14, 10, 10);
        g2.setColor(new Color(90, 70, 50));
        g2.fillRoundRect(doorX, doorY, doorW, doorH, 8, 8);
        g2.setColor(new Color(180, 220, 255, 120));
        g2.fillRect(doorX + 6, doorY + 6, doorW - 12, doorH / 2);
        g2.setColor(new Color(120, 100, 80));
        g2.fillRect(doorX - 30, doorY + doorH, doorW + 60, 28);

        // Steps
        g2.setColor(new Color(140, 140, 145));
        g2.fillRect(doorX - 40, doorY + doorH + 10, doorW + 80, 10);
        g2.setColor(new Color(120, 120, 125));
        g2.fillRect(doorX - 30, doorY + doorH + 2, doorW + 60, 8);

        // Landscaping (aligned with steps)
        int stepTopY = doorY + doorH + 2;
        int bushY = stepTopY + 2;
        int bushW = 32;
        int bushH = 20;
        g2.setColor(new Color(70, 110, 70));
        g2.fillRoundRect(x - 10, bushY, bushW, bushH, 12, 12);
        g2.fillRoundRect(x + w - (bushW - 10), bushY, bushW, bushH, 12, 12);
        g2.setColor(new Color(90, 140, 90));
        g2.fillOval(x - 6, bushY - 8, bushW - 8, bushH - 2);
        g2.fillOval(x + w - (bushW - 6), bushY - 8, bushW - 8, bushH - 2);

        // Lamps (sit on sidewalk line)
        int lampY = stepTopY + 18;
        drawLamp(g2, x - 26, lampY, night);
        drawLamp(g2, x + w + 8, lampY, night);
    }

    private void drawLamp(Graphics2D g2, int x, int y, float night) {
        g2.setColor(new Color(60, 60, 65));
        g2.fillRect(x + 8, y - 40, 4, 40);
        g2.fillRoundRect(x, y - 52, 20, 12, 6, 6);
        g2.setColor(new Color(255, 230, 160, (int) (120 + 100 * night)));
        g2.fillOval(x + 3, y - 50, 14, 10);
        g2.setColor(new Color(255, 230, 160, (int) (40 + 80 * night)));
        g2.fillOval(x - 8, y - 58, 36, 26);
    }

    private Color blendColors(Color c1, Color c2, float ratio) {
        float r = c1.getRed() * (1 - ratio) + c2.getRed() * ratio;
        float g = c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio;
        float b = c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio;
        return new Color((int) r, (int) g, (int) b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cycle += cycleSpeed;
        if (cycle > 1f)
            cycle -= 1f;

        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);

        carX1 += 2.4f;
        carX2 += 3.1f;
        if (carX1 > width + 80)
            carX1 = -120;
        if (carX2 > width + 120)
            carX2 = -180;

        roadOffset += 3f;
        if (roadOffset > 70f)
            roadOffset -= 70f;

        for (int i = 0; i < CLOUD_COUNT; i++) {
            cloudX[i] += cloudSpeed[i];
            if (cloudX[i] > width + 200) {
                cloudX[i] = -200 * cloudScale[i];
                cloudY[i] = 40 + rand.nextInt(140);
            }
        }

        for (int i = 0; i < CONFETTI_COUNT; i++) {
            confettiY[i] += confettiSpeed[i] / height;
            if (confettiY[i] > 1f) {
                confettiY[i] = -0.05f;
                confettiX[i] = rand.nextFloat();
            }
        }

        if (frameCount % 20 == 0) {
            int idx = rand.nextInt(windowLights.length);
            windowLights[idx] = rand.nextBoolean();
        }

        titlePulse += 0.06f;
        frameCount++;
        repaint();
    }
}

// ===================== (MenuButton & SettingsDialog remain unchanged)
// =====================

class OpeningSceneDialog extends JDialog {
    private final Runnable onStoryComplete;
    private final JTextPane narrativeArea = new JTextPane();
    private final JLabel continueLabel = new JLabel("Press Enter to continue", SwingConstants.CENTER);
    private final PhotoNotePanel photoNotePanel = new PhotoNotePanel();
    private final RainAudioPlayer rainAudioPlayer = new RainAudioPlayer();
    private final String[] scenes = {
            "\"Some places don't die.\nThey wait.\"",
            "\nA small, dimly lit rented room in a distant city.\nArman sits on the edge of the bed, still in work clothes: tired, worn, but alert.",
            "On a small table:\n- An unpaid electricity bill\n- A cheap phone\n- A folded photograph turned face-down",
            "The phone vibrates. Not a ringtone. Just vibration.\n\nUnknown Number.",
            "A shaky, old voice comes through:\n\n\"Arman... You need to come home.\"\n\nA pause.\n\n\"Your father... he's gone.\"",
            "Grandfather continues:\n\n\"There's something else.\nSomething you were meant to inherit.\"\n\nArman stares at the face-down photograph.",
            "The photograph is turned over.\n\nIt's the hotel - old, but alive.\n\n\"The hotel... we never sold it.\"\n\"It's still ours.\"",
            "ECHOES OF A FORGOTTEN LEGACY\n\nChapter 1: Homecoming to Ruins"
    };
    private final String[] sceneImageFiles = {
            null,
            "scene1.jpg",
            "scene1.jpg",
            "caller.png",
            "scene3.jpg",
            "scene3.jpg",
            "scene4.jpg",
            "scene4.jpg"
    };
    private final double[] sceneTiltDegrees = { -7.0, -9.5, -6.0, -12.0, -8.0, -5.5, -10.0, -7.5 };

    private int sceneIndex = 0;
    private boolean completionDispatched = false;

    public OpeningSceneDialog(JFrame parent, Runnable onStoryComplete) {
        super(parent, true);
        this.onStoryComplete = onStoryComplete;

        setTitle("Opening Story");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(parent.getBounds());
        setResizable(true);

        JPanel root = new JPanel(null);
        root.setBackground(Color.BLACK);
        setContentPane(root);

        narrativeArea.setEditable(false);
        narrativeArea.setOpaque(false);
        narrativeArea.setForeground(new Color(236, 236, 236));
        narrativeArea.setFont(new Font("Serif", Font.PLAIN, 28));
        narrativeArea.setFocusable(false);
        narrativeArea.setBorder(null);
        narrativeArea.setHighlighter(null);

        continueLabel.setForeground(new Color(160, 160, 160));
        continueLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        photoNotePanel.setOpaque(false);

        root.add(narrativeArea);
        root.add(continueLabel);
        root.add(photoNotePanel);

        root.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutStoryComponents();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                layoutStoryComponents();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                dispatchCompletionIfNeeded();
            }
        });

        setupKeyBindings(root);
        layoutStoryComponents();
        playRainForStory();
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
    }

    private void showScene(int index) {
        sceneIndex = index;
        setCenteredStoryText(scenes[index]);
        updateSceneImage(index);
        layoutStoryComponents();

        if (sceneIndex == scenes.length - 1) {
            continueLabel.setText("Press Enter to return to menu");
        } else {
            continueLabel.setText("Press Enter to continue");
        }
    }

    private void advanceScene() {
        if (sceneIndex < scenes.length - 1) {
            showScene(sceneIndex + 1);
            return;
        }

        dispose();
    }

    private void layoutStoryComponents() {
        int width = getContentPane().getWidth();
        int height = getContentPane().getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        int sideMargin = 80;

        int noteWidth = 245;
        int noteHeight = 245;
        int noteX = width - noteWidth - 34;
        int noteY = height - noteHeight - 56;
        photoNotePanel.setBounds(noteX, noteY, noteWidth, noteHeight);

        int rightReserved = photoNotePanel.isVisible() ? (noteWidth + 64) : sideMargin;
        int textWidth = Math.max(240, width - sideMargin - rightReserved);
        int textHeight = Math.max(170, (int) (height * 0.56));
        int textY = Math.max(38, (height - textHeight) / 2 - 16);
        narrativeArea.setBounds(sideMargin, textY, textWidth, textHeight);

        continueLabel.setBounds(0, height - 36, width, 20);
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

    private void updateSceneImage(int index) {
        if (index < 0 || index >= sceneImageFiles.length) {
            photoNotePanel.setVisible(false);
            return;
        }

        String imageName = sceneImageFiles[index];
        if (imageName == null) {
            photoNotePanel.setVisible(false);
            return;
        }

        File imageFile = resolveAssetFile(imageName);
        if (!imageFile.exists()) {
            photoNotePanel.setVisible(false);
            return;
        }

        Image image = new ImageIcon(imageFile.getAbsolutePath()).getImage();
        photoNotePanel.setSceneImage(image, sceneTiltDegrees[index]);
        photoNotePanel.setVisible(true);
        photoNotePanel.repaint();
    }

    private void playRainForStory() {
        File rainFile = resolveAssetFile("rain.mp3");
        rainAudioPlayer.start(rainFile);
    }

    private File resolveAssetFile(String fileName) {
        File local = new File(fileName);
        if (local.exists()) {
            return local;
        }
        return new File("..", fileName);
    }

    @Override
    public void dispose() {
        rainAudioPlayer.stop();
        super.dispose();
    }

    private void dispatchCompletionIfNeeded() {
        if (completionDispatched) {
            return;
        }
        completionDispatched = true;
        onStoryComplete.run();
    }

    private static class PhotoNotePanel extends JPanel {
        private Image sceneImage;
        private double tiltDegrees = -7.0;

        public void setSceneImage(Image sceneImage, double tiltDegrees) {
            this.sceneImage = sceneImage;
            this.tiltDegrees = tiltDegrees;
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
            g2.translate(panelW / 2.0, panelH / 2.0);
            g2.rotate(Math.toRadians(tiltDegrees));

            int frameW = (int) (panelW * 0.86);
            int frameH = (int) (panelH * 0.90);
            int frameX = -frameW / 2;
            int frameY = -frameH / 2;

            g2.setColor(new Color(0, 0, 0, 95));
            g2.fillRoundRect(frameX + 8, frameY + 10, frameW, frameH, 18, 18);

            g2.setColor(new Color(250, 245, 232));
            g2.fillRoundRect(frameX, frameY, frameW, frameH, 16, 16);

            g2.setColor(new Color(194, 167, 120));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(frameX, frameY, frameW, frameH, 16, 16);

            int innerPad = 13;
            int bottomPad = 36;
            int photoX = frameX + innerPad;
            int photoY = frameY + innerPad;
            int photoW = frameW - (innerPad * 2);
            int photoH = frameH - innerPad - bottomPad;

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

            g2.setColor(new Color(110, 95, 78, 100));
            g2.drawRect(photoX, photoY, photoW, photoH);
            g2.dispose();
        }
    }

    private static class RainAudioPlayer {
        private Clip clip;
        private Process windowsPlayerProcess;

        public void start(File audioFile) {
            stop();
            if (audioFile == null || !audioFile.exists()) {
                return;
            }

            if (startWithJavaClip(audioFile)) {
                return;
            }

            if (isWindows() && startWithWindowsMediaPlayer(audioFile)) {
                return;
            }

            System.err.println("Rain audio could not be played: File of unsupported format");
        }

        public void stop() {
            if (clip != null) {
                clip.stop();
                clip.close();
                clip = null;
            }
            if (windowsPlayerProcess != null) {
                windowsPlayerProcess.destroy();
                windowsPlayerProcess = null;
            }
        }

        private boolean startWithJavaClip(File audioFile) {
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile)) {
                clip = AudioSystem.getClip();
                clip.open(stream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
                return true;
            } catch (Exception ex) {
                return false;
            }
        }

        private boolean startWithWindowsMediaPlayer(File audioFile) {
            try {
                String path = audioFile.getAbsolutePath().replace("\\", "\\\\").replace("'", "''");
                String script = "$ErrorActionPreference='Stop';"
                        + "Add-Type -AssemblyName presentationCore;"
                        + "$player=New-Object System.Windows.Media.MediaPlayer;"
                        + "$player.Open([Uri]'" + path + "');"
                        + "$player.Volume=0.70;"
                        + "$player.MediaEnded += { $player.Position=[TimeSpan]::Zero; $player.Play() };"
                        + "$player.Play();"
                        + "while($true){ Start-Sleep -Milliseconds 300 }";

                ProcessBuilder builder = new ProcessBuilder(
                        "powershell",
                        "-NoProfile",
                        "-WindowStyle",
                        "Hidden",
                        "-ExecutionPolicy",
                        "Bypass",
                        "-Command",
                        script);
                builder.redirectErrorStream(true);
                windowsPlayerProcess = builder.start();
                try {
                    Thread.sleep(120);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (!windowsPlayerProcess.isAlive()) {
                    windowsPlayerProcess = null;
                    return false;
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }

        private boolean isWindows() {
            return System.getProperty("os.name", "").toLowerCase().contains("win");
        }
    }
}

// ===================== CONTROLLER =====================
public class homepage extends JFrame {
    private final BackgroundPanel backgroundPanel;

    public homepage() {
        setTitle("Hotel Tycoon");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 40, 10, 0);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        MenuButton startBtn = new MenuButton("START GAME");
        MenuButton loadBtn = new MenuButton("LOAD GAME");
        MenuButton exitBtn = new MenuButton("EXIT");
        startBtn.setToolTipText(null);
        loadBtn.setToolTipText(null);
        exitBtn.setToolTipText(null);

        gbc.gridy = 0;
        backgroundPanel.add(startBtn, gbc);
        gbc.gridy++;
        backgroundPanel.add(loadBtn, gbc);
        gbc.gridy++;
        backgroundPanel.add(exitBtn, gbc);

        startBtn.addActionListener(e -> showOpeningScene());
        loadBtn.addActionListener(e -> showLoadDialog());
        exitBtn.addActionListener(e -> System.exit(0));

        getRootPane().registerKeyboardAction(e -> System.exit(0),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void showOpeningScene() {
        backgroundPanel.stopAnimation();
        backgroundPanel.setVisible(false);

        OpeningSceneDialog openingSceneDialog = new OpeningSceneDialog(this, this::onStoryFinished);
        openingSceneDialog.setVisible(true);
    }

    // private void onStoryFinished() {
    // backgroundPanel.setVisible(true);
    // backgroundPanel.startAnimation();
    // backgroundPanel.repaint();
    // }
    private void onStoryFinished() {
        // Instead of returning to menu, start the game
        startGame(null);
    }

    private void startGame(Hotel loadedHotel) {
        // Hide the main menu
        backgroundPanel.stopAnimation();
        getContentPane().removeAll();

        // Create and show NEW reception panel (instead of GamePanel)
        ReceptionPanel receptionPanel = new ReceptionPanel(this, loadedHotel);
        getContentPane().add(receptionPanel);
        revalidate();
        repaint();
    }

    private void showLoadDialog() {
        File savesDir = new File("saves");
        if (!savesDir.exists() || savesDir.listFiles() == null || savesDir.listFiles().length == 0) {
            JOptionPane.showMessageDialog(this, "No saved games found.");
            return;
        }
        File[] saves = savesDir.listFiles((dir, name) -> name.endsWith(".ser"));
        String[] names = new String[saves.length];
        for (int i = 0; i < saves.length; i++) {
            names[i] = saves[i].getName().replace(".ser", "");
        }

        // Create custom dialog with load and delete options
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(new JLabel("Select a saved game:"), BorderLayout.NORTH);

        JList<String> saveList = new JList<>(names);
        saveList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saveList.setSelectedIndex(0);
        JScrollPane scrollPane = new JScrollPane(saveList);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton loadBtn = new JButton("Load Game");
        JButton deleteBtn = new JButton("Delete Save");
        JButton cancelBtn = new JButton("Cancel");

        buttonPanel.add(loadBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(cancelBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, "Load Game", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        loadBtn.addActionListener(e -> {
            String selected = saveList.getSelectedValue();
            if (selected != null) {
                dialog.dispose();
                try {
                    ObjectInputStream ois = new ObjectInputStream(
                            new FileInputStream(new File(savesDir, selected + ".ser")));
                    Hotel loadedHotel = (Hotel) ois.readObject();
                    ois.close();
                    startGame(loadedHotel);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Load failed: " + ex.getMessage());
                }
            }
        });

        deleteBtn.addActionListener(e -> {
            String selected = saveList.getSelectedValue();
            if (selected != null) {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                        "Are you sure you want to delete '" + selected + "'?\nThis action cannot be undone!",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    File saveFile = new File(savesDir, selected + ".ser");
                    if (saveFile.delete()) {
                        JOptionPane.showMessageDialog(dialog, "Save file deleted successfully!");
                        dialog.dispose();
                        // Refresh the dialog if there are still saves
                        if (savesDir.listFiles((dir, name) -> name.endsWith(".ser")).length > 0) {
                            showLoadDialog();
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to delete save file!");
                    }
                }
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            homepage menu = new homepage();
            menu.setVisible(true);
        });
    }
}
