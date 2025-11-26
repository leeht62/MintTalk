import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;

public class FriendList extends JFrame {
  private JPanel contentPane;
  private JPanel friendPanel;
  private JScrollPane scrollPane;
  private String username;
  private String ip;
  private JLabel lblUser;
  private int port;
  private DataOutputStream out;
  private Vector<String> friendNames = new Vector<>();
  private static Vector<ChatRoomInfo> chatRooms = new Vector<>();

  public FriendList(String username, String ip, int port, DataOutputStream out) {
    this.username = username;
    this.ip = ip;
    this.port = port;
    this.out = out;

    setTitle("Friend List - " + username);
    setSize(300, 600);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // 배경 이미지 패널 설정
    contentPane = new ImagePanel("image/abc.jpg");
    contentPane.setLayout(new BorderLayout());
    setContentPane(contentPane);

    // =================================================================
    // [구조 변경 핵심] 1. 왼쪽 사이드바 패널 (이제 전체 높이를 차지함)
    // =================================================================
    JPanel sidePanel = new JPanel();
    sidePanel.setOpaque(false);
    sidePanel.setPreferredSize(new Dimension(60, 0)); // 너비 60 고정
    // 상단 여백(vgap)을 15로 주어 프로필 라인과 높이를 맞춤
    sidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

    // (1) 친구 아이콘 (people.jpg)
    JLabel lblPeopleIcon = new JLabel();
    lblPeopleIcon.setPreferredSize(new Dimension(35, 35));
    lblPeopleIcon.setHorizontalAlignment(SwingConstants.CENTER);

    try {
      ImageIcon peopleIcon = new ImageIcon("image/people.jpg");
      Image img = peopleIcon.getImage();
      Image newImg = img.getScaledInstance(35, 35, java.awt.Image.SCALE_SMOOTH);
      lblPeopleIcon.setIcon(new ImageIcon(newImg));
    } catch (Exception e) {
      lblPeopleIcon.setText("P");
    }

    // (2) 말풍선 버튼
    JButton btnChatList = new JButton();
    btnChatList.setBorderPainted(false);
    btnChatList.setContentAreaFilled(false);
    btnChatList.setFocusPainted(false);
    btnChatList.setMargin(new Insets(0, 0, 0, 0));

    try {
      ImageIcon chatIcon = new ImageIcon("image/chat_icon.png");
      if (chatIcon.getIconWidth() == -1) {
        chatIcon = new ImageIcon("image/balloon.jpg");
      }
      Image img = chatIcon.getImage();
      Image newImg = img.getScaledInstance(35, 35, java.awt.Image.SCALE_SMOOTH);
      btnChatList.setIcon(new ImageIcon(newImg));
    } catch (Exception e) {
      btnChatList.setText("Talk");
    }
    btnChatList.addActionListener(e -> {
      this.setVisible(false);
      new ChatRoomList(username, ip, port, out, chatRooms, this);
    });

    sidePanel.add(lblPeopleIcon);
    sidePanel.add(btnChatList);

    // [중요] 사이드바를 프레임의 WEST에 가장 먼저 배치
    contentPane.add(sidePanel, BorderLayout.WEST);


    // =================================================================
    // [구조 변경 핵심] 2. 오른쪽 영역 (헤더 + 리스트)을 감싸는 패널 생성
    // =================================================================
    JPanel rightAreaPanel = new JPanel(new BorderLayout());
    rightAreaPanel.setOpaque(false); // 배경 투명하게

    // --- A. 상단 헤더 패널 ---
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setOpaque(false);
    headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    // [중앙] 내 프로필과 이름
    JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
    userPanel.setOpaque(false);

    JLabel myProfileLabel = new JLabel();
    myProfileLabel.setPreferredSize(new Dimension(50, 50));
    myProfileLabel.setHorizontalAlignment(SwingConstants.CENTER);

    ImageIcon defaultIcon = getProfileIcon("profile.jpg");
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

    lblUser = new JLabel("(" + username + ")");
    lblUser.setFont(new Font("Dialog", Font.BOLD, 18));
    lblUser.setForeground(Color.BLACK);

    userPanel.add(myProfileLabel);
    userPanel.add(lblUser);

    // [오른쪽] 대화하기 버튼
    JButton btnOpenSelect = new JButton("➕ 대화");
    btnOpenSelect.setFocusPainted(false);
    btnOpenSelect.setBackground(Color.WHITE);
    btnOpenSelect.setMargin(new Insets(5, 10, 5, 10));
    btnOpenSelect.addActionListener(e -> openSelectDialog());

    JPanel buttonWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 5));
    buttonWrapper.setOpaque(false);
    buttonWrapper.add(btnOpenSelect);

    headerPanel.add(userPanel, BorderLayout.CENTER);
    headerPanel.add(buttonWrapper, BorderLayout.EAST);

    // 헤더를 오른쪽 영역 패널의 상단(NORTH)에 배치
    rightAreaPanel.add(headerPanel, BorderLayout.NORTH);


    // --- B. 중앙 친구 목록 스크롤 영역 ---
    friendPanel = new JPanel();
    friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.Y_AXIS));
    friendPanel.setOpaque(false);

    scrollPane = new JScrollPane(friendPanel);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setBorder(null);

    // 리스트를 오른쪽 영역 패널의 중앙(CENTER)에 배치
    rightAreaPanel.add(scrollPane, BorderLayout.CENTER);

    // [중요] 오른쪽 영역 전체를 프레임의 CENTER에 배치
    contentPane.add(rightAreaPanel, BorderLayout.CENTER);

    setVisible(true);
  }

  // =================================================================
  // 친구 추가 메서드 (이전과 동일)
  // =================================================================
  public void addFriend(String friendName, String imageName) {
    if (friendName.equals(username)) return;
    if (friendNames.contains(friendName)) return;

    friendNames.add(friendName);

    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.setPreferredSize(new Dimension(260, 70));
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));

    Border lineBorder = new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220));
    panel.setBorder(new CompoundBorder(lineBorder, new EmptyBorder(0, 0, 0, 10)));
    panel.setOpaque(false);

    JLabel profileLabel = new JLabel();
    profileLabel.setPreferredSize(new Dimension(50, 50));
    profileLabel.setHorizontalAlignment(SwingConstants.CENTER);
    profileLabel.setBorder(new EmptyBorder(0, 10, 0, 10));

    ImageIcon defaultIcon = getProfileIcon("profile.jpg");
    if (defaultIcon != null) {
      profileLabel.setIcon(defaultIcon);
    } else {
      profileLabel.setText("👤");
    }
    ImageIcon currentIcon = getProfileIcon(imageName);
    if (currentIcon != null) {
      profileLabel.setIcon(currentIcon);
    }

    profileLabel.setName("ProfileImageLabel_" + friendName);

    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
    nameLabel.setForeground(Color.BLACK);
    nameLabel.setName("FriendNameLabel_" + friendName);
    nameLabel.setBorder(new EmptyBorder(0, 20, 0, 0));

    JPanel westWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
    westWrapper.setOpaque(false);
    westWrapper.add(profileLabel);
    westWrapper.add(nameLabel);

    panel.add(westWrapper, BorderLayout.CENTER);

    friendPanel.add(panel);
    friendPanel.revalidate();
    friendPanel.repaint();
  }

  // ... 나머지 메서드들 (openSelectDialog, updateFriends 등)은 기존과 동일하게 유지 ...
  // 아래 메서드들을 이전에 작성한 코드에서 그대로 복사해서 사용하세요.
  // (지면 관계상 중복되는 긴 메서드는 생략하지 않고 필요한 경우 요청하시면 다시 드립니다.)

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
      selected.add(username);

      for (JCheckBox cb : boxes) {
        if (cb.isSelected()) selected.add(cb.getText());
      }

      if (selected.size() < 2) {
        JOptionPane.showMessageDialog(dialog, "대화 상대를 선택하세요!");
        return;
      }

      String roomName = String.join("_", selected);

      try {
        out.writeUTF("MAKE_ROOM:" + roomName + ":" + String.join(",", selected));
        out.flush();
      } catch (Exception e) {
        e.printStackTrace();
      }

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

  public void updateFriends(Vector<String> names, HashMap<String, String> imageMap) {
    friendPanel.removeAll();
    friendNames.clear();
    for (String n : names) {
      if (n == null) continue;
      String trimmed = n.trim();
      if (!trimmed.isEmpty() && !trimmed.equals(username)) {
        String imageName = imageMap.getOrDefault(trimmed, "profile.jpg");
        addFriend(trimmed, imageName);
      }
    }
  }

  public void updateFriendProfileImage(String targetUser, String imageName) {
    for (Component comp : friendPanel.getComponents()) {
      if (comp instanceof JPanel) {
        JPanel friendEntry = (JPanel) comp;
        updateImageRecursive(friendEntry, targetUser, imageName);
      }
    }
  }

  private void updateImageRecursive(Container container, String targetUser, String imageName) {
    for (Component child : container.getComponents()) {
      if (child instanceof JLabel) {
        JLabel lbl = (JLabel) child;
        if (lbl.getName() != null && lbl.getName().equals("ProfileImageLabel_" + targetUser)) {
          ImageIcon newIcon = getProfileIcon(imageName);
          lbl.setIcon(newIcon);
          lbl.setText("");
          lbl.revalidate();
          lbl.repaint();
          return;
        }
      } else if (child instanceof Container) {
        updateImageRecursive((Container) child, targetUser, imageName);
      }
    }
  }

  private void openChatRoom(String roomName) {
    new JavaChatClientView(username, ip, String.valueOf(port), roomName);
  }

