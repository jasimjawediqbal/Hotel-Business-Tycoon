import java.io.Serializable;

public class Room implements Serializable {
    private int roomNumber;
    private String type;
    private RoomCondition condition;
    private boolean occupied;
    private Customer currentGuest;
    private int nightsBooked;

    public Room(int roomNumber, String type, RoomCondition condition) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.condition = condition;
        this.occupied = false;
        this.currentGuest = null;
        this.nightsBooked = 0;
    }

    public void checkIn(Customer guest, int nights) {
        this.occupied = true;
        this.currentGuest = guest;
        this.nightsBooked = nights;
    }

    public Customer checkOut() {
        Customer guest = this.currentGuest;
        this.occupied = false;
        this.currentGuest = null;
        this.nightsBooked = 0;
        return guest;
    }

    public void repair() {
        if (condition == RoomCondition.BROKEN) {
            condition = RoomCondition.POOR;
        } else if (condition == RoomCondition.VERY_POOR) {
            condition = RoomCondition.POOR;
        } else if (condition == RoomCondition.POOR) {
            condition = RoomCondition.GOOD;
        }
    }

    public int getBasePrice() {
        switch (condition) {
            case GOOD:
                return 100;
            case POOR:
                return 50;
            case VERY_POOR:
                return 30;
            case BROKEN:
                return 0;
            default:
                return 50;
        }
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public String getType() {
        return type;
    }

    public RoomCondition getCondition() {
        return condition;
    }

    public void setCondition(RoomCondition condition) {
        this.condition = condition;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Customer getCurrentGuest() {
        return currentGuest;
    }

    public int getNightsBooked() {
        return nightsBooked;
    }

    public boolean decrementNight() {
        if (nightsBooked > 0)
            nightsBooked--;
        return nightsBooked <= 0; 
    }
}