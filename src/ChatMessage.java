// ChatMessage.java
// 역할: 채팅 메시지 1개의 정보를 저장하는 데이터 상자
public class ChatMessage {
    private String sender;    // 보낸사람
    private String message;   // 메시지 내용
    private boolean isMine;   // 내가 보낸 메시지인지 여부

    public ChatMessage(String sender, String message, boolean isMine) {
        this.sender = sender;
        this.message = message;
        this.isMine = isMine;
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
}