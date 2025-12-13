package chat;

import javax.swing.ImageIcon;

public class ChatMessage {
    private String sender;
    private String message;
    private boolean isMine;
    private String profileImageName;
    
    // 이미지 전송 기능을 위한 필드
    private boolean isImage;
    ImageIcon contentImage;

    // 모든 정보를 다 받는 생성자
    public ChatMessage(String sender, String message, boolean isMine, String profileImageName, boolean isImage, ImageIcon contentImage) {
        this.sender = sender;
        this.message = message;
        this.isMine = isMine;
        this.profileImageName = profileImageName;
        this.isImage = isImage;
        this.contentImage = contentImage;
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

    public boolean isImage() {
        return isImage;
    }

    public ImageIcon getContentImage() {
        return contentImage;
    }
}