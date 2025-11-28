package chatclient;

import chat.ChatMessage;
import friendlist.ChatCellRenderer;
import image.ImagePanel;
import image.RoundedButton;
import image.RoundedTextField;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Base64;

public class JavaChatClientView extends JFrame {
    private JPanel contentPane;
    private RoundedTextField txtInput;
    private String UserName;
    private RoundedButton btnSend;

    private JButton btnImage; // 사진 전송 버튼
    private JButton btnEmoticon; // 이모티콘 버튼
    
    private JList<ChatMessage> chatList;
    private DefaultListModel<ChatMessage> listModel;
    
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private JLabel lblUserName;
    private String currentRoomName;
    
    // [삭제됨] private JLabel lblMembers; // 더 이상 사용하지 않음

    public JavaChatClientView(String username, String ip_addr, String port_no, String roomName) {
        this.currentRoomName = roomName;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 392, 462);

        contentPane = new ImagePanel("image/mint2.jpg");
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // [삭제됨] lblMembers 생성 및 추가 코드 제거
        /*
        lblMembers = new JLabel("Members: Loading...");
        lblMembers.setBounds(12, 10, 352, 25);
        ...
        contentPane.add(lblMembers);
        */

        listModel = new DefaultListModel<>();
        chatList = new JList<>(listModel);
        chatList.setCellRenderer(new ChatCellRenderer());
        chatList.setOpaque(false);
        chatList.setBackground(new Color(0, 0, 0, 0));
        chatList.setFocusable(false);
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem whisperItem = new JMenuItem("귓속말 보내기");
        popupMenu.add(whisperItem);

        // 2. 리스트에 마우스 리스너 추가
        chatList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                handleMouseClick(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                handleMouseClick(e);
            }

