
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import java.io.*;

public class ReceptionPanel extends JPanel {
    Hotel hotel;
    private JFrame parentFrame;
    private Timer animationTimer;
    private Timer dayTimer;
    Random random = new Random();

    private int secondsElapsed = 0;
    private final int SECONDS_PER_DAY = 120;
    private int gameHour = 0;
    private int gameMinute = 0;

    ArrayList<AnimatedCustomer> walkingCustomers = new ArrayList<>();
    ArrayList<CustomerRequest> pendingRequests = new ArrayList<>();
    public int frameCount = 0;
    private int hotelDoorTargetX = 560;
    private int deskQueueTargetX = 130;
    private int sceneWalkY = 405;
    private float roadCarAX = -180f;
    private float roadCarBX = 260f;
    private final int REQUEST_DURATION_FRAMES = 900; 

    private JLayeredPane layeredPane;
    private ReceptionBackgroundPanel backgroundPanel;
    private JPanel tabBar;
    private JLabel dayLabel, moneyLabel, reputationLabel, timeLabel;
    private JLabel chapterToastLabel;
    private Timer chapterToastTimer;

    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String currentOpenTab = null;

    private JButton requestsTabBtn, guestsTabBtn, roomsTabBtn, employeesTabBtn, historyTabBtn;
    private Color activeTabColor = new Color(70, 130, 180);
    private Color inactiveTabColor = new Color(100, 100, 110);

    RequestsTabPanel requestsTab;
    GuestsTabPanel guestsTab;
    RoomTabPanel roomTab;
    EmployeeTabPanel employeeTab;
    HistoryTabPanel historyTab;

    private int currentChapter = 1;
    private boolean chapter1Complete = false;
    private boolean chapter2Complete = false;
    private boolean chapter3Complete = false;
    private boolean chapter4Complete = false;
    private boolean chapter5Complete = false;
    private boolean chapter6Complete = false;
    private boolean chapter7Complete = false;
    private boolean chapter8Complete = false;

