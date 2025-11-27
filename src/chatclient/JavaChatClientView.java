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
        btnSend = new RoundedButton("Send");
        btnSend.setBounds(288, 364, 76, 40);
        btnSend.setBackground(new Color(0, 150, 136));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFont(new Font("Tahoma", Font.BOLD, 14));
        contentPane.add(btnSend);

        lblUserName = new JLabel("Name"); 
        lblUserName.setBounds(0,0,0,0);
        contentPane.add(lblUserName);
        setVisible(true);

        UserName = username;
        // 초기 타이틀 설정
//        setTitle(currentRoomName);

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
                                 // 🚀 [수정] 라벨 대신 윈도우 타이틀에 표시
                                 setTitle("참여자: " + membersList.replace(",", ", "));
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