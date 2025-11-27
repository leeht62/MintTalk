package friendlist;

import chat.ChatMessage;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JLabel lblSender = new JLabel();
    private final JTextArea txtMessage = new JTextArea();
    private final JLabel lblProfile = new JLabel();
    
    // 🚀 [추가] 이미지를 보여줄 라벨
    private final JLabel lblContentImage = new JLabel(); 
    
    private final JPanel pnlBubble = new JPanel(new BorderLayout()); 
    private final JPanel pnlAlign = new JPanel(new BorderLayout()); 

    public ChatCellRenderer() {
        super(new BorderLayout());
        setOpaque(false);

        // ... (lblProfile, lblSender 설정은 기존과 동일) ...
        lblProfile.setPreferredSize(new Dimension(60, 40)); 
        lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
        lblProfile.setVerticalAlignment(SwingConstants.TOP);
        lblProfile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 

        lblSender.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblSender.setOpaque(false);
        lblSender.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0)); 

        // 텍스트 메시지 설정
        txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        txtMessage.setEditable(false);
        txtMessage.setLineWrap(true);       
        txtMessage.setWrapStyleWord(true);  
        txtMessage.setMargin(new Insets(8, 10, 8, 10));
        
        // 🚀 [추가] 이미지 메시지 설정
        lblContentImage.setOpaque(true); // 배경색 적용을 위해 true

        // 말풍선 패널 초기화
        pnlBubble.setOpaque(true);
        // 기본은 텍스트 추가
        pnlBubble.add(txtMessage, BorderLayout.CENTER);

        pnlAlign.setOpaque(false);
        add(pnlAlign, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        
        pnlAlign.removeAll(); 
        pnlBubble.removeAll(); // 내용 초기화
        pnlBubble.setLayout(new BorderLayout());
        
        String sender = message.getSender();
        
        // 🚀 [핵심] 텍스트냐 이미지냐에 따라 말풍선 내용 결정
        if (message.isImage()) {
            // 이미지일 경우
            lblContentImage.setIcon(message.getContentImage());
            pnlBubble.add(lblContentImage, BorderLayout.CENTER);
        } else {
            // 텍스트일 경우
            txtMessage.setText(message.getMessage());
            pnlBubble.add(txtMessage, BorderLayout.CENTER);
        }

        lblSender.setText(sender);

        // 리스트 너비 설정
        int listWidth = list.getWidth();
        if (listWidth == 0) listWidth = 300; 
        int maxWidth = (int)(listWidth * 0.65);
        
        // 텍스트 크기 조정 (이미지는 크기 고정이므로 패스)
        if (!message.isImage()) {
            txtMessage.setSize(new Dimension(maxWidth, Short.MAX_VALUE)); 
            Dimension prefSize = txtMessage.getPreferredSize();
            txtMessage.setSize(new Dimension(maxWidth, prefSize.height));
        }

        // --- 정렬 로직 (배경색 등) ---
        
        Color bubbleColor;
        
        if ("System".equals(sender)) {
            // ... (시스템 메시지 처리 - 기존 동일) ...
             lblSender.setText("");
             txtMessage.setBackground(new Color(200, 200, 200, 100));
             txtMessage.setText(message.getMessage()); // 시스템은 무조건 텍스트
             pnlBubble.add(txtMessage);
             
             pnlBubble.setLayout(new FlowLayout(FlowLayout.CENTER));
             
             JPanel pnlCenter = new JPanel();
             pnlCenter.setOpaque(false);
             pnlCenter.add(pnlBubble);
             pnlAlign.add(pnlCenter, BorderLayout.CENTER);
             return this;

        } else if (message.isMine()) {
            // [나]
            bubbleColor = new Color(255, 235, 51); // 노란색
            lblSender.setHorizontalAlignment(SwingConstants.RIGHT);
            
            JPanel pnlRight = new JPanel(new BorderLayout());
            pnlRight.setOpaque(false);
            pnlRight.add(lblSender, BorderLayout.NORTH);
            pnlRight.add(pnlBubble, BorderLayout.EAST);
            pnlAlign.add(pnlRight, BorderLayout.EAST);

        } else {
            // [상대방]
            bubbleColor = Color.WHITE; // 흰색
            lblSender.setHorizontalAlignment(SwingConstants.LEFT);
            
            // 프로필 이미지
            lblProfile.setIcon(getProfileIcon(message.getProfileImageName()));
            
            JPanel pnlContent = new JPanel(new BorderLayout());
            pnlContent.setOpaque(false);
            pnlContent.add(lblSender, BorderLayout.NORTH);
            pnlContent.add(pnlBubble, BorderLayout.WEST);
            
            pnlAlign.add(lblProfile, BorderLayout.WEST);
            pnlAlign.add(pnlContent, BorderLayout.CENTER);
        }

        // 말풍선 색상 적용
        if (message.isImage()) {
            lblContentImage.setBackground(bubbleColor);
        } else {
            txtMessage.setBackground(bubbleColor);
        }

        return this;
    }
    
    // ... (getProfileIcon, loadIcon 등 기존 메서드 그대로 유지) ...
    private ImageIcon getProfileIcon(String name) {
        if (name == null || name.isEmpty()) name = "profile.jpg";
        File f = new File("image/" + name);
        if(f.exists()) return loadIcon("image/" + name);
        String[] exts = {".jpg", ".png", ".jpeg", ".gif"};
        for(String ext : exts) {
            File fExt = new File("image/" + name + ext);
            if(fExt.exists()) return loadIcon("image/" + name + ext);
        }
        return loadIcon("image/profile.jpg");
    }
    private ImageIcon loadIcon(String path) {
        try {
            ImageIcon originalIcon = new ImageIcon(path);
            Image image = originalIcon.getImage();
            if (image.getWidth(null) != -1) {
                Image newimg = image.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                return new ImageIcon(newimg);
            }
        } catch (Exception e) { }
        return null;
    }
}