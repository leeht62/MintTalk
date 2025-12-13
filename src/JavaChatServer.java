import chat.ChatRoomInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class JavaChatServer extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket serverSocket;
    
    // Thread-Safe한 리스트와 맵 사용
    private final Vector<UserService> userVec = new Vector<>(); 
    private final Vector<String> userList = new Vector<>();
    private final ConcurrentHashMap<String, ChatRoomInfo> chatRooms = new ConcurrentHashMap<>();

    // 사용자 프로필 정보 저장소 (동시성 제어를 위해 ConcurrentHashMap 권장)
    private final ConcurrentHashMap<String, String> userProfileImages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userBgImages = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userStatusMsgs = new ConcurrentHashMap<>();

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

        // 서버 시작 버튼 액션
        btnStart.addActionListener(e -> {
            try {
                int port = Integer.parseInt(txtPortNumber.getText());
                serverSocket = new ServerSocket(port);
                appendText("Chat Server Running on port " + port);
                
                btnStart.setEnabled(false);
                txtPortNumber.setEnabled(false);
                
                new AcceptServer().start(); // 클라이언트 접속 대기 스레드 시작
            } catch (Exception ex) {
                appendText("Server start error: " + ex.getMessage());
            }
        });
    }

    // 서버 로그 출력용
    private void appendText(String msg) {
        SwingUtilities.invokeLater(() -> {
            textArea.append(msg + "\n");
            textArea.setCaretPosition(textArea.getText().length());
        });
    }

    // --- Inner Class: 클라이언트 접속 수락 스레드 ---
    class AcceptServer extends Thread {
        public void run() {
            while (true) {
                try {
                    appendText("Waiting for clients...");
                    Socket clientSocket = serverSocket.accept();
                    appendText("New client connected: " + clientSocket.getInetAddress());
                    
                    UserService user = new UserService(clientSocket);
                    user.start();
                } catch (IOException e) {
                    appendText("Accept error: " + e.getMessage());
                    break;
                }
            }
        }
    }

    // --- Inner Class: 개별 클라이언트 처리 스레드 ---
    class UserService extends Thread {
        private Socket socket;
        private DataInputStream dis;
        private DataOutputStream dos;
        private String userName;

        public UserService(Socket socket) {
            this.socket = socket;
            try {
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                appendText("Stream init error: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                // 1. 로그인 처리
                if (!handleLogin()) return;

                // 2. 메시지 수신 루프
                while (true) {
                    String msg = dis.readUTF().trim();
                    if (msg == null || msg.equals("/exit")) {
                        disconnect();
                        break;
                    }
                    parseAndProcessMessage(msg);
                }
            } catch (IOException e) {
                disconnect();
            }
        }

        // 로그인 및 초기화
        private boolean handleLogin() throws IOException {
            String firstMsg = dis.readUTF(); // Expecting: "/login username"
            String[] tokens = firstMsg.split(" ");
            
            if (tokens.length >= 2) {
                userName = tokens[1].trim();
                appendText("User joined: " + userName);
                
                userVec.add(this);
                
                if (!userList.contains(userName)) {
                    userList.add(userName);
                    // 초기 프로필 정보 세팅 (없을 경우 기본값)
                    userProfileImages.putIfAbsent(userName, "profile.jpg");
                    userBgImages.putIfAbsent(userName, "ab.jpg");
                    userStatusMsgs.putIfAbsent(userName, "");
                }

                writeOne("Welcome " + userName + "\n");
                sendUserListToAll(); // 전체 접속자 명단 브로드캐스트
                return true;
            }
            return false;
        }

        // 메시지 종류별 분기 처리
        private void parseAndProcessMessage(String msg) {
            if (msg.startsWith("/to ")) {
                handleWhisper(msg);
            } else if (msg.startsWith("HEALTH_SEND:")) {
                String broadcastMsg = msg.replace("HEALTH_SEND:", "HEALTH_BROADCAST:");
                broadcast(broadcastMsg);
            } else if (msg.startsWith("MAKE_ROOM:")) {
                handleMakeRoom(msg);
            } else if (msg.startsWith("SEND_ROOM_MSG:")) {
                handleSendRoomMsg(msg);
            } else if (msg.startsWith("GET_ROOM_MEMBERS:")) {
                handleGetRoomMembers(msg);
            } else if (msg.startsWith("CHANGE_PROFILE_IMAGE:")) {
                handleProfileUpdate(msg, userProfileImages);
            } else if (msg.startsWith("CHANGE_BG_IMAGE:")) {
                handleProfileUpdate(msg, userBgImages);
            } else if (msg.startsWith("CHANGE_STATUS:")) {
                handleProfileUpdate(msg, userStatusMsgs);
            }
        }

        // 귓속말 처리
        private void handleWhisper(String msg) {
            System.out.println("[서버] 귓속말 요청: " + msg);
            String[] parts = msg.split(" ", 3);
            
            if (parts.length >= 3) {
                String targetUser = parts[1];
                String privateMsg = parts[2];
                boolean found = false;

                synchronized (userVec) {
                    for (UserService u : userVec) {
                        if (u.userName.equals(targetUser)) {
                            u.writeOne("WHISPER:" + userName + ":" + privateMsg);
                            found = true;
                        }
                    }
                }

                // 보낸 사람에게도 확인 메시지 전송 (동기화 필요)
                if (found) {
                    synchronized (userVec) {
                        for (UserService u : userVec) {
                            if (u.userName.equals(userName)) {
                                u.writeOne("WHISPER_SENT:" + targetUser + ":" + privateMsg);
                            }
                        }
                    }
                } else {
                    writeOne("WHISPER_FAIL:" + targetUser);
                }
            } else {
                writeOne("[시스템] 사용법: /to [상대방이름] [메시지]");
            }
        }

        // 채팅방 생성 처리
        private void handleMakeRoom(String msg) {
            try {
                String[] parts = msg.split(":");
                String roomName = parts[1];
                Vector<String> members = new Vector<>(Arrays.asList(parts[2].split(",")));

                ChatRoomInfo room = new ChatRoomInfo(roomName, members);
                chatRooms.put(roomName, room);

                // 방 멤버들에게 알림
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
        }

        // 방 메시지 전송 처리
        private void handleSendRoomMsg(String msg) {
            String[] parts = msg.split(":", 3);
            String roomName = parts[1];
            String message = parts[2];
            String formattedMsg = "[" + userName + "] " + message;
            
            sendRoomMessage(roomName, formattedMsg);
        }

        // 방 멤버 조회 요청 처리
        private void handleGetRoomMembers(String msg) {
            String roomName = msg.split(":")[1];
            ChatRoomInfo room = chatRooms.get(roomName);
            if (room != null) {
                String membersMsg = "ROOM_MEMBERS:" + roomName + ":" + String.join(",", room.members);
                writeOne(membersMsg);
            }
        }

        // 프로필/배경/상태메시지 업데이트 공통 처리
        private void handleProfileUpdate(String msg, ConcurrentHashMap<String, String> storage) {
            // 메시지 내에 ':'가 데이터로 포함될 수 있으므로 split limit 3
            String[] parts = msg.split(":", 3); 
            if (parts.length >= 3) {
                String targetUser = parts[1];
                String value = parts[2];
                
                storage.put(targetUser, value);
                sendUserListToAll(); // 변경 사항 즉시 전파
            }
        }

        // 특정 채팅방에 메시지 브로드캐스트
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

        // 전체 접속자 목록 및 프로필 정보 전송
        private void sendUserListToAll() {
            String listMsg = "USERLIST:" + String.join(",", userList);
            StringBuilder imageInfo = new StringBuilder();
            for (String user : userList) {
                String img = userProfileImages.getOrDefault(user, "profile.jpg");
                String bg = userBgImages.getOrDefault(user, "ab.jpg");
                String msg = userStatusMsgs.getOrDefault(user, "");

                imageInfo.append(user).append("=")
                         .append(img).append("|")
                         .append(bg).append("|")
                         .append(msg).append(";");
            }
            String fullMsg = listMsg + ":" + imageInfo.toString();
            broadcast(fullMsg);
        }

        // 단일 전송
        public void writeOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                disconnect();
            }
        }

        // 전체 전송
        public void broadcast(String msg) {
            synchronized (userVec) {
                for (UserService u : userVec) {
                    u.writeOne(msg);
                }
            }
        }

        // 연결 종료 처리
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
    }
}