    public ReceptionPanel(JFrame parent, Hotel loadedHotel) {
        this.parentFrame = parent;
        if (loadedHotel == null) {
            this.hotel = new Hotel(Hotel.DEFAULT_HOTEL_NAME);
        } else {
            this.hotel = loadedHotel;
        }
        this.currentChapter = hotel.getCurrentChapter();
        this.chapter1Complete = hotel.isChapter1Complete();
        this.chapter2Complete = hotel.isChapter2Complete();
        this.chapter3Complete = hotel.isChapter3Complete();
        this.chapter4Complete = hotel.isChapter4Complete();
        this.chapter5Complete = hotel.isChapter5Complete();
        this.chapter6Complete = hotel.isChapter6Complete();
        this.chapter7Complete = hotel.isChapter7Complete();
        this.chapter8Complete = hotel.isChapter8Complete();
        normalizeChapterState();
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(240, 235, 220));
        initializeUI();
        startAnimation();
        startDayTimer();
        SwingUtilities.invokeLater(() -> maybeShowChapterIntro(currentChapter));
    }

    private void normalizeChapterState() {
        currentChapter = Math.max(1, Math.min(8, currentChapter));
        if (currentChapter >= 2) {
            chapter1Complete = true;
            hotel.setChapter1Complete(true);
        }
        if (currentChapter >= 3) {
            chapter2Complete = true;
            hotel.setChapter2Complete(true);
        }
        if (currentChapter >= 4) {
            chapter3Complete = true;
            hotel.setChapter3Complete(true);
        }
        if (currentChapter >= 5) {
            chapter4Complete = true;
            hotel.setChapter4Complete(true);
        }
        if (currentChapter >= 6) {
            chapter5Complete = true;
            hotel.setChapter5Complete(true);
        }
        if (currentChapter >= 7) {
            chapter6Complete = true;
            hotel.setChapter6Complete(true);
        }
        if (currentChapter >= 8) {
            chapter7Complete = true;
            hotel.setChapter7Complete(true);
        }
        if (chapter2Complete && !hotel.isChapter2DecisionMade()) {
            hotel.setChapter2DecisionMade(true);
        }
        if (chapter3Complete && !hotel.isChapter3DecisionMade()) {
            hotel.setChapter3DecisionMade(true);
        }
        if (chapter8Complete) {
            hotel.setChapter8StorySeen(true);
        }
        hotel.setCurrentChapter(currentChapter);
    }

    private void initializeUI() {
        add(createStatsPanel(), BorderLayout.NORTH);

        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 600));

        backgroundPanel = new ReceptionBackgroundPanel();
        backgroundPanel.setBounds(0, 0, 900, 600);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        tabBar = createModernTabBar();
        tabBar.setBounds(20, 20, 860, 50);
        tabBar.setOpaque(false);
        layeredPane.add(tabBar, JLayeredPane.PALETTE_LAYER);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBounds(20, 75, 550, 450);
        contentPanel.setBackground(new Color(255, 255, 255, 240));
        contentPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1),
                new EmptyBorder(15, 15, 15, 15)));
        contentPanel.setVisible(false); 

        requestsTab = new RequestsTabPanel(this);
        guestsTab = new GuestsTabPanel(this);
        roomTab = new RoomTabPanel(this);
        employeeTab = new EmployeeTabPanel(this);
        historyTab = new HistoryTabPanel(this);

        contentPanel.add(requestsTab, "requests");
        contentPanel.add(guestsTab, "guests");
        contentPanel.add(roomTab, "rooms");
        contentPanel.add(employeeTab, "employees");
        contentPanel.add(historyTab, "history");

        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);
        layeredPane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutOverlayComponents();
            }
        });
        add(layeredPane, BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);

        for (String log : hotel.getHistoryLogs()) {
            historyTab.addLog(log);
        }

        SwingUtilities.invokeLater(this::layoutOverlayComponents);
        refreshAllTabs();
    }

    private void layoutOverlayComponents() {
        if (layeredPane == null || backgroundPanel == null || tabBar == null || contentPanel == null) {
            return;
        }
        int width = Math.max(1, layeredPane.getWidth());
        int height = Math.max(1, layeredPane.getHeight());

        int margin = clampInt(width / 45, 12, 24);
        int tabHeight = 50;
        int tabWidth = Math.max(320, width - (margin * 2));

        int contentTop = margin + tabHeight + 6;
        int contentWidth = clampInt((int) (width * 0.56f), 520, Math.max(520, width - margin * 2 - 12));
        int contentHeight = clampInt((int) (height * 0.73f), 320, Math.max(320, height - contentTop - 14));

        backgroundPanel.setBounds(0, 0, width, height);
        tabBar.setBounds(margin, margin, tabWidth, tabHeight);
        contentPanel.setBounds(margin, contentTop, contentWidth, contentHeight);
        if (chapterToastLabel != null) {
            int toastW = Math.min(520, Math.max(280, width - 40));
            chapterToastLabel.setBounds((width - toastW) / 2, 18, toastW, 40);
        }
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(new Color(45, 45, 55));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        dayLabel = new JLabel("Day: 1", SwingConstants.CENTER);
        timeLabel = new JLabel("00:00", SwingConstants.CENTER);
        moneyLabel = new JLabel("Money: $500", SwingConstants.CENTER);
        reputationLabel = new JLabel("Rep: 20/100", SwingConstants.CENTER);

        Font f = new Font("SansSerif", Font.BOLD, 16);
        dayLabel.setFont(f);
        dayLabel.setForeground(Color.WHITE);
        timeLabel.setFont(f);
        timeLabel.setForeground(new Color(150, 200, 255));
        moneyLabel.setFont(f);
        moneyLabel.setForeground(new Color(100, 255, 100));
        reputationLabel.setFont(f);
        reputationLabel.setForeground(new Color(255, 215, 100));

        panel.add(dayLabel);
        panel.add(timeLabel);
        panel.add(moneyLabel);
        panel.add(reputationLabel);
        return panel;
    }

    private JPanel createModernTabBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setOpaque(false);
        requestsTabBtn = createTabButton("Requests", "requests");
        guestsTabBtn = createTabButton("Guests", "guests");
        roomsTabBtn = createTabButton("Rooms", "rooms");
        employeesTabBtn = createTabButton("Staff", "employees");
        historyTabBtn = createTabButton("History", "history");
        bar.add(requestsTabBtn);
        bar.add(guestsTabBtn);
        bar.add(roomsTabBtn);
        bar.add(employeesTabBtn);
        bar.add(historyTabBtn);
        return bar;
    }

    private JButton createTabButton(String text, String tabName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBackground(inactiveTabColor);
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(140, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> toggleTab(tabName));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!tabName.equals(currentOpenTab))
                    btn.setBackground(new Color(120, 120, 130));
            }

            public void mouseExited(MouseEvent e) {
                if (!tabName.equals(currentOpenTab))
                    btn.setBackground(inactiveTabColor);
            }
        });
        return btn;
    }

    
    private void toggleTab(String tabName) {
        if (tabName.equals(currentOpenTab)) {
            currentOpenTab = null;
            contentPanel.setVisible(false);
            resetTabColors();
        } else {
            currentOpenTab = tabName;
            contentPanel.setVisible(true);
            cardLayout.show(contentPanel, tabName);
            resetTabColors();
            getTabBtn(tabName).setBackground(activeTabColor);
            refreshTab(tabName);
        }
    }

    private void resetTabColors() {
        for (JButton b : new JButton[] { requestsTabBtn, guestsTabBtn, roomsTabBtn, employeesTabBtn, historyTabBtn })
            b.setBackground(inactiveTabColor);
    }

    private JButton getTabBtn(String name) {
        switch (name) {
            case "requests":
                return requestsTabBtn;
            case "guests":
                return guestsTabBtn;
            case "rooms":
                return roomsTabBtn;
            case "employees":
                return employeesTabBtn;
            default:
                return historyTabBtn;
        }
    }

    private void refreshTab(String name) {
        switch (name) {
            case "requests":
                requestsTab.refresh();
                break;
            case "guests":
                guestsTab.refresh();
                break;
            case "rooms":
                roomTab.refresh();
                break;
            case "employees":
                employeeTab.refresh();
                break;
        }
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(new Color(45, 45, 55));
        JButton storyBtn = new JButton("Story Event");
        JButton saveBtn = new JButton("Save Game");
        JButton menuBtn = new JButton("Main Menu");
        styleBtn(storyBtn);
        styleBtn(saveBtn);
        styleBtn(menuBtn);
        storyBtn.addActionListener(e -> showStoryEvent());
        saveBtn.addActionListener(e -> saveGame());
        menuBtn.addActionListener(e -> returnToMenu());
        panel.add(storyBtn);
        panel.add(saveBtn);
        panel.add(menuBtn);
        return panel;
    }

    private void styleBtn(JButton b) {
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setBackground(new Color(70, 130, 180));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setBackground(new Color(100, 150, 200));
            }

            public void mouseExited(MouseEvent e) {
                b.setBackground(new Color(70, 130, 180));
            }
        });
    }

    private void saveGame() {
        String name = JOptionPane.showInputDialog(this, "Enter save name:");
        if (name == null || name.trim().isEmpty())
            return;
        try {
            SaveFileManager.save(name.trim(), hotel);
            JOptionPane.showMessageDialog(this, "Game saved as " + name);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage());
        }
    }

    private void startDayTimer() {
        dayTimer = new Timer(1000, e -> {
            secondsElapsed++;
            int totalGameMins = (int) ((secondsElapsed / (double) SECONDS_PER_DAY) * 1440);
            gameHour = (totalGameMins / 60) % 24;
            gameMinute = totalGameMins % 60;
            timeLabel.setText(String.format("%02d:%02d", gameHour, gameMinute));

            if (secondsElapsed >= SECONDS_PER_DAY) {
                endDay();
                secondsElapsed = 0;
            }
        });
        dayTimer.start();
    }

    private void endDay() {
        for (Room room : hotel.getRooms()) {
            if (room.isOccupied() && room.decrementNight()) {
                Customer guest = room.checkOut();
                hotel.changeReputation(guest.getReputationImpact());
                addCheckoutDepartureCustomer(guest, room.getRoomNumber());
                historyTab.addLog("<- " + guest.getName() + " auto-checked out | Sat: "
                        + guest.getSatisfactionLevel() + "% | Rep "
                        + (guest.getReputationImpact() >= 0 ? "+" : "") + guest.getReputationImpact());
            }
        }

        int before = hotel.getMoney();
        hotel.endDay();
        historyTab.addLog("===== Day " + hotel.getDay() + " | Expenses: $" + (before - hotel.getMoney()) + " =====");
        historyTab.addLog("");

        refreshAllTabs();
        checkChapterProgress();
        if (hotel.getMoney() < 0)
            gameOver();
    }

    private void onChapterComplete(int ch) {
        int toAdd = hotel.getRooms().size(); 
        int added = hotel.addRooms(toAdd);
        if (added > 0) {
            historyTab.addLog("Hotel expanded! +" + added + " rooms (total: " + hotel.getRooms().size() + ")");
        } else {
            historyTab.addLog("Room expansion skipped. Max capacity reached (" + Hotel.MAX_ROOMS + ").");
        }
        backgroundPanel.updateRoomLights();
        refreshAllTabs();
    }

    private void generateCustomerRequest() {
        int availableRooms = hotel.getAvailableRoomCount();
        if (availableRooms <= 0)
            return;

        if (pendingRequests.size() >= availableRooms)
            return;

        int chance = hotel.getReputation() + 20;
        if (random.nextInt(100) > chance)
            return;

        ArrayList<Integer> roomsAlreadyRequested = new ArrayList<>();
        for (CustomerRequest req : pendingRequests) {
            roomsAlreadyRequested.add(req.room.getRoomNumber());
        }

        Room available = null;
        for (Room room : hotel.getRooms()) {
            if (!room.isOccupied()
                    && room.getCondition() != RoomCondition.BROKEN
                    && !roomsAlreadyRequested.contains(room.getRoomNumber())) {
                available = room;
                break;
            }
        }

        if (available == null)
            return;

        Customer guest = new Customer();
        int nights = 1 + random.nextInt(3);
        int price = available.getBasePrice() * nights;

        CustomerRequest req = new CustomerRequest(guest, available, nights, price);
        pendingRequests.add(req);

        addWalkingCustomer(guest, available.getRoomNumber(), req);
        requestsTab.refresh();
        historyTab.addLog("New request: " + guest.getName() + " for Room " + available.getRoomNumber());
        updateTabBadge();
    }

    public void acceptRequest(CustomerRequest request) {
        if (!pendingRequests.contains(request))
            return;

        request.customer.setPayment(request.price);
        request.room.checkIn(request.customer, request.nights);
        hotel.earnMoney(request.price);

        RoomCondition cond = request.room.getCondition();
        if (cond == RoomCondition.GOOD)
            request.customer.adjustSatisfaction(20);
        else if (cond == RoomCondition.POOR)
            request.customer.adjustSatisfaction(-10);
        else if (cond == RoomCondition.VERY_POOR)
            request.customer.adjustSatisfaction(-25);

        for (AnimatedCustomer ac : walkingCustomers) {
            if (ac.linkedRequest == request) {
                ac.accept();
                break;
            }
        }

        historyTab.addLog("Accepted: " + request.customer.getName() +
                " -> Room " + request.room.getRoomNumber() +
                " | " + request.nights + " night(s) | $" + request.price);

        pendingRequests.remove(request);
        refreshAllTabs();
        checkChapterProgress();
    }

    public void rejectRequest(CustomerRequest request) {
        if (!pendingRequests.contains(request))
            return;

        for (AnimatedCustomer ac : walkingCustomers) {
            if (ac.linkedRequest == request) {
                ac.reject();
                break;
            }
        }

        historyTab.addLog("Rejected: " + request.customer.getName());
        pendingRequests.remove(request);
        refreshAllTabs();
    }

    private void updateTabBadge() {
        requestsTabBtn.setText(pendingRequests.isEmpty()
                ? "Requests"
                : "Requests (" + pendingRequests.size() + ")");
    }

    private void addWalkingCustomer(Customer guest, int roomNumber, CustomerRequest request) {
        walkingCustomers.add(new AnimatedCustomer(this, guest, roomNumber, request));
    }

    private void addCheckoutDepartureCustomer(Customer guest, int roomNumber) {
        walkingCustomers.add(new AnimatedCustomer(this, guest, roomNumber));
    }

    private void showStorySequence(String title, String... scenes) {
        CinematicStoryDialog.showSequence(parentFrame, title, scenes);
    }

    private void showStorySequenceWithImages(String title, String[] scenes, String[] sceneImages) {
        CinematicStoryDialog.showSequenceWithImages(parentFrame, title, scenes, sceneImages);
    }

    private String hotelName() {
        return (hotel.getName() == null || hotel.getName().trim().isEmpty())
                ? Hotel.DEFAULT_HOTEL_NAME
                : hotel.getName();
    }

    private int showStoryChoice(String title, String[] scenes, String... choices) {
        return CinematicStoryDialog.showChoice(parentFrame, title, scenes, choices);
    }

    private int showStoryChoiceWithImages(String title, String[] scenes, String[] sceneImages, String... choices) {
        return CinematicStoryDialog.showChoiceWithImages(parentFrame, title, scenes, sceneImages, choices);
    }

    private void showStoryEvent() {
        checkChapterProgress();
        if (currentChapter == 1 && !chapter1Complete) {
            if (maybeShowChapterIntro(1))
                return;
            showChapter1Events();
        } else if (currentChapter == 2 && !chapter2Complete) {
            if (maybeShowChapterIntro(2))
                return;
            showChapter2Events();
        } else if (currentChapter == 3 && !chapter3Complete) {
            if (maybeShowChapterIntro(3))
                return;
            showChapter3Events();
        } else if (currentChapter == 4 && !chapter4Complete) {
            if (maybeShowChapterIntro(4))
                return;
            showChapter4Events();
        } else if (currentChapter == 5 && !chapter5Complete) {
            if (maybeShowChapterIntro(5))
                return;
            showChapter5Events();
        } else if (currentChapter == 6 && !chapter6Complete) {
            if (maybeShowChapterIntro(6))
                return;
            showChapter6Events();
        } else if (currentChapter == 7 && !chapter7Complete) {
            if (maybeShowChapterIntro(7))
                return;
            showChapter7Events();
        } else if (currentChapter == 8 && !chapter8Complete) {
            if (maybeShowChapterIntro(8))
                return;
            showChapter8Events();
        } else {
            showStorySequence("Story",
                    "No new story event right now.",
                    "Current progression requirements:",
                    chapterRequirementSummary(currentChapter));
        }
    }

    private boolean maybeShowChapterIntro(int chapter) {
        if (isChapterStorySeen(chapter)) {
            return false;
        }
        markChapterStorySeen(chapter, true);
        showChapterIntro(chapter);
        return true;
    }

    private boolean isChapterStorySeen(int chapter) {
        switch (chapter) {
            case 1:
                return hotel.isChapter1StorySeen();
            case 2:
                return hotel.isChapter2StorySeen();
            case 3:
                return hotel.isChapter3StorySeen();
            case 4:
                return hotel.isChapter4StorySeen();
            case 5:
                return hotel.isChapter5StorySeen();
            case 6:
                return hotel.isChapter6StorySeen();
            case 7:
                return hotel.isChapter7StorySeen();
            case 8:
                return hotel.isChapter8StorySeen();
            default:
                return true;
        }
    }

    private void markChapterStorySeen(int chapter, boolean value) {
        switch (chapter) {
            case 1:
                hotel.setChapter1StorySeen(value);
                break;
            case 2:
                hotel.setChapter2StorySeen(value);
                break;
            case 3:
                hotel.setChapter3StorySeen(value);
                break;
            case 4:
                hotel.setChapter4StorySeen(value);
                break;
            case 5:
                hotel.setChapter5StorySeen(value);
                break;
            case 6:
                hotel.setChapter6StorySeen(value);
                break;
            case 7:
                hotel.setChapter7StorySeen(value);
                break;
            case 8:
                hotel.setChapter8StorySeen(value);
                break;
            default:
                break;
        }
    }

    private String chapterRequirementSummary(int chapter) {
        switch (chapter) {
            case 1:
                return "Need 3+ usable rooms, $300+ revenue, Rashid met, and old register found.";
            case 2:
                return "Need Chapter 2 decision complete and reputation 30+.";
            case 3:
                return "Need a Chapter 3 decision.";
            case 4:
                return "Need 2+ employees and Rashid debt resolved.";
            case 5:
                return "Need rival offer refused and reputation 50+.";
            case 6:
                return "Need a Chapter 6 decision.";
            case 7:
                return "Need a Chapter 7 decision.";
            case 8:
                return "Need final ending choice to complete the game.";
            default:
                return "Keep managing the hotel.";
        }
    }

    private void showChapter1Events() {
        String hn = hotelName();
        if (!hotel.hasMetRashid()) {
            int c = showStoryChoiceWithImages("Chapter 1 - Meeting Rashid",
                    new String[] {
                            "While cleaning " + hn + "'s lobby, Arman sees an old man waiting in silence.",
                            "\"I am Rashid. I managed " + hn + " before it was abandoned.\"",
                            "Rashid offers to return as manager for $30 per day."
                    },
                    new String[] { "scene5.jpg", "scene6.jpg", "scene6.jpg" },
                    "Hire Rashid", "Walk away");
            if (c == 0) {
                hotel.hireEmployee(new Employee("Rashid", "manager", 30));
                hotel.setMetRashid(true);
                historyTab.addLog("Rashid joined as manager.");
                refreshAllTabs();
            }
            return;
        }

        if (!hotel.hasFoundOldRegister()) {
            int c = showStoryChoiceWithImages("Chapter 1 - Old Register",
                    new String[] {
                            "Rashid opens a dusty register from the 1970s.",
                            "Artists. Activists. Politicians. Several pages are torn out.",
                            "Rashid says, \"If you read further, " + hn + " will never feel ordinary again.\""
                    },
                    new String[] { "scene7.jpg", "scene7.jpg", "scene6.jpg" },
                    "Investigate", "Ignore for now");
            hotel.setFoundOldRegister(true);
            if (c == 0) {
                hotel.setExploringMystery(true);
                hotel.changeReputation(5);
                historyTab.addLog("Investigating the hotel's past. Reputation +5.");
            } else if (c == 1) {
                historyTab.addLog("Ignored the register for now.");
            }
            refreshAllTabs();
            return;
        }

        showStorySequence("Chapter 1 Progress",
                "Goal status:",
                "3+ usable rooms, $300 revenue, Rashid met, old register found.");
    }

    private void showChapter2Events() {
        String hn = hotelName();
        if (!hotel.isChapter2DecisionMade()) {
            int choice = showStoryChoice("Chapter 2 - The Locked Gates",
                    new String[] {
                            "Locals still avoid " + hn + " after sunset.",
                            "You need one move to rebuild trust."
                    },
                    "Launch local campaign", "Host community night");
            hotel.setChapter2DecisionMade(true);
            if (choice == 0) {
                hotel.changeReputation(4);
                historyTab.addLog("Chapter 2 decision: local campaign. Reputation +4.");
            } else if (choice == 1) {
                hotel.earnMoney(120);
                hotel.changeReputation(2);
                historyTab.addLog("Chapter 2 decision: community night. Money +120, reputation +2.");
            }
            refreshAllTabs();
            checkChapterProgress();
            return;
        }
        showStorySequence("Chapter 2 Progress",
                "Chapter 2 decision is done.",
                "Current requirement:",
                "Reach 30 reputation. Current: " + hotel.getReputation());
    }

    private void showChapter3Events() {
        String hn = hotelName();
        if (!hotel.hasFoundOldRegister()) {
            showStorySequence("Chapter 3 Locked",
                    "Requirements for Chapter 3:",
                    "Find the old register in Chapter 1.");
            return;
        }
        if (hotel.isChapter3DecisionMade()) {
            showStorySequence("Chapter 3 Progress",
                    "Chapter 3 decision already made.",
                    "Continue management to trigger the next chapter.");
            return;
        }

        int choice = showStoryChoiceWithImages("Chapter 3 - Hidden Ledger",
                new String[] {
                        "Names in the register map to missing pages and missing people.",
                        "Rashid looks uneasy. \"Some truths make enemies.\""
                },
                new String[] { "scene7.jpg", "scene6.jpg" },
                "Investigate deeper", "Ignore and focus business");
        hotel.setChapter3DecisionMade(true);
        if (choice == 0) {
            hotel.setExploringMystery(true);
            hotel.changeReputation(5);
            historyTab.addLog("Investigated the hidden ledger. Reputation +5.");
        } else if (choice == 1) {
            historyTab.addLog("Ignored the hidden ledger.");
        }
        refreshAllTabs();
        checkChapterProgress();
    }

    private void showChapter4Events() {
        if (!hotel.hasRashidDebt()) {
            showStorySequence("Chapter 4 Progress",
                    "Rashid's debt is already resolved.",
                    "Current requirement:",
                    "Need at least 2 employees. Current: " + hotel.getEmployees().size());
            return;
        }

        int choice = showStoryChoiceWithImages("Chapter 4 - Debt and Pressure",
                new String[] {
                        "Rashid admits he owes $200 to a dangerous lender tied to local power brokers.",
                        "How do you handle the debt?"
                },
                new String[] { "scene8.jpg", "scene9.jpg" },
                "Pay debt ($200)", "Negotiate", "Expose lender");
        if (choice == 0) {
            if (hotel.spendMoney(200)) {
                hotel.setRashidDebt(false);
                hotel.changeReputation(5);
                historyTab.addLog("Paid Rashid's debt. Reputation +5.");
            } else {
                showStorySequence("Debt", "Not enough money to pay the debt.");
            }
        } else if (choice == 1) {
            if (random.nextBoolean()) {
                hotel.setRashidDebt(false);
                historyTab.addLog("Negotiation worked. Debt settled.");
            } else {
                hotel.changeReputation(-10);
                historyTab.addLog("Negotiation failed. Reputation -10.");
            }
        } else if (choice == 2) {
            hotel.setRashidDebt(false);
            hotel.changeReputation(-15);
            historyTab.addLog("Exposed the lender. Debt cleared, reputation -15.");
        }
        refreshAllTabs();
        checkChapterProgress();
    }

    private void showChapter5Events() {
        String hn = hotelName();
        if (!hotel.isRivalApproached()) {
            showStorySequence("Chapter 5 Locked",
                    "Requirements for Chapter 5:",
                    "Complete Chapter 4 to trigger the rival approach.");
            return;
        }
        if (hotel.isRivalRefused()) {
            showStorySequence("Chapter 5 Progress",
                    "Rival offer already rejected.",
                    "Current requirement:",
                    "Reach 50 reputation to unlock Chapter 6. Current: " + hotel.getReputation());
            checkChapterProgress();
            return;
        }

        int choice = showStoryChoiceWithImages("Chapter 5 - Rival Offer",
                new String[] {
                        "A corporate chain offers to buy " + hn + ".",
                        "\"Sell now. Let the past stay buried.\""
                },
                new String[] { "scene9.jpg", "scene9.jpg" },
                "Sell the hotel", "Refuse");
        if (choice == 0) {
            hotel.earnMoney(2000);
            showStorySequenceWithImages("Sold Silence",
                    new String[] {
                            "You took the money.",
                            "The building is eventually demolished.",
                            "Legacy erased."
                    },
                    new String[] { "scene9.jpg", "scene9.jpg", "scene9.jpg" });
            returnToMenu();
            return;
        }
        if (choice == 1) {
            hotel.setRivalRefused(true);
            historyTab.addLog("Refused the rival offer. Hard mode begins.");
            refreshAllTabs();
            checkChapterProgress();
        }
    }

    private void showChapter6Events() {
        int choice = showStoryChoice("Chapter 6 - The Ghost Room",
                new String[] {
                        "Guests report crying at night and lights switching on by themselves.",
                        "Grandfather finally admits that someone died in that room decades ago.",
                        "Do you hide the past or confront it?"
                },
                "Uncover the truth", "Seal the room");
        if (choice == 0) {
            hotel.setGhostRoomTruth(true);
            hotel.changeReputation(10);
            historyTab.addLog("Ghost room truth uncovered. Reputation +10.");
            showStorySequence("Ghost Room",
                    new String[] {
                            "The story spreads through the city.",
                            "Fear turns into curiosity, and bookings rise."
                    });
        } else if (choice == 1) {
            hotel.setGhostRoomSealed(true);
            hotel.changeReputation(-2);
            historyTab.addLog("Ghost room sealed. Safety first, growth slower.");
            showStorySequence("Ghost Room",
                    new String[] {
                            "The room is sealed quietly.",
                            "Rumors calm down, but the mystery remains unresolved."
                    });
        }
        if (!chapter6Complete && (hotel.isGhostRoomTruth() || hotel.isGhostRoomSealed())) {
            chapter6Complete = true;
            currentChapter = 7;
            hotel.setChapter6Complete(true);
            hotel.setCurrentChapter(7);
            showChapterComplete(6);
            onChapterComplete(6);
        }
        refreshAllTabs();
    }

    private void showChapter7Events() {
        if (hotel.isTreasureFound() || hotel.isOwnershipWarResolved()) {
            showStorySequence("Chapter 7", "Chapter 7 decision already made.");
            return;
        }

        int choice = showStoryChoiceWithImages("Chapter 7 - Treasure and Ownership War",
                new String[] {
                        "An old employee whispers that Arman's father hid evidence for a crisis.",
                        "At the same time, legal papers appear and rivals challenge ownership.",
                        "Pick your response."
                },
                new String[] { "scene10.jpg", "scene7.jpg", "scene9.jpg" },
                "Search treasure", "Focus business", "Fight legally", "Settle quietly");

        if (choice == 0) {
            hotel.earnMoney(1200);
            hotel.changeReputation(6);
            hotel.setTreasureFound(true);
            historyTab.addLog("Hidden treasure found. Money +1200, reputation +6.");
        } else if (choice == 1) {
            hotel.earnMoney(350);
            hotel.changeReputation(2);
            hotel.setOwnershipWarResolved(true);
            historyTab.addLog("Focused on operations. Money +350, reputation +2.");
        } else if (choice == 2) {
            if (hotel.spendMoney(300)) {
                hotel.changeReputation(8);
                hotel.setOwnershipWarResolved(true);
                historyTab.addLog("Won legal fight. Money -300, reputation +8.");
            } else {
                hotel.changeReputation(-4);
                historyTab.addLog("Legal fight failed from low funds. Reputation -4.");
            }
        } else if (choice == 3) {
            if (hotel.spendMoney(100)) {
                hotel.changeReputation(-5);
                hotel.setOwnershipWarResolved(true);
                historyTab.addLog("Settled quietly. Money -100, reputation -5.");
            }
        }
        if (!chapter7Complete && (hotel.isTreasureFound() || hotel.isOwnershipWarResolved())) {
            chapter7Complete = true;
            currentChapter = 8;
            hotel.setChapter7Complete(true);
            hotel.setCurrentChapter(8);
            showChapterComplete(7);
            onChapterComplete(7);
        }
        refreshAllTabs();
    }

    private void showChapter8Events() {
        String hn = hotelName();
        if (chapter8Complete) {
            showStorySequence("Finale", "The legacy has already reached its ending.");
            return;
        }

        int endingChoice = showStoryChoiceWithImages("Chapter 8 - Legacy or Escape",
                new String[] {
                        "All systems collide: finances, reputation, staff, and truth.",
                        "Choose how Arman closes the legacy."
                },
                new String[] { "scene11.jpg", "scene11.jpg" },
                "Reveal full truth", "Protect business first", "Take the money and leave");

        boolean bestEnding = hotel.getReputation() >= 70
                && hotel.getMoney() >= 1500
                && hotel.isTreasureFound()
                && hotel.isGhostRoomTruth()
                && !hotel.hasRashidDebt();
        boolean midEnding = hotel.getReputation() >= 50 && hotel.getMoney() >= 700;
        String endingTitle;
        String[] endingScenes;

        if (endingChoice == 2) {
            endingTitle = "Ending - Sold Memory";
            endingScenes = new String[] {
                    "You choose cash over legacy and walk away.",
                    "The old halls are sold to outside investors.",
                    hn + " survives, but no longer belongs to its history."
            };
        } else if (endingChoice == 0 && bestEnding) {
            endingTitle = "Ending - Golden Legacy";
            endingScenes = new String[] {
                    "History is restored and the truth is documented.",
                    "The rival is defeated and ownership is secured.",
                    hn + " becomes a cultural landmark of mystery and hospitality."
            };
        } else if (endingChoice == 1 && midEnding) {
            endingTitle = "Ending - Quiet Success";
            endingScenes = new String[] {
                    hn + " survives and grows into a stable business.",
                    "Some truths stay buried, but the team stays together.",
                    "A peaceful success, with a few unanswered questions."
            };
        } else {
            endingTitle = "Ending - Lost Legacy";
            endingScenes = new String[] {
                    "Debt and pressure force painful compromises.",
                    "The legacy slips away before it can be restored.",
                    hn + " survives only as a memory."
            };
        }

        CinematicStoryDialog.showSequenceWithPopupAndImages(
                parentFrame,
                endingTitle,
                "GAME COMPLETED",
                endingScenes,
                new String[] { "scene11.jpg", "scene11.jpg", "scene11.jpg" });

        chapter8Complete = true;
        hotel.setChapter8Complete(true);
        hotel.setCurrentChapter(8);
        historyTab.addLog("Final chapter completed: " + endingTitle + ".");
        showChapterComplete(8);
        refreshAllTabs();
    }

    private void checkChapterProgress() {
        if (currentChapter == 1 && !chapter1Complete
                && hotel.getUsableRoomCount() >= 3
                && hotel.getTotalRevenue() >= 300
                && hotel.hasMetRashid()
                && hotel.hasFoundOldRegister()) {
            chapter1Complete = true;
            currentChapter = 2;
            hotel.setChapter1Complete(true);
            hotel.setCurrentChapter(2);
            showChapterComplete(1);
            onChapterComplete(1);
            return;
        }

        if (currentChapter == 2 && chapter1Complete && !chapter2Complete && hotel.getReputation() >= 30) {
            if (!hotel.isChapter2DecisionMade()) {
                return;
            }
            chapter2Complete = true;
            currentChapter = 3;
            hotel.setChapter2Complete(true);
            hotel.setCurrentChapter(3);
            showChapterComplete(2);
            onChapterComplete(2);
            return;
        }

        if (currentChapter == 3 && chapter2Complete && !chapter3Complete && hotel.isChapter3DecisionMade()) {
            chapter3Complete = true;
            currentChapter = 4;
            hotel.setChapter3Complete(true);
            hotel.setCurrentChapter(4);
            showChapterComplete(3);
            onChapterComplete(3);
            return;
        }

        if (currentChapter == 4 && chapter3Complete && !chapter4Complete
                && hotel.getEmployees().size() >= 2 && !hotel.hasRashidDebt()) {
            chapter4Complete = true;
            currentChapter = 5;
            hotel.setChapter4Complete(true);
            hotel.setCurrentChapter(5);
            hotel.setRivalApproached(true);
            showChapterComplete(4);
            onChapterComplete(4);
            return;
        }

        if (currentChapter == 5 && chapter4Complete && !chapter5Complete
                && hotel.isRivalRefused() && hotel.getReputation() >= 50) {
            chapter5Complete = true;
            currentChapter = 6;
            hotel.setChapter5Complete(true);
            hotel.setCurrentChapter(6);
            showChapterComplete(5);
            onChapterComplete(5);
        }
    }

    private void showChapterIntro(int ch) {
        String hn = hotelName();
        String[] scenes;
        switch (ch) {
            case 1:
                scenes = new String[] {
                        "CHAPTER 1: Homecoming to Ruins",
                        "Restore " + hn + ": fix 3+ usable rooms, earn $300 revenue, meet Rashid, and find the old register."
                };
                break;
            case 2:
                scenes = new String[] {
                        "CHAPTER 2: The Locked Gates",
                        "Raise reputation to 30+ while rebuilding trust in " + hn + "."
                };
                break;
            case 3:
                scenes = new String[] {
                        "CHAPTER 3: The Old Register",
                        "Choose whether to investigate " + hn + "'s hidden past."
                };
                break;
            case 4:
                scenes = new String[] {
                        "CHAPTER 4: Staff, Salaries, and Debt",
                        "Build a team and survive Rashid's debt crisis."
                };
                break;
            case 5:
                scenes = new String[] {
                        "CHAPTER 5: The Rival's Shadow",
                        "Reject or accept the corporate buyout."
                };
                break;
            case 6:
                scenes = new String[] {
                        "CHAPTER 6: The Ghost Room",
                        "At 50+ reputation, face the truth about the haunted room."
                };
                break;
            case 7:
                scenes = new String[] {
                        "CHAPTER 7: Lost Treasure and Ownership War",
                        "Search hidden evidence or resolve ownership pressure."
                };
                break;
            case 8:
                scenes = new String[] {
                        "CHAPTER 8: Legacy or Escape",
                        "Your decisions now decide the final ending."
                };
                break;
            default:
                scenes = new String[] { "Continuing the legacy..." };
        }
        showStorySequence("Chapter " + ch, scenes);
    }

    private void showChapterComplete(int ch) {
        String msg = (ch < 8)
                ? "Chapter " + ch + " complete. Chapter " + (ch + 1) + " unlocked."
                : "Final chapter complete.";
        showChapterUnlockToast(msg);
        historyTab.addLog("[CHAPTER] " + msg);
    }
    public void refreshAllTabs() {
        dayLabel.setText("Day: " + hotel.getDay());
        moneyLabel.setText("Money: $" + hotel.getMoney());
        reputationLabel.setText("Rep: " + hotel.getReputation() + "/100");
        moneyLabel.setForeground(hotel.getMoney() < 100 ? Color.RED : new Color(100, 255, 100));
        if (currentOpenTab != null)
            refreshTab(currentOpenTab);
        backgroundPanel.updateRoomLights();
        updateTabBadge();
    }

    int getHotelDoorTargetX() {
        return hotelDoorTargetX;
    }

    int getDeskQueueTargetX() {
        return deskQueueTargetX;
    }

    int getSceneWalkY() {
        return sceneWalkY;
    }

    int getSceneWidth() {
        return Math.max(1, backgroundPanel.getWidth());
    }

    int getGameHour() {
        return gameHour;
    }

    int getGameMinute() {
        return gameMinute;
    }

    private void showChapterUnlockToast(String message) {
        if (layeredPane == null) {
            return;
        }
        if (chapterToastLabel == null) {
            chapterToastLabel = new JLabel("", SwingConstants.CENTER);
            chapterToastLabel.setOpaque(true);
            chapterToastLabel.setBackground(new Color(20, 26, 36, 230));
            chapterToastLabel.setForeground(new Color(255, 232, 170));
            chapterToastLabel.setBorder(new CompoundBorder(
                    new LineBorder(new Color(255, 215, 120), 1, true),
                    new EmptyBorder(8, 14, 8, 14)));
            chapterToastLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            layeredPane.add(chapterToastLabel, JLayeredPane.MODAL_LAYER);
            layoutOverlayComponents();
        }

        chapterToastLabel.setText(message);
        chapterToastLabel.setVisible(true);
        chapterToastLabel.repaint();

        if (chapterToastTimer != null) {
            chapterToastTimer.stop();
        }
        chapterToastTimer = new Timer(2300, e -> {
            chapterToastLabel.setVisible(false);
            ((Timer) e.getSource()).stop();
        });
        chapterToastTimer.setRepeats(false);
        chapterToastTimer.start();
    }

    private int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void startAnimation() {
        animationTimer = new Timer(33, e -> { 
            frameCount++;
            int sceneWidth = Math.max(900, getSceneWidth());
            roadCarAX += 2.4f;
            roadCarBX += 3.1f;
            if (roadCarAX > sceneWidth + 140f)
                roadCarAX = -220f;
            if (roadCarBX > sceneWidth + 180f)
                roadCarBX = -280f;

            if (frameCount % 60 == 0)
                generateCustomerRequest();

            if (frameCount % 30 == 0 && "requests".equals(currentOpenTab)) {
                requestsTab.refresh();
            }

            for (int i = pendingRequests.size() - 1; i >= 0; i--) {
                CustomerRequest req = pendingRequests.get(i);
                req.framesLived++;
                if (req.framesLived >= REQUEST_DURATION_FRAMES) {
                    historyTab.addLog("Expired: " + req.customer.getName());
                    pendingRequests.remove(i);
                    if ("requests".equals(currentOpenTab))
                        requestsTab.refresh();
                    updateTabBadge();
                    refreshAllTabs();
                }
            }

            for (int i = walkingCustomers.size() - 1; i >= 0; i--) {
                AnimatedCustomer ac = walkingCustomers.get(i);
                ac.update();
                if (ac.isFinished())
                    walkingCustomers.remove(i);
            }

            backgroundPanel.repaint();
        });
        animationTimer.start();
    }

    private void gameOver() {
        animationTimer.stop();
        dayTimer.stop();
        JOptionPane.showMessageDialog(this,
                "GAME OVER\n\nRan out of money!\nRevenue: $" + hotel.getTotalRevenue() + "\nDays: " + hotel.getDay(),
                "Game Over", JOptionPane.ERROR_MESSAGE);
        returnToMenu();
    }

    private void returnToMenu() {
        animationTimer.stop();
        dayTimer.stop();
        if (JOptionPane.showConfirmDialog(this, "Return to menu? Progress will be lost.",
                "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            parentFrame.dispose();
            SwingUtilities.invokeLater(() -> new homepage().setVisible(true));
        } else {
            animationTimer.start();
            dayTimer.start();
        }
    }

    class ReceptionBackgroundPanel extends JPanel {
        private boolean[] roomLights = new boolean[0];

        public ReceptionBackgroundPanel() {
            setOpaque(true);
        }

        public void updateRoomLights() {
            roomLights = new boolean[hotel.getRooms().size()];
            for (int i = 0; i < hotel.getRooms().size(); i++) {
                roomLights[i] = hotel.getRooms().get(i).isOccupied();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                g2.dispose();
                return;
            }

            float dayFactor = computeDayFactor();
            float nightFactor = 1f - dayFactor;
            drawEnhancedSky(g2, w, h, dayFactor, nightFactor);
            boolean isNight = nightFactor > 0.55f;

            int roadHeight = Math.max(108, (int) (h * 0.21f));
            int roadTop = h - roadHeight;
            int sidewalkHeight = Math.max(24, (int) (h * 0.05f));
            int sidewalkTop = roadTop - sidewalkHeight;
            int lawnHeight = Math.max(56, (int) (h * 0.11f));
            int lawnTop = sidewalkTop - lawnHeight;

            Rectangle hotelBounds = calculateHotelBounds(w, h, sidewalkTop);
            sceneWalkY = clampInt(sidewalkTop - 58, 250, h - 136);

            drawDistantHills(g2, w, lawnTop, isNight);
            drawGroundAndRoad(g2, w, h, lawnTop, sidewalkTop, roadTop);
            drawTrafficCars(g2, roadTop, h);
            drawStreetLamps(g2, w, sidewalkTop, roadTop, isNight, hotelBounds);
            drawBuilding(g2, hotelBounds.x, hotelBounds.y, hotelBounds.width, hotelBounds.height, sidewalkTop);
            drawForegroundLandscaping(g2, w, sidewalkTop, hotelBounds);
            drawEntrancePath(g2, w, sidewalkTop, roadTop);

            int deskX = clampInt((int) (w * 0.05f), 24, Math.max(24, hotelBounds.x - 236));
            int desiredDeskY = sceneWalkY - 38;
            int maxDeskY = sidewalkTop - 90; 
            int deskY = Math.max(24, Math.min(desiredDeskY, maxDeskY));
            if (deskY + 88 < roadTop) {
                drawDesk(g2, deskX, deskY);
            }

            for (AnimatedCustomer ac : walkingCustomers) {
                ac.draw(g2);
            }

            g2.dispose();
        }

        private float computeDayFactor() {
            float t = (gameHour + (gameMinute / 60f)) / 24f;
            return (float) (0.5 - 0.5 * Math.cos(t * Math.PI * 2));
        }

        private void drawEnhancedSky(Graphics2D g2, int w, int h, float dayFactor, float nightFactor) {
            Color dayTop = new Color(135, 206, 235);
            Color dayBottom = new Color(180, 225, 255);
            Color nightTop = new Color(10, 14, 40);
            Color nightBottom = new Color(25, 30, 70);
            Color skyTop = blendColor(nightTop, dayTop, dayFactor);
            Color skyBottom = blendColor(nightBottom, dayBottom, dayFactor);
            g2.setPaint(new GradientPaint(0, 0, skyTop, 0, h, skyBottom));
            g2.fillRect(0, 0, w, h);

            if (nightFactor > 0.08f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f * nightFactor));
                Random sr = new Random(42);
                for (int i = 0; i < 60; i++) {
                    int sx = sr.nextInt(Math.max(1, w));
                    int sy = sr.nextInt(Math.max(1, Math.max(80, h / 2)));
                    int size = 1 + sr.nextInt(3);
                    g2.setColor(new Color(255, 255, 255, 120 + ((frameCount + i * 11) % 120)));
                    g2.fillOval(sx, sy, size, size);
                }
                g2.setComposite(AlphaComposite.SrcOver);
            }

            int orbX = w - 160;
            int orbY = 48;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, dayFactor));
            g2.setColor(new Color(255, 236, 160, 185));
            g2.fillOval(orbX - 18, orbY - 12, 84, 84);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, nightFactor));
            g2.setColor(new Color(225, 236, 255, 190));
            g2.fillOval(orbX, orbY, 68, 68);
            g2.setComposite(AlphaComposite.SrcOver);

            float cloudAlpha = 0.22f + 0.45f * dayFactor;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cloudAlpha));
            drawSkyCloud(g2, (w * 0.08f) + (frameCount * 0.22f) % (w + 180) - 140, 58f, 0.95f);
            drawSkyCloud(g2, (w * 0.32f) + (frameCount * 0.16f) % (w + 220) - 180, 84f, 1.10f);
            drawSkyCloud(g2, (w * 0.60f) + (frameCount * 0.11f) % (w + 200) - 160, 46f, 0.80f);
            g2.setComposite(AlphaComposite.SrcOver);
        }

        private void drawSkyCloud(Graphics2D g2, float x, float y, float scale) {
            int w = (int) (136 * scale);
            int h = (int) (54 * scale);
            g2.setPaint(new GradientPaint((int) x, (int) y, new Color(255, 255, 255, 176),
                    (int) x + w, (int) y + h, new Color(255, 255, 255, 52)));
            g2.fillOval((int) x, (int) y, w, h);
            g2.fillOval((int) (x + w * 0.2f), (int) (y - h * 0.3f), (int) (w * 0.6f), (int) (h * 0.7f));
            g2.fillOval((int) (x + w * 0.54f), (int) (y - h * 0.2f), (int) (w * 0.48f), (int) (h * 0.6f));
        }

        private Color blendColor(Color c1, Color c2, float ratio) {
            float r = c1.getRed() * (1 - ratio) + c2.getRed() * ratio;
            float g = c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio;
            float b = c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio;
            return new Color((int) r, (int) g, (int) b);
        }

        private void drawDistantHills(Graphics2D g2, int w, int lawnTop, boolean isNight) {
            int baseY = lawnTop + 8;
            Color hillA = isNight ? new Color(36, 48, 70, 176) : new Color(112, 146, 120, 172);
            Color hillB = isNight ? new Color(30, 40, 62, 184) : new Color(90, 126, 102, 176);

            Polygon p1 = new Polygon(
                    new int[] { -30, w / 6, w / 3, w / 2, (int) (w * 0.73), w + 30 },
                    new int[] { baseY + 30, baseY - 22, baseY + 12, baseY - 26, baseY + 6, baseY + 32 },
                    6);
            Polygon p2 = new Polygon(
                    new int[] { -20, w / 4, (int) (w * 0.48), (int) (w * 0.70), w + 20 },
                    new int[] { baseY + 46, baseY - 2, baseY + 20, baseY - 6, baseY + 48 },
                    5);

            g2.setColor(hillA);
            g2.fillPolygon(p1);
            g2.setColor(hillB);
            g2.fillPolygon(p2);
        }

        private void drawGroundAndRoad(Graphics2D g2, int w, int h, int lawnTop, int sidewalkTop, int roadTop) {
            g2.setPaint(new GradientPaint(0, lawnTop, new Color(82, 132, 80), 0, sidewalkTop, new Color(56, 92, 58)));
            g2.fillRect(0, lawnTop, w, sidewalkTop - lawnTop);

            g2.setColor(new Color(64, 110, 62, 95));
            for (int gx = 6; gx < w; gx += 18) {
                int bladeTop = lawnTop + 10 + ((gx / 18) % 2) * 6;
                g2.fillRect(gx, bladeTop, 2, Math.max(6, sidewalkTop - bladeTop - 2));
            }

            g2.setColor(new Color(174, 162, 146));
            g2.fillRect(0, sidewalkTop, w, roadTop - sidewalkTop);
            g2.setColor(new Color(148, 136, 120));
            for (int sx = 0; sx < w; sx += 36) {
                g2.drawLine(sx, sidewalkTop, sx + 16, roadTop);
            }

            g2.setPaint(new GradientPaint(0, roadTop, new Color(86, 92, 102), 0, h, new Color(54, 58, 68)));
            g2.fillRect(0, roadTop, w, h - roadTop);

            g2.setColor(new Color(238, 214, 143, 102));
            g2.drawLine(0, roadTop + 6, w, roadTop + 6);

            int stripeY = roadTop + (h - roadTop) / 2 - 3;
            int stripeOffset = frameCount % 84;
            g2.setColor(new Color(220, 220, 220, 178));
            for (int x = -84 + stripeOffset; x < w + 84; x += 84) {
                g2.fillRoundRect(x, stripeY, 38, 6, 4, 4);
            }
        }

        private void drawTrafficCars(Graphics2D g2, int roadTop, int panelH) {
            int laneA = roadTop + (panelH - roadTop) / 2 - 22;
            int laneB = roadTop + (panelH - roadTop) / 2 + 8;
            drawRoadCar(g2, roadCarAX, laneA, new Color(214, 82, 82));
            drawRoadCar(g2, roadCarBX, laneB, new Color(76, 138, 220));
        }

        private void drawRoadCar(Graphics2D g2, float x, int y, Color body) {
            int w = 50;
            int h = 18;
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRoundRect((int) x + 3, y + h + 1, w, h / 3, 8, 8);
            g2.setColor(body);
            g2.fillRoundRect((int) x, y, w, h, 8, 8);
            g2.fillRoundRect((int) x + 8, y - 10, 32, 14, 8, 8);
            g2.setColor(new Color(180, 220, 255, 180));
            g2.fillRect((int) x + 16, y - 6, 14, 6);
            g2.setColor(Color.BLACK);
            g2.fillOval((int) x + 6, y + h - 2, 10, 10);
            g2.fillOval((int) x + w - 16, y + h - 2, 10, 10);
        }

        private void drawStreetLamps(Graphics2D g2, int w, int sidewalkTop, int roadTop, boolean isNight,
                Rectangle hotelBounds) {
            int[] lampX = {
                    clampInt((int) (w * 0.12f), 26, Math.max(26, w - 26)),
                    clampInt((int) (w * 0.30f), 26, Math.max(26, w - 26)),
                    clampInt((int) (w * 0.49f), 26, Math.max(26, w - 26)),
                    clampInt((int) (w * 0.67f), 26, Math.max(26, w - 26)),
                    clampInt((int) (w * 0.85f), 26, Math.max(26, w - 26))
            };

            for (int lx : lampX) {
                if (lx > hotelBounds.x - 24 && lx < hotelBounds.x + hotelBounds.width + 24) {
                    continue;
                }
                g2.setColor(new Color(64, 64, 72));
                g2.fillRect(lx, sidewalkTop - 72, 5, 62);
                g2.setColor(new Color(85, 85, 92));
                g2.fillRoundRect(lx - 8, sidewalkTop - 76, 22, 10, 5, 5);
                g2.setColor(new Color(68, 68, 76));
                g2.fillRoundRect(lx - 4, roadTop - 8, 14, 8, 4, 4);
                if (isNight) {
                    g2.setColor(new Color(255, 230, 160, 114));
                    g2.fillOval(lx - 23, sidewalkTop - 64, 52, 38);
                }
            }
        }

        private Rectangle calculateHotelBounds(int panelW, int panelH, int sidewalkTop) {
            int totalRooms = Math.max(4, hotel.getRooms().size());
            int visualRooms = Math.min(64, totalRooms);
            int growth = Math.max(0, visualRooms - 4);

            int bw = clampInt(332 + growth * 3, 320, Math.max(320, panelW - 260));
            int bh = clampInt(230 + growth * 4, 230, Math.min(520, panelH - 130));

            int rightMargin = clampInt((int) (panelW * 0.03f), 14, 28);
            int leftLimit = clampInt((int) (panelW * 0.37f), 220, Math.max(220, panelW - 340));
            int bx = Math.max(leftLimit, panelW - bw - rightMargin);
            bx = Math.max(12, Math.min(bx, Math.max(12, panelW - bw - 8)));

            int by = clampInt(sidewalkTop - bh - 12, 18, Math.max(18, panelH - bh - 120));
            return new Rectangle(bx, by, bw, bh);
        }

        private void drawBuilding(Graphics2D g2, int x, int y, int bw, int bh, int sidewalkTop) {
            int depth = Math.max(16, bw / 10);

            g2.setColor(new Color(0, 0, 0, 72));
            g2.fillRoundRect(x + depth / 2 + 8, y + 10, bw, bh, 12, 12);

            Polygon side = new Polygon();
            side.addPoint(x + bw, y);
            side.addPoint(x + bw + depth, y + depth / 2);
            side.addPoint(x + bw + depth, y + bh + depth / 2);
            side.addPoint(x + bw, y + bh);
            g2.setPaint(
                    new GradientPaint(x + bw, y, new Color(176, 152, 124), x + bw + depth, y + bh, new Color(138, 116, 92)));
            g2.fillPolygon(side);

            g2.setPaint(new GradientPaint(x, y, new Color(230, 205, 170), x, y + bh, new Color(194, 168, 136)));
            g2.fillRoundRect(x, y, bw, bh, 12, 12);

            g2.setColor(new Color(130, 110, 90));
            g2.fillPolygon(new int[] { x - 12, x + bw / 2, x + bw + 12 }, new int[] { y, y - 32, y }, 3);

            int signW = Math.max(190, (int) (bw * 0.66f));
            int signH = 34;
            int signX = x + (bw - signW) / 2;
            int signY = Math.max(6, y - 58);
            g2.setColor(new Color(70, 50, 40));
            g2.fillRoundRect(signX, signY, signW, signH, 9, 9);
            g2.setColor(new Color(255, 230, 160));
            g2.drawRoundRect(signX, signY, signW, signH, 9, 9);
            Font signFont = new Font("Serif", Font.BOLD, signW < 220 ? 13 : 15);
            String hotelName = hotel.getName() == null ? "HOTEL" : hotel.getName().toUpperCase();
            g2.setFont(signFont);
            while (g2.getFontMetrics().stringWidth(hotelName) > signW - 12 && signFont.getSize() > 10) {
                signFont = signFont.deriveFont((float) (signFont.getSize() - 1));
                g2.setFont(signFont);
            }
            int textX = signX + (signW - g2.getFontMetrics().stringWidth(hotelName)) / 2;
            g2.drawString(hotelName, textX, signY + 22);

            int dW = clampInt(bw / 5, 58, 86);
            int dH = clampInt((int) (bh * 0.26f), 80, 108);
            int dX = x + bw / 2 - dW / 2;
            int dY = y + bh - dH;
            int maxDoorBottom = sidewalkTop - 8;
            int doorBottom = dY + dH;
            if (doorBottom > maxDoorBottom) {
                dY -= (doorBottom - maxDoorBottom);
            }
            hotelDoorTargetX = dX + dW / 2;

            drawRoomWindows(g2, x + 14, y + 44, bw - 28, bh - 156, hotel.getRooms().size(),
                    dX - 16, dY - 20, dW + 32, dH + 36);

            g2.setColor(new Color(120, 92, 66));
            g2.fillRoundRect(dX - 6, dY - 8, dW + 12, dH + 10, 10, 10);
            g2.setColor(new Color(156, 131, 106));
            g2.drawRoundRect(dX - 6, dY - 8, dW + 12, dH + 10, 10, 10);

            int panelGap = 4;
            int panelW = Math.max(18, (dW - panelGap * 3) / 2);
            int panelH = dH - 14;
            int panelY = dY + 7;
            int leftDoorX = dX + panelGap;
            int rightDoorX = leftDoorX + panelW + panelGap;

            g2.setColor(new Color(78, 56, 42));
            g2.fillRoundRect(leftDoorX, panelY, panelW, panelH, 7, 7);
            g2.fillRoundRect(rightDoorX, panelY, panelW, panelH, 7, 7);

            int glassH = Math.max(18, panelH / 2 - 2);
            g2.setColor(new Color(162, 206, 232, 156));
            g2.fillRoundRect(leftDoorX + 4, panelY + 4, panelW - 8, glassH, 5, 5);
            g2.fillRoundRect(rightDoorX + 4, panelY + 4, panelW - 8, glassH, 5, 5);
            g2.setColor(new Color(110, 150, 180, 118));
            g2.drawLine(leftDoorX + 6, panelY + 6, leftDoorX + panelW - 8, panelY + glassH - 2);
            g2.drawLine(rightDoorX + 6, panelY + 6, rightDoorX + panelW - 8, panelY + glassH - 2);

            g2.setColor(new Color(207, 186, 108));
            g2.fillOval(leftDoorX + panelW - 9, panelY + panelH / 2, 6, 6);
            g2.fillOval(rightDoorX + 3, panelY + panelH / 2, 6, 6);

            int canopyW = dW + 56;
            int canopyX = dX - (canopyW - dW) / 2;
            int canopyY = dY - 18;
            g2.setColor(new Color(116, 92, 70));
            g2.fillRoundRect(canopyX, canopyY, canopyW, 12, 8, 8);
            g2.setColor(new Color(166, 142, 118));
            g2.drawRoundRect(canopyX, canopyY, canopyW, 12, 8, 8);

            int stepW = dW + 74;
            int stepX = dX - (stepW - dW) / 2;
            int stepY1 = Math.min(dY + dH + 4, sidewalkTop - 18);
            int stepY2 = stepY1 + 10;
            g2.setColor(new Color(142, 124, 104));
            g2.fillRoundRect(stepX + 8, stepY1, stepW - 16, 10, 6, 6);
            g2.setColor(new Color(122, 104, 86));
            g2.fillRoundRect(stepX, stepY2, stepW, 12, 8, 8);

            int planterW = 20;
            int planterH = 26;
            g2.setColor(new Color(104, 74, 52));
            g2.fillRoundRect(dX - 40, dY + dH - planterH, planterW, planterH, 4, 4);
            g2.fillRoundRect(dX + dW + 20, dY + dH - planterH, planterW, planterH, 4, 4);
            g2.setColor(new Color(72, 136, 74));
            g2.fillOval(dX - 45, dY + dH - planterH - 18, planterW + 10, 20);
            g2.fillOval(dX + dW + 15, dY + dH - planterH - 18, planterW + 10, 20);

            if (hotel.hasEmployeeType("security")) {
                int guardX = dX + dW + 14;
                if (guardX + 24 > x + bw - 8) {
                    guardX = dX - 26;
                }
                int guardY = dY + dH - 52;
                drawSecurityGuard(g2, guardX, guardY);
            }
        }

        private void drawRoomWindows(Graphics2D g2, int areaX, int areaY, int areaW, int areaH, int totalRooms,
                int keepoutX, int keepoutY, int keepoutW, int keepoutH) {
            if (totalRooms <= 0 || areaW <= 16 || areaH <= 16) {
                return;
            }

            int visualRooms = Math.min(64, totalRooms);

            int cols = clampInt((int) Math.ceil(Math.sqrt(visualRooms * 1.15)), 4, 12);
            int rows = Math.max(1, (int) Math.ceil(visualRooms / (double) cols));
            int cellW = Math.max(12, areaW / cols);
            int cellH = Math.max(14, areaH / rows);
            int winW = Math.max(9, cellW - 6);
            int winH = Math.max(9, cellH - 6);

            int idx = 0;
            for (int r = 0; r < rows && idx < visualRooms; r++) {
                for (int c = 0; c < cols && idx < visualRooms; c++) {
                    int wx = areaX + c * cellW + (cellW - winW) / 2;
                    int wy = areaY + r * cellH + (cellH - winH) / 2;
                    if (rectIntersects(wx, wy, winW, winH, keepoutX, keepoutY, keepoutW, keepoutH)) {
                        continue;
                    }

                    g2.setColor(new Color(100, 80, 60));
                    g2.fillRoundRect(wx, wy, winW, winH, 5, 5);

                    boolean lit = idx < roomLights.length && roomLights[idx];
                    g2.setColor(lit ? new Color(255, 230, 150) : new Color(60, 70, 90));
                    g2.fillRoundRect(wx + 2, wy + 2, winW - 4, winH - 4, 4, 4);

                    if (winW >= 10 && winH >= 10) {
                        g2.setColor(new Color(80, 70, 60));
                        g2.drawLine(wx + winW / 2, wy + 2, wx + winW / 2, wy + winH - 2);
                        g2.drawLine(wx + 2, wy + winH / 2, wx + winW - 2, wy + winH / 2);
                    }

                    if (winW >= 13 && winH >= 12) {
                        int labelSize = winH <= 14 ? 7 : 8;
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("SansSerif", Font.BOLD, labelSize));
                        String rn = String.valueOf(idx + 1);
                        int tx = wx + (winW - g2.getFontMetrics().stringWidth(rn)) / 2;
                        int ty = wy + (winH + labelSize - 2) / 2;
                        g2.drawString(rn, tx, ty);
                    }
                    idx++;
                }
            }

            if (totalRooms > visualRooms) {
                String overflow = "+" + (totalRooms - visualRooms) + " rooms";
                g2.setFont(new Font("SansSerif", Font.BOLD, 11));
                int tw = g2.getFontMetrics().stringWidth(overflow);
                int bx = areaX + Math.max(0, areaW - tw - 10);
                int by = areaY + 4;
                g2.setColor(new Color(0, 0, 0, 130));
                g2.fillRoundRect(bx - 4, by - 11, tw + 8, 15, 8, 8);
                g2.setColor(new Color(255, 230, 170));
                g2.drawString(overflow, bx, by);
            }
        }

        private boolean rectIntersects(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2) {
            return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
        }

        private void drawEntrancePath(Graphics2D g2, int panelW, int sidewalkTop, int roadTop) {
            int pathW = clampInt(48 + hotel.getRooms().size() / 5, 48, 84);
            int pathX = clampInt(hotelDoorTargetX - pathW / 2, 4, Math.max(4, panelW - pathW - 4));
            int pathY = sidewalkTop - 8;
            int pathH = Math.max(14, roadTop - pathY - 6);

            g2.setColor(new Color(196, 184, 168));
            g2.fillRoundRect(pathX, pathY, pathW, pathH, 10, 10);
            g2.setColor(new Color(156, 142, 124));
            g2.drawRoundRect(pathX, pathY, pathW, pathH, 10, 10);
            g2.setColor(new Color(176, 164, 144, 165));
            for (int yy = pathY + 4; yy < pathY + pathH - 2; yy += 8) {
                g2.drawLine(pathX + 7, yy, pathX + pathW - 8, yy);
            }
        }

        private void drawForegroundLandscaping(Graphics2D g2, int w, int sidewalkTop, Rectangle hotelBounds) {
            int hedgeY = sidewalkTop - 16;
            int gapLeft = clampInt(hotelDoorTargetX - 44, 0, w);
            int gapRight = clampInt(hotelDoorTargetX + 44, 0, w);

            g2.setColor(new Color(58, 108, 58));
            for (int x = 8; x < w - 8; x += 22) {
                if (x + 26 >= gapLeft && x <= gapRight) {
                    continue;
                }
                g2.fillOval(x, hedgeY, 26, 14);
            }

            g2.setColor(new Color(50, 92, 50));
            if (gapLeft > 0) {
                g2.fillRect(0, hedgeY + 8, gapLeft, 6);
            }
            if (gapRight < w) {
                g2.fillRect(gapRight, hedgeY + 8, w - gapRight, 6);
            }

            int minTreeX = 36;
            int maxTreeX = Math.max(minTreeX + 2, w - 44);
            int leftTree = clampInt(hotelBounds.x - 170, minTreeX, maxTreeX);
            int midTree = clampInt(hotelBounds.x - 84, minTreeX, maxTreeX);
            int rightTree = clampInt(hotelBounds.x + hotelBounds.width + 22, minTreeX, maxTreeX);

            if (leftTree > gapLeft - 34 && leftTree < gapRight + 34) {
                leftTree = clampInt(gapLeft - 46, minTreeX, maxTreeX);
            }
            if (midTree > gapLeft - 34 && midTree < gapRight + 34) {
                midTree = clampInt(gapRight + 46, minTreeX, maxTreeX);
            }
            if (rightTree > gapLeft - 34 && rightTree < gapRight + 34) {
                rightTree = clampInt(gapRight + 52, minTreeX, maxTreeX);
            }

            drawTree(g2, leftTree, hedgeY + 10, 1.04f);
            drawTree(g2, midTree, hedgeY + 12, 0.92f);
            drawTree(g2, rightTree, hedgeY + 10, 1.0f);
        }

        private void drawTree(Graphics2D g2, int trunkX, int groundY, float scale) {
            int trunkW = (int) (8 * scale);
            int trunkH = (int) (26 * scale);
            int crownR = (int) (28 * scale);
            g2.setColor(new Color(98, 70, 50));
            g2.fillRoundRect(trunkX, groundY - trunkH, trunkW, trunkH, 4, 4);
            g2.setColor(new Color(68, 126, 68));
            g2.fillOval(trunkX - crownR / 2, groundY - trunkH - crownR / 2 - 4, crownR, crownR);
            g2.fillOval(trunkX - crownR / 2 - 10, groundY - trunkH - crownR / 2 + 2, crownR - 6, crownR - 6);
            g2.fillOval(trunkX - crownR / 2 + 12, groundY - trunkH - crownR / 2 + 3, crownR - 8, crownR - 8);
        }

        private void drawSecurityGuard(Graphics2D g2, int x, int y) {
            g2.setColor(new Color(0, 0, 0, 55));
            g2.fillOval(x - 8, y + 48, 34, 9);

            g2.setColor(new Color(36, 63, 98));
            g2.fillRoundRect(x, y + 16, 18, 23, 7, 7);
            g2.setColor(new Color(88, 130, 182));
            g2.fillRect(x + 4, y + 19, 10, 8);
            g2.setColor(new Color(230, 214, 132));
            g2.fillOval(x + 8, y + 28, 4, 4);

            g2.setColor(new Color(22, 40, 68));
            g2.fillRect(x + 3, y + 38, 5, 15);
            g2.fillRect(x + 10, y + 38, 5, 15);
            g2.setColor(new Color(17, 31, 53));
            g2.fillRect(x - 2, y + 21, 4, 13);
            g2.fillRect(x + 16, y + 21, 4, 13);

            g2.setColor(new Color(225, 185, 145));
            g2.fillOval(x + 3, y + 1, 12, 14);
            g2.setColor(new Color(20, 28, 46));
            g2.fillRoundRect(x + 2, y - 2, 14, 6, 4, 4);
            g2.setColor(new Color(16, 26, 44));
            g2.fillRect(x + 19, y + 24, 3, 17);

        }
    }

    private void drawDesk(Graphics2D g2, int x, int y) {
        deskQueueTargetX = x + 94;

        drawReceptionist(g2, x + 118, y - 8);

        g2.setColor(new Color(0, 0, 0, 45));
        g2.fillRoundRect(x + 5, y + 8, 188, 78, 12, 12);
        g2.setColor(new Color(145, 134, 118, 120));
        g2.fillRoundRect(x - 8, y + 66, 210, 22, 12, 12);

        g2.setPaint(new GradientPaint(x, y, new Color(148, 100, 66), x, y + 78, new Color(102, 66, 46)));
        g2.fillRoundRect(x, y, 188, 78, 10, 10);
        g2.setColor(new Color(184, 132, 90));
        g2.fillRoundRect(x - 5, y, 198, 14, 8, 8);
        g2.setColor(new Color(98, 70, 50));
        g2.drawRoundRect(x, y, 188, 78, 10, 10);

        g2.setColor(new Color(44, 48, 58));
        g2.fillRoundRect(x + 72, y - 22, 42, 20, 4, 4);
        g2.setColor(new Color(110, 170, 225));
        g2.fillRoundRect(x + 75, y - 19, 36, 14, 3, 3);
        g2.setColor(new Color(70, 70, 78));
        g2.fillRect(x + 90, y - 2, 5, 10);
        g2.setColor(new Color(240, 224, 170, 130));
        g2.fillOval(x + 105, y - 20, 18, 12);

        g2.setColor(new Color(76, 56, 44));
        g2.fillRoundRect(x + 12, y + 16, 82, 34, 7, 7);
        g2.setColor(new Color(228, 205, 152));
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.drawString("RECEPTION", x + 16, y + 36);

        g2.setColor(new Color(96, 84, 74));
        g2.fillRect(x + 176, y + 34, 4, 28);
        g2.fillRect(x + 206, y + 30, 4, 30);
        g2.setColor(new Color(138, 34, 34));
        g2.drawLine(x + 179, y + 42, x + 208, y + 38);
        g2.drawLine(x + 179, y + 48, x + 208, y + 44);
    }

    private void drawReceptionist(Graphics2D g2, int x, int y) {
        int bob = (int) Math.round(Math.sin(frameCount * 0.14) * 2.0);
        int armWave = (int) Math.round(Math.sin(frameCount * 0.18) * 4.0);
        boolean blink = (frameCount % 120) > 108;
        int drawY = y + bob;

        g2.setColor(new Color(0, 0, 0, 30));
        g2.fillOval(x + 0, drawY + 44, 28, 7);

        g2.setColor(new Color(236, 198, 164));
        g2.fillRoundRect(x + 2, drawY + 20 + armWave, 4, 12, 3, 3);
        g2.fillRoundRect(x + 20, drawY + 20 - armWave, 4, 12, 3, 3);

        g2.setColor(new Color(200, 78, 78));
        g2.fillRoundRect(x + 5, drawY + 17, 16, 23, 7, 7);
        g2.setColor(new Color(176, 62, 62));
        g2.drawLine(x + 13, drawY + 18, x + 13, drawY + 39);
        g2.setColor(new Color(120, 44, 44));
        g2.drawLine(x + 8, drawY + 22, x + 12, drawY + 27);
        g2.drawLine(x + 18, drawY + 22, x + 14, drawY + 27);

        g2.setColor(new Color(232, 194, 156));
        g2.fillOval(x + 6, drawY + 3, 14, 17);
        g2.setColor(new Color(210, 174, 140));
        g2.drawOval(x + 6, drawY + 3, 14, 17);
        g2.setColor(new Color(224, 186, 148));
        g2.fillOval(x + 11, drawY + 11, 2, 3);
        g2.setColor(new Color(204, 166, 132));
        g2.drawLine(x + 6, drawY + 10, x + 5, drawY + 12);
        g2.drawLine(x + 20, drawY + 10, x + 21, drawY + 12);

        g2.setColor(new Color(60, 44, 34));
        g2.fillArc(x + 5, drawY - 1, 16, 11, 0, 180);
        g2.fillRoundRect(x + 4, drawY + 4, 2, 8, 2, 2);
        g2.fillRoundRect(x + 19, drawY + 4, 2, 8, 2, 2);
        g2.fillRoundRect(x + 8, drawY + 1, 9, 2, 2, 2);

        g2.setColor(new Color(50, 50, 50));
        if (blink) {
            g2.drawLine(x + 9, drawY + 10, x + 11, drawY + 10);
            g2.drawLine(x + 14, drawY + 10, x + 16, drawY + 10);
        } else {
            g2.fillOval(x + 9, drawY + 9, 2, 2);
            g2.fillOval(x + 14, drawY + 9, 2, 2);
        }
        g2.setColor(new Color(168, 92, 92));
        g2.drawArc(x + 10, drawY + 12, 6, 4, -180, -180);
    }
}

