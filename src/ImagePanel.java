// ImagePanel.java
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

// JPanel을 상속받아, 이미지를 그리는 기능을 추가한 패널
public class ImagePanel extends JPanel {

  private Image backgroundImage; // 배경으로 사용할 이미지 객체

  // 생성자: 이미지 파일 경로를 받아서 이미지를 로드합니다.
  public ImagePanel(String imagePath) {
    try {
      // 파일을 읽어서 이미지 객체로 변환
      backgroundImage = ImageIO.read(new File(imagePath));
    } catch (IOException e) {
      System.err.println("배경 이미지 로드 실패: " + e.getMessage());
      e.printStackTrace();
    }
  }

  // paintComponent 메서드 수정
  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g); // JPanel이 원래 하던 작업을 먼저 수행

    if (backgroundImage != null) {
      int offsetX = -7; // 오른쪽으로 이동할 픽셀 수
      int offsetY = 0; // 위/아래 이동은 0 (필요하면 조절)

      // 이미지를 (offsetX, offsetY) 위치에서부터 그립니다.
      // 너비는 패널 너비에서 offsetX만큼 빼주고, 높이는 패널 높이와 동일하게 유지합니다.
      // 이렇게 하면 왼쪽 2픽셀이 잘려나가는 효과가 나면서, 이미지가 오른쪽으로 이동한 것처럼 보입니다.
      g.drawImage(backgroundImage, offsetX, offsetY, getWidth() - offsetX, getHeight() - offsetY, this);
    }
  }
}