//  private void showChatRoomsDialog() {
//    JDialog dialog = new JDialog(this, "채팅방 목록", true);
//    dialog.setSize(300, 400);
//    dialog.setLayout(new BorderLayout());
//
//    JPanel listPanel = new JPanel();
//    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
//
//    for (ChatRoomInfo room : chatRooms) {
//      JButton roomBtn = new JButton(room.toString());
//      roomBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
//      roomBtn.addActionListener(e -> {
//        new JavaChatClientView(username, ip, String.valueOf(port), room.roomName);
//      });
//      listPanel.add(roomBtn);
//    }
//
//    JScrollPane sp = new JScrollPane(listPanel);
//    dialog.add(sp, BorderLayout.CENTER);
//
//    dialog.setLocationRelativeTo(this);
//    dialog.setVisible(true);
//  }

  public void addChatRoom(ChatRoomInfo room) {
    chatRooms.add(room);
  }

  private ImageIcon getProfileIcon(String imageName) {
    if (imageName == null || imageName.isEmpty()) {
      imageName = "profile.jpg";
    }
    try {
      ImageIcon originalIcon = new ImageIcon("image/" + imageName);
      Image image = originalIcon.getImage();
      if (image.getWidth(null) == -1) {
        originalIcon = new ImageIcon("image/profile.jpg");
        image = originalIcon.getImage();
      }
      Image newimg = image.getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH);
      return new ImageIcon(newimg);
    } catch (Exception e) {
      System.err.println("이미지 파일을 찾을 수 없습니다: image/" + imageName);
      return null;
    }
  }

  private void uploadProfileImage(JLabel profileLabel) {
    JFileChooser fileChooser = new JFileChooser();
    int result = fileChooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) {
      java.io.File selectedFile = fileChooser.getSelectedFile();
      File imageDir = new File("image");
      if (!imageDir.exists()) {
        imageDir.mkdirs();
      }
      String imageName = selectedFile.getName();
      File targetFile = new File(imageDir, imageName);

      try {
        copyFile(selectedFile, targetFile);
        ImageIcon originalIcon = new ImageIcon(targetFile.getAbsolutePath());
        Image image = originalIcon.getImage();
        Image newimg = image.getScaledInstance(50, 50, java.awt.Image.SCALE_SMOOTH);
        ImageIcon newIcon = new ImageIcon(newimg);

        profileLabel.setIcon(newIcon);
        profileLabel.setText("");
        out.writeUTF("CHANGE_PROFILE_IMAGE:" + username + ":" + imageName);
        out.flush();

      } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "파일 복사 실패: " + e.getMessage());
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "이미지 로드 실패: " + ex.getMessage());
      }
    }
  }

  private void copyFile(File source, File dest) throws IOException {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = new FileInputStream(source);
      os = new FileOutputStream(dest);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    } finally {
      if (is != null) is.close();
      if (os != null) os.close();
    }
  }
}