class AnimatedCustomer {
    private ReceptionPanel parent;
    private Customer customer;
    private int roomNumber;
    private float x, y;
    private int laneOffset;
    private Color shirtColor;
    private float speed;
    CustomerRequest linkedRequest; 

    private int state = 0;
    private float alpha = 1.0f; 

    public AnimatedCustomer(ReceptionPanel parent, Customer customer, int roomNumber, CustomerRequest request) {
        this.parent = parent;
        this.customer = customer;
        this.roomNumber = roomNumber;
        this.linkedRequest = request;
        this.x = -50 - parent.random.nextInt(80);
        this.laneOffset = parent.random.nextInt(18) - 9;
        this.y = parent.getSceneWalkY() + laneOffset;
        this.speed = 2.5f + parent.random.nextFloat() * 1.5f;
        this.shirtColor = new Color(
                80 + parent.random.nextInt(176),
                80 + parent.random.nextInt(176),
                80 + parent.random.nextInt(176));
    }

    public AnimatedCustomer(ReceptionPanel parent, Customer customer, int roomNumber) {
        this.parent = parent;
        this.customer = customer;
        this.roomNumber = roomNumber;
        this.linkedRequest = null;
        this.x = parent.getHotelDoorTargetX() + parent.random.nextInt(24);
        this.laneOffset = parent.random.nextInt(18) - 9;
        this.y = parent.getSceneWalkY() + laneOffset;
        this.speed = 2.4f + parent.random.nextFloat() * 1.2f;
        this.shirtColor = new Color(
                80 + parent.random.nextInt(176),
                80 + parent.random.nextInt(176),
                80 + parent.random.nextInt(176));
        this.state = 5;
    }

    
    public void accept() {
        state = 2;
    }

    
    public void reject() {
        state = 3;
    }

    
    public boolean isWaiting() {
        return state == 1;
    }

