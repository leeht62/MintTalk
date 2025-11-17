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
  private JLabel lblUser; // 필드 선언 위치 유지
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

    JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    userPanel.setBackground(Color.WHITE);

    JLabel myProfileLabel = new JLabel();
    myProfileLabel.setPreferredSize(new Dimension(50, 50));
    myProfileLabel.setHorizontalAlignment(SwingConstants.CENTER);

    ImageIcon defaultIcon = getDefaultProfileIcon();
    if (defaultIcon != null) {
      myProfileLabel.setIcon(defaultIcon);
    } else {
      myProfileLabel.setText("👤");
    }

    myProfileLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 1) {
          uploadProfileImage(myProfileLabel);
        }
      }
    });

    // 💡 사용자 이름 레이블 (기존 lblUser 역할)
    // 필드로 선언된 lblUser 변수에 할당합니다.
    lblUser = new JLabel("("+username+")");
    lblUser.setFont(new Font("Dialog", Font.BOLD, 18));

    userPanel.add(myProfileLabel); // 이미지 추가
    userPanel.add(lblUser);        // 이름 추가

    contentPane.add(userPanel, BorderLayout.NORTH); // userPanel을 NORTH에 추가


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

    // 버튼 위치는 BorderLayout.EAST를 유지합니다.
    contentPane.add(btnOpenSelect, BorderLayout.EAST);

    scrollPane = new JScrollPane(friendPanel);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    contentPane.add(scrollPane, BorderLayout.CENTER);

    JButton btnRooms = new JButton("채팅창 확인");
    btnRooms.addActionListener(e -> showChatRoomsDialog());
    contentPane.add(btnRooms, BorderLayout.SOUTH);


    setVisible(true);
  }

  // 단일 친구 추가 (친구 목록에 프로필 이미지 공간 포함)
  public void addFriend(String friendName) {
    if (friendName.equals(username)) return;
    if (friendNames.contains(friendName)) return;

    friendNames.add(friendName);

    JPanel panel = new JPanel(new BorderLayout(10, 0)); // 10px 간격 추가
    panel.setPreferredSize(new Dimension(260, 50));
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
    panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
    panel.setBackground(Color.WHITE);

    // 💡 1. 프로필 이미지 공간 (JLabel)
    JLabel profileLabel = new JLabel();
    profileLabel.setPreferredSize(new Dimension(50, 50)); // 이미지 크기
    profileLabel.setHorizontalAlignment(SwingConstants.CENTER);
    profileLabel.setBorder(new EmptyBorder(0, 5, 0, 0)); // 왼쪽 여백

    // 기본 이미지 설정
    ImageIcon defaultIcon = getDefaultProfileIcon();
    if (defaultIcon != null) {
      profileLabel.setIcon(defaultIcon);
    } else {
      profileLabel.setText("👤"); // 이미지가 없을 경우 대체 텍스트
    }

    // 💡 2. 프로필 이미지 클릭 이벤트 추가 (친구 목록에서는 업로드 기능 비활성화)
    profileLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 1) {
          // 자기 자신이 아닌 친구의 프로필을 클릭했을 때의 동작
          JOptionPane.showMessageDialog(null, friendName + "님의 프로필입니다.");
        }
      }
    });

    // 💡 3. 친구 이름 레이블
    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));

    panel.add(profileLabel, BorderLayout.WEST); // 왼쪽에 이미지 추가
    panel.add(nameLabel, BorderLayout.CENTER);  // 가운데에 이름 추가

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
    // ChatRoomInfo 클래스가 없으므로 주석 처리하거나, ChatRoomInfo 클래스가 있어야 컴파일됩니다.
    // new JavaChatClientView(username, ip, String.valueOf(port),roomName);
  }

  private void showChatRoomsDialog() {
    JDialog dialog = new JDialog(this, "채팅방 목록", true);
    dialog.setSize(300, 400);
    dialog.setLayout(new BorderLayout());

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

    // ChatRoomInfo 클래스가 없으므로 주석 처리하거나, ChatRoomInfo 클래스가 있어야 컴파일됩니다.
    /*
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
    */

    JScrollPane sp = new JScrollPane(listPanel);
    dialog.add(sp, BorderLayout.CENTER);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  public void addChatRoom(Object room) { // ChatRoomInfo 대신 Object로 임시 변경
    // ChatRoomInfo 클래스가 없으므로 주석 처리하거나, ChatRoomInfo 클래스가 있어야 컴파일됩니다.
    /*
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
    */
  }

  // 기본 프로필 이미지를 불러와 크기를 조정합니다.
  private ImageIcon getDefaultProfileIcon() {
    try {
      ImageIcon originalIcon = new ImageIcon("image/profile.jpg");
      Image image = originalIcon.getImage();
      Image newimg = image.getScaledInstance(50, 50,  java.awt.Image.SCALE_SMOOTH);
      return new ImageIcon(newimg);
    } catch (Exception e) {
      System.err.println("기본 이미지 파일을 찾을 수 없습니다: " + e.getMessage());
      return null;
    }
  }

  // 파일 업로드 다이얼로그를 띄우고 이미지를 설정합니다.
  private void uploadProfileImage(JLabel profileLabel) {
    JFileChooser fileChooser = new JFileChooser();
    // 이미지 파일만 필터링하도록 설정할 수 있습니다.
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      java.io.File selectedFile = fileChooser.getSelectedFile();

      try {
        ImageIcon originalIcon = new ImageIcon(selectedFile.getAbsolutePath());
        Image image = originalIcon.getImage();
        // 50x50 크기로 이미지 조정
        Image newimg = image.getScaledInstance(50, 50,  java.awt.Image.SCALE_SMOOTH);
        ImageIcon newIcon = new ImageIcon(newimg);

        profileLabel.setIcon(newIcon);
        profileLabel.setText(""); // 이미지가 성공적으로 로드되면 텍스트 제거


      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "이미지 로드에 실패했습니다: " + ex.getMessage(),
            "오류", JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}