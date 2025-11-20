// ChatMessage.java (전체 코드)
public class ChatMessage {
    private String sender;    // 보낸사람
    private String message;   // 메시지 내용
    private boolean isMine;   // 내가 보낸 메시지인지 여부
    private String profileImageName; // 프로필 이미지 파일명 (여기선 유저 이름이 들어감)

    public ChatMessage(String sender, String message, boolean isMine, String profileImageName) {
        this.sender = sender;
        this.message = message;
        this.isMine = isMine;
        this.profileImageName = profileImageName;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public boolean isMine() {
        return isMine;
    }
    
    public String getProfileImageName() {
        return profileImageName;
    }
}