    public void update() {
        y = parent.getSceneWalkY() + laneOffset;
        switch (state) {
            case 0: 
                x += speed;
                if (x >= parent.getDeskQueueTargetX())
                    state = 1; 
                break;
            case 1: 
                if (!parent.pendingRequests.contains(linkedRequest))
                    state = 3;
                break;
            case 2: 
                x += speed + 1;
                if (x >= parent.getHotelDoorTargetX())
                    state = 4; 
                break;
            case 3: 
                x -= speed;
                break;
            case 4: 
                alpha -= 0.03f;
                break;
            case 5: 
                x += speed + 0.8f;
                break;
        }
    }

    public boolean isFinished() {
        return (state == 3 && x < -110)
                || (state == 4 && alpha <= 0)
                || (state == 5 && x > parent.getSceneWidth() + 120);
    }

    public void draw(Graphics2D g2) {
        int ix = (int) x, iy = (int) y;
        boolean moving = state == 0 || state == 2 || state == 3 || state == 5;

        Composite originalComposite = g2.getComposite();
        if (state == 4) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
        }

        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(ix + 4, iy + 50, 26, 8);

        int leg = moving ? ((parent.frameCount / 8) % 2 == 0 ? 3 : -3) : 0;
        g2.setColor(new Color(50, 50, 80));
        g2.fillRect(ix + 8, iy + 32, 5, 18 + leg);
        g2.fillRect(ix + 17, iy + 32, 5, 18 - leg);