            private void handleMouseClick(java.awt.event.MouseEvent e) {
                // 우클릭인지 확인
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    
                    // 클릭한 위치가 리스트의 몇 번째 항목인지 확인
                    int index = chatList.locationToIndex(e.getPoint());
                    
                    if (index != -1 && chatList.getCellBounds(index, index).contains(e.getPoint())) {
                        // 해당 항목 선택 (시각적 효과)
                        chatList.setSelectedIndex(index);
                        
                        // 클릭한 메시지 정보 가져오기
                        ChatMessage clickedMsg = chatList.getModel().getElementAt(index);
                        String targetUser = clickedMsg.getSender();
                        
                        // 나 자신이나 시스템 메시지에는 귓속말 불가
                        if (!targetUser.equals(username) && !targetUser.equals("System") && !targetUser.equals("Unknown")) {
                            
                            // 기존 리스너 제거 (중복 방지)
                            for (ActionListener al : whisperItem.getActionListeners()) {
                                whisperItem.removeActionListener(al);
                            }
                            
                            // 클릭 시 동작: 입력창에 "/to 이름 " 자동 입력
                            whisperItem.addActionListener(event -> {
                                txtInput.setText("/to " + targetUser + " ");
                                txtInput.requestFocus(); // 바로 입력할 수 있게 포커스 이동
                            });
                            
                            // 팝업 메뉴 띄우기
                            popupMenu.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(chatList);
        
        // 🚀 [수정] 채팅창 위치를 위로 올리고(Y:10), 높이를 키움(H:310)
        scrollPane.setBounds(12, 10, 352, 310); 
        
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setOpaque(false);
        contentPane.add(scrollPane);

        // --- 하단 입력창 및 버튼 영역 ---
        
        // 1. 사진 버튼 (+)
        btnImage = new RoundedButton("+");
        btnImage.setBounds(12, 364, 45, 40);
        btnImage.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        btnImage.setMargin(new Insets(0, 0, 7, 0)); 
        btnImage.addActionListener(e -> sendImageAction()); 
        contentPane.add(btnImage);

        // 2. 이모티콘 버튼 (😊)
        btnEmoticon = new RoundedButton("😊"); 
        btnEmoticon.setBounds(60, 364, 45, 40);
        btnEmoticon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20)); 
        btnEmoticon.setMargin(new Insets(7, 0, 0, 0)); 
        btnEmoticon.addActionListener(e -> sendEmoticonAction()); 
        contentPane.add(btnEmoticon);

        // 3. 입력창
        txtInput = new RoundedTextField();
        txtInput.setBounds(110, 365, 166, 40); 
        txtInput.setBackground(Color.WHITE);
        txtInput.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        contentPane.add(txtInput);
        txtInput.setColumns(10);

        // 4. 전송 버튼
     // 4. 전송 버튼
        btnSend = new RoundedButton("Send");
        btnSend.setBounds(288, 364, 76, 40);
        btnSend.setBackground(new Color(0, 150, 136));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Tahoma", Font.BOLD, 14));
        contentPane.add(btnSend);

        // 🚀 [수정] 사용자 이름 라벨 설정 (입력창 바로 위에 배치)
        // x=115 (입력창과 줄맞춤), y=340 (채팅창과 입력창 사이 빈 공간)
        lblUserName = new JLabel(username); 
        lblUserName.setBounds(115, 342, 150, 20); 
        lblUserName.setOpaque(false);
        lblUserName.setForeground(Color.DARK_GRAY); // 잘 보이게 진한 회색
        lblUserName.setFont(new Font("Malgun Gothic", Font.BOLD, 12)); // 폰트 설정
        lblUserName.setHorizontalAlignment(SwingConstants.LEFT); // 왼쪽 정렬
        contentPane.add(lblUserName);
        
        setVisible(true);

        UserName = username;
        
        // 🚀 [수정] 텍스트 설정 ( > 화살표 추가)
        lblUserName.setText(username);

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
            AppendMessage("System", "Connect error", false, false, null);
        }
    }

    private void sendImageMessage(File file, int maxWidth) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                JOptionPane.showMessageDialog(this, "이미지 파일이 아니거나 손상되었습니다.");
                return;
            }

            String fileName = file.getName().toLowerCase();
            boolean isPng = fileName.endsWith(".png");

            int newWidth = maxWidth; 
            int newHeight = (int)(image.getHeight() * ((double)newWidth / image.getWidth()));
            
            Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
            
            int imageType = isPng ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
            BufferedImage outputImage = new BufferedImage(newWidth, newHeight, imageType);
            Graphics2D g = outputImage.createGraphics();
            
            if (isPng) {
                g.setComposite(AlphaComposite.Src);
            }
            g.drawImage(scaledImage, 0, 0, null);
            g.dispose();
            
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            String formatName = isPng ? "png" : "jpg";
            
            ImageIO.write(outputImage, formatName, bos);
            byte[] imageBytes = bos.toByteArray();
            String base64String = Base64.getEncoder().encodeToString(imageBytes);
            
            String msgToSend = "SEND_ROOM_MSG:" + currentRoomName + ":<<IMG>>" + base64String;
            SendMessage(msgToSend);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "이미지 전송 실패: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void sendImageAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            sendImageMessage(file, 120); 
        }
    }

    private void sendEmoticonAction() {
        File emoDir = new File("image/emoticon");
        if (!emoDir.exists()) {
            emoDir.mkdirs();
            JOptionPane.showMessageDialog(this, "image/emoticon 폴더에 이미지를 넣어주세요!");
            return;
        }

        File[] files = emoDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".gif");
        });

        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "사용 가능한 이모티콘이 없습니다.");
            return;
        }

        JDialog dialog = new JDialog(this, "이모티콘 선택", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 4, 5, 5));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(Color.WHITE);

        for (File f : files) {
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            
            JButton btn = new JButton(new ImageIcon(img));
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(220,220,220), 1));
            btn.setFocusPainted(false);
            
            btn.addActionListener(e -> {
                sendImageMessage(f, 70); 
                dialog.dispose();
            });
            
            panel.add(btn);
        }
        
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);
        
        JButton btnClose = new JButton("닫기");
        btnClose.addActionListener(e -> dialog.dispose());
        dialog.add(btnClose, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // 네트워크 수신 스레드
 // JavaChatClientView.java -> ListenNetwork 클래스 내부

     class ListenNetwork extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();

                    // ---------------------------------------------------------
                    // 1. 귓속말 처리 (상대방이 나에게 보냄)
                    // ---------------------------------------------------------
                    if (msg.startsWith("WHISPER:")) {
                        // 서버가 보낸 형식: WHISPER:보낸사람:내용
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) {
                            String sender = parts[1];
                            String content = parts[2];
                            
                            // 화면에 표시 (왼쪽, 흰색 말풍선)
                            AppendMessage(sender, "[귓속말] " + content, false, false, null);
                        }
                    }
                    
                    // ---------------------------------------------------------
                    // 2. 귓속말 보낸 확인 (내가 보낸 거)
                    // ---------------------------------------------------------
                    else if (msg.startsWith("WHISPER_SENT:")) {
                        // 서버가 보낸 형식: WHISPER_SENT:받는사람:내용
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) {
                            String target = parts[1];
                            String content = parts[2];
                            
                            // 화면에 표시 (오른쪽, 노란 말풍선)
                            // 내 이름(UserName)을 넣어서 '나'인 것을 인식시킴
                            AppendMessage(UserName, "[귓속말][" + target + "에게] " + content, true, false, null);
                        }
                    }
                    
                    // ---------------------------------------------------------
                    // 3. 귓속말 실패 알림
                    // ---------------------------------------------------------
                    else if (msg.startsWith("WHISPER_FAIL:")) {
                        String target = msg.split(":")[1];
                        AppendMessage("System", "'" + target + "'님은 접속 중이 아닙니다.", false, false, null);
                    }

                    // ---------------------------------------------------------
                    // 4. 일반 채팅방 메시지
                    // ---------------------------------------------------------
                    else if (msg.startsWith("ROOM_MSG:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) {
                            String receivedRoomName = parts[1];
                            String actualMsg = parts[2];

                            // 현재 내가 보고 있는 방의 메시지만 표시
                            if (receivedRoomName.equals(currentRoomName)) {
                                String sender = "Unknown";
                                String message = actualMsg;

                                // 이름 파싱 (서버 형식에 따라 유연하게 대처)
                                if (actualMsg.startsWith("[") && actualMsg.contains("]")) {
                                    int endBracket = actualMsg.indexOf("]");
                                    sender = actualMsg.substring(1, endBracket).trim();
                                    message = actualMsg.substring(endBracket + 1).trim();
                                } else if (actualMsg.contains(":")) {
                                    int colonIndex = actualMsg.indexOf(":");
                                    if (!actualMsg.startsWith("<<IMG>>")) { // 이미지가 아닐 때만
                                        sender = actualMsg.substring(0, colonIndex).trim();
                                        message = actualMsg.substring(colonIndex + 1).trim();
                                    }
                                }

                                boolean isMine = sender.equals(UserName);

                                // 이미지인지 텍스트인지 확인 후 출력
                                if (message.startsWith("<<IMG>>")) {
                                    try {
                                        String base64 = message.substring(7);
                                        byte[] imageBytes = Base64.getDecoder().decode(base64);
                                        ImageIcon icon = new ImageIcon(imageBytes);
                                        AppendMessage(sender, "", isMine, true, icon);
                                    } catch (Exception e) {
                                        AppendMessage(sender, "[이미지 에러]", isMine, false, null);
                                    }
                                } else {
                                    AppendMessage(sender, message, isMine, false, null);
                                }
                            }
                        }
                    }
                    
                    // ---------------------------------------------------------
                    // 5. 방 제목(참여자) 업데이트
                    // ---------------------------------------------------------
                    else if (msg.startsWith("ROOM_MEMBERS:")) {
                        String[] parts = msg.split(":", 3);
                        if (parts.length >= 3) {
                            String receivedRoomName = parts[1];
                            String membersList = parts[2];
                            if (receivedRoomName.equals(currentRoomName)) {
                                setTitle("참여자: " + membersList.replace(",", ", "));
                            }
                        }
                    }
                    
                    // ---------------------------------------------------------
                    // 6. 기타 시스템 메시지 (입장, 퇴장 등)
                    // ---------------------------------------------------------
                    else if (msg.startsWith("ROOM_CREATED:") || msg.startsWith("USERLIST:") || msg.toLowerCase().contains("welcome")) {
                        continue; // 그냥 무시
                    } else {
                        // 그 외 알 수 없는 메시지는 시스템 메시지로 출력
                        AppendMessage("System", msg, false, false, null);
                    }

                } catch (IOException e) {
                    AppendMessage("System", "서버와의 연결이 끊어졌습니다.", false, false, null);
                    break;
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
                
                if (inputMsg.startsWith("/to ")) {
                    SendMessage(inputMsg);
                }
                
                else if (!currentRoomName.isEmpty()) {
                    String msgToSend = "SEND_ROOM_MSG:" + currentRoomName + ":" + inputMsg;
                    SendMessage(msgToSend);
                } 
                else {
                    SendMessage(inputMsg);
                }
                txtInput.setText("");
                txtInput.requestFocus();
            }
        }
    }

    public void AppendMessage(String sender, String message, boolean isMine, boolean isImage, ImageIcon contentImage) {
        String profileName = sender;
        ChatMessage chatMessage = new ChatMessage(sender, message, isMine, profileName, isImage, contentImage);
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
            // ...
        }
    }
}