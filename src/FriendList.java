import java.awt.*;
import java.awt.event.*;
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

  // ★ 추가된 데이터 저장소 (상세창을 위해 필요)
  private HashMap<String, String> userImages = new HashMap<>();
  private HashMap<String, String> userBgImages = new HashMap<>();
  private HashMap<String, String> userStatusMsgs = new HashMap<>();

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

    JPanel sidePanel = new JPanel();
    sidePanel.setOpaque(false);
    sidePanel.setPreferredSize(new Dimension(60, 0));
    sidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

    // (1) 친구 아이콘
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
      if (chatIcon.getIconWidth() == -1) chatIcon = new ImageIcon("image/balloon.jpg");
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
    contentPane.add(sidePanel, BorderLayout.WEST);

    JPanel rightAreaPanel = new JPanel(new BorderLayout());
    rightAreaPanel.setOpaque(false);

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
    myProfileLabel.setName("ProfileImageLabel_" + username); // 식별자 추가

    ImageIcon defaultIcon = getProfileIcon("profile.jpg");
    if (defaultIcon != null) myProfileLabel.setIcon(defaultIcon);
    else myProfileLabel.setText("👤");

    // ★ [수정] 내 프로필 클릭 시 -> 상세창(ProfileDetailDialog) 오픈
    myProfileLabel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        openProfileDetail(username);
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

    rightAreaPanel.add(scrollPane, BorderLayout.CENTER);
    contentPane.add(rightAreaPanel, BorderLayout.CENTER);

    setVisible(true);
  }

  // ★ [수정] updateFriends: 서버에서 받은 긴 문자열(detailInfo)을 파싱하여 저장
  public void updateFriends(Vector<String> names, String detailInfo) {
    friendPanel.removeAll();
    friendNames.clear();

    // 1. 상세 정보 파싱 (이름=이미지|배경|상태메시지)
    if (detailInfo != null && !detailInfo.isEmpty()) {
      String[] users = detailInfo.split(";");
      for (String u : users) {
        String[] parts = u.split("=");
        if (parts.length == 2) {
          String uName = parts[0];
          String[] vals = parts[1].split("\\|"); // | 기호로 분리
          if (vals.length >= 3) {
            userImages.put(uName, vals[0]);
            userBgImages.put(uName, vals[1]);
            userStatusMsgs.put(uName, vals[2]);
          }
        }
      }
    }

    // 2. 내 프로필 이미지(헤더) 업데이트
    updateImageRecursive(contentPane, username, userImages.getOrDefault(username, "profile.jpg"));

    // 3. 친구 목록 재생성
    for (String n : names) {
      if (n == null) continue;
      String trimmed = n.trim();
      if (!trimmed.isEmpty() && !trimmed.equals(username)) {
        // 저장된 맵에서 이미지 정보를 가져옴
        String imageName = userImages.getOrDefault(trimmed, "profile.jpg");
        addFriend(trimmed, imageName);
      }
    }

    friendPanel.revalidate();
    friendPanel.repaint();
  }

  // ★ [수정] addFriend: 친구 목록 UI 생성 (상태메시지 표시 및 클릭 이벤트 추가)
  public void addFriend(String friendName, String imageName) {
    friendNames.add(friendName);

    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.setPreferredSize(new Dimension(260, 70));
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));

    Border lineBorder = new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220));
    panel.setBorder(new CompoundBorder(lineBorder, new EmptyBorder(0, 0, 0, 10)));
    panel.setOpaque(false);

    // 프로필 이미지 라벨
    JLabel profileLabel = new JLabel();
    profileLabel.setPreferredSize(new Dimension(50, 50));
    profileLabel.setHorizontalAlignment(SwingConstants.CENTER);
    profileLabel.setBorder(new EmptyBorder(0, 10, 0, 10));
    profileLabel.setName("ProfileImageLabel_" + friendName);

    ImageIcon currentIcon = getProfileIcon(imageName);
    if (currentIcon != null) profileLabel.setIcon(currentIcon);
    else profileLabel.setText("👤");

    // ★ 친구 이미지 클릭 시 -> 상세창 오픈
    profileLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        openProfileDetail(friendName);
      }
    });

    // 텍스트 패널 (이름 + 상태메시지)
    JPanel textPanel = new JPanel(new GridLayout(2, 1));
    textPanel.setOpaque(false);
    textPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
    nameLabel.setForeground(Color.BLACK);
    nameLabel.setName("FriendNameLabel_" + friendName);

    // 상태 메시지 라벨 추가
    String status = userStatusMsgs.getOrDefault(friendName, "");
    JLabel statusLabel = new JLabel(status);
    statusLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
    statusLabel.setForeground(Color.GRAY);

    textPanel.add(nameLabel);
    textPanel.add(statusLabel);

    JPanel westWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 10));
    westWrapper.setOpaque(false);
    westWrapper.add(profileLabel);
    westWrapper.add(textPanel);

    panel.add(westWrapper, BorderLayout.CENTER);

    friendPanel.add(panel);
  }

  // ★ [추가] 프로필 상세 창 열기 메서드
  private void openProfileDetail(String targetName) {
    String img = userImages.getOrDefault(targetName, "profile.jpg");
    String bg = userBgImages.getOrDefault(targetName, "ab.jpg");
    String msg = userStatusMsgs.getOrDefault(targetName, "");

    // 상세 다이얼로그 생성
    new ProfileDetailDialog(this, username, targetName, img, bg, msg, out);
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
      selected.add(username);
      for (JCheckBox cb : boxes) {
        if (cb.isSelected()) selected.add(cb.getText());
      }
      if (selected.size() < 2) {
        JOptionPane.showMessageDialog(dialog, "대화 상대를 선택하세요!");
        return;
      }
      String roomName = String.join(" ", selected);
      try {
        out.writeUTF("MAKE_ROOM:" + roomName + ":" + String.join(",", selected));
        out.flush();
      } catch (Exception e) { e.printStackTrace(); }
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

  // 실시간 업데이트용 (이제 updateFriends가 전체를 다시 그리므로 보조 역할)
  public void updateFriendProfileImage(String targetUser, String imageName) {
    userImages.put(targetUser, imageName);
    updateImageRecursive(contentPane, targetUser, imageName);
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

  public void addChatRoom(ChatRoomInfo room) {
    chatRooms.add(room);
  }

  private ImageIcon getProfileIcon(String imageName) {
    if (imageName == null || imageName.isEmpty()) imageName = "profile.jpg";
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

  // (구형 업로드 메서드 제거 - 이제 ProfileDetailDialog에서 처리함)
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