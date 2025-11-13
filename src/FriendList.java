import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class FriendList extends JFrame {
  private JPanel contentPane;
  private JPanel friendPanel;
  private JScrollPane scrollPane;
  private String username;
  private String ip;
  private int port;
  private Vector<String> friendNames = new Vector<>();

  public FriendList(String username,String ip,int port) {
    this.username = username;
    this.ip=ip;
    this.port=port;

    setTitle("Friend List - " + username);
    setSize(300, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    contentPane = new JPanel();
    contentPane.setLayout(new BorderLayout());
    setContentPane(contentPane);

    JLabel lblUser = new JLabel("("+username+")" , SwingConstants.CENTER);
    lblUser.setFont(new Font("Dialog", Font.BOLD, 18));
    lblUser.setBorder(new EmptyBorder(10, 10, 10, 10));
    contentPane.add(lblUser, BorderLayout.NORTH);

    JButton btnCreateChat = new JButton("그룹 채팅 만들기");
    btnCreateChat.setFont(new Font("Dialog", Font.BOLD, 14));
    btnCreateChat.setBackground(new Color(70, 130, 180));
    btnCreateChat.setForeground(Color.WHITE);
    btnCreateChat.setFocusPainted(false);
    btnCreateChat.setBorder(new EmptyBorder(10, 10, 10, 10));

    btnCreateChat.addActionListener(e -> createGroupChat());

    contentPane.add(btnCreateChat, BorderLayout.SOUTH);

    friendPanel = new JPanel();
    friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.Y_AXIS));
    friendPanel.setBackground(Color.WHITE);

    scrollPane = new JScrollPane(friendPanel);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    contentPane.add(scrollPane, BorderLayout.CENTER);

    setVisible(true);
  }

  // 단일 친구 추가
  public void addFriend(String friendName) {
    if (friendName.equals(username)) return;
    if (friendNames.contains(friendName)) return;

    friendNames.add(friendName);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(260, 50));
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
    panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
    panel.setBackground(Color.WHITE);

    JCheckBox checkBox = new JCheckBox();
    checkBox.setBackground(Color.WHITE);
    checkBox.setPreferredSize(new Dimension(40, 50));

    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));

    panel.add(checkBox, BorderLayout.WEST);
    panel.add(nameLabel, BorderLayout.CENTER);

    friendPanel.add(panel);
    friendPanel.revalidate();
    friendPanel.repaint();
  }
  public void createGroupChat() {
    Vector<String> selectedFriends = new Vector<>();

    // friendPanel 안의 모든 컴포넌트 탐색
    for (Component c : friendPanel.getComponents()) {
      if (c instanceof JPanel panel) {
        Component[] comps = panel.getComponents();
        JCheckBox checkBox = null;
        JLabel nameLabel = null;

        for (Component comp : comps) {
          if (comp instanceof JCheckBox cb) checkBox = cb;
          if (comp instanceof JLabel lbl) nameLabel = lbl;
        }

        if (checkBox != null && nameLabel != null && checkBox.isSelected()) {
          selectedFriends.add(nameLabel.getText());
        }
      }
    }

    if (selectedFriends.isEmpty()) {
      JOptionPane.showMessageDialog(this, "친구를 선택하세요!");
      return;
    }

    // 선택된 친구들과 그룹 채팅 시작
    String members = String.join(", ", selectedFriends);


    // 예시: 서버 주소와 포트는 임시로 지정
    new JavaChatClientView(username, ip, String.valueOf(port));
  }

  // 전체 친구 목록 갱신
  public void updateFriends(Vector<String> names) {
    friendPanel.removeAll();
    friendNames.clear();
    for (String n : names) {
      addFriend(n);
    }
  }
}
