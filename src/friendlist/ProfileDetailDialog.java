package friendlist;

import javax.swing.*;
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
	    System.out.println("============== [이미지 업로드 시작] ==============");
	    
	    JFileChooser fileChooser = new JFileChooser();
	    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	        
	        // 1. 선택한 파일 확인
	        File selectedFile = fileChooser.getSelectedFile();
	        System.out.println("[1] 사용자가 선택한 파일: " + selectedFile.getAbsolutePath());

	        // 2. 저장할 폴더 확인
	        String projectPath = System.getProperty("user.dir");
	        File imageDir = new File(projectPath, "image");
	        
	        System.out.println("[2] 저장될 폴더 위치: " + imageDir.getAbsolutePath());

	        if (!imageDir.exists()) {
	            System.out.println("[알림] image 폴더가 없어서 새로 만듭니다.");
	            imageDir.mkdirs();
	        }

	        // 🚀 [핵심 변경] 원본 이름 대신 '아이디'로 파일명 만들기
	        String originalName = selectedFile.getName();
	        String extension = "";
	        
	        // 확장자(.jpg, .png)만 추출하기
	        int dotIndex = originalName.lastIndexOf('.');
	        if (dotIndex >= 0) {
	            extension = originalName.substring(dotIndex); 
	        } else {
	            extension = ".jpg"; // 확장자가 없으면 강제로 .jpg 붙임
	        }

	        // 최종 파일명 결정 (중요!)
	        // 프로필이면 "아이디.jpg", 배경이면 "아이디_bg.jpg"로 저장 (서로 안 덮어쓰게)
	        String fileName;
	        if (isBg) {
	            fileName = username + "_bg" + extension; 
	        } else {
	            fileName = username + extension; // 여기가 원하시던 "ioi.jpg" 부분!
	        }

	        // 3. 타겟 파일 설정
	        File targetFile = new File(imageDir, fileName);
	        System.out.println("[3] 최종 저장될 경로(이름 변경됨): " + targetFile.getAbsolutePath());

	        try {
	            // 4. 복사 실행
	            System.out.println("[4] 파일 복사를 시도합니다...");
	            copyFile(selectedFile, targetFile);
	            System.out.println("[성공] 파일 복사 완료! 파일 존재 확인 -> " + targetFile.exists());

	            // 5. 서버 전송
	            String protocol = isBg ? "CHANGE_BG_IMAGE" : "CHANGE_PROFILE_IMAGE";
	            System.out.println("[5] 서버로 변경 요청 전송: " + protocol + ":" + username + ":" + fileName);
	            
	            out.writeUTF(protocol + ":" + username + ":" + fileName);
	            out.flush();

	            // 6. 내 화면 즉시 갱신
	            if (isBg) updateImage(lblBgImg, fileName, 350, 500, "ab.jpg");
	            else updateImage(lblProfileImg, fileName, 90, 90, "profile.jpg");

	        } catch (Exception e) {
	            System.err.println("!!!!!!!!!! [에러 발생] !!!!!!!!!!!");
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(this, "실패: " + e.getMessage());
	        }
	    } else {
	        System.out.println("[취소] 사용자가 파일 선택을 취소했습니다.");
	    }
	    System.out.println("===============================================");
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