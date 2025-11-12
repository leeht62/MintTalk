import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

public class FriendList extends JFrame {
  private JPanel contentPane;
  private JPanel friendPanel;
  private JScrollPane scrollPane;
  private String username;
  private Vector<String> friendNames = new Vector<>();

  public FriendList(String username) {
    this.username = username;
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

    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    panel.setPreferredSize(new Dimension(260, 50));
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
    panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
    panel.setBackground(Color.WHITE);

    JLabel icon = new JLabel("👤", SwingConstants.CENTER);
    icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
    icon.setPreferredSize(new Dimension(50, 50));

    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
    nameLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

    panel.add(icon, BorderLayout.WEST);
    panel.add(nameLabel, BorderLayout.CENTER);

    friendPanel.add(panel);
    friendPanel.revalidate();
    friendPanel.repaint();
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
