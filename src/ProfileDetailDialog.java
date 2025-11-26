import java.io.FileInputStream;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;

public class ProfileDetailDialog extends JDialog {
  private String username;
  private String targetName; // 보고 있는 프로필의 주인 이름
  private DataOutputStream out;
  private boolean isMine; // 내 프로필인지 여부

  private JLabel lblProfileImg;
  private JLabel lblBgImg;
  private JTextField txtStatus;
  private JLabel lblName;

  // 현재 정보 임시 저장
  private String currentProfileImg;
  private String currentBgImg;

  public ProfileDetailDialog(JFrame owner, String username, String targetName,
                             String profileImg, String bgImg, String statusMsg,
                             DataOutputStream out) {
    super(owner, true); // 모달 창 (뒤에꺼 클릭 불가)
    this.username = username;
    this.targetName = targetName;
    this.out = out;
    this.currentProfileImg = profileImg;
    this.currentBgImg = bgImg;
    this.isMine = username.equals(targetName);

    setTitle(targetName + "의 프로필");
    setSize(350, 500);
    setLocationRelativeTo(owner);
    setLayout(null);

    // --- 1. 배경 이미지 (가장 뒤) ---
    lblBgImg = new JLabel();
    lblBgImg.setBounds(0, 0, 350, 500);
    updateImage(lblBgImg, bgImg, 350, 500, "ab.jpg");

    // 내 프로필이면 배경 클릭 시 변경 가능
    if (isMine) {
      lblBgImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblBgImg.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
          if (evt.getClickCount() == 2) { // 더블 클릭시 배경 변경
            uploadImage(true); // true = 배경
          }
        }
      });
      lblBgImg.setToolTipText("더블 클릭하여 배경 변경");
    }

    // 내용을 담을 투명 패널 (배경 위에 올라감)
    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(null);
    contentPanel.setBounds(0, 0, 350, 500);
    contentPanel.setOpaque(false);

    // --- 2. 닫기 버튼 (우상단) ---
    JButton btnClose = new JButton("X");
    btnClose.setBounds(300, 10, 30, 30);
    btnClose.setBorderPainted(false);
    btnClose.setContentAreaFilled(false);
    btnClose.setForeground(Color.WHITE);
    btnClose.setFont(new Font("Arial", Font.BOLD, 15));
    btnClose.addActionListener(e -> dispose());
    contentPanel.add(btnClose);

    // --- 3. 프로필 이미지 (하단 중앙) ---
    lblProfileImg = new JLabel();
    int pSize = 90;
    lblProfileImg.setBounds((350 - pSize) / 2 - 8, 250, pSize, pSize); // 위치 조정
    updateImage(lblProfileImg, profileImg, pSize, pSize, "profile.jpg");

    if (isMine) {
      lblProfileImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lblProfileImg.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
          uploadImage(false); // false = 프로필
        }
      });
      lblProfileImg.setToolTipText("클릭하여 프로필 변경");
    }
    contentPanel.add(lblProfileImg);

    // --- 4. 이름 (프로필 아래) ---
    lblName = new JLabel(targetName);
    lblName.setForeground(Color.WHITE);
    lblName.setFont(new Font("Dialog", Font.BOLD, 18));
    lblName.setHorizontalAlignment(SwingConstants.CENTER);
    lblName.setBounds(0, 350, 335, 30);
    contentPanel.add(lblName);

    // --- 5. 상태 메시지 (이름 아래) ---
    txtStatus = new JTextField(statusMsg);
    txtStatus.setBounds(40, 385, 255, 30);
    txtStatus.setHorizontalAlignment(SwingConstants.CENTER);
    txtStatus.setOpaque(false);
    txtStatus.setForeground(Color.WHITE);
    txtStatus.setBorder(null); // 테두리 제거
    txtStatus.setFont(new Font("Dialog", Font.PLAIN, 14));

    if (!isMine) {
      txtStatus.setEditable(false);
    } else {
      // 내 프로필이면 하단에 밑줄이나 힌트 표시 (여기선 간단히 툴팁)
      txtStatus.setToolTipText("엔터를 눌러 상태메시지 변경");
      txtStatus.addActionListener(e -> {
        String newMsg = txtStatus.getText();
        try {
          out.writeUTF("CHANGE_STATUS:" + username + ":" + newMsg);
          out.flush();
          JOptionPane.showMessageDialog(this, "상태메시지가 변경되었습니다.");
          // 포커스 해제 효과
          this.requestFocus();
        } catch (IOException ex) {
          ex.printStackTrace();
        }
      });
    }
    contentPanel.add(txtStatus);

    // --- 6. 하단 버튼 패널 (나와의 채팅 등) ---
    // (필요 시 추가, 여기서는 생략)

    // 패널 조립 (순서 중요: 배경 -> 콘텐츠)
    getLayeredPane().add(lblBgImg, JLayeredPane.DEFAULT_LAYER);
    getLayeredPane().add(contentPanel, JLayeredPane.PALETTE_LAYER);

    setVisible(true);
  }

  private void updateImage(JLabel label, String imgName, int w, int h, String defaultImg) {
    try {
      ImageIcon icon = new ImageIcon("image/" + imgName);
      if (icon.getIconWidth() == -1) icon = new ImageIcon("image/" + defaultImg);
      Image img = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
      label.setIcon(new ImageIcon(img));
    } catch (Exception e) {
      label.setText("Img");
    }
  }

  private void uploadImage(boolean isBg) {
    JFileChooser fileChooser = new JFileChooser();
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selectedFile = fileChooser.getSelectedFile();
      File imageDir = new File("image");
      if (!imageDir.exists()) imageDir.mkdirs();

      String fileName = selectedFile.getName();
      File targetFile = new File(imageDir, fileName);

      try {
        // 파일 복사
        copyFile(selectedFile, targetFile);

        // 서버 전송 프로토콜 결정
        String protocol = isBg ? "CHANGE_BG_IMAGE" : "CHANGE_PROFILE_IMAGE";
        out.writeUTF(protocol + ":" + username + ":" + fileName);
        out.flush();

        // 즉시 UI 반영
        if (isBg) updateImage(lblBgImg, fileName, 350, 500, "ab.jpg");
        else updateImage(lblProfileImg, fileName, 90, 90, "profile.jpg");

      } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "이미지 변경 실패: " + e.getMessage());
      }
    }
  }

  private void copyFile(File source, File dest) throws IOException {
    try (InputStream is = new FileInputStream(source);
         OutputStream os = new FileOutputStream(dest)) {
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) os.write(buffer, 0, length);
    }
  }
}