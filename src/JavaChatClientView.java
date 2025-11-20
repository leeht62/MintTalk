import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class JavaChatClientView extends JFrame {
    private JPanel contentPane;
    private RoundedTextField txtInput;
    private String UserName;
    private RoundedButton btnSend;
    
    // 채팅 목록 관련 변수
    private JList<ChatMessage> chatList;
    private DefaultListModel<ChatMessage> listModel;
    
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private JLabel lblUserName;
    private String currentRoomName;

    private JLabel lblRoomName; // 사용되지 않지만 선언 유지
    private JLabel lblMembers;

    public JavaChatClientView(String username, String ip_addr, String port_no, String roomName) {
        this.currentRoomName = roomName;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 392, 462);

        // [사용자 설정 유지] 배경 이미지 경로
        contentPane = new ImagePanel("image/mint2.jpg");
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        lblMembers = new JLabel("Members: Loading...");
        lblMembers.setBounds(12, 10, 352, 25);
        lblMembers.setFont(new Font("Dialog", Font.BOLD, 14));
        lblMembers.setOpaque(false);
        lblMembers.setForeground(Color.BLACK);
        contentPane.add(lblMembers);

        // --- JList 설정 (채팅창) ---
        listModel = new DefaultListModel<>();
        chatList = new JList<>(listModel);
        chatList.setCellRenderer(new ChatCellRenderer()); // 렌더러 연결
        chatList.setOpaque(false);
        chatList.setBackground(new Color(0, 0, 0, 0));
        chatList.setFocusable(false);
        
        JScrollPane scrollPane = new JScrollPane(chatList);
        scrollPane.setBounds(12, 40, 352, 280);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        contentPane.add(scrollPane);
        // -------------------------

        txtInput = new RoundedTextField();
        txtInput.setBounds(91, 365, 185, 40);
        txtInput.setBackground(Color.WHITE);
        txtInput.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        contentPane.add(txtInput);
        txtInput.setColumns(10);

        btnSend = new RoundedButton("Send");
        btnSend.setBounds(288, 364, 76, 40);
        btnSend.setBackground(new Color(0, 150, 136));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Tahoma", Font.BOLD, 14));
        contentPane.add(btnSend);

        lblUserName = new JLabel("Name");
        lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
        lblUserName.setBounds(12, 364, 67, 40);
        lblUserName.setOpaque(false);
        lblUserName.setForeground(Color.BLACK);
        lblUserName.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        contentPane.add(lblUserName);
        setVisible(true);

        UserName = username;
        lblUserName.setText(username + ">");

        try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            SendMessage("/login " + UserName);

            if (!currentRoomName.isEmpty()) {
                SendMessage("GET_ROOM_MEMBERS:" + currentRoomName);
            }

            ListenNetwork net = new ListenNetwork();
            net.start();
            Myaction action = new Myaction();
            btnSend.addActionListener(action);
            txtInput.addActionListener(action);
            txtInput.requestFocus();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendMessage("Error", "Connect error", false);
        }
    }

    // 네트워크 수신 스레드
    class ListenNetwork extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();

                    if (msg.startsWith("ROOM_MEMBERS:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) {
                            String receivedRoomName = parts[1];
                            String membersList = parts[2];

                            if (receivedRoomName.equals(currentRoomName)) {
                                lblMembers.setText("Members: " + membersList.replace(",", ", "));
                                AppendMessage("System", "현재 접속 인원: " + membersList.replace(",", ", "), false);
                            }
                        }
                    } else if (msg.startsWith("ROOM_MSG:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) {
                            String receivedRoomName = parts[1];
                            String actualMsg = parts[2];

                            if (receivedRoomName.equals(currentRoomName)) {
                                String sender = "Unknown";
                                String message = actualMsg;

                                // [이름] 내용 파싱
                                if (actualMsg.startsWith("[") && actualMsg.contains("]")) {
                                    int endOfSender = actualMsg.indexOf("]");
                                    sender = actualMsg.substring(1, endOfSender).trim();
                                    message = actualMsg.substring(endOfSender + 1).trim();
                                }

                                boolean isMine = sender.equals(UserName);
                                AppendMessage(sender, message, isMine);
                            }
                        }
                    } else if (msg.startsWith("ROOM_CREATED:") || msg.startsWith("USERLIST:")) {
                        continue;
                    } else if (msg.toLowerCase().contains("welcome")) {
                        continue;
                    } else {
                        AppendMessage("System", msg, false);
                    }

                } catch (IOException e) {
                    AppendMessage("Error", "Connection lost", false);
                    try {
                        dos.close();
                        dis.close();
                        socket.close();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }

    class Myaction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnSend || e.getSource() == txtInput) {
                String inputMsg = txtInput.getText().trim();
                if (inputMsg.isEmpty()) return;

                if (!currentRoomName.isEmpty()) {
                    String msgToSend = "SEND_ROOM_MSG:" + currentRoomName + ":" + inputMsg;
                    SendMessage(msgToSend);
                } else {
                    if (inputMsg.equals("/exit")) {
                        SendMessage("/exit");
                        System.exit(0);
                    } else {
                        SendMessage(inputMsg);
                    }
                }

                txtInput.setText("");
                txtInput.requestFocus();
            }
        }
    }

    public void AppendMessage(String sender, String message, boolean isMine) {
        // [핵심] 보낸 사람 이름(sender)을 프로필 이미지 찾는 키워드로 전달
        // ChatCellRenderer가 "image/sender.jpg" 등을 찾게 됨
        String profileName = sender; 
        
        ChatMessage chatMessage = new ChatMessage(sender, message, isMine, profileName);
        listModel.addElement(chatMessage);

        int lastIndex = listModel.getSize() - 1;
        if (lastIndex >= 0) {
            chatList.ensureIndexIsVisible(lastIndex);
        }
    }

    public void SendMessage(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            AppendMessage("Error", "dos.write() error", false);
            try {
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }
}