        g2.setColor(new Color(30, 30, 40));
        g2.fillOval(ix + 7, iy + 48, 7, 3);
        g2.fillOval(ix + 17, iy + 48, 7, 3);

        g2.setColor(shirtColor);
        g2.fillRoundRect(ix + 6, iy + 15, 18, 18, 6, 6);

        int arm = moving ? ((parent.frameCount / 8) % 2 == 0 ? 4 : -4) : 0;
        g2.setColor(shirtColor.darker());
        g2.fillRect(ix + 2, iy + 18 + arm, 4, 12);
        g2.fillRect(ix + 24, iy + 18 - arm, 4, 12);

        g2.setColor(new Color(220, 180, 140));
        g2.fillOval(ix + 8, iy + 2, 14, 16);

        g2.setColor(new Color(60, 40, 30));
        g2.fillArc(ix + 8, iy, 14, 10, 0, 180);

        g2.setColor(Color.BLACK);
        g2.fillOval(ix + 10, iy + 7, 2, 2);
        g2.fillOval(ix + 17, iy + 7, 2, 2);

        if (state == 3) {
            g2.drawArc(ix + 11, iy + 11, 7, 5, 0, 180);
        } else {
            g2.drawArc(ix + 11, iy + 9, 7, 5, -180, -180);
        }

