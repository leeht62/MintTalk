package friendlist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
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
    private DefaultListModel<String> exerciseListModel;
    private JList<String> exerciseList;

    private JTextArea txtDiet, txtPlan;
    private JTextArea displayLog;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public HealthCare(String username, DataOutputStream out, FriendList parent) {
        this.username = username;
        this.out = out;
        this.parent = parent;

        setTitle("헬스케어 & 일정 공유 - " + username);
        setSize(550, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel mainInputPanel = new JPanel();
        mainInputPanel.setLayout(new BoxLayout(mainInputPanel, BoxLayout.Y_AXIS));
        mainInputPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        // 운동 입력
        JPanel exercisePanel = createExercisePanel();

        // 식단, 계획 입력창
        txtDiet = createTitledTextArea("오늘 식단");
        txtPlan = createTitledTextArea("내일 계획/일정");

        mainInputPanel.add(exercisePanel);
        mainInputPanel.add(Box.createVerticalStrut(10)); // 간격
        mainInputPanel.add(new JScrollPane(txtDiet));
        mainInputPanel.add(Box.createVerticalStrut(10)); // 간격
        mainInputPanel.add(new JScrollPane(txtPlan));

        add(mainInputPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        bottomPanel.setPreferredSize(new Dimension(0, 300));


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
        displayLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        displayLog.setText("=== 공유된 헬스케어 기록 ===\n");
        displayLog.setLineWrap(true);

        bottomPanel.add(btnPanel, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(displayLog), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // 운동 입력 패널 생성 메소드
    private JPanel createExercisePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("오늘 운동량 (추가 후 공유)"));
        panel.setPreferredSize(new Dimension(0, 150)); // 높이 고정

        JPanel inputRow = new JPanel(new BorderLayout(5, 0));

        JTextField tfExName = new JTextField();
        String[] times = {
            "30분", "1시간", "1시간 30분", "2시간",
            "2시간 30분", "3시간", "3시간 30분", "4시간"
        };
        JComboBox<String> cbTime = new JComboBox<>(times);

        JButton btnAdd = new JButton("추가");
        styleButton(btnAdd, new Color(100, 100, 100));
        btnAdd.setFont(new Font("Malgun Gothic", Font.BOLD, 12));
        exerciseListModel = new DefaultListModel<>();
        exerciseList = new JList<>(exerciseListModel);
        exerciseList.setVisibleRowCount(4);

        // 추가 버튼 로직
        btnAdd.addActionListener(e -> {
            String name = tfExName.getText().trim();
            String time = (String) cbTime.getSelectedItem();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "운동 종류를 입력해주세요.");
                return;
            }

            String entry = name + " : " + time;
            exerciseListModel.addElement(entry);
            tfExName.setText("");
            tfExName.requestFocus();
        });

        tfExName.addActionListener(e -> btnAdd.doClick());


        JPanel rightBox = new JPanel(new BorderLayout());
        rightBox.add(cbTime, BorderLayout.CENTER);
        rightBox.add(btnAdd, BorderLayout.EAST);

        inputRow.add(new JLabel(" 종목: "), BorderLayout.WEST);
        inputRow.add(tfExName, BorderLayout.CENTER);
        inputRow.add(rightBox, BorderLayout.EAST);


        JScrollPane listScroll = new JScrollPane(exerciseList);
        listScroll.setBorder(new TitledBorder("추가된 운동 목록 (더블클릭시 삭제)"));

        // 리스트 항목
        exerciseList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = exerciseList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        exerciseListModel.remove(index);
                    }
                }
            }
        });

        panel.add(inputRow, BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);

        return panel;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Malgun Gothic", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
    }

    private JTextArea createTitledTextArea(String title) {
        JTextArea ta = new JTextArea(4, 20);
        ta.setLineWrap(true);
        ta.setBorder(new TitledBorder(title));
        ta.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        return ta;
    }

    // 입력한 데이터 서버로 보내는 메소드
    private void sendHealthData() {
        StringBuilder exBuilder = new StringBuilder();
        for (int i = 0; i < exerciseListModel.size(); i++) {
            if (i > 0) exBuilder.append(", ");
            exBuilder.append(exerciseListModel.get(i));
        }
        String ex = exBuilder.toString();

        String dt = txtDiet.getText().replace("|", "/").trim();
        String pl = txtPlan.getText().replace("|", "/").trim();

        if (ex.isEmpty() && dt.isEmpty() && pl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "내용을 적어도 하나는 입력해주세요.");
            return;
        }

        // 프로토콜 HEALTH_SEND:username:운동|식단|계획
        String msg = "HEALTH_SEND:" + username + ":" + ex + "|" + dt + "|" + pl;
        try {
            out.writeUTF(msg);
            out.flush();
            exerciseListModel.clear();
            txtDiet.setText("");
            txtPlan.setText("");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 로그 파일 저장 기능
    private void saveLogToFile() {
        String logContent = displayLog.getText();
        if (logContent.length() < 30) {
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
        if (msg.startsWith("HEALTH_BROADCAST:")) {
            String[] parts = msg.split(":", 3);
            if (parts.length < 3) return;

            String sender = parts[1];
            String[] data = parts[2].split("\\|", -1);

            if (data.length < 3) return;

            String time = sdf.format(new Date());

            StringBuilder sb = new StringBuilder();
            sb.append("\n====================================\n");
            sb.append("[").append(sender).append("] 님의 기록 - ").append(time).append("\n");
            sb.append("------------------------------------\n");

            if(!data[0].trim().isEmpty()) {
                sb.append("운동:\n");
                String[] exercises = data[0].split(",");
                for (String exercise : exercises) {
                    sb.append("    - ").append(exercise.trim()).append("\n");
                }
            }
            if(!data[1].trim().isEmpty()) sb.append("식단:\n").append("    ").append(data[1]).append("\n");
            if(!data[2].trim().isEmpty()) sb.append("계획:\n").append("    ").append(data[2]).append("\n");
            sb.append("====================================\n");

            displayLog.append(sb.toString());
            displayLog.setCaretPosition(displayLog.getDocument().getLength());
        }
    }
}