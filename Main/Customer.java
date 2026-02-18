import java.util.Random;
import java.io.Serializable;

public class Customer implements Serializable {
    private String name;
    private int paymentAmount;
    private int satisfactionLevel; // 0-100
    private boolean willLeaveReview;

    private static final String[] FIRST_NAMES = {
            "Ahmed", "Fatima", "Ali", "Sara", "Hassan", "Zainab", "Omar", "Amina",
            "Ibrahim", "Layla", "Yusuf", "Maryam", "Bilal", "Aisha", "Tariq", "Noor"
    };

    private static final String[] LAST_NAMES = {
            "Khan", "Ahmed", "Ali", "Hassan", "Mahmood", "Shah", "Malik", "Rashid",
            "Siddiqui", "Hussain", "Iqbal", "Raza", "Abbas", "Naqvi", "Zaidi"
    };

    public Customer() {
        Random rand = new Random();
        this.name = FIRST_NAMES[rand.nextInt(FIRST_NAMES.length)] + " " +
                LAST_NAMES[rand.nextInt(LAST_NAMES.length)];
        this.satisfactionLevel = 50; // Neutral
        this.willLeaveReview = rand.nextBoolean();
    }

    public void setPayment(int amount) {
        this.paymentAmount = amount;
    }

    public void adjustSatisfaction(int change) {
        satisfactionLevel += change;
        if (satisfactionLevel > 100)
            satisfactionLevel = 100;
        if (satisfactionLevel < 0)
            satisfactionLevel = 0;
    }

    public int getReputationImpact() {
        if (!willLeaveReview)
            return 0;

        if (satisfactionLevel >= 80)
            return 3;
        else if (satisfactionLevel >= 60)
            return 1;
        else if (satisfactionLevel >= 40)
            return 0;
        else if (satisfactionLevel >= 20)
            return -1;
        else
            return -3;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getPaymentAmount() {
        return paymentAmount;
    }

    public int getSatisfactionLevel() {
        return satisfactionLevel;
    }

    public boolean willLeaveReview() {
        return willLeaveReview;
    }

    // Main method for testing
    public static void main(String[] args) {
        System.out.println("Testing Customer class:");
        System.out.println("=======================");

        // Create and test multiple customers
        for (int i = 1; i <= 5; i++) {
            Customer customer = new Customer();
            customer.setPayment(100 + (i * 20)); // Different payment amounts

            System.out.println("\nCustomer " + i + ":");
            System.out.println("Name: " + customer.getName());
            System.out.println("Payment Amount: $" + customer.getPaymentAmount());
            System.out.println("Satisfaction Level: " + customer.getSatisfactionLevel() + "/100");
            System.out.println("Will Leave Review: " + customer.willLeaveReview());

            // Test satisfaction adjustment
            customer.adjustSatisfaction(20);
            System.out.println("After +20 satisfaction: " + customer.getSatisfactionLevel() + "/100");
            System.out.println("Reputation Impact: " + customer.getReputationImpact());
        }
    }
}