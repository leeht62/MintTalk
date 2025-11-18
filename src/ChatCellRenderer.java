// ChatCellRenderer.java
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension; // Dimension import 확인
import java.awt.Font;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

// JList의 각 항목(ChatMessage)을 '어떻게 그릴지' 정하는 클래스
public class ChatCellRenderer extends JPanel implements ListCellRenderer<ChatMessage> {

    private final JLabel lblSender = new JLabel();
    private final JTextArea txtMessage = new JTextArea();
    private final JPanel pnlBubble = new JPanel(new BorderLayout()); // 말풍선 패널
    private final JPanel pnlAlign = new JPanel(new BorderLayout()); // 좌우 정렬용 패널

    public ChatCellRenderer() {
        super(new BorderLayout()); // 최종적으로 이 패널이 JList의 한 줄이 됨

        // 1. 보낸사람 라벨 설정
        lblSender.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        lblSender.setOpaque(false); // 배경 투명

        // 2. 메시지 내용 (JTextArea는 자동 줄바꿈을 위해 사용)
        txtMessage.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        txtMessage.setEditable(false);
        txtMessage.setLineWrap(true);       // 자동 줄바꿈
        txtMessage.setWrapStyleWord(true);  // 단어 단위 줄바꿈
        txtMessage.setMargin(new Insets(8, 10, 8, 10)); // 말풍선 안쪽 여백

        // 3. 말풍선 패널 (메시지 내용을 감쌈)
        pnlBubble.setOpaque(true);
        pnlBubble.add(txtMessage, BorderLayout.CENTER);

        // 4. 정렬용 패널 (보낸사람 + 말풍선)
        pnlAlign.setOpaque(false); // 배경 투명

        // 5. 최종 패널에 정렬용 패널을 추가
        add(pnlAlign, BorderLayout.CENTER);
        setOpaque(false); // 최종 패널 자체도 투명
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // 메시지 간 상하 여백
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ChatMessage> list, ChatMessage message, int index,
            boolean isSelected, boolean cellHasFocus) {
        
        // 1. 데이터 설정
        lblSender.setText(message.getSender());
        txtMessage.setText(message.getMessage());

        // --- 2. 정렬 및 스타일링 (핵심) ---
        
        // 패널 초기화 (이전 설정 제거)
        pnlAlign.remove(lblSender);
        pnlAlign.remove(pnlBubble);

        if (message.isMine()) {
            // [나] (오른쪽 정렬, 노란색 배경)
            lblSender.setForeground(Color.black); // 배경에 따라 잘 보이게 설정 (배경이 어두우면 흰색 추천)
            lblSender.setHorizontalAlignment(SwingConstants.RIGHT);
            
            txtMessage.setBackground(new Color(207, 255, 229)); // 카톡 노란색
            txtMessage.setForeground(Color.BLACK); // 글자 검은색
            
            pnlAlign.add(lblSender, BorderLayout.NORTH);
            pnlAlign.add(pnlBubble, BorderLayout.EAST); // 말풍선을 동쪽에

        } else {
            // [상대방] (왼쪽 정렬, 흰색 배경)
            lblSender.setForeground(Color.black); // 배경에 따라 잘 보이게 설정
            lblSender.setHorizontalAlignment(SwingConstants.LEFT);

            txtMessage.setBackground(Color.WHITE); // 기본 흰색
            txtMessage.setForeground(Color.BLACK); // 글자 검은색
            
            pnlAlign.add(lblSender, BorderLayout.NORTH);
            pnlAlign.add(pnlBubble, BorderLayout.WEST); // 말풍선을 서쪽에
        }
        
        // JList 너비에 맞춰 JTextArea 크기 강제 조절 (자동 줄바꿈을 위함)
        int listWidth = list.getWidth();
        if (listWidth > 0) {
            // 말풍선 최대 너비를 리스트의 70% 정도로 제한
            int maxWidth = (int)(listWidth * 0.7);
            txtMessage.setSize(new Dimension(maxWidth, 1)); 
        }

        return this; // 이 패널(this)을 JList의 한 줄로 반환
    }
} 
// 👆 여기에 닫는 괄호가 꼭 있어야 합니다!