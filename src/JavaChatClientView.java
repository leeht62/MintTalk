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

    private JLabel lblRoomName;
    private JLabel lblMembers;

    public JavaChatClientView(String username, String ip_addr, String port_no, String roomName) {
        this.currentRoomName = roomName;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 392, 462);

        contentPane = new ImagePanel("image/mint2.jpg");
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        lblMembers = new JLabel("Members: Loading...");
        lblMembers.setBounds(12, 10, 352, 25);
        lblMembers.setFont(new Font("Dialog", Font.BOLD, 14));
        lblMembers.setOpaque(false);
        lblMembers.setForeground(Color.black);
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
        lblUserName.setFont(new Font("Tahoma", Font.BOLD, 14));
        contentPane.add(lblUserName);
        setVisible(true);

//        AppendMessage("System", "User " + username + " connecting...", false);
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
 // JavaChatClientView.java 내부의 ListenNetwork 클래스 수정

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
            }
            else if (msg.startsWith("ROOM_MSG:")) {
              String[] parts = msg.split(":", 3);
              if (parts.length >= 3) {
                String receivedRoomName = parts[1];
                String actualMsg = parts[2];
                
                if (receivedRoomName.equals(currentRoomName)) {
                  String sender = "Unknown";
                  String message = actualMsg;
                  
                  // [수정] 파싱 로직 강화 (공백 제거 및 디버깅)
                  // 형식: "[이름] 내용" 또는 "[이름]내용" 모두 처리
                  if(actualMsg.startsWith("[") && actualMsg.contains("]")) {
                      int endOfSender = actualMsg.indexOf("]");
                      // 이름 추출 및 앞뒤 공백 제거 (.trim())
                      sender = actualMsg.substring(1, endOfSender).trim(); 
                      // 메시지 추출 (] 뒤의 모든 내용)
                      message = actualMsg.substring(endOfSender + 1).trim();
                  }
                  
                  // [중요] 내 이름과 보낸 사람 이름 비교
                  boolean isMine = sender.equals(UserName);
                  
                  // [디버깅용 로그] 이클립스 콘솔창을 확인해보세요!
                  System.out.println("보낸사람: [" + sender + "] / 내이름: [" + UserName + "] -> 내꺼인가? " + isMine);
                  
                  AppendMessage(sender, message, isMine);
                }
              }
            }
            else if (msg.startsWith("ROOM_CREATED:") || msg.startsWith("USERLIST:")) {
                continue;
              }
              
              // 🚀 [추가] 서버에서 오는 "Welcome" 메시지(혹은 환영 메시지)를 필터링하여 무시합니다.
              else if (msg.toLowerCase().contains("welcome")) {
                  continue; // 해당 메시지를 무시하고 다음 루프로 넘어갑니다.
              }
              
              else {
                // 기타 시스템 메시지
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

    // 버튼 액션 처리 클래스 (에러 수정됨)
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

    // 채팅 메시지 추가 메서드 (JList용)
public void AppendMessage(String sender, String message, boolean isMine) {
        
        // 🚀 프로필 이미지 이름으로 'sender(유저이름)'를 그대로 넘깁니다.
        // ChatCellRenderer가 "image/유저이름.jpg" 등을 자동으로 찾아줄 것입니다.
        String profileName = sender; 
        
        ChatMessage chatMessage = new ChatMessage(sender, message, isMine, profileName);
        listModel.addElement(chatMessage);

        // 자동 스크롤
        int lastIndex = listModel.getSize() - 1;
        if (lastIndex >= 0) {
            chatList.ensureIndexIsVisible(lastIndex);
        }
    }
    // 서버로 메시지 전송
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