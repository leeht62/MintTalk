import java.util.Vector;

public class ChatRoomInfo {
  public String roomName;
  public Vector<String> members;

  public ChatRoomInfo(String roomName, Vector<String> members) {
    this.roomName = roomName;
    this.members = members;
  }

  @Override
  public String toString() {
    return roomName + " (" + members.size() + "명)";
  }
}