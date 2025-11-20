import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class JavaChatClientView extends JFrame {
    // ... (기존 필드들)
    private JPanel contentPane;
    private RoundedTextField txtInput;
    private String UserName;
    private RoundedButton btnSend;
    
    // 🚀 [추가] 기능 버튼들
    private JButton btnImage; // 사진 전송 버튼
    private JButton btnEmoticon; // 이모티콘 버튼
    
    private JList<ChatMessage> chatList;
    private DefaultListModel<ChatMessage> listModel;
    
    // ... (Socket 관련 필드들 기존과 동일)
    private Socket socket;
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    private JLabel lblUserName;
    private String currentRoomName;
    private JLabel lblMembers;

    public JavaChatClientView(String username, String ip_addr, String port_no, String roomName) {
        this.currentRoomName = roomName;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 392, 462);

        contentPane = new ImagePanel("image/mint2.jpg");
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        // ... (lblMembers, scrollPane 등 기존 UI 설정 동일) ...
        lblMembers = new JLabel("Members: Loading...");
        lblMembers.setBounds(12, 10, 352, 25);
        lblMembers.setFont(new Font("Dialog", Font.BOLD, 14));
        lblMembers.setOpaque(false);
        lblMembers.setForeground(Color.BLACK);
        contentPane.add(lblMembers);

        listModel = new DefaultListModel<>();
        chatList = new JList<>(listModel);
        chatList.setCellRenderer(new ChatCellRenderer());
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

        // 🚀 [UI 수정] 입력창과 버튼 위치 조정 및 새 버튼 추가
        
     // 1. 사진 버튼 (+)
        btnImage = new RoundedButton("+");
        btnImage.setBounds(12, 364, 45, 40);
        btnImage.setFont(new Font("Malgun Gothic", Font.BOLD, 24));
        btnImage.setMargin(new Insets(0, 0, 7, 0)); // [추가] 내부 여백 제거 (중요!)
        btnImage.addActionListener(e -> sendImageAction()); 
        contentPane.add(btnImage);
        
        // 2. 이모티콘 버튼 (😊)
        // "emoj"는 너무 길어서 ...이 뜰 수 있으니 "😊" 또는 "emo"로 변경하세요.
        btnEmoticon = new RoundedButton("😊"); 
        btnEmoticon.setBounds(60, 364, 45, 40);
        btnEmoticon.setFont(new Font("Segoe UI Emoji", Font.BOLD, 20)); // [추가] 이모티콘 전용 폰트 추천 (없으면 Malgun Gothic)
        btnEmoticon.setMargin(new Insets(7, 0, 0, 0)); // [추가] 내부 여백 제거 (중요!)
        btnEmoticon.addActionListener(e -> sendEmoticonAction()); 
        contentPane.add(btnEmoticon);

        // 3. 입력창 (위치 조정)
        txtInput = new RoundedTextField();
        // 버튼들이 좁아 보이면 X좌표를 110에서 115 정도로 살짝 밀어도 됩니다.
        txtInput.setBounds(110, 365, 166, 40); 
        txtInput.setBackground(Color.WHITE);
        txtInput.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        contentPane.add(txtInput);
        txtInput.setColumns(10);

        // 4. 전송 버튼
        btnSend = new RoundedButton("Send");
        btnSend.setBounds(288, 364, 76, 40);
        btnSend.setBackground(new Color(0, 150, 136));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Tahoma", Font.BOLD, 14));
        contentPane.add(btnSend);

        // ... (나머지 초기화 코드 동일) ...
        lblUserName = new JLabel("Name"); // 안보이지만 에러 방지용
        lblUserName.setBounds(0,0,0,0);
        contentPane.add(lblUserName);
        setVisible(true);

        UserName = username;

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

 // 🚀 [수정] 이미지 크기 축소 + 투명 배경(PNG) 지원 메서드
 // 🚀 [수정] 이미지 전송 메서드 (원하는 크기를 지정할 수 있음)
    // 매개변수에 int maxWidth 추가됨
    private void sendImageMessage(File file, int maxWidth) {
        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                JOptionPane.showMessageDialog(this, "이미지 파일이 아니거나 손상되었습니다.");
                return;
            }

            String fileName = file.getName().toLowerCase();
            boolean isPng = fileName.endsWith(".png");

            // 🚀 [핵심] 받아온 maxWidth 값으로 리사이징
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
 // 🚀 [기능 1] 사진 전송 액션 (+ 버튼)
    private void sendImageAction() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // 사진은 크게! (120px)
            sendImageMessage(file, 120); 
        }
    }
 // 🚀 [기능 2] 이모티콘 전송 액션 (😊 버튼)
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
            // 미리보기 아이콘 생성
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            
            JButton btn = new JButton(new ImageIcon(img));
            btn.setBackground(Color.WHITE);
            btn.setBorder(BorderFactory.createLineBorder(new Color(220,220,220), 1));
            btn.setFocusPainted(false);
            
            // 버튼 클릭 시
            btn.addActionListener(e -> {
                // 이모티콘은 작게! (70px)
                sendImageMessage(f, 70); 
                dialog.dispose();
            });
            
            panel.add(btn);
        }
        
        // ... (이하 스크롤팬, 닫기 버튼 등 기존 코드와 동일) ...
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
    class ListenNetwork extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF();

                    if (msg.startsWith("ROOM_MEMBERS:")) {
                        // ... (기존 코드)
                         String[] parts = msg.split(":", 3);
                         if (parts.length >= 3) {
                             String receivedRoomName = parts[1];
                             String membersList = parts[2];
                             if (receivedRoomName.equals(currentRoomName)) {
                                 lblMembers.setText("Members: " + membersList.replace(",", ", "));
                                 AppendMessage("System", "현재 접속 인원: " + membersList.replace(",", ", "), false, false, null);
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

                                if (actualMsg.startsWith("[") && actualMsg.contains("]")) {
                                    int endOfSender = actualMsg.indexOf("]");
                                    sender = actualMsg.substring(1, endOfSender).trim();
                                    message = actualMsg.substring(endOfSender + 1).trim();
                                }

                                boolean isMine = sender.equals(UserName);
                                
                                // 🚀 [확인] 이미지 메시지인지 체크
                                if (message.startsWith("<<IMG>>")) {
                                    try {
                                        String base64 = message.substring(7); // "<<IMG>>" 제거
                                        byte[] imageBytes = Base64.getDecoder().decode(base64);
                                        ImageIcon icon = new ImageIcon(imageBytes);
                                        AppendMessage(sender, "", isMine, true, icon);
                                    } catch (Exception e) {
                                        AppendMessage(sender, "[이미지 깨짐]", isMine, false, null);
                                    }
                                } else {
                                    // 일반 텍스트
                                    AppendMessage(sender, message, isMine, false, null);
                                }
                            }
                        }
                    } else if (msg.startsWith("ROOM_CREATED:") || msg.startsWith("USERLIST:") || msg.toLowerCase().contains("welcome")) {
                        continue;
                    } else {
                        AppendMessage("System", msg, false, false, null);
                    }

                } catch (IOException e) {
                    AppendMessage("Error", "Connection lost", false, false, null);
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

                if (!currentRoomName.isEmpty()) {
                    String msgToSend = "SEND_ROOM_MSG:" + currentRoomName + ":" + inputMsg;
                    SendMessage(msgToSend);
                } else {
                    SendMessage(inputMsg);
                }
                txtInput.setText("");
                txtInput.requestFocus();
            }
        }
    }

    // 🚀 [수정] AppendMessage: 이미지 지원
    public void AppendMessage(String sender, String message, boolean isMine, boolean isImage, ImageIcon contentImage) {
        String profileName = sender;
        // 수정된 ChatMessage 생성자 호출
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