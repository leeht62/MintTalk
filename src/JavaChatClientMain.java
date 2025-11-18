import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.io.*;
import java.net.*;
import java.util.*;

public class JavaChatClientMain extends JFrame {

  private JPanel contentPane;
  private RoundedTextField txtUserName;
  private RoundedTextField txtIpAddress;
  private RoundedTextField txtPortNumber;

  private Socket socket;
  private DataInputStream dis;
  private DataOutputStream dos;
  private FriendList friendList;

  public static void main(String[] args) {
    EventQueue.invokeLater(() -> {
      try {
        JavaChatClientMain frame = new JavaChatClientMain();
        frame.setVisible(true);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
  }

  public JavaChatClientMain() {
    setTitle("Mint Talk Login");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 258, 400);

    contentPane = new ImagePanel("image/mint.jpg");
    contentPane.setBorder(new EmptyBorder(5,5,5,5));
    contentPane.setLayout(null);
    setContentPane(contentPane);

    JLabel lblTitle = new JLabel("Mint Talk");
    lblTitle.setFont(new Font("Serif", Font.BOLD, 48));
    lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
    lblTitle.setBounds(12, 10, 218, 39);
    lblTitle.setForeground(Color.WHITE);
    lblTitle.setOpaque(false);
    contentPane.add(lblTitle);

    JLabel lblUser = new JLabel("User Name");
    lblUser.setBounds(67, 60, 120, 25);
    lblUser.setHorizontalAlignment(SwingConstants.CENTER);
    lblUser.setForeground(Color.WHITE);
    lblUser.setOpaque(false);
    contentPane.add(lblUser);

    txtUserName = new RoundedTextField();
    txtUserName.setBounds(67, 90, 120, 33);
    txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
    contentPane.add(txtUserName);

    JLabel lblIp = new JLabel("IP Address");
    lblIp.setBounds(67, 138, 120, 25);
    lblIp.setHorizontalAlignment(SwingConstants.CENTER);
    lblIp.setForeground(Color.WHITE);
    lblIp.setOpaque(false);
    contentPane.add(lblIp);

    txtIpAddress = new RoundedTextField();
    txtIpAddress.setBounds(67, 168, 120, 33);
    txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
    txtIpAddress.setText("127.0.0.1");
    contentPane.add(txtIpAddress);

    JLabel lblPort = new JLabel("Port Number");
    lblPort.setBounds(67, 216, 120, 25);
    lblPort.setHorizontalAlignment(SwingConstants.CENTER);
    lblPort.setForeground(Color.WHITE);
    lblPort.setOpaque(false);
    contentPane.add(lblPort);

    txtPortNumber = new RoundedTextField();
    txtPortNumber.setBounds(67, 246, 120, 33);
    txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
    txtPortNumber.setText("30000");
    contentPane.add(txtPortNumber);

    JButton btnConnect = new RoundedButton("LOGIN");
    btnConnect.setBounds(67, 300, 120, 38);
    btnConnect.setFont(new Font("Tahoma", Font.BOLD, 14));
    btnConnect.setBackground(Color.WHITE);
    btnConnect.setForeground(Color.BLACK);
    btnConnect.setBorderPainted(false);
    btnConnect.setFocusPainted(false);
    contentPane.add(btnConnect);

    // ActionListener
    ActionListener action = e -> connectServer();
    btnConnect.addActionListener(action);
    txtUserName.addActionListener(action);
    txtIpAddress.addActionListener(action);
    txtPortNumber.addActionListener(action);
  }

  private void connectServer() {
    String username = txtUserName.getText().trim();
    String ip = txtIpAddress.getText().trim();
    int port;
    try {
      port = Integer.parseInt(txtPortNumber.getText().trim());
    } catch (NumberFormatException e) {
      JOptionPane.showMessageDialog(this, "포트번호를 숫자로 입력하세요.");
      return;
    }

    if (username.isEmpty()) {
      JOptionPane.showMessageDialog(this, "사용자 이름을 입력하세요.");
      return;
    }

    try {
      socket = new Socket(ip, port);
      dis = new DataInputStream(socket.getInputStream());
      dos = new DataOutputStream(socket.getOutputStream());

      // 서버에 username 전송 ("/login username")
      dos.writeUTF("/login " + username);

      // 친구 목록 창 생성
      friendList = new FriendList(username, ip, port,dos);

      // 서버에서 USERLIST 수신 스레드
      new Thread(() -> {
        try {
          while (true) {
            String msg = dis.readUTF();
            if (msg.startsWith("USERLIST:")) {
              String[] parts = msg.substring(9).split(":"); // "user1,user2...:user1=image1.jpg;user2=image2.jpg;"
              String[] names = parts[0].split(","); // user1,user2...

              Vector<String> users = new Vector<>(Arrays.asList(names));

              // 💡 이미지 정보를 파싱합니다.
              HashMap<String, String> imageMap = new HashMap<>();
              if (parts.length > 1) {
                String[] imageEntries = parts[1].split(";"); // user1=image1.jpg, user2=image2.jpg
                for (String entry : imageEntries) {
                  String[] kv = entry.split("=");
                  if (kv.length == 2) {
                    imageMap.put(kv[0], kv[1]);
                  }
                }
              }
              SwingUtilities.invokeLater(() -> friendList.updateFriends(users, imageMap)); // FriendList.updateFriends 메서드 수정 필요
            }
            if (msg.startsWith("ROOM_CREATED:")) {
              String[] parts = msg.split(":");
              String roomName = parts[1];
              Vector<String> members = new Vector<>(Arrays.asList(parts[2].split(",")));
              SwingUtilities.invokeLater(() -> {
                ChatRoomInfo room = new ChatRoomInfo(roomName, members);
                friendList.addChatRoom(room);
              });
            }
            // 💡 프로필 이미지 변경 메시지 수신 및 FriendList 갱신 요청
            if (msg.startsWith("CHANGE_PROFILE_IMAGE:")) {
              String[] parts = msg.split(":");
              if (parts.length >= 3) {
                String targetUser = parts[1]; // 변경한 사용자 이름
                String imageName = parts[2];  // 새 이미지 이름

                // 자기 자신 외의 프로필 변경만 처리 (자신의 변경은 이미 로컬에서 적용됨)
                if (!targetUser.equals(username)) {
                  SwingUtilities.invokeLater(() -> {
                    friendList.updateFriendProfileImage(targetUser, imageName);
                  });
                }
              }
            }
            // 필요 시 일반 메시지 처리도 여기에 추가 가능
          }
        } catch (IOException e) {
          System.out.println("Disconnected from server.");
          SwingUtilities.invokeLater(() -> {
            // friendList가 이미 닫혀 있을 수 있으므로 null 체크
            if(friendList != null) friendList.dispose();
            setVisible(true);
          });
        }
      }).start();

      // 로그인 창 닫기
      setVisible(false);

    } catch (IOException e) {
      JOptionPane.showMessageDialog(this, "서버 연결 실패: " + e.getMessage());
      e.printStackTrace();
    }
  }
}