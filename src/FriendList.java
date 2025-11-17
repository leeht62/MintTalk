//FriendList.java
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.DataOutputStream;
import java.util.*;

public class FriendList extends JFrame {
  private JPanel contentPane;
  private JPanel friendPanel;
  private JScrollPane scrollPane;
  private String username;
  private String ip;
  private int port;
  private DataOutputStream out;
  private Vector<String> friendNames = new Vector<>();
  private static Vector<ChatRoomInfo> chatRooms = new Vector<>();

  public FriendList(String username,String ip,int port,DataOutputStream out) {
    this.username = username;
    this.ip=ip;
    this.port=port;
    this.out=out;

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

    JPanel topRight = new JPanel(new BorderLayout());
    topRight.setBackground(Color.WHITE);

    JButton btnOpenSelect = new JButton("➕ 대화");
    btnOpenSelect.setFocusPainted(false);
    btnOpenSelect.setBackground(Color.WHITE);
    btnOpenSelect.setBorder(new EmptyBorder(5,5,5,5));

    btnOpenSelect.addActionListener(e -> openSelectDialog());

    contentPane.add(btnOpenSelect, BorderLayout.EAST);

    scrollPane = new JScrollPane(friendPanel);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    contentPane.add(scrollPane, BorderLayout.CENTER);

    JButton btnRooms = new JButton("채팅창 확인");
    btnRooms.addActionListener(e -> showChatRoomsDialog());
    contentPane.add(btnRooms, BorderLayout.SOUTH);


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


    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));

    panel.add(nameLabel, BorderLayout.CENTER);

    friendPanel.add(panel);
    friendPanel.revalidate();
    friendPanel.repaint();
  }


  private void openSelectDialog() {
    JDialog dialog = new JDialog(this, "대화상대 선택", true);
    dialog.setSize(300, 350);
    dialog.setLayout(new BorderLayout());

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    Vector<JCheckBox> boxes = new Vector<>();

    for (String name : friendNames) {
      if (name.equals(username)) continue;

      JCheckBox box = new JCheckBox(name);
      box.setFont(new Font("Dialog", Font.PLAIN, 15));
      boxes.add(box);
      listPanel.add(box);
    }

    JScrollPane sp = new JScrollPane(listPanel);
    dialog.add(sp, BorderLayout.CENTER);

    JPanel bottom = new JPanel();
    JButton ok = new JButton("확인");
    JButton cancel = new JButton("취소");

    ok.addActionListener(ev -> {
      Vector<String> selected = new Vector<>();
      selected.add(username);  // 자기 자신 포함

      for (JCheckBox cb : boxes) {
        if (cb.isSelected()) selected.add(cb.getText());
      }

      if (selected.size() < 2) {
        JOptionPane.showMessageDialog(dialog, "대화 상대를 선택하세요!");
        return;
      }

      // 💡 서버에 전송할 방 이름 (참여자 이름_조합) 생성
      // 이 이름을 ChatClientView에 전달해야 합니다.
      String roomName = String.join("_", selected);

      try {
        out.writeUTF("MAKE_ROOM:" + roomName + ":" + String.join(",", selected));
        out.flush();
      } catch (Exception e) {
        e.printStackTrace();
      }

      // 💡 통일된 roomName을 ChatClientView에 전달
      openChatRoom(roomName);

      dialog.dispose();
    });

    cancel.addActionListener(ev -> dialog.dispose());

    bottom.add(ok);
    bottom.add(cancel);

    dialog.add(bottom, BorderLayout.SOUTH);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  // 전체 친구 목록 갱신
  public void updateFriends(Vector<String> names) {
    friendPanel.removeAll();
    friendNames.clear();
    for (String n : names) {
      if (n == null) continue;
      String trimmed = n.trim();
      if (!trimmed.isEmpty() && !trimmed.equals(username)) {
        addFriend(trimmed);
      }
    }
  }


  // 💡 openChatRoom 메소드를 통일된 roomName을 받는 형태로 수정
  private void openChatRoom(String roomName) {
    // 채팅방 실행
    new JavaChatClientView(username, ip, String.valueOf(port),roomName);
  }

  private void showChatRoomsDialog() {
    JDialog dialog = new JDialog(this, "채팅방 목록", true);
    dialog.setSize(300, 400);
    dialog.setLayout(new BorderLayout());

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    for (ChatRoomInfo room : chatRooms) {
      JButton roomBtn = new JButton(room.toString());
      roomBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

      // 방 클릭하면 재입장
      roomBtn.addActionListener(e -> {
        // 💡 room.roomName (서버가 인식하는 이름)을 ChatClientView에 전달
        new JavaChatClientView(username, ip, String.valueOf(port),room.roomName);
      });

      listPanel.add(roomBtn);
    }

    JScrollPane sp = new JScrollPane(listPanel);
    dialog.add(sp, BorderLayout.CENTER);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  public void addChatRoom(ChatRoomInfo room) {
    chatRooms.add(room);

    JButton roomBtn = new JButton(room.toString());
    roomBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
    roomBtn.addActionListener(e -> {
      // 💡 room.roomName (서버가 인식하는 이름)을 ChatClientView에 전달
      new JavaChatClientView(username, ip, String.valueOf(port),room.roomName);
    });

    friendPanel.add(roomBtn);
    friendPanel.revalidate();
    friendPanel.repaint();
  }
}