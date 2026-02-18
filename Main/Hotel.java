import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Hotel implements Serializable {
    private String name;
    private int money;
    private int reputation; // 0-100
    private List<Room> rooms;
    private List<Employee> employees;
    private int dailyExpenses;
    private int totalRevenue;
    private int day;

    // Story flags
    private boolean foundOldRegister;
    private boolean metRashid;
    private boolean exploringMystery;

    // Chapter system
    private int currentChapter = 1;
    private boolean chapter1Complete = false;
    private boolean chapter2Complete = false;
    private boolean chapter3Complete = false;
    private boolean chapter4Complete = false;
    private boolean chapter5Complete = false;
    private boolean chapter6Complete = false;

    // Additional flags
    private boolean rashidDebt = true;
    private boolean rivalApproached = false;
    private boolean rivalRefused = false;
    private boolean ghostRoomTruth = false;
    private boolean ghostRoomSealed = false;

    // History logs
    private List<String> historyLogs = new ArrayList<>();

    public Hotel(String name) {
        this.name = name;
        this.money = 500; // Starting money - very tight!
        this.reputation = 20; // Poor reputation due to closure
        this.rooms = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.dailyExpenses = 50; // Basic utilities
        this.totalRevenue = 0;
        this.day = 1;
        this.foundOldRegister = false;
        this.metRashid = false;
        this.exploringMystery = false;

        // Initialize 4 starting rooms in poor condition
        initializeStartingRooms();
    }

    private void initializeStartingRooms() {
        rooms.add(new Room(1, "Standard", RoomCondition.POOR));
        rooms.add(new Room(2, "Standard", RoomCondition.POOR));
        rooms.add(new Room(3, "Standard", RoomCondition.VERY_POOR));
        rooms.add(new Room(4, "Standard", RoomCondition.BROKEN));
    }

    // Money management
    public boolean spendMoney(int amount) {
        if (money >= amount) {
            money -= amount;
            return true;
        }
        return false;
    }

    public void earnMoney(int amount) {
        money += amount;
        totalRevenue += amount;
    }

    // REPLACE endDay() with this:
    public void endDay() {
        day++;
        money -= dailyExpenses;
        for (Employee emp : employees) {
            money -= emp.getDailySalary();
        }
        // NOTE: checkout is now handled by ReceptionPanel, not here
    }

    // ADD these two new methods anywhere in Hotel.java:
    public int getAvailableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (!room.isOccupied() && room.getCondition() != RoomCondition.BROKEN) {
                count++;
            }
        }
        return count;
    }

    public void addRooms(int count) {
        int nextNumber = rooms.size() + 1;
        for (int i = 0; i < count; i++) {
            rooms.add(new Room(nextNumber + i, "Standard", RoomCondition.POOR));
        }
    }
    // // Daily operations
    // public void endDay() {
    // day++;
    // money -= dailyExpenses;

    // for (Employee emp : employees) {
    // money -= emp.getDailySalary();
    // }

    // // Decrement nights for occupied rooms
    // for (Room room : rooms) {
    // if (room.isOccupied()) {
    // room.decrementNight();
    // if (room.getNightsBooked() <= 0) {
    // Customer guest = room.checkOut();
    // changeReputation(guest.getReputationImpact());
    // }
    // }
    // }
    // }

    // Room management
    public Room getAvailableRoom() {
        for (Room room : rooms) {
            if (!room.isOccupied() && room.getCondition() != RoomCondition.BROKEN) {
                return room;
            }
        }
        return null;
    }

    public int getUsableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (room.getCondition() != RoomCondition.BROKEN) {
                count++;
            }
        }
        return count;
    }

    public boolean repairRoom(int roomNumber, int cost) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                if (spendMoney(cost)) {
                    room.repair();
                    return true;
                }
            }
        }
        return false;
    }

    // Reputation management
    public void changeReputation(int change) {
        reputation += change;
        if (reputation > 100)
            reputation = 100;
        if (reputation < 0)
            reputation = 0;
    }

    // Employee management
    public void hireEmployee(Employee employee) {
        employees.add(employee);
        dailyExpenses += employee.getDailySalary();
    }

    public boolean hasEmployeeType(String type) {
        for (Employee emp : employees) {
            if (emp.getType().equals(type)) {
                return true;
            }
        }
        return false;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public int getReputation() {
        return reputation;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public int getDay() {
        return day;
    }

    public int getDailyExpenses() {
        int total = dailyExpenses;
        for (Employee emp : employees) {
            total += emp.getDailySalary();
        }
        return total;
    }

    public int getTotalRevenue() {
        return totalRevenue;
    }

    // Getters and setters for chapters and flags
    public int getCurrentChapter() {
        return currentChapter;
    }

    public void setCurrentChapter(int c) {
        currentChapter = c;
    }

    public boolean isChapter1Complete() {
        return chapter1Complete;
    }

    public void setChapter1Complete(boolean b) {
        chapter1Complete = b;
    }

    public boolean isChapter2Complete() {
        return chapter2Complete;
    }

    public void setChapter2Complete(boolean b) {
        chapter2Complete = b;
    }

    public boolean isChapter3Complete() {
        return chapter3Complete;
    }

    public void setChapter3Complete(boolean b) {
        chapter3Complete = b;
    }

    public boolean isChapter4Complete() {
        return chapter4Complete;
    }

    public void setChapter4Complete(boolean b) {
        chapter4Complete = b;
    }

    public boolean isChapter5Complete() {
        return chapter5Complete;
    }

    public void setChapter5Complete(boolean b) {
        chapter5Complete = b;
    }

    public boolean isChapter6Complete() {
        return chapter6Complete;
    }

    public void setChapter6Complete(boolean b) {
        chapter6Complete = b;
    }

    public boolean hasRashidDebt() {
        return rashidDebt;
    }

    public void setRashidDebt(boolean b) {
        rashidDebt = b;
    }

    public boolean isRivalApproached() {
        return rivalApproached;
    }

    public void setRivalApproached(boolean b) {
        rivalApproached = b;
    }

    public boolean isRivalRefused() {
        return rivalRefused;
    }

    public void setRivalRefused(boolean b) {
        rivalRefused = b;
    }

    public boolean isGhostRoomTruth() {
        return ghostRoomTruth;
    }

    public void setGhostRoomTruth(boolean b) {
        ghostRoomTruth = b;
    }

    public boolean isGhostRoomSealed() {
        return ghostRoomSealed;
    }

    public void setGhostRoomSealed(boolean b) {
        ghostRoomSealed = b;
    }

    public boolean hasMetRashid() {
        return metRashid;
    }

    public void setMetRashid(boolean b) {
        metRashid = b;
    }

    public boolean hasFoundOldRegister() {
        return foundOldRegister;
    }

    public void setFoundOldRegister(boolean b) {
        foundOldRegister = b;
    }

    public boolean isExploringMystery() {
        return exploringMystery;
    }

    public void setExploringMystery(boolean b) {
        exploringMystery = b;
    }

    public void addHistoryLog(String log) {
        historyLogs.add(log);
    }

    public List<String> getHistoryLogs() {
        return historyLogs;
    }
}