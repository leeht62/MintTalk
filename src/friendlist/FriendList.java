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
    
    private HealthCare healthWindow = null;

    private JLabel myProfileLabel;
    private JLabel lblUser;
    private JLabel lblMyStatus;

    private Vector<String> friendNames = new Vector<>();
    private static Vector<ChatRoomInfo> chatRooms = new Vector<>();

    // 사용자 프로필 정보 저장
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

        // 배경 이미지 설정
        contentPane = new ImagePanel("image/abc.jpg");
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        JPanel sidePanel = new JPanel();
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(60, 0));
        sidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

        // 친구목록으로 들어가는 아이콘
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

        // 채팅방 목록 이동 버튼
        JButton btnChatList = new JButton();
        btnChatList.setBorderPainted(false);
        btnChatList.setContentAreaFilled(false);
        btnChatList.setFocusPainted(false);
        btnChatList.setMargin(new Insets(0, 0, 0, 0));
        try {
            ImageIcon chatIcon = new ImageIcon("image/balloon.jpg");
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

        // 헬스케어 기능 버튼
        JButton btnHealth = new JButton();
        btnHealth.setBorderPainted(false);
        btnHealth.setContentAreaFilled(false);
        btnHealth.setFocusPainted(false);
        btnHealth.setMargin(new Insets(0, 0, 0, 0));
        try {
            ImageIcon healthIcon = new ImageIcon("image/health.jpg");
            Image img = healthIcon.getImage();
            Image newImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            btnHealth.setIcon(new ImageIcon(newImg));
        } catch (Exception e) {
            btnHealth.setText("Health");
        }
        btnHealth.addActionListener(e -> {
            if (healthWindow == null || !healthWindow.isVisible()) {
                healthWindow = new HealthCare(username, out, this);
            } else {
                healthWindow.toFront();
            }
        });

        sidePanel.add(lblPeopleIcon);
        sidePanel.add(btnChatList);
        sidePanel.add(btnHealth);
        contentPane.add(sidePanel, BorderLayout.WEST);

        JPanel rightAreaPanel = new JPanel(new BorderLayout());
        rightAreaPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        userPanel.setOpaque(false);

        myProfileLabel = new JLabel();
        myProfileLabel.setPreferredSize(new Dimension(50, 50));
        myProfileLabel.setHorizontalAlignment(SwingConstants.CENTER);
        myProfileLabel.setName("ProfileImageLabel_" + username);

        ImageIcon defaultIcon = getProfileIcon("profile.jpg");
        myProfileLabel.setIcon(defaultIcon);

        myProfileLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                openProfileDetail(username);
            }
        });

        JPanel myInfoTextPanel = new JPanel(new GridLayout(2, 1));
        myInfoTextPanel.setOpaque(false);

        lblUser = new JLabel("(" + username + ")");
        lblUser.setFont(new Font("Dialog", Font.BOLD, 18));
        lblUser.setForeground(Color.BLACK);

        lblMyStatus = new JLabel("");
        lblMyStatus.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblMyStatus.setForeground(Color.GRAY);

        myInfoTextPanel.add(lblUser);
        myInfoTextPanel.add(lblMyStatus);

        userPanel.add(myProfileLabel);
        userPanel.add(myInfoTextPanel);

        // 대화하기(방 만들기) 버튼
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

        // 친구 목록 리스트 패널
        friendPanel = new JPanel();
        friendPanel.setLayout(new BoxLayout(friendPanel, BoxLayout.Y_AXIS));
        friendPanel.setOpaque(false);

        scrollPane = new JScrollPane(friendPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        rightAreaPanel.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(rightAreaPanel, BorderLayout.CENTER);

        setVisible(true);
    }

    // 헬스케어 메시지 전송
    public void handleHealthCommand(String msg) {
        if (healthWindow != null && healthWindow.isVisible()) {
            healthWindow.processMessage(msg);
        }
    }

    // 친구 목록 및 프로필 정보 업데이트
    public void updateFriends(Vector<String> names, String detailInfo) {
        if (detailInfo != null && !detailInfo.isEmpty()) {
            String[] users = detailInfo.split(";");
            for (String u : users) {
                String[] parts = u.split("=");
                if (parts.length == 2) {
                    String uName = parts[0];
                    String[] vals = parts[1].split("\\|", -1); 
                    if (vals.length >= 3) {
                        userImages.put(uName, vals[0]);
                        userBgImages.put(uName, vals[1]);
                        userStatusMsgs.put(uName, vals[2]);
                    }
                }
            }
        }

        refreshMyProfile();

        friendPanel.removeAll();
        friendNames.clear();

        for (String n : names) {
            if (n == null) continue;
            String trimmed = n.trim();
            if (!trimmed.isEmpty() && !trimmed.equals(username)) {
                String imageName = userImages.getOrDefault(trimmed, "profile.jpg");
                addFriend(trimmed, imageName);
            }
        }

        friendPanel.revalidate();
        friendPanel.repaint();
    }

    // 내 프로필 정보(이미지, 상태메시지) UI 갱신
    private void refreshMyProfile() {
        String myImg = userImages.getOrDefault(username, "profile.jpg");
        ImageIcon newIcon = getProfileIcon(myImg);
        if(myProfileLabel != null) myProfileLabel.setIcon(newIcon);

        String myMsg = userStatusMsgs.getOrDefault(username, "");
        if(lblMyStatus != null) lblMyStatus.setText(myMsg);
    }

    // 친구 목록 추가
    public void addFriend(String friendName, String imageName) {
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
        profileLabel.setName("ProfileImageLabel_" + friendName);

        ImageIcon currentIcon = getProfileIcon(imageName);
        profileLabel.setIcon(currentIcon);


        profileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openProfileDetail(friendName);
            }
        });

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel nameLabel = new JLabel(friendName);
        nameLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setName("FriendNameLabel_" + friendName);

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

    // 상세 프로필 열기
    private void openProfileDetail(String targetName) {
        String img = userImages.getOrDefault(targetName, "profile.jpg");
        String bg = userBgImages.getOrDefault(targetName, "ab.jpg");
        String msg = userStatusMsgs.getOrDefault(targetName, "");
        new ProfileDetailDialog(this, username, targetName, img, bg, msg, out);
    }

    // 대화 상대 선택
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

    private void openChatRoom(String roomName) {
        new JavaChatClientView(username, ip, String.valueOf(port), roomName);
    }

    public void addChatRoom(ChatRoomInfo room) {
        chatRooms.add(room);
    }

    // 이미지 로딩 메소드
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
            return null;
        }
    }
}