
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

    // Time: 60 real seconds = 24 game hours
    private int secondsElapsed = 0;
    private final int SECONDS_PER_DAY = 120;
    private int gameHour = 0;
    private int gameMinute = 0;

    // Animation
    ArrayList<AnimatedCustomer> walkingCustomers = new ArrayList<>();
    ArrayList<CustomerRequest> pendingRequests = new ArrayList<>();
    public int frameCount = 0;
    private final int REQUEST_DURATION_FRAMES = 900; // 30 seconds × 30fps

    // UI
    private ReceptionBackgroundPanel backgroundPanel;
    private JLabel dayLabel, moneyLabel, reputationLabel, timeLabel;

    // Toggleable tabs (null = none open)
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

    // Story
    private int currentChapter = 1;
    private boolean chapter1Complete = false;
    private boolean chapter2Complete = false;
    private boolean chapter3Complete = false;
    private boolean chapter4Complete = false;
    private boolean chapter5Complete = false;
    private boolean chapter6Complete = false;

    public ReceptionPanel(JFrame parent, Hotel loadedHotel) {
        this.parentFrame = parent;
        if (loadedHotel == null) {
            this.hotel = new Hotel("Hotel Taj");
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
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(240, 235, 220));
        initializeUI();
        startAnimation();
        startDayTimer();
        SwingUtilities.invokeLater(() -> showChapterIntro(currentChapter));
    }

    // ==================== UI INIT ====================

    private void initializeUI() {
        add(createStatsPanel(), BorderLayout.NORTH);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 600));

        backgroundPanel = new ReceptionBackgroundPanel();
        backgroundPanel.setBounds(0, 0, 900, 600);
        layeredPane.add(backgroundPanel, JLayeredPane.DEFAULT_LAYER);

        JPanel tabBar = createModernTabBar();
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
        contentPanel.setVisible(false); // starts hidden - no tab open

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
        add(layeredPane, BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);

        // Load history logs
        for (String log : hotel.getHistoryLogs()) {
            historyTab.addLog(log);
        }

        refreshAllTabs();
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
        requestsTabBtn = createTabButton("📩 Requests", "requests");
        guestsTabBtn = createTabButton("🏨 Guests", "guests");
        roomsTabBtn = createTabButton("🏠 Rooms", "rooms");
        employeesTabBtn = createTabButton("💼 Staff", "employees");
        historyTabBtn = createTabButton("📜 History", "history");
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

    /** Click active tab → closes. Click another → opens it. */
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
        JButton storyBtn = new JButton("📖 Story Event");
        JButton saveBtn = new JButton("💾 Save Game");
        JButton menuBtn = new JButton("🏠 Main Menu");
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
            File savesDir = new File("saves");
            if (!savesDir.exists())
                savesDir.mkdir();
            File saveFile = new File(savesDir, name + ".ser");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile));
            oos.writeObject(hotel);
            oos.close();
            JOptionPane.showMessageDialog(this, "Game saved as " + name);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Save failed: " + e.getMessage());
        }
    }

    // ==================== TIME: 60 real seconds = 24 game hours
    // ====================

    private void startDayTimer() {
        dayTimer = new Timer(1000, e -> {
            secondsElapsed++;
            // 60 real seconds → 1440 game minutes
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
        // Auto-checkout guests whose nights are done
        for (Room room : hotel.getRooms()) {
            if (room.isOccupied() && room.decrementNight()) {
                Customer guest = room.checkOut();
                hotel.changeReputation(guest.getReputationImpact());
                historyTab.addLog("← " + guest.getName() + " auto-checked out | Sat: "
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

    // ==================== CHAPTER ROOM DOUBLING ====================

    private void onChapterComplete(int ch) {
        int toAdd = hotel.getRooms().size(); // double = add equal amount
        hotel.addRooms(toAdd);
        historyTab.addLog("🏨 Hotel expanded! +" + toAdd + " rooms (total: " + hotel.getRooms().size() + ")");
        backgroundPanel.updateRoomLights();
        refreshAllTabs();
    }

    // ==================== GAME LOGIC ====================

    // private void generateCustomerRequest() {
    // int availableRooms = hotel.getAvailableRoomCount();
    // if (availableRooms <= 0) return;
    // // Cap pending requests to number of available rooms
    // if (pendingRequests.size() >= availableRooms) return;

    // int chance = hotel.getReputation() + 20;
    // if (random.nextInt(100) > chance) return;

    // Room available = hotel.getAvailableRoom();
    // if (available == null) return;

    // Customer guest = new Customer();
    // int nights = 1 + random.nextInt(3);
    // int price = available.getBasePrice() * nights;

    // CustomerRequest req = new CustomerRequest(guest, available, nights, price);
    // pendingRequests.add(req);
    // requestsTab.refresh();
    // historyTab.addLog("📩 New request: " + guest.getName());
    // updateTabBadge();

    // // Add animated customer walking in immediately
    // addWalkingCustomer(guest, available.getRoomNumber());
    // }
    // private void generateCustomerRequest() {
    // int availableRooms = hotel.getAvailableRoomCount();
    // if (availableRooms <= 0) return;
    // if (pendingRequests.size() >= availableRooms) return;

    // int chance = hotel.getReputation() + 20;
    // if (random.nextInt(100) > chance) return;

    // Room available = hotel.getAvailableRoom();
    // if (available == null) return;

    // Customer guest = new Customer();
    // int nights = 1 + random.nextInt(3);
    // int price = available.getBasePrice() * nights;

    // CustomerRequest req = new CustomerRequest(guest, available, nights, price);
    // pendingRequests.add(req);

    // // Spawn walker immediately — they walk to desk and WAIT there
    // addWalkingCustomer(guest, available.getRoomNumber(), req);

    // requestsTab.refresh();
    // historyTab.addLog("📩 New request: " + guest.getName());
    // updateTabBadge();
    // }

    private void generateCustomerRequest() {
        int availableRooms = hotel.getAvailableRoomCount();
        if (availableRooms <= 0)
            return;

        // Cap: never more pending requests than available rooms
        if (pendingRequests.size() >= availableRooms)
            return;

        int chance = hotel.getReputation() + 20;
        if (random.nextInt(100) > chance)
            return;

        // Collect which rooms already have a pending request
        ArrayList<Integer> roomsAlreadyRequested = new ArrayList<>();
        for (CustomerRequest req : pendingRequests) {
            roomsAlreadyRequested.add(req.room.getRoomNumber());
        }

        // Find an available room that does NOT already have a pending request
        Room available = null;
        for (Room room : hotel.getRooms()) {
            if (!room.isOccupied()
                    && room.getCondition() != RoomCondition.BROKEN
                    && !roomsAlreadyRequested.contains(room.getRoomNumber())) {
                available = room;
                break;
            }
        }

        // No eligible room found
        if (available == null)
            return;

        Customer guest = new Customer();
        int nights = 1 + random.nextInt(3);
        int price = available.getBasePrice() * nights;

        CustomerRequest req = new CustomerRequest(guest, available, nights, price);
        pendingRequests.add(req);

        addWalkingCustomer(guest, available.getRoomNumber(), req);
        requestsTab.refresh();
        historyTab.addLog("📩 New request: " + guest.getName() + " for Room " + available.getRoomNumber());
        updateTabBadge();
    }

    // public void acceptRequest(CustomerRequest request) {
    // if (!pendingRequests.contains(request)) return;
    // request.customer.setPayment(request.price);
    // request.room.checkIn(request.customer, request.nights);
    // hotel.earnMoney(request.price);

    // RoomCondition cond = request.room.getCondition();
    // if (cond == RoomCondition.GOOD) request.customer.adjustSatisfaction(20);
    // else if (cond == RoomCondition.POOR)
    // request.customer.adjustSatisfaction(-10);
    // else if (cond == RoomCondition.VERY_POOR)
    // request.customer.adjustSatisfaction(-25);

    // historyTab.addLog("✅ " + request.customer.getName() + " → Room " +
    // request.room.getRoomNumber()
    // + " | " + request.nights + " night(s) | $" + request.price);

    // pendingRequests.remove(request);
    // refreshAllTabs();
    // checkChapter1Progress();
    // }

    // public void rejectRequest(CustomerRequest request) {
    // if (!pendingRequests.contains(request)) return;
    // historyTab.addLog("❌ Rejected: " + request.customer.getName());
    // pendingRequests.remove(request);
    // refreshAllTabs();
    // }

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

        // Signal the walker for this request to head to hotel
        for (AnimatedCustomer ac : walkingCustomers) {
            if (ac.linkedRequest == request) {
                ac.accept();
                break;
            }
        }

        historyTab.addLog("✅ " + request.customer.getName() +
                " → Room " + request.room.getRoomNumber() +
                " | " + request.nights + " night(s) | $" + request.price);

        pendingRequests.remove(request);
        refreshAllTabs();
        checkChapter1Progress();
    }

    public void rejectRequest(CustomerRequest request) {
        if (!pendingRequests.contains(request))
            return;

        // Signal the walker to turn around and leave
        for (AnimatedCustomer ac : walkingCustomers) {
            if (ac.linkedRequest == request) {
                ac.reject();
                break;
            }
        }

        historyTab.addLog("❌ Rejected: " + request.customer.getName());
        pendingRequests.remove(request);
        refreshAllTabs();
    }

    private void updateTabBadge() {
        requestsTabBtn.setText(pendingRequests.isEmpty()
                ? "📩 Requests"
                : "📩 Requests (" + pendingRequests.size() + ")");
    }

    // private void addWalkingCustomer(Customer guest, int roomNumber) {
    // walkingCustomers.add(new AnimatedCustomer(guest, roomNumber));
    // }
    // AFTER:
    private void addWalkingCustomer(Customer guest, int roomNumber, CustomerRequest request) {
        walkingCustomers.add(new AnimatedCustomer(this, guest, roomNumber, request));
    }

    // ==================== STORY ====================

    private void showStoryEvent() {
        if (currentChapter == 1 && !chapter1Complete)
            showChapter1Events();
        else if (currentChapter == 2 && !chapter2Complete)
            showChapter2Events();
        else if (currentChapter == 3 && !chapter3Complete)
            showChapter3Events();
        else if (currentChapter == 4 && !chapter4Complete)
            showChapter4Events();
        else if (currentChapter == 5 && !chapter5Complete)
            showChapter5Events();
        else if (currentChapter == 6 && !chapter6Complete)
            showChapter6Events();
        else
            JOptionPane.showMessageDialog(this, "No story events available.\nKeep managing your hotel!",
                    "Story", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showChapter1Events() {
        if (!hotel.hasMetRashid()) {
            int c = JOptionPane.showConfirmDialog(this,
                    "While cleaning, you find an old man in the lobby.\n\n" +
                            "\"Hello, Arman. I'm Rashid.\nI was the manager here... long ago.\"\n\nHire Rashid? (Cost: $30/day)",
                    "Meeting Rashid", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) {
                hotel.hireEmployee(new Employee("Rashid", "manager", 30));
                hotel.setMetRashid(true);
                historyTab.addLog("★ Rashid joined as Manager!");
                refreshAllTabs();
            }
        } else if (!hotel.hasFoundOldRegister()) {
            int c = JOptionPane.showConfirmDialog(this,
                    "Rashid shows you a dusty old register.\n\nNames from the 1970s:\n• Artists • Activists • Politicians\n\nSome pages are torn out...\n\nInvestigate?",
                    "The Old Register", JOptionPane.YES_NO_OPTION);
            hotel.setFoundOldRegister(true);
            if (c == JOptionPane.YES_OPTION) {
                hotel.setExploringMystery(true);
                hotel.changeReputation(5);
                historyTab.addLog("📖 Investigating hotel's past... Reputation +5");
            }
            refreshAllTabs();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Chapter 1 Progress:\n• 3+ usable rooms\n• $300 total revenue\n• Meet Rashid ✓\n• Find old register ✓",
                    "Chapter 1", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showChapter2Events() {
        JOptionPane.showMessageDialog(this,
                "Chapter 2: The Mystery\n\nThe hotel is starting to attract attention.\nRumors of the past linger.",
                "Chapter 2", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showChapter3Events() {
        if (!hotel.hasFoundOldRegister()) {
            JOptionPane.showMessageDialog(this, "You haven't found the old register yet.", "Chapter 3",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Object[] options = { "Investigate the past", "Ignore it", "Cancel" };
        int choice = JOptionPane.showOptionDialog(this,
                "The old register reveals names from the 1970s.\nRashid is uneasy. Investigate?", "The Old Register",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            hotel.setExploringMystery(true);
            hotel.changeReputation(5);
            historyTab.addLog("📖 Investigating hotel's past... Reputation +5");
        } else if (choice == 1) {
            historyTab.addLog("📖 Ignored the old register.");
        }
        refreshAllTabs();
    }

    private void showChapter4Events() {
        if (!hotel.hasRashidDebt())
            return;
        Object[] options = { "Pay off debt ($200)", "Negotiate", "Expose lender", "Cancel" };
        int choice = JOptionPane.showOptionDialog(this, "Rashid owes $200 to a dangerous lender.\nWhat do you do?",
                "Rashid's Debt", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            if (hotel.spendMoney(200)) {
                hotel.setRashidDebt(false);
                hotel.changeReputation(5);
                historyTab.addLog("✅ Paid off Rashid's debt. Loyalty increased, reputation +5");
            } else {
                JOptionPane.showMessageDialog(this, "Not enough money!");
            }
        } else if (choice == 1) {
            if (random.nextBoolean()) {
                hotel.setRashidDebt(false);
                historyTab.addLog("🤝 Negotiated successfully. Debt settled.");
            } else {
                hotel.changeReputation(-10);
                historyTab.addLog("❌ Negotiation failed. Reputation -10");
            }
        } else if (choice == 2) {
            hotel.setRashidDebt(false);
            hotel.changeReputation(-15);
            historyTab.addLog("📰 Exposed the lender. Debt gone, but reputation -15");
        }
        refreshAllTabs();
    }

    private void showChapter5Events() {
        if (!hotel.isRivalApproached())
            return;
        Object[] options = { "Sell hotel", "Refuse", "Cancel" };
        int choice = JOptionPane.showOptionDialog(this,
                "A corporate chain offers to buy your hotel.\n'Sell the property. Let the past stay buried.'",
                "Rival Offer", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            hotel.earnMoney(2000);
            JOptionPane.showMessageDialog(this, "Hotel sold for $2000. Game over.");
            returnToMenu();
        } else if (choice == 1) {
            hotel.setRivalRefused(true);
            historyTab.addLog("🚫 Refused rival offer. Hard mode activated.");
        }
        refreshAllTabs();
    }

    private void showChapter6Events() {
        Object[] options = { "Uncover truth", "Seal room", "Cancel" };
        int choice = JOptionPane.showOptionDialog(this,
                "Guests complain about Room 9: sounds at night, lights turning on.\nGrandfather confesses: someone died mysteriously there decades ago.",
                "The Ghost Room", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            hotel.setGhostRoomTruth(true);
            hotel.changeReputation(10);
            historyTab.addLog("🔍 Uncovered the truth. Hotel becomes mystery destination, reputation +10");
        } else if (choice == 1) {
            hotel.setGhostRoomSealed(true);
            historyTab.addLog("🚧 Sealed Room 9. Avoids fear, but loses potential income.");
        }
        refreshAllTabs();
    }

    private void checkChapter1Progress() {
        if (!chapter1Complete && hotel.getUsableRoomCount() >= 3 &&
                hotel.getTotalRevenue() >= 300 && hotel.hasMetRashid() && hotel.hasFoundOldRegister()) {
            chapter1Complete = true;
            currentChapter = 2;
            hotel.setChapter1Complete(true);
            hotel.setCurrentChapter(2);
            SwingUtilities.invokeLater(() -> {
                showChapterComplete(1);
                onChapterComplete(1);
            });
        }
    }

    private void checkChapterProgress() {
        checkChapter1Progress();
        if (chapter1Complete && !chapter2Complete && hotel.getReputation() >= 30) {
            chapter2Complete = true;
            currentChapter = 3;
            hotel.setChapter2Complete(true);
            hotel.setCurrentChapter(3);
            SwingUtilities.invokeLater(() -> {
                showChapterComplete(2);
                onChapterComplete(2);
            });
        }
        if (chapter2Complete && !chapter3Complete && hotel.hasFoundOldRegister()) {
            chapter3Complete = true;
            currentChapter = 4;
            hotel.setChapter3Complete(true);
            hotel.setCurrentChapter(4);
            SwingUtilities.invokeLater(() -> {
                showChapterComplete(3);
                onChapterComplete(3);
            });
        }
        if (chapter3Complete && !chapter4Complete && hotel.getEmployees().size() >= 2) {
            chapter4Complete = true;
            currentChapter = 5;
            hotel.setChapter4Complete(true);
            hotel.setCurrentChapter(5);
            hotel.setRivalApproached(true);
            SwingUtilities.invokeLater(() -> {
                showChapterComplete(4);
                onChapterComplete(4);
            });
        }
        if (chapter4Complete && !chapter5Complete && hotel.isRivalRefused()) {
            chapter5Complete = true;
            currentChapter = 6;
            hotel.setChapter5Complete(true);
            hotel.setCurrentChapter(6);
            SwingUtilities.invokeLater(() -> {
                showChapterComplete(5);
                onChapterComplete(5);
            });
        }
        if (chapter5Complete && !chapter6Complete && hotel.getReputation() >= 50) {
            chapter6Complete = true;
            hotel.setChapter6Complete(true);
            SwingUtilities.invokeLater(() -> {
                showChapterComplete(6);
            });
        }
    }

    private void showChapterIntro(int ch) {
        String msg;
        switch (ch) {
            case 1:
                msg = "═══ CHAPTER 1: Homecoming ═══\n\n3 rooms functional, 1 broken\n$500 in hand\nDaily expenses: $50\n\n"
                        +
                        "Requirements:\n• 3+ usable rooms\n• $300 total revenue\n• Meet Rashid\n• Find old register";
                break;
            case 2:
                msg = "═══ CHAPTER 2: The Mystery ═══\n\nReputation 30+ to unlock Chapter 3";
                break;
            case 3:
                msg = "═══ CHAPTER 3: The Old Register ═══\n\nInvestigate the past";
                break;
            case 4:
                msg = "═══ CHAPTER 4: Staff & Debt ═══\n\nHire 2+ employees";
                break;
            case 5:
                msg = "═══ CHAPTER 5: The Rival ═══\n\nA corporate offer";
                break;
            case 6:
                msg = "═══ CHAPTER 6: The Ghost Room ═══\n\nReputation 50+ to unlock";
                break;
            default:
                msg = "Continuing the legacy...";
        }
        JOptionPane.showMessageDialog(this, msg, "Chapter " + ch, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showChapterComplete(int ch) {
        JOptionPane.showMessageDialog(this,
                "★★★ CHAPTER " + ch + " COMPLETE! ★★★\n\nChapter " + (ch + 1) + " unlocked!",
                "Achievement!", JOptionPane.INFORMATION_MESSAGE);
        if (ch + 1 <= 6)
            showChapterIntro(ch + 1);
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

    // ==================== ANIMATION LOOP ====================

    private void startAnimation() {
        animationTimer = new Timer(33, e -> { // ~30fps
            frameCount++;

            // Try to generate requests every ~2 seconds
            if (frameCount % 60 == 0)
                generateCustomerRequest();

            // Refresh requests tab every second to update countdown timers
            if (frameCount % 30 == 0 && "requests".equals(currentOpenTab)) {
                requestsTab.refresh();
            }

            // Expire requests after 30 seconds (900 frames)
            for (int i = pendingRequests.size() - 1; i >= 0; i--) {
                CustomerRequest req = pendingRequests.get(i);
                req.framesLived++;
                if (req.framesLived >= REQUEST_DURATION_FRAMES) {
                    historyTab.addLog("⏰ Expired: " + req.customer.getName());
                    pendingRequests.remove(i);
                    if ("requests".equals(currentOpenTab))
                        requestsTab.refresh();
                    updateTabBadge();
                    refreshAllTabs();
                }
            }

            // Update walking customers
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

    // ==================== CUSTOMER REQUEST ====================

    // ==================== BACKGROUND PANEL ====================

    class ReceptionBackgroundPanel extends JPanel {
        private boolean[] roomLights = new boolean[0];

        public ReceptionBackgroundPanel() {
            setOpaque(true);
        }

        public void updateRoomLights() {
            roomLights = new boolean[hotel.getRooms().size()];
            for (int i = 0; i < hotel.getRooms().size(); i++)
                roomLights[i] = hotel.getRooms().get(i).isOccupied();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Sky changes with game time
            Color skyTop, skyBot;
            if (gameHour >= 6 && gameHour < 18) {
                skyTop = new Color(135, 206, 235);
                skyBot = new Color(245, 240, 230);
            } else if (gameHour >= 18 && gameHour < 21) {
                skyTop = new Color(255, 140, 60);
                skyBot = new Color(200, 100, 80);
            } else {
                skyTop = new Color(20, 20, 60);
                skyBot = new Color(40, 40, 80);
            }

            g2.setPaint(new GradientPaint(0, 0, skyTop, 0, h, skyBot));
            g2.fillRect(0, 0, w, h);

            // Stars at night
            if (gameHour < 6 || gameHour >= 20) {
                g2.setColor(new Color(255, 255, 255, 200));
                Random sr = new Random(42);
                for (int i = 0; i < 40; i++)
                    g2.fillOval(sr.nextInt(w), sr.nextInt(h / 2), 2, 2);
            }

            // Ground + tiles
            g2.setColor(new Color(160, 140, 120));
            g2.fillRect(0, h - 150, w, 150);
            g2.setColor(new Color(140, 120, 100));
            for (int x = 0; x < w; x += 40)
                for (int y = h - 150; y < h; y += 40)
                    if ((x / 40 + y / 40) % 2 == 0)
                        g2.fillRect(x, y, 40, 40);

            // Hotel building - static size
            int totalRooms = hotel.getRooms().size();
            int bw = 300;
            int bh = Math.min(520, 200 + totalRooms * 25);
            drawBuilding(g2, getWidth() - 350, getHeight() - bh - 150, bw, bh);
            drawDesk(g2, 80, h - 230);

            for (AnimatedCustomer ac : walkingCustomers)
                ac.draw(g2);
        }

        private void drawBuilding(Graphics2D g2, int x, int y, int bw, int bh) {
            g2.setColor(new Color(0, 0, 0, 60));
            g2.fillRoundRect(x + 10, y + 10, bw, bh, 10, 10);
            g2.setPaint(new GradientPaint(x, y, new Color(230, 205, 170), x, y + bh, new Color(200, 175, 140)));
            g2.fillRoundRect(x, y, bw, bh, 10, 10);

            // Roof
            g2.setColor(new Color(130, 110, 90));
            g2.fillPolygon(new int[] { x - 10, x + bw / 2, x + bw + 10 }, new int[] { y, y - 30, y }, 3);

            // Sign
            g2.setColor(new Color(70, 50, 40));
            g2.fillRoundRect(x + bw / 2 - 60, y - 60, 120, 30, 8, 8);
            g2.setColor(new Color(255, 230, 160));
            g2.setFont(new Font("Serif", Font.BOLD, 16));
            g2.drawString("HOTEL TAJ", x + bw / 2 - 45, y - 38);

            // Windows: dynamic grid based on room count
            int total = hotel.getRooms().size();
            int cols = Math.min(4, (int) Math.ceil(Math.sqrt(total)));
            int rows = (int) Math.ceil((double) total / cols);
            int wW = 40, wH = 32;
            int padX = Math.max(8, (bw - cols * wW) / (cols + 1));
            int padY = Math.max(8, (bh - 100 - rows * wH) / (rows + 1));

            for (int idx = 0; idx < total; idx++) {
                int row = idx / cols, col = idx % cols;
                int wx = x + padX + col * (wW + padX);
                int wy = y + 20 + padY + row * (wH + padY);

                g2.setColor(new Color(100, 80, 60));
                g2.fillRoundRect(wx, wy, wW, wH, 5, 5);

                boolean lit = idx < roomLights.length && roomLights[idx];
                g2.setColor(lit ? new Color(255, 230, 150) : new Color(60, 70, 90));
                g2.fillRoundRect(wx + 3, wy + 3, wW - 6, wH - 6, 4, 4);

                g2.setColor(new Color(80, 70, 60));
                g2.drawLine(wx + wW / 2, wy + 3, wx + wW / 2, wy + wH - 3);
                g2.drawLine(wx + 3, wy + wH / 2, wx + wW - 3, wy + wH / 2);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("SansSerif", Font.BOLD, 8));
                String rn = "" + (idx + 1);
                g2.drawString(rn, wx + wW / 2 - (rn.length() > 1 ? 5 : 2), wy + wH / 2 + 3);
            }

            // Door
            int dW = 60, dH = 80, dX = x + bw / 2 - 30, dY = y + bh - dH;
            g2.setColor(new Color(90, 70, 50));
            g2.fillRoundRect(dX, dY, dW, dH, 8, 8);
            g2.setColor(new Color(150, 200, 230, 150));
            g2.fillRoundRect(dX + 8, dY + 10, dW - 16, dH / 2, 6, 6);
            g2.setColor(new Color(200, 180, 100));
            g2.fillOval(dX + dW - 18, dY + dH / 2, 8, 8);
            g2.setColor(new Color(140, 120, 100));
            g2.fillRect(dX - 20, dY + dH, dW + 40, 12);
            g2.fillRect(dX - 30, dY + dH + 12, dW + 60, 12);
            g2.setColor(new Color(100, 70, 50));
            g2.fillRect(dX - 50, dY + dH - 30, 20, 30);
            g2.fillRect(dX + dW + 30, dY + dH - 30, 20, 30);
            g2.setColor(new Color(70, 130, 70));
            g2.fillOval(dX - 55, dY + dH - 50, 30, 30);
            g2.fillOval(dX + dW + 25, dY + dH - 50, 30, 30);
        }
    }

    private void drawDesk(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x + 3, y + 3, 200, 80, 8, 8);
        g2.setColor(new Color(139, 90, 60));
        g2.fillRoundRect(x, y, 200, 80, 8, 8);
        g2.setColor(new Color(160, 110, 80));
        g2.fillRoundRect(x - 5, y, 210, 15, 6, 6);
        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(x + 80, y - 25, 40, 22);
        g2.setColor(new Color(100, 150, 200));
        g2.fillRect(x + 83, y - 23, 34, 18);
        g2.setColor(new Color(70, 50, 40));
        g2.fillRect(x + 10, y + 15, 70, 40);
        g2.setColor(new Color(220, 200, 150));
        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.drawString("RECEPTION", x + 13, y + 35);
    }
}

// ==================== ANIMATED CUSTOMER (multiple at once, staggered)
// ====================

// class AnimatedCustomer {
// private Customer customer;
// private int roomNumber;
// private float x, y;
// private int state = 0; // 0=walk to desk, 1=wait, 2=walk to building
// private int waitTime = 0;
// private Color shirtColor;
// private float speed;

// AnimatedCustomer(Customer customer, int roomNumber) {
// this.customer = customer;
// this.roomNumber = roomNumber;
// this.x = -50 - random.nextInt(80); // stagger entries
// this.y = getHeight() - 195 + random.nextInt(25);
// this.speed = 2.5f + random.nextFloat() * 2f;
// this.shirtColor = new Color(
// 80 + random.nextInt(176),
// 80 + random.nextInt(176),
// 80 + random.nextInt(176)
// );
// }

// void update() {
// if (state == 0) { x += speed; if (x >= 120 + random.nextInt(50)) state = 1; }
// else if (state == 1) { if (++waitTime > 35) state = 2; }
// else { x += speed + 1; }
// }

// boolean isFinished() { return state == 2 && x > getWidth() + 60; }

// void draw(Graphics2D g2) {
// int ix = (int)x, iy = (int)y;
// boolean moving = state == 0 || state == 2;

// // Shadow
// g2.setColor(new Color(0,0,0,40));
// g2.fillOval(ix+4, iy+50, 26, 8);

// // Legs (walking animation)
// int leg = moving ? ((frameCount/8)%2==0 ? 3 : -3) : 0;
// g2.setColor(new Color(50,50,80));
// g2.fillRect(ix+8, iy+32, 5, 18+leg);
// g2.fillRect(ix+17, iy+32, 5, 18-leg);

// // Shoes
// g2.setColor(new Color(30,30,40));
// g2.fillOval(ix+7, iy+48, 7, 3);
// g2.fillOval(ix+17, iy+48, 7, 3);

// // Body
// g2.setColor(shirtColor);
// g2.fillRoundRect(ix+6, iy+15, 18, 18, 6, 6);

// // Arms swing
// int arm = moving ? ((frameCount/8)%2==0 ? 4 : -4) : 0;
// g2.setColor(shirtColor.darker());
// g2.fillRect(ix+2, iy+18+arm, 4, 12);
// g2.fillRect(ix+24, iy+18-arm, 4, 12);

// // Head
// g2.setColor(new Color(220,180,140));
// g2.fillOval(ix+8, iy+2, 14, 16);

// // Hair
// g2.setColor(new Color(60,40,30));
// g2.fillArc(ix+8, iy, 14, 10, 0, 180);

// // Eyes
// g2.setColor(Color.BLACK);
// g2.fillOval(ix+10, iy+7, 2, 2);
// g2.fillOval(ix+17, iy+7, 2, 2);
// g2.drawArc(ix+11, iy+9, 7, 5, -180, -180);

// // Suitcase when walking
// if (moving) {
// g2.setColor(new Color(100,70,50));
// g2.fillRect(ix-6, iy+23, 8, 12);
// g2.setColor(new Color(200,200,200));
// g2.fillRect(ix-5, iy+25, 6, 2);
// }

// // Name bubble when waiting at desk
// if (state == 1) {
// String firstName = customer.getName().split(" ")[0];
// int bw = firstName.length() * 7 + 10;
// g2.setColor(new Color(255,255,255,210));
// g2.fillRoundRect(ix-5, iy-18, bw, 15, 6, 6);
// g2.setColor(new Color(50,50,100));
// g2.setFont(new Font("SansSerif", Font.BOLD, 9));
// g2.drawString(firstName, ix-2, iy-6);
// }
// }
// }

class AnimatedCustomer {
    private ReceptionPanel parent;
    private Customer customer;
    private int roomNumber;
    private float x, y;
    private Color shirtColor;
    private float speed;
    CustomerRequest linkedRequest; // which request this person belongs to

    // States:
    // 0 = walking right toward desk
    // 1 = waiting at desk (standing still)
    // 2 = accepted: walking right toward hotel door
    // 3 = rejected: walking left back off screen
    // 4 = fading out at hotel door
    private int state = 0;
    private float alpha = 1.0f; // for fade out

    public AnimatedCustomer(ReceptionPanel parent, Customer customer, int roomNumber, CustomerRequest request) {
        this.parent = parent;
        this.customer = customer;
        this.roomNumber = roomNumber;
        this.linkedRequest = request;
        this.x = -50 - parent.random.nextInt(80);
        this.y = 405 + parent.random.nextInt(20); // 600 - 195 = 405
        this.speed = 2.5f + parent.random.nextFloat() * 1.5f;
        this.shirtColor = new Color(
                80 + parent.random.nextInt(176),
                80 + parent.random.nextInt(176),
                80 + parent.random.nextInt(176));
    }

    /** Called by acceptRequest() to tell this person they were accepted */
    public void accept() {
        state = 2;
    }

    /** Called by rejectRequest() to tell this person they were rejected */
    public void reject() {
        state = 3;
    }

    /** True if request still pending and person is waiting */
    public boolean isWaiting() {
        return state == 1;
    }

    public void update() {
        switch (state) {
            case 0: // Walk to desk
                x += speed;
                if (x >= 130)
                    state = 1; // arrived at desk, now wait
                break;
            case 1: // Waiting — do nothing, stand still
                // If the linked request was removed externally (expired),
                // start walking back
                if (!parent.pendingRequests.contains(linkedRequest))
                    state = 3;
                break;
            case 2: // Accepted — walk toward hotel door (right side ~540px)
                x += speed + 1;
                if (x >= 540)
                    state = 4; // reached door, start fade
                break;
            case 3: // Rejected — walk back left off screen
                x -= speed;
                break;
            case 4: // Fading out
                alpha -= 0.03f;
                break;
        }
    }

    public boolean isFinished() {
        return (state == 3 && x < -80) || (state == 4 && alpha <= 0);
    }

    public void draw(Graphics2D g2) {
        int ix = (int) x, iy = (int) y;
        boolean moving = state == 0 || state == 2 || state == 3;

        // Apply alpha for fade-out
        Composite originalComposite = g2.getComposite();
        if (state == 4) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
        }

        // Shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(ix + 4, iy + 50, 26, 8);

        // Legs (animate when moving)
        int leg = moving ? ((parent.frameCount / 8) % 2 == 0 ? 3 : -3) : 0;
        g2.setColor(new Color(50, 50, 80));
        g2.fillRect(ix + 8, iy + 32, 5, 18 + leg);
        g2.fillRect(ix + 17, iy + 32, 5, 18 - leg);

        // Shoes
        g2.setColor(new Color(30, 30, 40));
        g2.fillOval(ix + 7, iy + 48, 7, 3);
        g2.fillOval(ix + 17, iy + 48, 7, 3);

        // Body
        g2.setColor(shirtColor);
        g2.fillRoundRect(ix + 6, iy + 15, 18, 18, 6, 6);

        // Arms swing when moving
        int arm = moving ? ((parent.frameCount / 8) % 2 == 0 ? 4 : -4) : 0;
        g2.setColor(shirtColor.darker());
        g2.fillRect(ix + 2, iy + 18 + arm, 4, 12);
        g2.fillRect(ix + 24, iy + 18 - arm, 4, 12);

        // Head
        g2.setColor(new Color(220, 180, 140));
        g2.fillOval(ix + 8, iy + 2, 14, 16);

        // Hair
        g2.setColor(new Color(60, 40, 30));
        g2.fillArc(ix + 8, iy, 14, 10, 0, 180);

        // Eyes
        g2.setColor(Color.BLACK);
        g2.fillOval(ix + 10, iy + 7, 2, 2);
        g2.fillOval(ix + 17, iy + 7, 2, 2);

        // Smile if accepted, frown if rejected/waiting-long
        if (state == 3) {
            // Frown
            g2.drawArc(ix + 11, iy + 11, 7, 5, 0, 180);
        } else {
            // Smile
            g2.drawArc(ix + 11, iy + 9, 7, 5, -180, -180);
        }

        // Suitcase when walking to hotel (accepted) or arriving
        if (state == 2 || state == 0) {
            g2.setColor(new Color(100, 70, 50));
            g2.fillRect(ix - 6, iy + 23, 8, 12);
            g2.setColor(new Color(200, 200, 200));
            g2.fillRect(ix - 5, iy + 25, 6, 2);
        }

        // Name bubble only while waiting at desk
        if (state == 1) {
            String firstName = customer.getName().split(" ")[0];
            int bw = firstName.length() * 7 + 10;
            g2.setColor(new Color(255, 255, 255, 210));
            g2.fillRoundRect(ix - 5, iy - 20, bw, 15, 6, 6);
            g2.setColor(new Color(50, 50, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 9));
            g2.drawString(firstName, ix - 2, iy - 8);

            // Small "waiting" dots animation
            g2.setColor(new Color(150, 150, 150));
            int dot = (parent.frameCount / 15) % 3;
            for (int d = 0; d <= dot; d++) {
                g2.fillOval(ix + bw - 4 + d * 5, iy - 14, 3, 3);
            }
        }

        // Restore composite
        g2.setComposite(originalComposite);
    }
}
// ==================== REQUESTS TAB ====================

class RequestsTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel listPanel;
    private JButton acceptAllBtn, rejectAllBtn;

    RequestsTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        JLabel title = new JLabel("📩 Booking Requests");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Select All buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        acceptAllBtn = new JButton("✓ Accept All");
        rejectAllBtn = new JButton("✗ Reject All");

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

        JLabel hint = new JLabel("Requests expire in 30s  •  Scroll to see all");
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

        // Enable/disable select all buttons based on pending requests
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

        JLabel nameLbl = new JLabel("👤 " + req.customer.getName());
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel roomLbl = new JLabel("🏠 Room " + req.room.getRoomNumber() + " ("
                + req.room.getCondition().toString().replace("_", " ") + ")");
        JLabel nightLbl = new JLabel("🌙 " + req.nights + " night(s)");
        JLabel priceLbl = new JLabel("💰 $" + req.price);
        priceLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceLbl.setForeground(new Color(0, 130, 0));

        int secs = req.getSecondsRemaining();
        JLabel timeLbl = new JLabel("⏰ " + secs + "s remaining");
        timeLbl.setFont(new Font("SansSerif", Font.ITALIC, 11));
        timeLbl.setForeground(secs <= 10 ? Color.RED : new Color(180, 100, 0));

        info.add(nameLbl);
        info.add(roomLbl);
        info.add(nightLbl);
        info.add(priceLbl);
        info.add(timeLbl);

        JPanel btns = new JPanel(new GridLayout(2, 1, 0, 8));
        btns.setOpaque(false);
        JButton acc = new JButton("✓ ACCEPT");
        JButton rej = new JButton("✗ REJECT");
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

        parent.historyTab.addLog("✓ Accepted all " + accepted + " pending requests");
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

        parent.historyTab.addLog("✗ Rejected all " + rejected + " pending requests");
        JOptionPane.showMessageDialog(this, "Rejected " + rejected + " requests!");
        refresh();
    }
}

// ==================== GUESTS TAB ====================

class GuestsTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel listPanel;

    GuestsTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        JLabel title = new JLabel("🏨 Current Guests");
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

            JLabel nameLbl = new JLabel("👤 " + g.getName());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 13));
            JLabel detLbl = new JLabel("🏠 Room " + room.getRoomNumber() +
                    "  |  🌙 " + room.getNightsBooked() + " nights left  |  😊 " + g.getSatisfactionLevel() + "%");
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

// ==================== ROOM TAB ====================

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
        JLabel title = new JLabel("🏠 Rooms");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        // Selection buttons
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

        // Auto-update button
        autoUpdateBtn = new JButton("🔄 Upgrade Selected ($10,000 min)");
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

        roomGrid = new JPanel(new GridLayout(0, 2, 10, 10)); // 0 rows = unlimited
        roomGrid.setOpaque(false);

        JScrollPane scroll = new JScrollPane(roomGrid);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        add(topPanel, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("Click rooms for upgrade options • Select multiple for bulk upgrade");
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

            // Highlight selected rooms
            boolean isSelected = selectedRooms.contains(rn);
            Color baseColor = getColor(room.getCondition());
            if (isSelected) {
                // Make selected rooms brighter
                baseColor = new Color(
                        Math.min(255, baseColor.getRed() + 50),
                        Math.min(255, baseColor.getGreen() + 50),
                        Math.min(255, baseColor.getBlue() + 50));
            }

            btn.setBackground(baseColor);
            btn.setText("<html><div style='text-align:center;padding:3px'>" +
                    "<b style='font-size:13px'>Room " + rn + "</b><br>" +
                    "<span style='font-size:11px'>" + (room.isOccupied() ? "🔴 OCCUPIED" : "🟢 AVAILABLE")
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

        // Enable/disable auto-update button based on money and selection
        autoUpdateBtn.setEnabled(parent.hotel.getMoney() >= 10000 && !selectedRooms.isEmpty());
        autoUpdateBtn.setText("🔄 Upgrade Selected (" + selectedRooms.size() + " rooms, $10,000 min)");
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

        // Show options dialog
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
            case 0: // Select/Deselect for bulk upgrade
                if (selectedRooms.contains(rn)) {
                    selectedRooms.remove(Integer.valueOf(rn));
                } else {
                    selectedRooms.add(rn);
                }
                refresh();
                break;

            case 1: // Upgrade individually
                upgradeRoomIndividually(rn);
                break;

            case 2: // View info (default behavior)
            default:
                JOptionPane.showMessageDialog(this,
                        "Room " + rn + "\nCondition: " + room.getCondition() +
                                "\nStatus: "
                                + (room.isOccupied() ? "Occupied – " + room.getNightsBooked() + " nights left"
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
                        parent.historyTab.addLog("✓ Repaired Room " + rn + " ($200)");
                        parent.refreshAllTabs();
                    } else
                        JOptionPane.showMessageDialog(this, "Not enough money!");
                break;
            case VERY_POOR:
                if (confirm("Repair Room " + rn + " for $100?"))
                    if (parent.hotel.repairRoom(rn, 100)) {
                        parent.historyTab.addLog("✓ Repaired Room " + rn + " ($100)");
                        parent.refreshAllTabs();
                    } else
                        JOptionPane.showMessageDialog(this, "Not enough money!");
                break;
            case POOR:
                if (confirm("Upgrade Room " + rn + " for $150?"))
                    if (parent.hotel.repairRoom(rn, 150)) {
                        parent.historyTab.addLog("✓ Upgraded Room " + rn + " ($150)");
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
                        cost = 500; // Upgrade from broken to excellent
                        break;
                    case VERY_POOR:
                        cost = 400; // Upgrade from very poor to excellent
                        break;
                    case POOR:
                        cost = 300; // Upgrade from poor to excellent
                        break;
                    case GOOD:
                        cost = 200; // Upgrade from good to excellent
                        break;
                }

                if (cost > 0 && parent.hotel.spendMoney(cost)) {
                    room.setCondition(RoomCondition.EXCELLENT);
                    roomsUpdated++;
                    totalCost += cost;
                }
            }
        }

        selectedRooms.clear(); // Clear selection after upgrade

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

// ==================== EMPLOYEE TAB ====================

class EmployeeTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JPanel listPanel;

    EmployeeTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);
        JLabel title = new JLabel("👥 Your Team");
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

        JButton hire = new JButton("➕ Hire Employee");
        hire.setFocusPainted(false);
        hire.setFont(new Font("SansSerif", Font.BOLD, 14));
        hire.setBackground(new Color(70, 130, 180));
        hire.setForeground(Color.WHITE);
        hire.setPreferredSize(new Dimension(180, 38));
        hire.setCursor(new Cursor(Cursor.HAND_CURSOR));
        hire.addActionListener(e -> showHireDialog());

        // JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        // bottom.setOpaque(false); bottom.add(hire);

        // add(title, BorderLayout.NORTH);
        // add(scroll, BorderLayout.CENTER);
        // add(bottom, BorderLayout.SOUTH);
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

            JLabel nameLbl = new JLabel("👤 " + emp.getName());
            nameLbl.setFont(new Font("SansSerif", Font.BOLD, 14));
            String em = emp.getType().equals("manager") ? "👔" : emp.getType().equals("cleaner") ? "🧹" : "🛡️";
            JLabel detLbl = new JLabel(em + " " + emp.getType().toUpperCase() + "  |  💵 $" + emp.getDailySalary()
                    + "/day  |  ⚡ " + emp.getEfficiency() + "%");
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

    // private void showHireDialog() {
    // String[] opts = {"Cleaner ($25/day)", "Security ($35/day)", "Cancel"};
    // int c = JOptionPane.showOptionDialog(this, "Choose employee type:", "Hire",
    // JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, opts,
    // opts[0]);
    // if (c == 0) {
    // String name = JOptionPane.showInputDialog(this, "Enter cleaner's name:");
    // if (name != null && !name.trim().isEmpty()) {
    // hotel.hireEmployee(new Employee(name, "cleaner", 25));
    // historyTab.addLog("✅ Hired " + name + " as Cleaner ($25/day)");
    // refreshAllTabs();
    // }
    // } else if (c == 1) {
    // String name = JOptionPane.showInputDialog(this, "Enter security's name:");
    // if (name != null && !name.trim().isEmpty()) {
    // hotel.hireEmployee(new Employee(name, "security", 35));
    // historyTab.addLog("✅ Hired " + name + " as Security ($35/day)");
    // refreshAllTabs();
    // }
    // }
    // }
    // private void showHireDialog() {
    // // Pre-defined employee roster
    // String[][] roster = {
    // // {name, type, salary, reputationBoost, description}
    // {"Ayesha", "cleaner", "25", "3", "🧹 Keeps rooms spotless"},
    // {"Bilal", "cleaner", "25", "3", "🧹 Fast and reliable cleaner"},
    // {"Zara", "concierge", "40", "8", "💁 Helps guests, boosts satisfaction"},
    // {"Omar", "security", "35", "5", "🛡️ Keeps hotel safe"},
    // {"Kamran", "chef", "45", "10", "👨‍🍳 Great food = happy guests"},
    // {"Sana", "receptionist","30","6", "📋 Handles bookings efficiently"},
    // {"Tariq", "maintenance","35", "4", "🔧 Repairs rooms faster"},
    // {"Nadia", "manager", "55", "12", "👔 Boosts overall efficiency"},
    // {"Cancel", "", "0", "0", ""}
    // };

    // // Build display strings
    // String[] options = new String[roster.length];
    // for (int i = 0; i < roster.length - 1; i++) {
    // options[i] = "<html><b>" + roster[i][0] + "</b> — " +
    // roster[i][4] + "<br>" +
    // "<font color='gray'>$" + roster[i][2] + "/day | +" +
    // roster[i][3] + " reputation</font></html>";
    // }
    // options[roster.length - 1] = "Cancel";

    // int choice = JOptionPane.showOptionDialog(
    // this,
    // "Select an employee to hire:",
    // "Hire Employee",
    // JOptionPane.DEFAULT_OPTION,
    // JOptionPane.QUESTION_MESSAGE,
    // null,
    // options,
    // options[0]
    // );

    // if (choice >= 0 && choice < roster.length - 1) {
    // String[] emp = roster[choice];
    // String name = emp[0];
    // String type = emp[1];
    // int salary = Integer.parseInt(emp[2]);
    // int repBoost = Integer.parseInt(emp[3]);

    // // Check if already hired
    // for (Employee e : hotel.getEmployees()) {
    // if (e.getName().equals(name)) {
    // JOptionPane.showMessageDialog(this,
    // name + " is already on your team!",
    // "Already Hired", JOptionPane.WARNING_MESSAGE);
    // return;
    // }
    // }

    // hotel.hireEmployee(new Employee(name, type, salary));
    // hotel.changeReputation(repBoost);
    // historyTab.addLog("✅ Hired " + name + " as " + type.toUpperCase() +
    // " ($" + salary + "/day) | Reputation +" + repBoost);
    // refreshAllTabs();
    // }
    // }
    private void showHireDialog() {
        String[][] roster = {
                { "Ayesha", "cleaner", "25", "3", "🧹 Cleaner", "Keeps rooms spotless" },
                { "Bilal", "cleaner", "25", "3", "🧹 Cleaner", "Fast and reliable" },
                { "Zara", "concierge", "40", "8", "💁 Concierge", "Boosts guest satisfaction" },
                { "Omar", "security", "35", "5", "🛡️ Security", "Keeps hotel safe" },
                { "Kamran", "chef", "45", "10", "👨‍🍳 Chef", "Great food = happy guests" },
                { "Sana", "receptionist", "30", "6", "📋 Receptionist", "Handles bookings efficiently" },
                { "Tariq", "maintenance", "35", "4", "🔧 Maintenance", "Repairs rooms faster" },
                { "Nadia", "manager", "55", "12", "👔 Manager", "Boosts overall efficiency" },
        };

        // Build custom dialog
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Hire Employee", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(420, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        // Title
        JLabel title = new JLabel("Select an Employee to Hire", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setBorder(new EmptyBorder(15, 10, 5, 10));
        title.setOpaque(true);
        title.setBackground(new Color(45, 45, 55));
        title.setForeground(Color.WHITE);
        dialog.add(title, BorderLayout.NORTH);

        // Scrollable employee list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(248, 248, 252));
        listPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Already hired names
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

            // Left: name + role + description
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

            // Right: salary + rep badge
            JPanel badgePanel = new JPanel(new GridLayout(2, 1, 0, 4));
            badgePanel.setOpaque(false);
            badgePanel.setPreferredSize(new Dimension(95, 50));

            JLabel salaryLbl = new JLabel("💵 $" + emp[2] + "/day", SwingConstants.CENTER);
            salaryLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            salaryLbl.setForeground(new Color(0, 130, 0));

            JLabel repLbl = new JLabel("⭐ +" + emp[3] + " rep", SwingConstants.CENTER);
            repLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            repLbl.setForeground(new Color(180, 120, 0));

            badgePanel.add(salaryLbl);
            badgePanel.add(repLbl);

            // Already hired overlay label
            if (alreadyHired) {
                JLabel hiredLbl = new JLabel("✓ Hired", SwingConstants.CENTER);
                hiredLbl.setFont(new Font("SansSerif", Font.BOLD, 11));
                hiredLbl.setForeground(new Color(100, 150, 100));
                badgePanel.add(hiredLbl);
            }

            card.add(textPanel, BorderLayout.CENTER);
            card.add(badgePanel, BorderLayout.EAST);

            // Click to select (only if not hired)
            if (!alreadyHired) {
                card.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        // Deselect all
                        for (JPanel c : cards) {
                            c.setBackground(Color.WHITE);
                            c.setBorder(new CompoundBorder(
                                    new LineBorder(new Color(200, 200, 210), 1),
                                    new EmptyBorder(10, 14, 10, 14)));
                        }
                        // Select this one
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

        // Bottom buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        bottomPanel.setBackground(new Color(45, 45, 55));

        JButton hireBtn = new JButton("✓ Hire");
        JButton cancelBtn = new JButton("✗ Cancel");

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

// ==================== HISTORY TAB ====================

class HistoryTabPanel extends JPanel {
    private ReceptionPanel parent;
    private JTextArea historyArea;

    HistoryTabPanel(ReceptionPanel parent) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setOpaque(false);
        JLabel title = new JLabel("📜 Activity History");
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
        addLog("=== Hotel Taj Management System ===");
        addLog("Welcome! Accept guests to grow your hotel.");
        addLog("");
    }

    void addLog(String msg) {
        historyArea.append(msg + "\n");
        historyArea.setCaretPosition(historyArea.getDocument().getLength());
    }

    void refresh() {
    }
}

// ==================== CUSTOMER REQUEST (TOP-LEVEL CLASS) ====================

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