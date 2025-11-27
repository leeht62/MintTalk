package friendlist;

import chat.ChatRoomInfo;
import chatclient.JavaChatClientView;
import image.ImagePanel;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.DataOutputStream;
import java.util.Vector;

public class ChatRoomList extends JFrame {
  private JPanel contentPane;
  private JPanel roomListPanel; // 친구 목록 대신 채팅방 목록 패널
  private JScrollPane scrollPane;

  // 데이터 및 통신 관련 변수
  private String username;
  private String ip;
  private int port;
  private DataOutputStream out;
  private Vector<ChatRoomInfo> chatRooms; // 채팅방 데이터
  private FriendList mainFriendList; // 뒤로가기(친구목록)를 위해 원본 창 저장

  // 생성자: FriendList의 정보를 받아옴
  public ChatRoomList(String username, String ip, int port, DataOutputStream out,
                      Vector<ChatRoomInfo> chatRooms, FriendList mainFriendList) {
    this.username = username;
    this.ip = ip;
    this.port = port;
    this.out = out;
    this.chatRooms = chatRooms;
    this.mainFriendList = mainFriendList;

    setTitle("Chat Room List - " + username);
    setSize(300, 600);
    if (mainFriendList != null) {
      this.setLocation(mainFriendList.getLocation());
    }
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // 1. 배경 설정 (FriendList와 동일)
    contentPane = new ImagePanel("image/abc.jpg");
    contentPane.setLayout(new BorderLayout());
    setContentPane(contentPane);

    // 2. 왼쪽 사이드바 (FriendList와 동일 + 버튼 기능 변경)
    JPanel sidePanel = new JPanel();
    sidePanel.setOpaque(false);
    sidePanel.setPreferredSize(new Dimension(60, 0));
    sidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

    // (A) 친구 목록으로 돌아가는 버튼 (사람 아이콘)
    JButton btnFriendList = new JButton();
    btnFriendList.setBorderPainted(false);
    btnFriendList.setContentAreaFilled(false);
    btnFriendList.setFocusPainted(false);
    btnFriendList.setMargin(new Insets(0, 0, 0, 0));
    setSidebarIcon(btnFriendList, "image/people.jpg", "P");

    // [중요] 친구 목록 아이콘 클릭 시 -> 현재 창 끄고 FriendList 다시 보여주기
    btnFriendList.addActionListener(e -> {
      this.setVisible(false);       // 현재 창 숨김
      mainFriendList.setVisible(true); // 친구 목록 창 다시 보임
      this.dispose(); // 메모리 해제
    });

    // (B) 채팅방 목록 아이콘 (현재 화면임)
    JLabel lblChatIcon = new JLabel();
    lblChatIcon.setPreferredSize(new Dimension(35, 35));
    lblChatIcon.setHorizontalAlignment(SwingConstants.CENTER);
    setSidebarIconLabel(lblChatIcon, "image/chat_icon.png", "image/balloon.jpg", "T");

    sidePanel.add(btnFriendList);
    sidePanel.add(lblChatIcon);
    contentPane.add(sidePanel, BorderLayout.WEST);

    // 3. 오른쪽 영역 (헤더 + 리스트)
    JPanel rightAreaPanel = new JPanel(new BorderLayout());
    rightAreaPanel.setOpaque(false);

    // (A) 헤더 (제목 표시)
    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    headerPanel.setOpaque(false);
    headerPanel.setBorder(new EmptyBorder(15, 10, 10, 10));

    JLabel titleLabel = new JLabel("채팅방 목록");
    titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
    titleLabel.setForeground(Color.BLACK);
    headerPanel.add(titleLabel);

    rightAreaPanel.add(headerPanel, BorderLayout.NORTH);

    // (B) 채팅방 리스트 패널
    roomListPanel = new JPanel();
    roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
    roomListPanel.setOpaque(false);

    scrollPane = new JScrollPane(roomListPanel);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.setBorder(null);

    rightAreaPanel.add(scrollPane, BorderLayout.CENTER);
    contentPane.add(rightAreaPanel, BorderLayout.CENTER);

    loadChatRooms();

    setVisible(true);
  }

  private void loadChatRooms() {
    roomListPanel.removeAll();

    if (chatRooms != null) {
      for (ChatRoomInfo room : chatRooms) {
        addRoomItem(room);
      }
    }

    roomListPanel.revalidate();
    roomListPanel.repaint();
  }

  private void addRoomItem(ChatRoomInfo room) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(new Dimension(220, 60)); // 높이 60
    panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
    panel.setOpaque(false);
    panel.setBorder(new CompoundBorder(
        new MatteBorder(0, 0, 1, 0, new Color(200, 200, 200, 100)), // 밑줄
        new EmptyBorder(5, 5, 5, 5)
    ));

    JLabel nameLabel = new JLabel(room.roomName); // roomName 필드 사용 가정
    nameLabel.setFont(new Font("Dialog", Font.PLAIN, 15));
    nameLabel.setForeground(Color.BLACK);
    nameLabel.setBorder(new EmptyBorder(0, 10, 0, 0)); // 왼쪽 여백

    JButton btnEnter = new JButton("입장");
    btnEnter.setFont(new Font("Dialog", Font.PLAIN, 12));
    btnEnter.setBackground(Color.WHITE);
    btnEnter.setFocusPainted(false);
    btnEnter.addActionListener(e -> {
      new JavaChatClientView(username, ip, String.valueOf(port), room.roomName);
    });

    panel.add(nameLabel, BorderLayout.CENTER);
    panel.add(btnEnter, BorderLayout.EAST);

    roomListPanel.add(panel);
  }

  private void setSidebarIcon(JButton btn, String path, String altText) {
    try {
      ImageIcon icon = new ImageIcon(path);
      Image img = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
      btn.setIcon(new ImageIcon(img));
    } catch (Exception e) {
      btn.setText(altText);
    }
  }

  private void setSidebarIconLabel(JLabel lbl, String path1, String path2, String altText) {
    try {
      ImageIcon icon = new ImageIcon(path1);
      if (icon.getIconWidth() == -1 && path2 != null) icon = new ImageIcon(path2);
      Image img = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
      lbl.setIcon(new ImageIcon(img));
    } catch (Exception e) {
      lbl.setText(altText);
    }
  }
}