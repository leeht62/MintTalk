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

    // 💡 초기 프로필 아이콘 설정 (기본 이미지)
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

    lblUser = new JLabel("("+username+")");
    lblUser.setFont(new Font("Dialog", Font.BOLD, 18));

    userPanel.add(myProfileLabel);
    userPanel.add(lblUser);

    contentPane.add(userPanel, BorderLayout.NORTH);


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

  // 단일 친구 추가 (친구 목록에 프로필 이미지 공간 포함)
  public void addFriend(String friendName,String imageName) {
    if (friendName.equals(username)) return;
    if (friendNames.contains(friendName)) return;

    friendNames.add(friendName);

    JPanel panel = new JPanel(new BorderLayout(10, 0));
    panel.setPreferredSize(new Dimension(260, 50));
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
    panel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
    panel.setBackground(Color.WHITE);

    // 💡 1. 프로필 이미지 공간 (JLabel) - 기본 이미지로 시작
    JLabel profileLabel = new JLabel();
    profileLabel.setPreferredSize(new Dimension(50, 50));
    profileLabel.setHorizontalAlignment(SwingConstants.CENTER);
    profileLabel.setBorder(new EmptyBorder(0, 5, 0, 0));

    // 기본 이미지 설정
    ImageIcon defaultIcon = getProfileIcon("profile.jpg");
    if (defaultIcon != null) {
      profileLabel.setIcon(defaultIcon);
    } else {
      profileLabel.setText("👤");
    }
    ImageIcon currentIcon = getProfileIcon(imageName);
    if (currentIcon != null) {
      profileLabel.setIcon(currentIcon);
    } else {
      profileLabel.setText("👤");
    }

    // 💡 3. 친구 이름 레이블
    JLabel nameLabel = new JLabel(friendName);
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));

    // 💡 디버깅용: 이름 레이블을 찾기 위해 클라이언트 이름으로 이름을 지정
    nameLabel.setName("FriendNameLabel_" + friendName);
    profileLabel.setName("ProfileImageLabel_" + friendName);


    panel.add(profileLabel, BorderLayout.WEST);
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

      // 💡 주석 해제: 채팅방 실행
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
  public void updateFriends(Vector<String> names, HashMap<String, String> imageMap) {
    friendPanel.removeAll();
    friendNames.clear();
    for (String n : names) {
      if (n == null) continue;
      String trimmed = n.trim();
      if (!trimmed.isEmpty() && !trimmed.equals(username)) {
        // 💡 addFriend 호출 시 이미지 파일 이름을 전달합니다.
        String imageName = imageMap.getOrDefault(trimmed, "profile.jpg");
        addFriend(trimmed, imageName); // addFriend 시그니처 변경 필요
      }
    }
  }


  // 💡 서버에서 수신된 메시지를 통해 친구의 프로필 이미지를 갱신합니다.
  public void updateFriendProfileImage(String targetUser, String imageName) {
    for (Component comp : friendPanel.getComponents()) {
      if (comp instanceof JPanel) {
        JPanel friendEntry = (JPanel) comp;

        // 이름 레이블을 찾아 해당 사용자의 항목인지 확인합니다.
        for (Component child : friendEntry.getComponents()) {
          if (child instanceof JLabel && child.getName() != null && child.getName().equals("FriendNameLabel_" + targetUser)) {

            // 프로필 이미지 레이블을 찾아 아이콘 갱신
            for (Component profileChild : friendEntry.getComponents()) {
              if (profileChild instanceof JLabel && profileChild.getName() != null && profileChild.getName().equals("ProfileImageLabel_" + targetUser)) {
                JLabel profileLabel = (JLabel) profileChild;
                ImageIcon newIcon = getProfileIcon(imageName);
                profileLabel.setIcon(newIcon);
                profileLabel.setText(""); // 텍스트 제거
                friendEntry.revalidate();
                friendEntry.repaint();
                return; // 찾았으면 종료
              }
            }
          }
        }
      }
    }
  }

  // 💡 채팅방 실행 메소드
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

    // 💡 주석 해제: 채팅방 목록 표시 및 재입장 기능
    for (ChatRoomInfo room : chatRooms) {
      JButton roomBtn = new JButton(room.toString());
      roomBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

      // 방 클릭하면 재입장
      roomBtn.addActionListener(e -> {
        new JavaChatClientView(username, ip, String.valueOf(port),room.roomName);
      });

      listPanel.add(roomBtn);
    }


    JScrollPane sp = new JScrollPane(listPanel);
    dialog.add(sp, BorderLayout.CENTER);

    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }

  public void addChatRoom(ChatRoomInfo room) { // ChatRoomInfo로 타입 복구
    // 💡 주석 해제: 채팅방 목록에 추가
    chatRooms.add(room);
  }

  // 💡 지정된 이름의 프로필 이미지를 불러와 크기를 조정합니다.
  private ImageIcon getProfileIcon(String imageName) {
    if (imageName == null || imageName.isEmpty()) {
      imageName = "profile.jpg"; // 기본 이미지로 폴백
    }
    try {
      // 클라이언트 로컬의 'image' 폴더에 프로필 이미지가 저장되어 있다고 가정
      ImageIcon originalIcon = new ImageIcon("image/" + imageName);
      Image image = originalIcon.getImage();
      // 이미지 로드 실패 시, 기본 이미지 사용
      if (image.getWidth(null) == -1) {
        originalIcon = new ImageIcon("image/profile.jpg");
        image = originalIcon.getImage();
      }

      Image newimg = image.getScaledInstance(50, 50,  java.awt.Image.SCALE_SMOOTH);
      return new ImageIcon(newimg);
    } catch (Exception e) {
      System.err.println("이미지 파일을 찾을 수 없습니다: image/" + imageName + " 또는 image/profile.jpg");
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
        imageDir.mkdirs(); // 디렉토리가 없으면 생성
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
        JOptionPane.showMessageDialog(this, "파일 복사 또는 서버 통보에 실패했습니다: " + e.getMessage(),
            "오류", JOptionPane.ERROR_MESSAGE);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "이미지 로드에 실패했습니다: " + ex.getMessage(),
            "오류", JOptionPane.ERROR_MESSAGE);
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