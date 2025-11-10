import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class JavaChatClientMain extends JFrame {

  private JPanel contentPane;
  private RoundedTextField txtUserName;
  private RoundedTextField txtIpAddress;
  private RoundedTextField txtPortNumber;

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        try {
          JavaChatClientMain frame = new JavaChatClientMain();
          frame.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  /**
   * Create the frame.
   */
  public JavaChatClientMain() {
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 258, 390);

    // 1. [변경] 기존 JPanel 대신 ImagePanel 사용
    // "background.jpg" 부분에 실제 파일명을 쓰세요.
    contentPane = new ImagePanel("image/mint.jpg");

    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

    JLabel lblTitle = new JLabel("Mint Talk");
    lblTitle.setFont(new Font("Serif", Font.BOLD, 48));
    lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
    lblTitle.setBounds(12, 10, 218, 39);
    // 2. [추가] 라벨 배경을 투명하게 (Opaque = 불투명)
    lblTitle.setOpaque(false);
    // 3. [추가] 글자색을 설정 (배경이 어두우면 흰색, 밝으면 검은색)
    lblTitle.setForeground(Color.WHITE); // 배경 이미지에 맞춰 색상 변경
    contentPane.add(lblTitle);

    JLabel lblNewLabel = new JLabel("User Name");
    lblNewLabel.setBounds(67, 60, 120, 25);
    lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
    lblNewLabel.setOpaque(false); // [추가] 투명하게
    lblNewLabel.setForeground(Color.WHITE); // [추가] 글자색
    contentPane.add(lblNewLabel);

    txtUserName = new RoundedTextField();
    txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
    txtUserName.setBounds(67, 90, 120, 33);
    txtUserName.setBackground(Color.WHITE);
    contentPane.add(txtUserName);
    txtUserName.setColumns(10);
    // (JTextField는 투명하게 안해도 괜찮습니다)

    JLabel lblIpAddress = new JLabel("IP Address");
    lblIpAddress.setBounds(67, 138, 120, 25);
    lblIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
    lblIpAddress.setOpaque(false); // [추가] 투명하게
    lblIpAddress.setForeground(Color.WHITE); // [추가] 글자색
    contentPane.add(lblIpAddress);

    txtIpAddress = new RoundedTextField();
    txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
    txtIpAddress.setText("127.0.0.1");
    txtIpAddress.setColumns(10);
    txtIpAddress.setBounds(67, 168, 120, 33);
    txtIpAddress.setBackground(Color.WHITE);
    contentPane.add(txtIpAddress);

    JLabel lblPortNumber = new JLabel("Port Number");
    lblPortNumber.setBounds(67, 216, 120, 25);
    lblPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
    lblPortNumber.setOpaque(false); // [추가] 투명하게
    lblPortNumber.setForeground(Color.WHITE); // [추가] 글자색
    contentPane.add(lblPortNumber);

    txtPortNumber = new RoundedTextField();
    txtPortNumber.setText("30000");
    txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
    txtPortNumber.setColumns(10);
    txtPortNumber.setBounds(67, 246, 120, 33);
    txtPortNumber.setBackground(Color.WHITE);
    contentPane.add(txtPortNumber);

    JButton btnConnect = new RoundedButton("LOGIN");
    btnConnect.setFont(new Font("Tahoma", Font.BOLD, 14));
    btnConnect.setBounds(67, 300, 120, 38);

    // 버튼 스타일 (이전과 동일)
    btnConnect.setBackground(Color.white);
    btnConnect.setForeground(Color.black);
    btnConnect.setBorderPainted(false);
    btnConnect.setFocusPainted(false);




    contentPane.add(btnConnect);

    Myaction action = new Myaction();
    btnConnect.addActionListener(action);
    txtUserName.addActionListener(action);
    txtIpAddress.addActionListener(action);
    txtPortNumber.addActionListener(action);
  }

  class Myaction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      String username = txtUserName.getText().trim();
      String ip_addr = txtIpAddress.getText().trim();
      String port_no = txtPortNumber.getText().trim();
      JavaChatClientView view = new JavaChatClientView(username, ip_addr, port_no);
      setVisible(false);
    }
  }
}