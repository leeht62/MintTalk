// RoundedTextField.java
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * 둥근 모서리를 가진 JTextField
 */
public class RoundedTextField extends JTextField {

  // 모서리의 둥근 정도 (숫자가 클수록 더 둥글어짐)
  private int cornerRadius = 15;

  public RoundedTextField() {
    super();
    setOpaque(false); // 배경은 우리가 직접 그릴 것이므로

    // 텍스트가 둥근 모서리에 닿지 않도록 안쪽 여백(padding)을 줌
    // (위: 5, 왼쪽: 10, 아래: 5, 오른쪽: 10)
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