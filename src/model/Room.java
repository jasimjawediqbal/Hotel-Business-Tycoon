package model;

public class Room {
    public int id;
    public RoomType type;
    public RoomStatus status;
    public boolean isGhostRoom;

    public Room(int id, RoomType type, boolean ghost) {
        this.id = id;
        this.type = type;
        this.isGhostRoom = ghost;
        this.status = RoomStatus.BROKEN;
    }
}
