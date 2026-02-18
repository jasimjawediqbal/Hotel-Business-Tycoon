import java.io.Serializable;

public class Employee implements Serializable {
    private String name;
    private String type; // "cleaner", "receptionist", "security", "manager"
    private int dailySalary;
    private int efficiency; // 0-100

    public Employee(String name, String type, int dailySalary) {
        this.name = name;
        this.type = type;
        this.dailySalary = dailySalary;
        this.efficiency = 50 + (int) (Math.random() * 30); // 50-80
    }

    public void improveEfficiency(int amount) {
        efficiency += amount;
        if (efficiency > 100)
            efficiency = 100;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getDailySalary() {
        return dailySalary;
    }

    public int getEfficiency() {
        return efficiency;
    }
}