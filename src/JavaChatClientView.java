import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class JavaChatClientView extends JFrame {
  private JPanel contentPane;
  private JTextField txtInput;
  private String UserName;
  private JButton btnSend;
  private JTextArea textArea;
  private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
  private Socket socket; // 연결소켓
  private InputStream is;
  private OutputStream os;
  private DataInputStream dis;
  private DataOutputStream dos;
  private JLabel lblUserName;
  private String currentRoomName;

  // 💡 추가된 멤버 변수
  private JLabel lblRoomName;
  private JLabel lblMembers;

  /**
   * Create the frame.
   */
  public JavaChatClientView(String username, String ip_addr, String port_no,String roomName) {
    this.currentRoomName = roomName;
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setBounds(100, 100, 392, 462);
    contentPane = new JPanel();
    contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(contentPane);
    contentPane.setLayout(null);

     // 💡 방 이름 표시 레이블 추가 및 위치 설정
    // 💡 멤버 명단 표시 레이블 추가 및 위치 설정
    lblMembers = new JLabel("Members: Loading...");
    lblMembers.setBounds(12, 10, 352, 25);
    lblMembers.setFont(new Font("Dialog", Font.BOLD, 14));
    contentPane.add(lblMembers);


    // 💡 JScrollPane 위치 조정 (lblRoomName, lblMembers 공간 확보)
    JScrollPane scrollPane = new JScrollPane();
    scrollPane.setBounds(12, 40, 352, 280);
    contentPane.add(scrollPane);

    textArea = new JTextArea();
    textArea.setEditable(false);
    scrollPane.setViewportView(textArea);

    txtInput = new JTextField();
    txtInput.setBounds(91, 365, 185, 40);
    contentPane.add(txtInput);
    txtInput.setColumns(10);

    btnSend = new JButton("Send");
    btnSend.setBounds(288, 364, 76, 40);
    contentPane.add(btnSend);

    lblUserName = new JLabel("Name");
    lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
    lblUserName.setBounds(12, 364, 67, 40);
    contentPane.add(lblUserName);
    setVisible(true);

    AppendText("User " + username + " connecting " + ip_addr + " " + port_no + "\n");
    UserName = username;
    lblUserName.setText(username + ">");

    try {
      socket = new Socket(ip_addr, Integer.parseInt(port_no));
      is = socket.getInputStream();
      dis = new DataInputStream(is);
      os = socket.getOutputStream();
      dos = new DataOutputStream(os);

      SendMessage("/login " + UserName);

      // 💡 채팅방 멤버 명단 요청 (연결 및 로그인 후 바로 요청)
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
      AppendText("connect error");
    }
  }

  class ListenNetwork extends Thread {
    public void run() {
      while (true) {
        try {
          String msg = dis.readUTF();

          // 💡 멤버 명단 수신 처리 추가
          if (msg.startsWith("ROOM_MEMBERS:")) {
            String[] parts = msg.split(":", 3);
            if (parts.length >= 3) {
              String receivedRoomName = parts[1];
              String membersList = parts[2]; // member1,member2,...

              if (receivedRoomName.equals(currentRoomName)) {
                // 뷰의 멤버 명단 레이블 업데이트 및 채팅창에 알림
                lblMembers.setText("Members: " + membersList.replace(",", ", "));
                AppendText("현재 접속 인원: " + membersList.replace(",", ", ") + "\n");
              }
            }
          }
          // 💡 ROOM_MSG만 현재 방 이름과 일치하는지 필터링
          else if (msg.startsWith("ROOM_MSG:")) {
            String[] parts = msg.split(":", 3);
            if (parts.length >= 3) {
              String receivedRoomName = parts[1];
              String actualMsg = parts[2];
              // 💡 현재 보고 있는 방 이름과 메시지의 방 이름이 일치하는 경우에만 출력
              if (receivedRoomName.equals(currentRoomName)) {
                AppendText(actualMsg + "\n");
              }
            }
          }
          else if (msg.startsWith("ROOM_CREATED:") || msg.startsWith("USERLIST:")) {
            continue;
          }
          else {
            AppendText(msg);
          }

        } catch (IOException e) {
          AppendText("dis.read() error\n"); // 오류 발생 시 줄 바꿈 추가
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

  // 메시지를 입력 후 Send 버튼 또는  keyboard enter key를 치면 서버로(다른 사용자에게) 전송
  class Myaction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (e.getSource() == btnSend || e.getSource() == txtInput) {
        String inputMsg = txtInput.getText().trim();

        if (!currentRoomName.isEmpty()) {
          // 🚨 방 메시지 형식으로 서버에 전송 🚨
          // 💡 통일된 currentRoomName을 사용
          String msgToSend = "SEND_ROOM_MSG:" + currentRoomName + ":" + inputMsg;
          SendMessage(msgToSend);
        } else {
          // 일반 채팅 또는 기존 처리 유지
          // 현재 코드에선 일반 채팅이 없으므로, exit 처리만 고려
          if (inputMsg.equals("/exit")) {
            SendMessage("/exit");
            System.exit(0);
          } else if (inputMsg.startsWith("/to ")) {
            // 귓속말 처리도 SendMessage(inputMsg)로 전송
            SendMessage(inputMsg);
          } else {
            // 방이 없는 경우 일반 메시지 처리 (필요하다면)
            // 현재 구조상 방에서만 메시지 송수신 가정
            SendMessage(inputMsg);
          }
        }

        txtInput.setText("");
        txtInput.requestFocus();
      }
    }
  }

  // 화면에 출력
  public void AppendText(String msg) {
    textArea.append(msg);
    textArea.setCaretPosition(textArea.getText().length());
  }


  // Server에게 network로 전송
  public void SendMessage(String msg) {
    try {
      // Use writeUTF to send messages
      dos.writeUTF(msg);
    } catch (IOException e) {
      AppendText("dos.write() error");
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