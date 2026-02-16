import java.util.ArrayList;
import java.util.List;

public class Hotel {
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
    //     day++;
    //     money -= dailyExpenses;
        
    //     for (Employee emp : employees) {
    //         money -= emp.getDailySalary();
    //     }
        
    //     // Decrement nights for occupied rooms
    //     for (Room room : rooms) {
    //         if (room.isOccupied()) {
    //             room.decrementNight();
    //             if (room.getNightsBooked() <= 0) {
    //                 Customer guest = room.checkOut();
    //                 changeReputation(guest.getReputationImpact());
    //             }
    //         }
    //     }
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
        if (reputation > 100) reputation = 100;
        if (reputation < 0) reputation = 0;
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
    public String getName() { return name; }
    public int getMoney() { return money; }
    public int getReputation() { return reputation; }
    public List<Room> getRooms() { return rooms; }
    public List<Employee> getEmployees() { return employees; }
    public int getDay() { return day; }
    public int getDailyExpenses() { 
        int total = dailyExpenses;
        for (Employee emp : employees) {
            total += emp.getDailySalary();
        }
        return total;
    }
    public int getTotalRevenue() { return totalRevenue; }
    
    // Story flags
    public boolean hasFoundOldRegister() { return foundOldRegister; }
    public void setFoundOldRegister(boolean found) { this.foundOldRegister = found; }
    public boolean hasMetRashid() { return metRashid; }
    public void setMetRashid(boolean met) { this.metRashid = met; }
    public boolean isExploringMystery() { return exploringMystery; }
    public void setExploringMystery(boolean exploring) { this.exploringMystery = exploring; }
}