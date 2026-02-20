import java.io.Serializable;

public class Employee implements Serializable {
    private String name;
    private String type; 
    private int dailySalary;
    private int efficiency; 

    public Employee(String name, String type, int dailySalary) {
        this.name = name;
        this.type = type;
        this.dailySalary = dailySalary;
        this.efficiency = 50 + (int) (Math.random() * 30); 
    }

    public void improveEfficiency(int amount) {
        efficiency += amount;
        if (efficiency > 100)
            efficiency = 100;
    }

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