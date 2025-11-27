import chat.ChatRoomInfo;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class JavaChatServer extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket serverSocket;
    private Vector<UserService> userVec = new Vector<>(); // 연결된 사용자
    private Vector<String> userList = new Vector<>();     // 접속자 이름
    private HashMap<String, ChatRoomInfo> chatRooms = new HashMap<>();

    // [수정 1] 사용자 정보 저장소 (프로필, 배경, 상태메시지) 추가
    private HashMap<String, String> userProfileImages = new HashMap<>();
    private HashMap<String, String> userBgImages = new HashMap<>();     // 배경 이미지 저장소
    private HashMap<String, String> userStatusMsgs = new HashMap<>();   // 상태 메시지 저장소

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                JavaChatServer frame = new JavaChatServer();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public JavaChatServer() {
        setTitle("Java Chat Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 350, 400);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 300, 240);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblPort = new JLabel("Port Number");
        lblPort.setBounds(12, 260, 100, 25);
        contentPane.add(lblPort);

        txtPortNumber = new JTextField("30000");
        txtPortNumber.setBounds(120, 260, 190, 25);
        contentPane.add(txtPortNumber);

        JButton btnStart = new JButton("Server Start");
        btnStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnStart);

        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    serverSocket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                    appendText("Chat Server Running on port " + txtPortNumber.getText());
                    btnStart.setEnabled(false);
                    txtPortNumber.setEnabled(false);
                    new AcceptServer().start(); // 접속자 수락 스레드
                } catch (Exception ex) {
                    appendText("Server start error: " + ex.getMessage());
                }
            }
        });
    }

    private void appendText(String msg) {
        textArea.append(msg + "\n");
        textArea.setCaretPosition(textArea.getText().length());
    }

    //접속 스레드
    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    appendText("Waiting for clients...");
                    Socket clientSocket = serverSocket.accept();
                    appendText("New client connected: " + clientSocket);
                    UserService user = new UserService(clientSocket);
                    user.start();
                } catch (IOException e) {
                    appendText("Accept error: " + e.getMessage());
                    break;
                }
            }
        }
    }

    //클라이언트 스레드
    class UserService extends Thread {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String userName;

        public UserService(Socket socket) {
            try {
                this.socket = socket;
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                // 클라이언트 최초 접속시 username 수신
                String firstMsg = dis.readUTF(); // "/login username"
                String[] tokens = firstMsg.split(" ");
                if (tokens.length >= 2) {
                    userName = tokens[1].trim();
                    appendText("User joined: " + userName);
                    userVec.add(this);
                    if (!userList.contains(userName)) {
                        userList.add(userName);
                        // [수정 2] 초기값 설정 (프로필, 배경, 상태메시지)
                        if(!userProfileImages.containsKey(userName)) userProfileImages.put(userName, "profile.jpg");
                        if(!userBgImages.containsKey(userName)) userBgImages.put(userName, "ab.jpg"); // 기본 배경
                        if(!userStatusMsgs.containsKey(userName)) userStatusMsgs.put(userName, "");   // 기본 상태메시지 없음
                    }

                    writeOne("Welcome " + userName + "\n");

                    sendUserListToAll();
                }
            } catch (IOException e) {
                appendText("UserService init error: " + e.getMessage());
            }
        }

        // 클라이언트에게 단일 메시지 전송
        public void writeOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                disconnect();
            }
        }

        // 모든 사용자에게 메시지 전송
        public void broadcast(String msg) {
            synchronized (userVec) {
                for (UserService u : userVec) {
                    u.writeOne(msg);
                }
            }
        }

        // 모든 사용자에게 실시간 접속자 목록 전송 (배경, 상태메시지 포함)
        private void sendUserListToAll() {
            String listMsg = "USERLIST:" + String.join(",", userList);

            // 데이터 형식: user1=프로필|배경|상태메시지;user2=...
            StringBuilder imageInfo = new StringBuilder();
            for (String user : userList) {
                String img = userProfileImages.getOrDefault(user, "profile.jpg");
                String bg = userBgImages.getOrDefault(user, "ab.jpg");
                String msg = userStatusMsgs.getOrDefault(user, "");

                // 구분자 | 를 사용하여 3가지 정보를 묶음
                imageInfo.append(user).append("=")
                    .append(img).append("|")
                    .append(bg).append("|")
                    .append(msg).append(";");
            }

            // 최종 메시지 포맷: USERLIST:user1,user2...:user1=img|bg|msg;...
            String fullMsg = listMsg + ":" + imageInfo.toString();

            synchronized (userVec) {
                for (UserService u : userVec) {
                    u.writeOne(fullMsg);
                }
            }
        }

        // 로그아웃 처리
        private void disconnect() {
            try {
                appendText("User left: " + userName);
                userVec.remove(this);
                userList.remove(userName);
                sendUserListToAll();
                if (dis != null) dis.close();
                if (dos != null) dos.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //스레드 루프
        public void run() {
            try {
                while (true) {
                    String msg = dis.readUTF().trim();

                    if (msg.equals("/exit")) {
                        disconnect();
                        break;
                    }

                    // 귓속말 처리: /to username message
                    if (msg.startsWith("/to ")) {
                        String[] parts = msg.split(" ", 3);
                        if (parts.length >= 3) {
                            String target = parts[1];
                            String privateMsg = parts[2];
                            boolean found = false;
                            synchronized (userVec) {
                                for (UserService u : userVec) {
                                    if (u.userName.equals(target)) {
                                        u.writeOne("[귓속말][" + userName + "] " + privateMsg);
                                        writeOne("[귓속말][" + userName + "] " + privateMsg);
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if (!found) writeOne("사용자 " + target + "를 찾을 수 없습니다.");
                        } else {
                            writeOne("사용법: /to [username] [message]");
                        }
                        continue;
                    }

                    if (msg.startsWith("HEALTH_SEND:")) {
                        // 받은 메시지: HEALTH_SEND:홍길동:운동|식단|계획
                        // 보낼 메시지: HEALTH_BROADCAST:홍길동:운동|식단|계획
                        String broadcastMsg = msg.replace("HEALTH_SEND:", "HEALTH_BROADCAST:");
                        broadcast(broadcastMsg); // 모든 접속자에게 전송
                        continue;
                    }

                    if (msg.startsWith("MAKE_ROOM:")) {
                        try {
                            String[] parts = msg.split(":");
                            String roomName = parts[1];
                            Vector<String> members = new Vector<>(Arrays.asList(parts[2].split(",")));

                            // ChatRoomInfo 생성
                            ChatRoomInfo room = new ChatRoomInfo(roomName, members);

                            // 방 저장
                            chatRooms.put(roomName, room);

                            // 방에 속한 사람들에게 방 생성 메시지 전송
                            synchronized (userVec) {
                                for (UserService u : userVec) {
                                    if (members.contains(u.userName)) {
                                        u.writeOne("ROOM_CREATED:" + roomName + ":" + String.join(",", members));
                                    }
                                }
                            }

                            appendText("[SERVER] Room created: " + roomName);

                        } catch (Exception ex) {
                            appendText("[SERVER] Room create error: " + ex.getMessage());
                        }

                        continue; // 다음 메시지 처리
                    }
                    if (msg.startsWith("SEND_ROOM_MSG:")) {
                        String[] parts = msg.split(":", 3);
                        String roomName = parts[1];
                        String message = parts[2];

                        // 해당 방 멤버에게만 메시지 전송
                        String formattedMsg = "[" + userName + "] " + message;
                        sendRoomMessage(roomName, formattedMsg);

                        continue;
                    }
                    if (msg.startsWith("GET_ROOM_MEMBERS:")) {
                        String roomName = msg.split(":")[1];
                        ChatRoomInfo room = chatRooms.get(roomName);
                        if (room != null) {
                            String membersMsg = "ROOM_MEMBERS:" + roomName + ":" + String.join(",", room.members);
                            writeOne(membersMsg);
                        }
                        continue;
                    }

                    // 프로필 이미지, 배경 이미지, 상태 메시지 변경 처리
                    if (msg.startsWith("CHANGE_PROFILE_IMAGE:")) {
                        String[] parts = msg.split(":");
                        if (parts.length >= 3) {
                            String targetUser = parts[1];
                            String imageName = parts[2];
                            JavaChatServer.this.userProfileImages.put(targetUser, imageName);
                            sendUserListToAll(); // 변경 즉시 전파
                        }
                        continue;
                    }
                    else if (msg.startsWith("CHANGE_BG_IMAGE:")) {
                        String[] parts = msg.split(":");
                        if (parts.length >= 3) {
                            String targetUser = parts[1];
                            String imageName = parts[2];
                            JavaChatServer.this.userBgImages.put(targetUser, imageName);
                            sendUserListToAll(); // 변경 즉시 전파
                        }
                        continue;
                    }
                    else if (msg.startsWith("CHANGE_STATUS:")) {
                        String[] parts = msg.split(":", 3); // 메시지 내에 :가 있을 수 있으므로 limit 3
                        if (parts.length >= 3) {
                            String targetUser = parts[1];
                            String statusMsg = parts[2];
                            JavaChatServer.this.userStatusMsgs.put(targetUser, statusMsg);
                            sendUserListToAll(); // 변경 즉시 전파
                        }
                        continue;
                    }

                }
            } catch (IOException e) {
                disconnect();
            }
        }

        private void sendRoomMessage(String roomName, String msg) {
            ChatRoomInfo room = chatRooms.get(roomName);
            if (room == null) return;

            Vector<String> members = room.members;
            String msgToSend = "ROOM_MSG:" + roomName + ":" + msg;
            synchronized (userVec) {
                for (UserService u : userVec) {
                    if (members.contains(u.userName)) {
                        u.writeOne(msgToSend);
                    }
                }
            }
        }
    }
}