        if (state == 2 || state == 0 || state == 5) {
            g2.setColor(new Color(100, 70, 50));
            g2.fillRect(ix - 6, iy + 23, 8, 12);
            g2.setColor(new Color(200, 200, 200));
            g2.fillRect(ix - 5, iy + 25, 6, 2);
        }

        if (state == 1) {
            String firstName = customer.getName().split(" ")[0];
            int bw = firstName.length() * 7 + 10;
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(ix - 5, iy - 20, bw, 15, 6, 6);
            g2.setColor(new Color(50, 50, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString(firstName, ix - 2, iy - 8);

            g2.setColor(new Color(150, 150, 150));
            int dot = (parent.frameCount / 15) % 3;
            for (int d = 0; d <= dot; d++) {
                g2.fillOval(ix + bw - 4 + d * 5, iy - 14, 3, 3);
            }
        }

        g2.setComposite(originalComposite);
    }
}

class RequestsTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel listPanel;
    private JButton acceptAllBtn, rejectAllBtn;

    RequestsTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        JLabel title = new JLabel("Booking Requests");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        acceptAllBtn = new JButton("Accept All");
        rejectAllBtn = new JButton("Reject All");

        for (JButton btn : new JButton[] { acceptAllBtn, rejectAllBtn }) {
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(100, 28));
        }

        acceptAllBtn.setBackground(new Color(80, 200, 120));
        rejectAllBtn.setBackground(new Color(220, 100, 100));

        acceptAllBtn.addActionListener(e -> acceptAllRequests());
        rejectAllBtn.addActionListener(e -> rejectAllRequests());

        buttonPanel.add(acceptAllBtn);
        buttonPanel.add(rejectAllBtn);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel hint = new JLabel("Requests expire in 30s | Scroll to see all");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        add(headerPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);
    }

    void addRequest(CustomerRequest r) {
        refresh();
    }

    void refresh() {
        listPanel.removeAll();
        if (parent.pendingRequests.isEmpty()) {
            JLabel e = new JLabel("No pending requests");
            e.setFont(new Font("SansSerif", Font.ITALIC, 14));
            e.setForeground(Color.GRAY);
            e.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(e);
        } else {
            for (CustomerRequest req : new ArrayList<>(parent.pendingRequests)) {
                listPanel.add(buildCard(req));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();

        boolean hasRequests = !parent.pendingRequests.isEmpty();
        acceptAllBtn.setEnabled(hasRequests);
        rejectAllBtn.setEnabled(hasRequests);
    }

    private JPanel buildCard(CustomerRequest req) {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setOpaque(true);
        card.setBackground(new Color(255, 250, 235));
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 180, 120), 2),
                new EmptyBorder(12, 16, 12, 16)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel info = new JPanel(new GridLayout(5, 1, 0, 4));
        info.setOpaque(false);

        JLabel nameLbl = new JLabel(req.customer.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel roomLbl = new JLabel("Room " + req.room.getRoomNumber() + " ("
                + req.room.getCondition().toString().replace("_", " ") + ")");
        JLabel nightLbl = new JLabel(req.nights + " night(s)");
        JLabel priceLbl = new JLabel("$" + req.price);
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceLbl.setForeground(new Color(0, 130, 0));

        int secs = req.getSecondsRemaining();
        JLabel timeLbl = new JLabel(secs + "s remaining");
        timeLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        timeLbl.setForeground(secs <= 10 ? Color.RED : new Color(180, 100, 0));

        info.add(nameLbl);
        info.add(roomLbl);
        info.add(nightLbl);
        info.add(priceLbl);
        info.add(timeLbl);

        JPanel btns = new JPanel(new GridLayout(2, 1, 0, 8));
        btns.setOpaque(false);
        JButton acc = new JButton("ACCEPT");
        JButton rej = new JButton("REJECT");
        for (JButton b : new JButton[] { acc, rej }) {
            b.setFocusPainted(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 12));
            b.setForeground(Color.WHITE);
            b.setPreferredSize(new Dimension(105, 32));
        }
        acc.setBackground(new Color(80, 200, 120));
        rej.setBackground(new Color(220, 100, 100));
        acc.addActionListener(e -> {
            parent.acceptRequest(req);
            refresh();
        });
        rej.addActionListener(e -> {
            parent.rejectRequest(req);
            refresh();
        });
        btns.add(acc);
        btns.add(rej);

        card.add(info, BorderLayout.CENTER);
        card.add(btns, BorderLayout.EAST);
        return card;
    }

    private void acceptAllRequests() {
        if (parent.pendingRequests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending requests to accept!");
            return;
        }

        int accepted = 0;
        for (CustomerRequest req : new ArrayList<>(parent.pendingRequests)) {
            parent.acceptRequest(req);
            accepted++;
        }

        parent.historyTab.addLog("Accepted all " + accepted + " pending requests");
        JOptionPane.showMessageDialog(this, "Accepted " + accepted + " requests!");
        refresh();
    }

    private void rejectAllRequests() {
        if (parent.pendingRequests.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No pending requests to reject!");
            return;
        }

        int rejected = 0;
        for (CustomerRequest req : new ArrayList<>(parent.pendingRequests)) {
            parent.rejectRequest(req);
            rejected++;
        }

        parent.historyTab.addLog("Rejected all " + rejected + " pending requests");
        JOptionPane.showMessageDialog(this, "Rejected " + rejected + " requests!");
        refresh();
    }
}

class GuestsTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel listPanel;

    GuestsTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        JLabel title = new JLabel("Current Guests");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JLabel hint = new JLabel("Guests check out automatically when stay ends");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(hint, BorderLayout.SOUTH);
    }

    void refresh() {
        listPanel.removeAll();
        boolean any = false;
        for (Room room : parent.hotel.getRooms()) {
            if (!room.isOccupied())
                continue;
            any = true;
            Customer g = room.getCurrentGuest();

            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setOpaque(true);
            card.setBackground(new Color(240, 255, 245));
            card.setBorder(new CompoundBorder(
                    new LineBorder(new Color(120, 200, 120), 2),
                    new EmptyBorder(10, 14, 10, 14)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

            JLabel nameLbl = new JLabel(g.getName());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel detLbl = new JLabel("Room " + room.getRoomNumber() +
                    "  |  " + room.getNightsBooked() + " nights left  |  " + g.getSatisfactionLevel() + "%");
            detLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

            JPanel txt = new JPanel(new GridLayout(2, 1, 0, 2));
            txt.setOpaque(false);
            txt.add(nameLbl);
            txt.add(detLbl);
            card.add(txt, BorderLayout.CENTER);
            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(8));
        }
        if (!any) {
            JLabel e = new JLabel("No guests currently checked in");
            e.setFont(new Font("SansSerif", Font.ITALIC, 14));
            e.setForeground(Color.GRAY);
            e.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(e);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}

class RoomTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel roomGrid;
    private JButton autoUpdateBtn;
    private ArrayList<Integer> selectedRooms = new ArrayList<>();
    private JButton selectAllBtn, clearSelectionBtn;

    RoomTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        JLabel title = new JLabel("Rooms");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        selectAllBtn = new JButton("Select All");
        clearSelectionBtn = new JButton("Clear");
        selectAllBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        clearSelectionBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        selectAllBtn.setBackground(new Color(150, 200, 150));
        clearSelectionBtn.setBackground(new Color(200, 150, 150));
        selectAllBtn.setForeground(Color.WHITE);
        clearSelectionBtn.setForeground(Color.WHITE);
        selectAllBtn.setFocusPainted(false);
        clearSelectionBtn.setFocusPainted(false);
        selectAllBtn.addActionListener(e -> selectAllRooms());
        clearSelectionBtn.addActionListener(e -> clearSelection());

        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        selectionPanel.setOpaque(false);
        selectionPanel.add(new JLabel("Select rooms to upgrade:"));
        selectionPanel.add(selectAllBtn);
        selectionPanel.add(clearSelectionBtn);

        autoUpdateBtn = new JButton("Upgrade Selected ($10,000 min)");
        autoUpdateBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        autoUpdateBtn.setBackground(new Color(100, 150, 255));
        autoUpdateBtn.setForeground(Color.WHITE);
        autoUpdateBtn.setFocusPainted(false);
        autoUpdateBtn.setEnabled(false);
        autoUpdateBtn.addActionListener(e -> autoUpdateSelectedRooms());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(autoUpdateBtn, BorderLayout.EAST);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(selectionPanel, BorderLayout.SOUTH);

        roomGrid = new JPanel(new GridLayout(0, 2, 10, 10)); 
        roomGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(roomGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("Click rooms for upgrade options | Select multiple for bulk upgrade");
        hint.setFont(new Font("SansSerif", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        add(hint, BorderLayout.SOUTH);
    }

    void refresh() {
        roomGrid.removeAll();
        for (Room room : parent.hotel.getRooms()) {
            final int rn = room.getRoomNumber();
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(155, 105));
            btn.setFocusPainted(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            boolean isSelected = selectedRooms.contains(rn);
            Color baseColor = getColor(room.getCondition());
            if (isSelected) {
                baseColor = new Color(
                        Math.min(255, baseColor.getRed() + 50),
                        Math.min(255, baseColor.getGreen() + 50),
                        Math.min(255, baseColor.getBlue() + 50));
            }

            btn.setBackground(baseColor);
            btn.setText("<html><div style='text-align:center;padding:3px'>" +
                    "<b style='font-size:13px'>Room " + rn + "</b><br>" +
                    "<span style='font-size:11px'>" + (room.isOccupied() ? "OCCUPIED" : "AVAILABLE")
                    + "</span><br>" +
                    "<span style='font-size:10px;color:#555'>" + room.getCondition().toString().replace("_", " ")
                    + "</span><br>" +
                    "<b style='color:#007700;font-size:12px'>$" + room.getBasePrice() + "/night</b>" +
                    (isSelected ? "<br><span style='color:#ff6600;font-size:10px'>SELECTED</span>"
                            : "<br><span style='color:#666;font-size:9px'>Click for options</span>")
                    +
                    "</div></html>");
            btn.addActionListener(e -> handleRoomClick(rn));
            roomGrid.add(btn);
        }
        roomGrid.revalidate();
        roomGrid.repaint();

        autoUpdateBtn.setEnabled(parent.hotel.getMoney() >= 10000 && !selectedRooms.isEmpty());
        autoUpdateBtn.setText("Upgrade Selected (" + selectedRooms.size() + " rooms, $10,000 min)");
    }

    private Color getColor(RoomCondition c) {
        switch (c) {
            case BROKEN:
                return new Color(240, 130, 130);
            case VERY_POOR:
                return new Color(250, 180, 130);
            case POOR:
                return new Color(255, 235, 180);
            case GOOD:
                return new Color(210, 250, 210);
            case EXCELLENT:
                return new Color(180, 240, 180);
            default:
                return Color.LIGHT_GRAY;
        }
    }

    private void handleRoomClick(int rn) {
        Room room = parent.hotel.getRooms().get(rn - 1);

        String[] options = { "Select/Deselect for Bulk Upgrade", "Upgrade Individually", "View Room Info" };
        int choice = JOptionPane.showOptionDialog(this,
                "Room " + rn + " - " + room.getCondition() + " condition\n" +
                        (room.isOccupied() ? "Currently occupied" : "Available for upgrade"),
                "Room Options",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0: 
                if (selectedRooms.contains(rn)) {
                    selectedRooms.remove(Integer.valueOf(rn));
                } else {
                    selectedRooms.add(rn);
                }
                refresh();
                break;

            case 1: 
                upgradeRoomIndividually(rn);
                break;

            case 2: 
            default:
                JOptionPane.showMessageDialog(this,
                        "Room " + rn + "\nCondition: " + room.getCondition() +
                                "\nStatus: "
                                + (room.isOccupied() ? "Occupied - " + room.getNightsBooked() + " nights left"
                                        : "Available"),
                        "Room Info", JOptionPane.INFORMATION_MESSAGE);
                break;
        }
    }

    private void upgradeRoomIndividually(int rn) {
        Room room = parent.hotel.getRooms().get(rn - 1);

        if (room.getCondition() == RoomCondition.EXCELLENT) {
            JOptionPane.showMessageDialog(this, "Room " + rn + " is already in EXCELLENT condition!");
            return;
        }

        switch (room.getCondition()) {
            case BROKEN:
                if (confirm("Repair Room " + rn + " for $200?"))
                    if (parent.hotel.repairRoom(rn, 200)) {
                        parent.historyTab.addLog("Repaired Room " + rn + " ($200)");
                        parent.refreshAllTabs();
                    } else
                        JOptionPane.showMessageDialog(this, "Not enough money!");
                break;
            case VERY_POOR:
                if (confirm("Repair Room " + rn + " for $100?"))
                    if (parent.hotel.repairRoom(rn, 100)) {
                        parent.historyTab.addLog("Repaired Room " + rn + " ($100)");
                        parent.refreshAllTabs();
                    } else
                        JOptionPane.showMessageDialog(this, "Not enough money!");
                break;
            case POOR:
                if (confirm("Upgrade Room " + rn + " for $150?"))
                    if (parent.hotel.repairRoom(rn, 150)) {
                        parent.historyTab.addLog("Upgraded Room " + rn + " ($150)");
                        parent.refreshAllTabs();
                    } else
                        JOptionPane.showMessageDialog(this, "Not enough money!");
                break;
            default:
                JOptionPane.showMessageDialog(this, "Room " + rn + " cannot be upgraded further!");
        }
    }

    private void selectAllRooms() {
        selectedRooms.clear();
        for (Room room : parent.hotel.getRooms()) {
            if (room.getCondition() != RoomCondition.EXCELLENT) {
                selectedRooms.add(room.getRoomNumber());
            }
        }
        refresh();
    }

    private void clearSelection() {
        selectedRooms.clear();
        refresh();
    }

    private void autoUpdateSelectedRooms() {
        if (selectedRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No rooms selected!");
            return;
        }

        if (parent.hotel.getMoney() < 10000) {
            JOptionPane.showMessageDialog(this, "You need at least $10,000 to upgrade rooms!");
            return;
        }

        int roomsUpdated = 0;
        int totalCost = 0;

        for (Integer roomNum : new ArrayList<>(selectedRooms)) {
            Room room = parent.hotel.getRooms().get(roomNum - 1);
            if (room.getCondition() != RoomCondition.EXCELLENT) {
                int cost = 0;
                switch (room.getCondition()) {
                    case BROKEN:
                        cost = 500; 
                        break;
                    case VERY_POOR:
                        cost = 400; 
                        break;
                    case POOR:
                        cost = 300; 
                        break;
                    case GOOD:
                        cost = 200; 
                        break;
                }

                if (cost > 0 && parent.hotel.spendMoney(cost)) {
                    room.setCondition(RoomCondition.EXCELLENT);
                    roomsUpdated++;
                    totalCost += cost;
                }
            }
        }

        selectedRooms.clear(); 

        if (roomsUpdated > 0) {
            parent.historyTab.addLog(roomsUpdated + " selected rooms upgraded to EXCELLENT ($" + totalCost + ")");
            JOptionPane.showMessageDialog(this, "Successfully upgraded " + roomsUpdated +
                    " rooms to EXCELLENT condition!\nTotal cost: $" + totalCost);
            parent.refreshAllTabs();
        } else {
            JOptionPane.showMessageDialog(this, "Selected rooms are already in EXCELLENT condition!");
            refresh();
        }
    }

    private boolean confirm(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}

class EmployeeTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel listPanel;

    EmployeeTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        JLabel title = new JLabel("Your Team");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton hire = new JButton("Hire Employee");
        hire.setFocusPainted(false);
        hire.setFont(new Font("SansSerif", Font.BOLD, 14));
        hire.setBackground(new Color(70, 130, 180));
        hire.setForeground(Color.WHITE);
        hire.setPreferredSize(new Dimension(180, 38));
        hire.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hire.addActionListener(e -> showHireDialog());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(0, 0, 8, 0));
        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(hire, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    void refresh() {
        listPanel.removeAll();
        for (Employee emp : parent.hotel.getEmployees()) {
            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setOpaque(true);
            card.setBackground(new Color(250, 250, 255));
            card.setBorder(new CompoundBorder(
                    new LineBorder(new Color(150, 150, 200), 2),
                    new EmptyBorder(12, 16, 12, 16)));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            JLabel nameLbl = new JLabel(emp.getName());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            JLabel detLbl = new JLabel(emp.getType().toUpperCase() + "  |  $" + emp.getDailySalary()
                    + "/day  |  " + emp.getEfficiency() + "%");
            detLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));

            JPanel txt = new JPanel(new GridLayout(2, 1, 0, 3));
            txt.setOpaque(false);
            txt.add(nameLbl);
            txt.add(detLbl);
            card.add(txt, BorderLayout.CENTER);
            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(8));
        }
        if (parent.hotel.getEmployees().isEmpty()) {
            JLabel e = new JLabel("No employees hired yet");
            e.setFont(new Font("SansSerif", Font.ITALIC, 14));
            e.setForeground(Color.GRAY);
            e.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalGlue());
            listPanel.add(e);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void showHireDialog() {
        String[][] roster = {
                { "Ayesha", "cleaner", "25", "3", "Cleaner", "Keeps rooms spotless" },
                { "Bilal", "cleaner", "25", "3", "Cleaner", "Fast and reliable" },
                { "Zara", "concierge", "40", "8", "Concierge", "Boosts guest satisfaction" },
                { "Omar", "security", "35", "5", "Security", "Keeps hotel safe" },
                { "Kamran", "chef", "45", "10", "Chef", "Great food = happy guests" },
                { "Sana", "receptionist", "30", "6", "Receptionist", "Handles bookings efficiently" },
                { "Tariq", "maintenance", "35", "4", "Maintenance", "Repairs rooms faster" },
                { "Nadia", "manager", "55", "12", "Manager", "Boosts overall efficiency" },
        };

        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Hire Employee", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(420, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JLabel title = new JLabel("Select an Employee to Hire", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(15, 10, 5, 10));
        title.setOpaque(true);
        title.setBackground(new Color(45, 45, 55));
        title.setForeground(Color.WHITE);
        dialog.add(title, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(248, 248, 252));
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        ArrayList<String> hiredNames = new ArrayList<>();
        for (Employee e : parent.hotel.getEmployees())
            hiredNames.add(e.getName());

        final int[] selectedIndex = { -1 };
        ArrayList<JPanel> cards = new ArrayList<>();

        for (int i = 0; i < roster.length; i++) {
            final int idx = i;
            String[] emp = roster[i];
            boolean alreadyHired = hiredNames.contains(emp[0]);

            JPanel card = new JPanel(new BorderLayout(10, 0));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            card.setBackground(alreadyHired ? new Color(220, 220, 220) : Color.WHITE);
            card.setBorder(new CompoundBorder(
                    new LineBorder(new Color(200, 200, 210), 1),
                    new EmptyBorder(10, 14, 10, 14)));
            card.setCursor(alreadyHired ? Cursor.getDefaultCursor() : new Cursor(Cursor.HAND_CURSOR));

            JPanel textPanel = new JPanel(new GridLayout(3, 1, 0, 2));
            textPanel.setOpaque(false);

            JLabel nameLbl = new JLabel(emp[0]);
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            nameLbl.setForeground(alreadyHired ? Color.GRAY : new Color(30, 30, 60));

            JLabel roleLbl = new JLabel(emp[4]);
            roleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
            roleLbl.setForeground(new Color(80, 80, 120));

            JLabel descLbl = new JLabel(emp[5]);
            descLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
            descLbl.setForeground(Color.GRAY);

            textPanel.add(nameLbl);
            textPanel.add(roleLbl);
            textPanel.add(descLbl);

            JPanel badgePanel = new JPanel(new GridLayout(2, 1, 0, 4));
            badgePanel.setOpaque(false);
            badgePanel.setPreferredSize(new Dimension(95, 50));

            JLabel salaryLbl = new JLabel("$" + emp[2] + "/day", SwingConstants.CENTER);
            salaryLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            salaryLbl.setForeground(new Color(0, 130, 0));

            JLabel repLbl = new JLabel("+" + emp[3] + " rep", SwingConstants.CENTER);
            repLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            repLbl.setForeground(new Color(180, 120, 0));

            badgePanel.add(salaryLbl);
            badgePanel.add(repLbl);

            if (alreadyHired) {
                JLabel hiredLbl = new JLabel("Hired", SwingConstants.CENTER);
                hiredLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                hiredLbl.setForeground(new Color(100, 150, 100));
                badgePanel.add(hiredLbl);
            }

            card.add(textPanel, BorderLayout.CENTER);
            card.add(badgePanel, BorderLayout.EAST);

            if (!alreadyHired) {
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        for (JPanel c : cards) {
                            c.setBackground(Color.WHITE);
                            c.setBorder(new CompoundBorder(
                                    new LineBorder(new Color(200, 200, 210), 1),
                                    new EmptyBorder(10, 14, 10, 14)));
                        }
                        card.setBackground(new Color(220, 235, 255));
                        card.setBorder(new CompoundBorder(
                                new LineBorder(new Color(70, 130, 180), 2),
                                new EmptyBorder(10, 14, 10, 14)));
                        selectedIndex[0] = idx;
                    }

                    public void mouseEntered(MouseEvent e) {
                        if (selectedIndex[0] != idx)
                            card.setBackground(new Color(240, 245, 255));
                    }

                    public void mouseExited(MouseEvent e) {
                        if (selectedIndex[0] != idx)
                            card.setBackground(Color.WHITE);
                    }
                });
            }

            cards.add(card);
            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(6));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(45, 45, 55));

        JButton hireBtn = new JButton("Hire");
        JButton cancelBtn = new JButton("Cancel");

        for (JButton b : new JButton[] { hireBtn, cancelBtn }) {
            b.setFocusPainted(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 13));
            b.setForeground(Color.WHITE);
            b.setPreferredSize(new Dimension(110, 36));
            b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        hireBtn.setBackground(new Color(70, 160, 100));
        cancelBtn.setBackground(new Color(180, 80, 80));

        hireBtn.addActionListener(e -> {
            if (selectedIndex[0] == -1) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select an employee first.",
                        "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String[] emp = roster[selectedIndex[0]];
            String name = emp[0];
            String type = emp[1];
            int salary = Integer.parseInt(emp[2]);
            int repBoost = Integer.parseInt(emp[3]);

            parent.hotel.hireEmployee(new Employee(name, type, salary));
            parent.hotel.changeReputation(repBoost);
            parent.historyTab.addLog("Hired " + name + " as " +
                    type.toUpperCase() + " ($" + salary + "/day) | Rep +" + repBoost);
            parent.refreshAllTabs();
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        bottomPanel.add(hireBtn);
        bottomPanel.add(cancelBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}

class HistoryTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JTextArea historyArea;

    HistoryTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setOpaque(false);
        JLabel title = new JLabel("Activity History");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setBackground(new Color(250, 250, 250));

        JScrollPane scroll = new JScrollPane(historyArea);
        scroll.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(title, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        addLog("=== " + parent.hotel.getName() + " Management System ===");
        addLog("Welcome! Accept guests to grow your hotel.");
        addLog("");
    }

    void addLog(String msg) {
        if (msg == null) {
            return;
        }
        String clean = msg.replaceAll("\\s+", " ").trim();
        if (clean.isEmpty()) {
            historyArea.append("\n");
            historyArea.setCaretPosition(historyArea.getDocument().getLength());
            return;
        }

        String category = detectCategory(clean);
        if (clean.startsWith("[CHAPTER]")) {
            clean = clean.substring("[CHAPTER]".length()).trim();
        }

        String line = String.format("Day %02d %02d:%02d | %-8s | %s",
                parent.hotel.getDay(),
                parent.getGameHour(),
                parent.getGameMinute(),
                category,
                clean);
        historyArea.append(line + "\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    private String detectCategory(String clean) {
        String lower = clean.toLowerCase();
        if (clean.startsWith("[CHAPTER]") || lower.contains("chapter")) {
            return "CHAPTER";
        }
        if (lower.contains("hired") || lower.contains("employee") || lower.contains("staff")) {
            return "STAFF";
        }
        if (lower.contains("request") || lower.contains("accepted") || lower.contains("rejected")
                || lower.contains("expired")) {
            return "GUEST";
        }
        if (lower.contains("repaired") || lower.contains("upgraded") || lower.contains("room")) {
            return "ROOM";
        }
        if (lower.contains("money") || lower.contains("expenses") || lower.contains("$")) {
            return "FINANCE";
        }
        if (lower.contains("story") || lower.contains("rival") || lower.contains("ghost")
                || lower.contains("treasure") || lower.contains("legacy")) {
            return "STORY";
        }
        return "SYSTEM";
    }

    void refresh() {
    }
}

class CustomerRequest {
    Customer customer;
    Room room;
    int nights, price;
    int framesLived = 0;

    CustomerRequest(Customer c, Room r, int n, int p) {
        customer = c;
        room = r;
        nights = n;
        price = p;
    }

    int getSecondsRemaining() {
        return Math.max(0, 30 - (framesLived / 30));
    }
}

