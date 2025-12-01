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
    private JPanel roomListPanel;
    
    private String username;
    private String ip;
    private int port;
    private DataOutputStream out;
    private Vector<ChatRoomInfo> chatRooms;
    private FriendList mainFriendList;

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

        contentPane = new ImagePanel("image/abc.jpg");
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        // 사이드바
        JPanel sidePanel = new JPanel();
        sidePanel.setOpaque(false);
        sidePanel.setPreferredSize(new Dimension(60, 0));
        sidePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 15));

        // 친구 목록으로 돌아가기 버튼
        JButton btnFriendList = new JButton();
        setSidebarButton(btnFriendList, "image/people.jpg", "P");
        btnFriendList.addActionListener(e -> {
            this.setVisible(false);
            if (mainFriendList != null) {
                mainFriendList.setLocation(this.getLocation());
                mainFriendList.setVisible(true);
            }
            this.dispose();
        });

        JLabel lblChatIcon = new JLabel();
        setSidebarIconLabel(lblChatIcon, "image/chat_icon.png", "T");

        sidePanel.add(btnFriendList);
        sidePanel.add(lblChatIcon);
        contentPane.add(sidePanel, BorderLayout.WEST);

        // 메인 영역
        JPanel rightAreaPanel = new JPanel(new BorderLayout());
        rightAreaPanel.setOpaque(false);

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 10, 10, 10));

        JLabel titleLabel = new JLabel("채팅");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 18));
        titleLabel.setForeground(Color.BLACK);
        headerPanel.add(titleLabel);

        rightAreaPanel.add(headerPanel, BorderLayout.NORTH);

        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(roomListPanel);
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
        panel.setPreferredSize(new Dimension(220, 70));
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(200, 200, 200, 100)),
            new EmptyBorder(5, 5, 5, 5)
        ));

        JLabel iconLabel = new JLabel();
        setSidebarIconLabel(iconLabel, "image/balloon.jpg", "Talk");
        iconLabel.setPreferredSize(new Dimension(50, 50));

        JLabel nameLabel = new JLabel(room.roomName);
        nameLabel.setFont(new Font("Dialog", Font.BOLD, 14));
        nameLabel.setForeground(Color.BLACK);
        nameLabel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JButton btnEnter = new JButton("입장");
        btnEnter.setFont(new Font("Dialog", Font.PLAIN, 12));
        btnEnter.setBackground(Color.WHITE);
        btnEnter.setFocusPainted(false);
        btnEnter.addActionListener(e -> {
            new JavaChatClientView(username, ip, String.valueOf(port), room.roomName);
        });

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(nameLabel, BorderLayout.CENTER);
        panel.add(btnEnter, BorderLayout.EAST);

        roomListPanel.add(panel);
    }

    private void setSidebarButton(JButton btn, String path, String alt) {
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setMargin(new Insets(0, 0, 0, 0));
        try {
            ImageIcon icon = new ImageIcon(path);
            btn.setIcon(new ImageIcon(icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
        } catch (Exception e) { btn.setText(alt); }
    }

    private void setSidebarIconLabel(JLabel lbl, String path, String alt) {
        lbl.setPreferredSize(new Dimension(35, 35));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            ImageIcon icon = new ImageIcon(path);
            lbl.setIcon(new ImageIcon(icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH)));
        } catch (Exception e) { lbl.setText(alt); }
    }
}