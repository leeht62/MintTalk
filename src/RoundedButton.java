// RoundedButton.java
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * 둥근 모서리를 가진 JButton
 */
public class RoundedButton extends JButton {

  // 모서리의 둥근 정도
  private int cornerRadius = 15;

  public RoundedButton(String text) {
    super(text);
    setOpaque(false);
    setContentAreaFilled(false); // 버튼의 기본 배경 채우기 비활성화
    setFocusPainted(false);      // 포커스 테두리 비활성화
    setBorderPainted(false);     // 기본 테두리 비활성화
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // 버튼 상태에 따른 배경색 설정
    if (getModel().isPressed()) {
      // 버튼이 눌렸을 때 (기존 배경색보다 조금 어둡게)
      g2.setColor(getBackground().darker());
    } else if (getModel().isRollover()) {
      // 마우스가 올라갔을 때 (기존 배경색보다 조금 밝게)
      g2.setColor(getBackground().brighter());
    } else {
      // 평상시 (설정된 배경색)
      g2.setColor(getBackground());
    }

    // 둥근 사각형 배경 그리기
    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));

    // 텍스트를 그리도록 super.paintComponent() 호출 (이게 텍스트를 그려줌)
    super.paintComponent(g);

    g2.dispose();
  }
}