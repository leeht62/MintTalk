package friendlist;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class ProfileDetailDialog extends JDialog {
    private String username;
    private String targetName; 
    private DataOutputStream out;
    private boolean isMine;

    private JLabel lblProfileImg;
    private JLabel lblBgImg;
    private JTextField txtStatus;
    private JLabel lblName;

    public ProfileDetailDialog(JFrame owner, String username, String targetName,
                               String profileImg, String bgImg, String statusMsg,
                               DataOutputStream out) {
        super(owner, true); // 모달 창 설정
        this.username = username;
        this.targetName = targetName;
        this.out = out;
        this.isMine = username.equals(targetName);

        setTitle(targetName + "의 프로필");
        setSize(350, 500);
        setLocationRelativeTo(owner);
        setLayout(null);

        // 배경 이미지
        lblBgImg = new JLabel();
        lblBgImg.setBounds(0, 0, 350, 500);
        updateImage(lblBgImg, bgImg, 350, 500, "ab.jpg");

        // 내 프로필이 참값일때 직접 프로필 배경화면 수정가능
        if (isMine) {
            lblBgImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblBgImg.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        uploadImage(true);
                    }
                }
            });
            lblBgImg.setToolTipText("더블 클릭하여 배경 변경");
        }

        // 콘텐츠 패널 (투명)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBounds(0, 0, 350, 500);
        contentPanel.setOpaque(false);

        // 닫기 버튼
        JButton btnClose = new JButton("X");
        btnClose.setBounds(300, 10, 30, 30);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Arial", Font.BOLD, 15));
        btnClose.addActionListener(e -> dispose());
        contentPanel.add(btnClose);

        // 프로필 이미지
        lblProfileImg = new JLabel();
        int pSize = 90;
        lblProfileImg.setBounds((350 - pSize) / 2 - 8, 250, pSize, pSize);
        updateImage(lblProfileImg, profileImg, pSize, pSize, "profile.jpg");

        // 프로필 이미지 수정 가능
        if (isMine) {
            lblProfileImg.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            lblProfileImg.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    uploadImage(false); // false = 프로필 사진 변경
                }
            });
            lblProfileImg.setToolTipText("클릭하여 프로필 변경");
        }
        contentPanel.add(lblProfileImg);

        // 이름
        lblName = new JLabel(targetName);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Dialog", Font.BOLD, 18));
        lblName.setHorizontalAlignment(SwingConstants.CENTER);
        lblName.setBounds(0, 350, 335, 30);
        contentPanel.add(lblName);

        // 상태 메시지
        txtStatus = new JTextField(statusMsg);
        txtStatus.setBounds(40, 385, 255, 30);
        txtStatus.setHorizontalAlignment(SwingConstants.CENTER);
        txtStatus.setOpaque(false);
        txtStatus.setForeground(Color.WHITE);
        txtStatus.setBorder(null);
        txtStatus.setFont(new Font("Dialog", Font.PLAIN, 14));

        if (!isMine) {
            txtStatus.setEditable(false);
        } else {
            txtStatus.setToolTipText("엔터를 눌러 상태메시지 변경");
            txtStatus.addActionListener(e -> {
                try {
                    out.writeUTF("CHANGE_STATUS:" + username + ":" + txtStatus.getText());
                    out.flush();
                    JOptionPane.showMessageDialog(this, "상태메시지가 변경되었습니다.");
                    this.requestFocus();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
        contentPanel.add(txtStatus);

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

    // 이미지 변경 및 업로드
    private void uploadImage(boolean isBg) {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            File imageDir = new File("image");
            if (!imageDir.exists()) imageDir.mkdirs();

            String originalName = selectedFile.getName();
            String ext = "";
            int i = originalName.lastIndexOf('.');
            if (i > 0) ext = originalName.substring(i);

            String fileName;
            if (isBg) fileName = username + "_bg" + ext;
            else fileName = username + ext;

            File targetFile = new File(imageDir, fileName);

            try {
                copyFile(selectedFile, targetFile);

                String protocol = isBg ? "CHANGE_BG_IMAGE" : "CHANGE_PROFILE_IMAGE";
                out.writeUTF(protocol + ":" + username + ":" + fileName);
                out.flush();

                if (isBg) updateImage(lblBgImg, fileName, 350, 500, "ab.jpg");
                else updateImage(lblProfileImg, fileName, 90, 90, "profile.jpg");

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, e.getMessage());
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