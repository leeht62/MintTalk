package friendlist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HealthCare extends JFrame {
    private String username;
    private DataOutputStream out;
    private FriendList parent;

    // UI 컴포넌트
    private JTextArea txtExercise, txtDiet, txtPlan;
    private JTextArea displayLog;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm"); // 연도까지 표시

    public HealthCare(String username, DataOutputStream out, FriendList parent) {
        this.username = username;
        this.out = out;
        this.parent = parent;

        setTitle("헬스케어 & 일정 공유 - " + username);
        setSize(550, 750); // 크기 약간 키움
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- 상단 입력 패널 ---
        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        txtExercise = createTitledTextArea("🏋️ 오늘 운동량 (Ctrl+Enter로 전송)");
        txtDiet = createTitledTextArea("🥗 오늘 식단");
        txtPlan = createTitledTextArea("📅 내일 계획/일정");

        inputPanel.add(new JScrollPane(txtExercise));
        inputPanel.add(new JScrollPane(txtDiet));
        inputPanel.add(new JScrollPane(txtPlan));

        add(inputPanel, BorderLayout.CENTER);

        // --- 하단 버튼 및 로그 패널 ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        bottomPanel.setPreferredSize(new Dimension(0, 350)); // 로그 영역 확보

        // 버튼 패널 (전송, 저장, 지우기)
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        
        JButton btnShare = new JButton("공유하기");
        styleButton(btnShare, new Color(70, 180, 70));
        btnShare.addActionListener(e -> sendHealthData());

        JButton btnSave = new JButton("파일저장");
        styleButton(btnSave, new Color(70, 130, 180));
        btnSave.addActionListener(e -> saveLogToFile());

        JButton btnClear = new JButton("지우기");
        styleButton(btnClear, new Color(200, 70, 70));
        btnClear.addActionListener(e -> displayLog.setText("=== 공유된 헬스케어 기록 ===\n"));

        btnPanel.add(btnShare);
        btnPanel.add(btnSave);
        btnPanel.add(btnClear);

        displayLog = new JTextArea();
        displayLog.setEditable(false);
        displayLog.setFont(new Font("Monospaced", Font.PLAIN, 13)); // 등폭 폰트로 줄맞춤
        displayLog.setText("=== 공유된 헬스케어 기록 ===\n");
        displayLog.setLineWrap(true);

        bottomPanel.add(btnPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(displayLog), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // 버튼 스타일링 헬퍼
    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    // 입력창 생성 헬퍼 (키 리스너 추가)
    private JTextArea createTitledTextArea(String title) {
        JTextArea ta = new JTextArea();
        ta.setLineWrap(true);
        ta.setBorder(new TitledBorder(title));
        ta.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        
        // [추가] Ctrl + Enter 누르면 전송되도록 설정
        ta.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendHealthData();
                }
            }
        });
        return ta;
    }

    // 데이터 전송
    private void sendHealthData() {
        String ex = txtExercise.getText().replace("|", "/").trim();
        String dt = txtDiet.getText().replace("|", "/").trim();
        String pl = txtPlan.getText().replace("|", "/").trim();

        if (ex.isEmpty() && dt.isEmpty() && pl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 적어도 하나는 입력해주세요.");
            return;
        }

        // 프로토콜: HEALTH_SEND:username:운동|식단|계획
        String msg = "HEALTH_SEND:" + username + ":" + ex + "|" + dt + "|" + pl;
        try {
            out.writeUTF(msg);
            out.flush();
            
            // 입력창 초기화
            txtExercise.setText("");
            txtDiet.setText("");
            txtPlan.setText("");
            
            // 포커스를 운동 입력창으로
            txtExercise.requestFocus(); 
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // [추가] 로그 파일 저장 기능
    private void saveLogToFile() {
        String logContent = displayLog.getText();
        if (logContent.length() < 30) { // 내용이 거의 없으면
            JOptionPane.showMessageDialog(this, "저장할 기록이 없습니다.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("healthcare_log.txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()))) {
                writer.write(logContent);
                JOptionPane.showMessageDialog(this, "파일이 성공적으로 저장되었습니다!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "저장 실패: " + e.getMessage());
            }
        }
    }

    // 서버로부터 받은 데이터 처리
    public void processMessage(String msg) {
        // 프로토콜: HEALTH_BROADCAST:username:운동|식단|계획
        if (msg.startsWith("HEALTH_BROADCAST:")) {
            String[] parts = msg.split(":", 3);
            if (parts.length < 3) return;

            String sender = parts[1];
            // -1 옵션: 빈 문자열도 배열에 포함시켜 인덱스 오류 방지
            String[] data = parts[2].split("\\|", -1); 

            if (data.length < 3) return; // 데이터 손상 방지

            String time = sdf.format(new Date());
            
            StringBuilder sb = new StringBuilder();
            sb.append("\n====================================\n");
            sb.append(" 👤 [").append(sender).append("] 님의 기록 - ").append(time).append("\n");
            sb.append("------------------------------------\n");
            
            if(!data[0].trim().isEmpty()) sb.append(" 💪 운동:\n").append("    ").append(data[0]).append("\n");
            if(!data[1].trim().isEmpty()) sb.append(" 🥗 식단:\n").append("    ").append(data[1]).append("\n");
            if(!data[2].trim().isEmpty()) sb.append(" 📅 계획:\n").append("    ").append(data[2]).append("\n");
            sb.append("====================================\n");

            displayLog.append(sb.toString());
            displayLog.setCaretPosition(displayLog.getDocument().getLength()); // 스크롤 맨 아래로
        }
    }
}