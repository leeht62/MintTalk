import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.io.File; // [필수] 파일 확인용 import

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
        
        // 패널 초기화
        pnlAlign.removeAll(); 
        pnlBubble.removeAll();
        pnlBubble.setLayout(new BorderLayout());
        pnlBubble.add(txtMessage, BorderLayout.CENTER);
        
        String sender = message.getSender();
        String msgContent = message.getMessage();
        
        lblSender.setText(sender);
        txtMessage.setText(msgContent);

        // 리스트 너비 처리
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
            
            // 🚀 프로필 이미지 로드
            // message.getProfileImageName()에는 "sender 이름"이 들어있음
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
    
    // 🚀 [핵심] 이름(name)을 받아 image/ 폴더에서 파일을 찾는 메서드
    private ImageIcon getProfileIcon(String name) {
        if (name == null || name.isEmpty()) name = "profile.jpg";
        
        // 1. 이름 자체가 파일명일 경우 (확장자가 이미 있는 경우)
        File f = new File("image/" + name);
        if(f.exists()) return loadIcon("image/" + name);
        
        // 2. 이름에 확장자가 없는 경우 -> .jpg, .png 등을 붙여서 찾음
        String[] exts = {".jpg", ".png", ".jpeg", ".gif"};
        for(String ext : exts) {
            File fExt = new File("image/" + name + ext);
            if(fExt.exists()) {
                return loadIcon("image/" + name + ext);
            }
        }

        // 3. 다 실패하면 기본 이미지
        return loadIcon("image/profile.jpg");
    }

    // 파일 로드 및 리사이징 헬퍼
    private ImageIcon loadIcon(String path) {
        try {
            ImageIcon originalIcon = new ImageIcon(path);
            Image image = originalIcon.getImage();
            if (image.getWidth(null) != -1) {
                Image newimg = image.getScaledInstance(40, 40, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(newimg);
            }
        } catch (Exception e) { }
        return null;
    }
}