package image;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ImagePanel extends JPanel {
    private Image backgroundImage;
    private int offsetX = 0; 
    private int offsetY = 0;

    // 생성자 1
    public ImagePanel(String imagePath) {
        this(imagePath, 0, 0);
    }

    // 생성자 2
    public ImagePanel(String imagePath, int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        loadImage(imagePath); // 이미지 로드 메서드 호출
    }

    // 🚀 [추가됨] 외부에서 이미지를 변경할 때 호출하는 메서드
    // FriendList에서 배경화면 바꿀 때 이 메서드를 사용합니다.
    public void setImagePath(String imagePath) {
        loadImage(imagePath);
        this.repaint(); // 이미지가 바뀌었으니 화면을 다시 그립니다.
    }

    // 이미지 로드 로직을 분리 (생성자와 setImagePath에서 같이 쓰기 위해)
    private void loadImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()) return;
            
            File f = new File(imagePath);
            if (f.exists()) {
                backgroundImage = ImageIO.read(f);
            } else {
                // 파일이 없으면 에러 메시지 출력 (혹은 기본 이미지 로드 로직 추가 가능)
                System.err.println("이미지 파일을 찾을 수 없습니다: " + imagePath);
            }
        } catch (IOException e) {
            System.err.println("이미지 로드 실패: " + imagePath);
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // 설정된 오프셋을 반영하여 그리기
            g.drawImage(backgroundImage,
                    offsetX, offsetY,
                    getWidth() - offsetX, getHeight() - offsetY,
                    this);
        }
    }
}