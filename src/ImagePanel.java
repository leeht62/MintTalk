import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
  private Image backgroundImage;
  private int offsetX = 0; // 기본값은 0 (조정 없음)
  private int offsetY = 0;

  // 생성자 1: 단순히 경로만 넣을 때 (FriendList 등 일반적인 경우) -> 오프셋 0
  public ImagePanel(String imagePath) {
    this(imagePath, 0, 0);
  }

  // 생성자 2: 위치 조정이 필요할 때 (ChatWindow 등 특수한 경우)
  public ImagePanel(String imagePath, int offsetX, int offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    try {
      backgroundImage = ImageIO.read(new File(imagePath));
    } catch (IOException e) {
      System.err.println("이미지 로드 실패: " + imagePath);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (backgroundImage != null) {
      // 설정된 오프셋(offsetX, offsetY)을 반영하여 그리기
      g.drawImage(backgroundImage,
          offsetX, offsetY,
          getWidth() - offsetX, getHeight() - offsetY,
          this);
    }
  }
}