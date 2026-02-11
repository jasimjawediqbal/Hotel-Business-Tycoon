package model;

import java.util.ArrayList;
import java.util.List;

public class Hotel {
    private List<Room> rooms;

    public Hotel() {
        rooms = new ArrayList<>();
        rooms.add(new Room(1, RoomType.BASIC, false));
        rooms.add(new Room(2, RoomType.BASIC, false));
        rooms.add(new Room(3, RoomType.STANDARD, false));
        rooms.add(new Room(4, RoomType.STANDARD, false));
        rooms.add(new Room(5, RoomType.APARTMENT, false));
        rooms.add(new Room(6, RoomType.BASIC, true)); // Ghost room
    }

    public List<Room> getRooms() { return rooms; }

    public int calculateMaintenance() { return rooms.size() * 10; }

    public Room getRoomById(int id) {
        for (Room r : rooms) if (r.id == id) return r;
        return null;
    }
}
