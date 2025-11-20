// ChatCellRenderer.java (전체 코드)
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JLabel lblSender = new JLabel();
    private final JTextArea txtMessage = new JTextArea();
    private final JLabel lblProfile = new JLabel(); // 프로필 사진 라벨
    
    private final JPanel pnlBubble = new JPanel(new BorderLayout()); 
    private final JPanel pnlAlign = new JPanel(new BorderLayout()); 

    public ChatCellRenderer() {
        super(new BorderLayout());
        setOpaque(false);

        // 1. 프로필 라벨 설정
        lblProfile.setPreferredSize(new Dimension(40, 40)); 
        lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
        lblProfile.setVerticalAlignment(SwingConstants.TOP);
        lblProfile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 

        // 2. 보낸사람 이름
        lblSender.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblSender.setOpaque(false);
        lblSender.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0)); 

        // 3. 메시지 내용
        txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        txtMessage.setEditable(false);
        txtMessage.setLineWrap(true);       
        txtMessage.setWrapStyleWord(true);  
        txtMessage.setMargin(new Insets(8, 10, 8, 10));

        // 4. 말풍선 패널
        pnlBubble.setOpaque(true);
        pnlBubble.add(txtMessage, BorderLayout.CENTER);

        // 5. 정렬 패널
        pnlAlign.setOpaque(false);
        add(pnlAlign, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
            boolean isSelected, boolean cellHasFocus) {
        
        pnlAlign.removeAll(); 
        pnlBubble.removeAll();
        pnlBubble.setLayout(new BorderLayout());
        pnlBubble.add(txtMessage, BorderLayout.CENTER);
        
        String sender = message.getSender();
        String msgContent = message.getMessage();
        
        lblSender.setText(sender);
        txtMessage.setText(msgContent);

        int listWidth = list.getWidth();
        if (listWidth == 0) listWidth = 300; 
        int maxWidth = (int)(listWidth * 0.65);
        
        txtMessage.setSize(new Dimension(maxWidth, Short.MAX_VALUE)); 
        Dimension prefSize = txtMessage.getPreferredSize();
        txtMessage.setSize(new Dimension(maxWidth, prefSize.height));

        // --- 정렬 로직 ---

        if ("System".equals(sender)) {
            // [System]
            lblSender.setText("");
            txtMessage.setBackground(new Color(200, 200, 200, 100));
            txtMessage.setForeground(Color.BLACK);
            txtMessage.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
            
            pnlBubble.setLayout(new FlowLayout(FlowLayout.CENTER)); 
            pnlBubble.add(txtMessage); 
            
            JPanel pnlCenter = new JPanel();
            pnlCenter.setOpaque(false);
            pnlCenter.add(pnlBubble);
            pnlAlign.add(pnlCenter, BorderLayout.CENTER);
            
        } else if (message.isMine()) {
            // [나]
            lblSender.setForeground(Color.BLACK);
            lblSender.setHorizontalAlignment(SwingConstants.RIGHT);
            
            txtMessage.setBackground(new Color(255, 235, 51)); 
            txtMessage.setForeground(Color.BLACK);
            
            JPanel pnlRight = new JPanel(new BorderLayout());
            pnlRight.setOpaque(false);
            pnlRight.add(lblSender, BorderLayout.NORTH);
            pnlRight.add(pnlBubble, BorderLayout.EAST);
            
            pnlAlign.add(pnlRight, BorderLayout.EAST);

        } else {
            // [상대방]
            lblSender.setForeground(Color.BLACK);
            lblSender.setHorizontalAlignment(SwingConstants.LEFT);

            txtMessage.setBackground(Color.WHITE); 
            txtMessage.setForeground(Color.BLACK);
            
            // 🚀 프로필 이미지 로드 (FriendList 로직 응용)
            // sender 이름을 넘기면 폴더에서 파일을 찾습니다.
            lblProfile.setIcon(getProfileIcon(message.getProfileImageName()));
            
            JPanel pnlContent = new JPanel(new BorderLayout());
            pnlContent.setOpaque(false);
            pnlContent.add(lblSender, BorderLayout.NORTH);
            pnlContent.add(pnlBubble, BorderLayout.WEST);
            
            pnlAlign.add(lblProfile, BorderLayout.WEST);
            pnlAlign.add(pnlContent, BorderLayout.CENTER);
        }

        return this;
    }
    
    // 🚀 [핵심] FriendList의 로직을 가져와서 강화한 메서드
    // 유저 이름(name)을 받아서 image/ 폴더 안의 파일을 찾습니다.
    private ImageIcon getProfileIcon(String name) {
        if (name == null || name.isEmpty()) name = "profile.jpg";
        
        // 1. 이름 자체가 파일명인 경우 (확장자 포함) 시도
        ImageIcon icon = loadIcon("image/" + name);
        if (icon != null) return icon;
        
        // 2. 이름에 확장자가 없는 경우, jpg/png 등을 붙여서 시도
        String[] exts = {".jpg", ".png", ".jpeg", ".gif"};
        for(String ext : exts) {
            icon = loadIcon("image/" + name + ext);
            if (icon != null) return icon;
        }

        // 3. 다 실패하면 기본 이미지
        icon = loadIcon("image/profile.jpg");
        if (icon != null) return icon;
        
        return null; // 정말 아무것도 없으면 null
    }

    // 파일 경로로 이미지를 불러와서 40x40으로 줄여주는 헬퍼 메서드
    private ImageIcon loadIcon(String path) {
        try {
            ImageIcon originalIcon = new ImageIcon(path);
            Image image = originalIcon.getImage();
            if (image.getWidth(null) != -1) { // 이미지가 정상적으로 로드되었는지 확인
                Image newimg = image.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(newimg);
            }
        } catch (Exception e) {
            // 로드 실패
        }
        return null;
    }
}