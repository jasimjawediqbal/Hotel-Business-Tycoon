package model;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    public int day;
    public int money;
    public int reputation;
    public Hotel hotel;
    public List<Staff> staffList;

    public boolean ledgerInvestigated;
    public boolean ghostRoomSolved;
    public boolean treasureFound;
    public boolean soldHotel;
    public boolean rashidDebtSolved;
    public boolean rivalDefeated;

    public GameState() {
        day = 1;
        money = 500;
        reputation = 20;
        hotel = new Hotel();
        staffList = new ArrayList<>();
        staffList.add(new Staff("Rashid", StaffRole.RECEPTIONIST, 50));
        staffList.add(new Staff("Maya", StaffRole.CLEANER, 30));
    }

    public void nextDay() {
        day++;
        money -= hotel.calculateMaintenance();
        updateStaffMorale();
    }

    private void updateStaffMorale() {
        for (Staff s : staffList) {
            if (s.unpaidDays > 0) s.morale -= 10;
        }
    }

    public void payStaffSalaries() {
        for (Staff s : staffList) {
            if (money >= s.salary) {
                money -= s.salary;
                s.morale += 10;
                s.unpaidDays = 0;
            } else {
                s.unpaidDays++;
            }
        }
    }

    public boolean isBankrupt() { return money <= 0; }
}
