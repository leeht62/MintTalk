package friendlist;

import chat.ChatMessage;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JLabel lblSender = new JLabel();
    private final JTextArea txtMessage = new JTextArea();
    private final JLabel lblProfile = new JLabel();
    private final JLabel lblContentImage = new JLabel(); 
    
    private final JPanel pnlBubble = new JPanel(new BorderLayout()); 
    private final JPanel pnlAlign = new JPanel(new BorderLayout()); 

    public ChatCellRenderer() {
        super(new BorderLayout());
        setOpaque(false);

        lblProfile.setPreferredSize(new Dimension(60, 40)); 
        lblProfile.setHorizontalAlignment(SwingConstants.CENTER);
        lblProfile.setVerticalAlignment(SwingConstants.TOP);
        lblProfile.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10)); 

        lblSender.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblSender.setOpaque(false);
        lblSender.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0)); 

        txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        txtMessage.setEditable(false);
        txtMessage.setLineWrap(true);       
        txtMessage.setWrapStyleWord(true);  
        txtMessage.setMargin(new Insets(8, 10, 8, 10));
        
        lblContentImage.setOpaque(true); 

        pnlBubble.setOpaque(true);
        pnlBubble.add(txtMessage, BorderLayout.CENTER);

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
        
        String sender = message.getSender();
        
        // 이미지 vs 텍스트
        if (message.isImage()) {
            lblContentImage.setIcon(message.getContentImage());
            pnlBubble.add(lblContentImage, BorderLayout.CENTER);
        } else {
            txtMessage.setText(message.getMessage());
            pnlBubble.add(txtMessage, BorderLayout.CENTER);
        }

        lblSender.setText(sender);

        int listWidth = list.getWidth();
        if (listWidth == 0) listWidth = 300; 
        int maxWidth = (int)(listWidth * 0.65);
        
        if (!message.isImage()) {
            txtMessage.setSize(new Dimension(maxWidth, Short.MAX_VALUE)); 
            Dimension prefSize = txtMessage.getPreferredSize();
            txtMessage.setSize(new Dimension(maxWidth, prefSize.height));
        }

        if ("System".equals(sender)) {
             lblSender.setText("");
             txtMessage.setBackground(new Color(200, 200, 200, 100));
             txtMessage.setText(message.getMessage()); 
             pnlBubble.removeAll();
             pnlBubble.add(txtMessage);
             pnlBubble.setLayout(new FlowLayout(FlowLayout.CENTER));
             
             JPanel pnlCenter = new JPanel();
             pnlCenter.setOpaque(false);
             pnlCenter.add(pnlBubble);
             pnlAlign.add(pnlCenter, BorderLayout.CENTER);

        } else if (message.isMine()) {
            lblSender.setHorizontalAlignment(SwingConstants.RIGHT);
            Color bubbleColor = new Color(255, 235, 51); 
            if (message.isImage()) lblContentImage.setBackground(bubbleColor);
            else txtMessage.setBackground(bubbleColor);
            
            JPanel pnlRight = new JPanel(new BorderLayout());
            pnlRight.setOpaque(false);
            pnlRight.add(lblSender, BorderLayout.NORTH);
            pnlRight.add(pnlBubble, BorderLayout.EAST);
            pnlAlign.add(pnlRight, BorderLayout.EAST);

        } else {
            lblSender.setHorizontalAlignment(SwingConstants.LEFT);
            Color bubbleColor = Color.WHITE;
            if (message.isImage()) lblContentImage.setBackground(bubbleColor);
            else txtMessage.setBackground(bubbleColor);
            
            // 프로필 이미지 로드
            ImageIcon icon = getProfileIcon(message.getProfileImageName());
            lblProfile.setIcon(icon);
            
            JPanel pnlContent = new JPanel(new BorderLayout());
            pnlContent.setOpaque(false);
            pnlContent.add(lblSender, BorderLayout.NORTH);
            pnlContent.add(pnlBubble, BorderLayout.WEST);
            
            pnlAlign.add(lblProfile, BorderLayout.WEST);
            pnlAlign.add(pnlContent, BorderLayout.CENTER);
        }

        return this;
    }
    
    private ImageIcon getProfileIcon(String name) {
        if (name == null || name.isEmpty()) name = "profile.jpg";
        
        File f = new File("image/" + name);
        if(f.exists()) return loadIcon(f);
        
        String[] exts = {".jpg", ".png", ".jpeg", ".gif"};
        for(String ext : exts) {
            File fExt = new File("image/" + name + ext);
            if(fExt.exists()) return loadIcon(fExt);
        }
        
        return loadIcon(new File("image/profile.jpg"));
    }

    private ImageIcon loadIcon(File file) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image != null) {
                return new ImageIcon(image.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
            }
        } catch (Exception e) {}
        return null;
    }
}