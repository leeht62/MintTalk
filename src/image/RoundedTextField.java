package image;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * 둥근 모서리를 가진 JTextField
 */
public class RoundedTextField extends JTextField {

  private int cornerRadius = 15;

  public RoundedTextField() {
    super();
    setOpaque(false);

    setBorder(new EmptyBorder(5, 10, 5, 10));
  }

  @Override
  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // 배경 그리기 (getBackground() 색상으로 채움)
    g2.setColor(getBackground());
    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, cornerRadius, cornerRadius));

    // 텍스트 필드의 기본 기능(텍스트, 커서) 실행
    super.paintComponent(g);
    g2.dispose();
  }
}