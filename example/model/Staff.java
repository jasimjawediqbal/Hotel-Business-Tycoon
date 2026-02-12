package model;

public class Staff {
    public String name;
    public StaffRole role;
    public int salary;
    public int morale = 100;
    public int unpaidDays = 0;

    public Staff(String name, StaffRole role, int salary) {
        this.name = name;
        this.role = role;
        this.salary = salary;
    }
}
