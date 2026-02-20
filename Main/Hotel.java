import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Hotel implements Serializable {
    private static final long serialVersionUID = 6482042822553431247L;
    public static final String DEFAULT_HOTEL_NAME = "Golden Horizon Resort";
    public static final int MAX_ROOMS = 150;

    private String name;
    private int money;
    private int reputation; 
    private List<Room> rooms;
    private List<Employee> employees;
    private int dailyExpenses;
    private int totalRevenue;
    private int day;

    private boolean foundOldRegister;
    private boolean metRashid;
    private boolean exploringMystery;

    private int currentChapter = 1;
    private boolean chapter1Complete = false;
    private boolean chapter2Complete = false;
    private boolean chapter3Complete = false;
    private boolean chapter4Complete = false;
    private boolean chapter5Complete = false;
    private boolean chapter6Complete = false;
    private boolean chapter7Complete = false;
    private boolean chapter8Complete = false;
    private boolean chapter1StorySeen = false;
    private boolean chapter2StorySeen = false;
    private boolean chapter3StorySeen = false;
    private boolean chapter4StorySeen = false;
    private boolean chapter5StorySeen = false;
    private boolean chapter6StorySeen = false;
    private boolean chapter7StorySeen = false;
    private boolean chapter8StorySeen = false;
    private boolean chapter2DecisionMade = false;
    private boolean chapter3DecisionMade = false;

    private boolean rashidDebt = true;
    private boolean rivalApproached = false;
    private boolean rivalRefused = false;
    private boolean ghostRoomTruth = false;
    private boolean ghostRoomSealed = false;
    private boolean treasureFound = false;
    private boolean ownershipWarResolved = false;

    private List<String> historyLogs = new ArrayList<>();

    public Hotel(String name) {
        this.name = name;
        this.money = 500; 
        this.reputation = 20; 
        this.rooms = new ArrayList<>();
        this.employees = new ArrayList<>();
        this.dailyExpenses = 50; 
        this.totalRevenue = 0;
        this.day = 1;
        this.foundOldRegister = false;
        this.metRashid = false;
        this.exploringMystery = false;

        initializeStartingRooms();
    }

    private void initializeStartingRooms() {
        rooms.add(new Room(1, "Standard", RoomCondition.POOR));
        rooms.add(new Room(2, "Standard", RoomCondition.POOR));
        rooms.add(new Room(3, "Standard", RoomCondition.VERY_POOR));
        rooms.add(new Room(4, "Standard", RoomCondition.BROKEN));
    }

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

    public void endDay() {
        day++;
        money -= dailyExpenses;
        for (Employee emp : employees) {
            money -= emp.getDailySalary();
        }
    }

    public int getAvailableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (!room.isOccupied() && room.getCondition() != RoomCondition.BROKEN) {
                count++;
            }
        }
        return count;
    }

    public int addRooms(int count) {
        if (count <= 0) {
            return 0;
        }
        int current = rooms.size();
        if (current >= MAX_ROOMS) {
            return 0;
        }
        int allowed = Math.min(count, MAX_ROOMS - current);
        int nextNumber = rooms.size() + 1;
        for (int i = 0; i < allowed; i++) {
            rooms.add(new Room(nextNumber + i, "Standard", RoomCondition.POOR));
        }
        return allowed;
    }

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

    public void changeReputation(int change) {
        reputation += change;
        if (reputation > 100)
            reputation = 100;
        if (reputation < 0)
            reputation = 0;
    }

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

    public boolean isChapter7Complete() {
        return chapter7Complete;
    }

    public void setChapter7Complete(boolean b) {
        chapter7Complete = b;
    }

    public boolean isChapter8Complete() {
        return chapter8Complete;
    }

    public void setChapter8Complete(boolean b) {
        chapter8Complete = b;
    }

    public boolean isChapter1StorySeen() {
        return chapter1StorySeen;
    }

    public void setChapter1StorySeen(boolean b) {
        chapter1StorySeen = b;
    }

    public boolean isChapter2StorySeen() {
        return chapter2StorySeen;
    }

    public void setChapter2StorySeen(boolean b) {
        chapter2StorySeen = b;
    }

    public boolean isChapter3StorySeen() {
        return chapter3StorySeen;
    }

    public void setChapter3StorySeen(boolean b) {
        chapter3StorySeen = b;
    }

    public boolean isChapter4StorySeen() {
        return chapter4StorySeen;
    }

    public void setChapter4StorySeen(boolean b) {
        chapter4StorySeen = b;
    }

    public boolean isChapter5StorySeen() {
        return chapter5StorySeen;
    }

    public void setChapter5StorySeen(boolean b) {
        chapter5StorySeen = b;
    }

    public boolean isChapter6StorySeen() {
        return chapter6StorySeen;
    }

    public void setChapter6StorySeen(boolean b) {
        chapter6StorySeen = b;
    }

    public boolean isChapter7StorySeen() {
        return chapter7StorySeen;
    }

    public void setChapter7StorySeen(boolean b) {
        chapter7StorySeen = b;
    }

    public boolean isChapter8StorySeen() {
        return chapter8StorySeen;
    }

    public void setChapter8StorySeen(boolean b) {
        chapter8StorySeen = b;
    }

    public boolean isChapter2DecisionMade() {
        return chapter2DecisionMade;
    }

    public void setChapter2DecisionMade(boolean b) {
        chapter2DecisionMade = b;
    }

    public boolean isChapter3DecisionMade() {
        return chapter3DecisionMade;
    }

    public void setChapter3DecisionMade(boolean b) {
        chapter3DecisionMade = b;
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

    public boolean isTreasureFound() {
        return treasureFound;
    }

    public void setTreasureFound(boolean b) {
        treasureFound = b;
    }

    public boolean isOwnershipWarResolved() {
        return ownershipWarResolved;
    }

    public void setOwnershipWarResolved(boolean b) {
        ownershipWarResolved = b;
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
