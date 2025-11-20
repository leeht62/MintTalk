import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

public class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JLabel lblSender = new JLabel();
    private final JTextArea txtMessage = new JTextArea();
    private final JPanel pnlBubble = new JPanel(new BorderLayout()); 
    private final JPanel pnlAlign = new JPanel(new BorderLayout()); 

    public ChatCellRenderer() {
        super(new BorderLayout());
        setOpaque(false); // 투명 배경

        // 1. 보낸사람 라벨
        lblSender.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblSender.setOpaque(false);

        // 2. 메시지 내용
        txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        txtMessage.setEditable(false);
        txtMessage.setLineWrap(true);       
        txtMessage.setWrapStyleWord(true);  
        txtMessage.setMargin(new Insets(8, 10, 8, 10));

        // 3. 말풍선 패널
        pnlBubble.setOpaque(true);
        pnlBubble.add(txtMessage, BorderLayout.CENTER);

        // 4. 정렬 패널
        pnlAlign.setOpaque(false);
        add(pnlAlign, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); 
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
            boolean isSelected, boolean cellHasFocus) {
        
        // 초기화
        pnlAlign.removeAll(); 
        
        String sender = message.getSender();
        String msgContent = message.getMessage();
        
        lblSender.setText(sender);
        txtMessage.setText(msgContent);

        // 🚀 [수정 1] 리스트 너비가 0일 때(초기 로딩) 기본값(300)을 줘서 세로 국수 현상 방지
        int listWidth = list.getWidth();
        if (listWidth == 0) listWidth = 300; 

        // 말풍선 최대 너비 설정 (리스트의 70%)
        int maxWidth = (int)(listWidth * 0.7);
        
        // 🚀 [수정 2] JTextArea 크기 강제 계산 (이게 있어야 줄바꿈이 정상 작동)
        txtMessage.setSize(new Dimension(maxWidth, Short.MAX_VALUE)); 
        Dimension prefSize = txtMessage.getPreferredSize();
        txtMessage.setSize(new Dimension(maxWidth, prefSize.height));

        // --- 스타일링 및 정렬 로직 ---

        if ("System".equals(sender)) {
            // [CASE 1] 시스템 메시지 (가운데 정렬)
            lblSender.setText(""); // 시스템은 이름 숨김 (내용만 표시)
            
            txtMessage.setBackground(new Color(200, 200, 200, 100)); // 연한 회색, 반투명
            txtMessage.setForeground(Color.BLACK);
            txtMessage.setFont(new Font("Malgun Gothic", Font.BOLD, 12)); // 약간 작게
            
            // 내용도 가운데 정렬처럼 보이게 트릭 (패널 자체를 가운데로)
            JPanel pnlCenter = new JPanel();
            pnlCenter.setOpaque(false);
            pnlCenter.add(pnlBubble);
            
            pnlAlign.add(pnlCenter, BorderLayout.CENTER); // 중앙 배치
            
        } else if (message.isMine()) {
            // [CASE 2] 나 (오른쪽 정렬)
            lblSender.setForeground(Color.BLACK);
            lblSender.setHorizontalAlignment(SwingConstants.RIGHT);
            
            txtMessage.setBackground(new Color(207, 255, 229)); // 카톡 노란색
            txtMessage.setForeground(Color.BLACK);
            txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
            
            pnlAlign.add(lblSender, BorderLayout.NORTH);
            pnlAlign.add(pnlBubble, BorderLayout.EAST); 

        } else {
            // [CASE 3] 상대방 (왼쪽 정렬)
            lblSender.setForeground(Color.BLACK);
            lblSender.setHorizontalAlignment(SwingConstants.LEFT);

            txtMessage.setBackground(Color.WHITE); 
            txtMessage.setForeground(Color.BLACK);
            txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
            
            pnlAlign.add(lblSender, BorderLayout.NORTH);
            pnlAlign.add(pnlBubble, BorderLayout.WEST); 
        }

        return this;
    }
}