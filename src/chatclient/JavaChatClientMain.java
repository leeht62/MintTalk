package chatclient;

import chat.ChatRoomInfo;
import friendlist.FriendList;
import image.ImagePanel;
import image.RoundedButton;
import image.RoundedTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

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
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JLabel lblTitle = new JLabel("Mint Talk");
        lblTitle.setFont(new Font("Serif", Font.BOLD, 40));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(12, 10, 218, 50);
        lblTitle.setForeground(Color.WHITE);
        contentPane.add(lblTitle);

        // 입력창 라벨 및 텍스트필드 추가
        addInputLabel("User Name", 67, 70);
        txtUserName = addInputField(67, 95);

        addInputLabel("IP Address", 67, 135);
        txtIpAddress = addInputField(67, 160);
        txtIpAddress.setText("127.0.0.1");

        addInputLabel("Port Number", 67, 200);
        txtPortNumber = addInputField(67, 225);
        txtPortNumber.setText("30000");

        // 로그인 버튼
        JButton btnConnect = new RoundedButton("LOGIN");
        btnConnect.setBounds(67, 280, 120, 40);
        btnConnect.setFont(new Font("Tahoma", Font.BOLD, 14));
        btnConnect.setBackground(Color.WHITE);
        btnConnect.setForeground(Color.BLACK);
        btnConnect.addActionListener(e -> connectServer());
        contentPane.add(btnConnect);

        txtUserName.addActionListener(e -> connectServer());
        txtIpAddress.addActionListener(e -> connectServer());
        txtPortNumber.addActionListener(e -> connectServer());
    }

    private void addInputLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 120, 25);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        contentPane.add(label);
    }

    private RoundedTextField addInputField(int x, int y) {
        RoundedTextField field = new RoundedTextField();
        field.setBounds(x, y, 120, 30);
        field.setHorizontalAlignment(SwingConstants.CENTER);
        contentPane.add(field);
        return field;
    }

    private void connectServer() {
        String username = txtUserName.getText().trim();
        String ip = txtIpAddress.getText().trim();
        int port;

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "사용자 이름을 입력하세요.");
            return;
        }

        try {
            port = Integer.parseInt(txtPortNumber.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "포트번호는 숫자여야 합니다.");
            return;
        }

        try {
            socket = new Socket(ip, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            dos.writeUTF("/login " + username);

            friendList = new FriendList(username, ip, port, dos);

            new Thread(this::listenToServer).start();

            this.setVisible(false);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "서버 연결 실패: " + e.getMessage());
        }
    }

    private void listenToServer() {
        try {
            while (true) {
                String msg = dis.readUTF();

                // 친구 목록 데이터 수신
                if (msg.startsWith("USERLIST:")) {
                    String[] sections = msg.split(":", 3);
                    String namesPart = sections[1];
                    String detailsPart = (sections.length > 2) ? sections[2] : "";
                    
                    Vector<String> users = new Vector<>(Arrays.asList(namesPart.split(",")));
                    SwingUtilities.invokeLater(() -> friendList.updateFriends(users, detailsPart));
                }
                // 채팅방 생성 알림
                else if (msg.startsWith("ROOM_CREATED:")) {
                    String[] parts = msg.split(":");
                    String roomName = parts[1];
                    Vector<String> members = new Vector<>(Arrays.asList(parts[2].split(",")));
                    
                    SwingUtilities.invokeLater(() -> {
                        ChatRoomInfo room = new ChatRoomInfo(roomName, members);
                        friendList.addChatRoom(room);
                    });
                }
                else if (msg.startsWith("HEALTH_BROADCAST:")) { 
                    friendList.handleHealthCommand(msg);
                }
            }
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                if (friendList != null) friendList.dispose();
                this.setVisible(true);
                JOptionPane.showMessageDialog(this, "서버 연결이 종료되었습니다.");
            });
        }
    }
}