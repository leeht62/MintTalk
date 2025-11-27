package friendlist;

import chatclient.JavaChatClientView;
import chat.ChatRoomInfo;
import image.ImagePanel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.HashMap;
import java.util.Vector;

public class FriendList extends JFrame {
    private JPanel contentPane;
    private JPanel friendPanel;
    private JScrollPane scrollPane;
    private String username;
    private String ip;
    private int port;
    private DataOutputStream out;

    // UI 컴포넌트 멤버 변수화 (갱신을 위해)
    private JLabel myProfileLabel;
    private JLabel lblUser;
    private JLabel lblMyStatus; // 내 상태메시지 표시용

    private Vector<String> friendNames = new Vector<>();
    private static Vector<ChatRoomInfo> chatRooms = new Vector<>();

    // 데이터 저장소
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

        // --- [왼쪽 사이드바] ---
        JPanel sidePanel = new JPanel();
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(60, 0));
        sidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

        // 친구 아이콘
        JLabel lblPeopleIcon = new JLabel();
        lblPeopleIcon.setPreferredSize(new Dimension(35, 35));
        lblPeopleIcon.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon peopleIcon = new ImageIcon("image/people.jpg");
            Image img = peopleIcon.getImage();
            Image newImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            lblPeopleIcon.setIcon(new ImageIcon(newImg));
        } catch (Exception e) {
            lblPeopleIcon.setText("P");
        }

        // 채팅방 목록 버튼
        JButton btnChatList = new JButton();
        btnChatList.setBorderPainted(false);
        btnChatList.setContentAreaFilled(false);
        btnChatList.setFocusPainted(false);
        btnChatList.setMargin(new Insets(0, 0, 0, 0));
        try {
            ImageIcon chatIcon = new ImageIcon("image/chat_icon.png");
            if (chatIcon.getIconWidth() == -1) chatIcon = new ImageIcon("image/balloon.jpg");
            Image img = chatIcon.getImage();
            Image newImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
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

        // --- [오른쪽 메인 영역] ---
        JPanel rightAreaPanel = new JPanel(new BorderLayout());
        rightAreaPanel.setOpaque(false);

        // 상단 헤더 패널 (내 프로필)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 내 프로필 정보 패널
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        userPanel.setOpaque(false);

        myProfileLabel = new JLabel();
        myProfileLabel.setPreferredSize(new Dimension(50, 50));
        myProfileLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myProfileLabel.setName("ProfileImageLabel_" + username);

        // 초기 이미지 로드
        ImageIcon defaultIcon = getProfileIcon("profile.jpg");
        if (defaultIcon != null) myProfileLabel.setIcon(defaultIcon);
        else myProfileLabel.setText("👤");

        myProfileLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                openProfileDetail(username);
            }
        });

        // 이름 및 상태메시지를 담을 패널
        JPanel myInfoTextPanel = new JPanel(new GridLayout(2, 1));
        myInfoTextPanel.setOpaque(false);

        lblUser = new JLabel("(" + username + ")");
        lblUser.setFont(new Font("Dialog", Font.BOLD, 18));
        lblUser.setForeground(Color.BLACK);

        lblMyStatus = new JLabel(""); // 내 상태메시지
        lblMyStatus.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblMyStatus.setForeground(Color.GRAY);

        myInfoTextPanel.add(lblUser);
        myInfoTextPanel.add(lblMyStatus);

        userPanel.add(myProfileLabel);
        userPanel.add(myInfoTextPanel);

        // 대화하기 버튼
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

        // 중앙 친구 목록 스크롤 영역
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

    // 🚀 [핵심 수정] updateFriends: 서버 정보를 받아 맵을 갱신하고 화면을 다시 그립니다.
    public void updateFriends(Vector<String> names, String detailInfo) {
        // 1. 상세 정보 파싱 및 저장
        if (detailInfo != null && !detailInfo.isEmpty()) {
            String[] users = detailInfo.split(";");
            for (String u : users) {
                String[] parts = u.split("=");
                if (parts.length == 2) {
                    String uName = parts[0];
                    // split에 -1을 주어야 빈 문자열(상태메시지 없음 등)도 배열에 포함됨
                    String[] vals = parts[1].split("\\|", -1); 
                    if (vals.length >= 3) {
                        userImages.put(uName, vals[0]);
                        userBgImages.put(uName, vals[1]);
                        userStatusMsgs.put(uName, vals[2]);
                    }
                }
            }
        }

        // 2. [추가] 내 프로필 영역(상단) 갱신 (이미지, 상태메시지, 전체 배경)
        refreshMyProfile();

        // 3. 친구 목록 재생성
        friendPanel.removeAll();
        friendNames.clear();

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

    // 🚀 [추가] 내 프로필 영역 및 전체 배경화면 새로고침
    private void refreshMyProfile() {
        // 1. 내 이미지 갱신
        String myImg = userImages.getOrDefault(username, "profile.jpg");
        ImageIcon newIcon = getProfileIcon(myImg);
        if(myProfileLabel != null) myProfileLabel.setIcon(newIcon);

        // 2. 내 상태메시지 갱신
        String myMsg = userStatusMsgs.getOrDefault(username, "");
        if(lblMyStatus != null) lblMyStatus.setText(myMsg);

        // 3. 전체 배경화면 갱신 (ImagePanel 캐스팅 필요)
        String myBg = userBgImages.getOrDefault(username, "image/abc.jpg");
        if (contentPane instanceof ImagePanel) {
            ((ImagePanel) contentPane).setImagePath("image/" + myBg);
            contentPane.repaint();
        }
    }

    // addFriend: 친구 목록 UI 생성
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

        // 상태 메시지 라벨
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

    private void openProfileDetail(String targetName) {
        String img = userImages.getOrDefault(targetName, "profile.jpg");
        String bg = userBgImages.getOrDefault(targetName, "ab.jpg");
        String msg = userStatusMsgs.getOrDefault(targetName, "");
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

    // 실시간 업데이트용 (보조)
    public void updateFriendProfileImage(String targetUser, String imageName) {
        userImages.put(targetUser, imageName);
        // 전체 갱신 호출 (상태메시지 등 일관성을 위해)
        refreshMyProfile(); 
        // 친구 목록의 특정 이미지만 바꾸고 싶다면 아래 재귀함수 사용 가능하지만,
        // 현재 구조상 updateFriends가 주기적으로 호출된다면 굳이 필요 없을 수 있음.
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
            Image newimg = image.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(newimg);
        } catch (Exception e) {
            System.err.println("이미지 파일을 찾을 수 없습니다: image/" + imageName);
            return null;
        }
    }
}