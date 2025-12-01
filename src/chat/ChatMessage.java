package chat;

import javax.swing.ImageIcon;

public class ChatMessage {
    private String sender;           // 보낸 사람 이름
    private String message;          // 텍스트 메시지 내용
    private boolean isMine;          // 내가 보낸 메시지인가? (오른쪽/왼쪽 배치용)
    private String profileImageName; // 프로필 이미지 파일명 (보통 sender와 동일)
    
    // 이미지 전송 기능을 위한 필드
    private boolean isImage;         // 이 메시지가 텍스트가 아니라 이미지인가?
    private ImageIcon contentImage;  // 실제 이미지 데이터 (사진/이모티콘)

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