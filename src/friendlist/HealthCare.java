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
    // [변경] 운동은 여러 개를 담아야 하므로 리스트 모델 사용
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
        setSize(550, 800); // UI가 늘어났으므로 세로 길이 조금 더 확보
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // --- 상단 입력 패널 ---
        // 기존 3행 -> GridBagLayout이나 BorderLayout 조합으로 변경하여 유연하게 배치
        JPanel mainInputPanel = new JPanel();
        mainInputPanel.setLayout(new BoxLayout(mainInputPanel, BoxLayout.Y_AXIS));
        mainInputPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

        // 1. 운동 입력 패널 (새로 만듦)
        JPanel exercisePanel = createExercisePanel();

        // 2. 식단, 계획 입력창 (기존 함수 재활용)
        txtDiet = createTitledTextArea("오늘 식단");
        txtPlan = createTitledTextArea("내일 계획/일정");

        // 패널에 추가
        mainInputPanel.add(exercisePanel);
        mainInputPanel.add(Box.createVerticalStrut(10)); // 간격
        mainInputPanel.add(new JScrollPane(txtDiet));
        mainInputPanel.add(Box.createVerticalStrut(10)); // 간격
        mainInputPanel.add(new JScrollPane(txtPlan));

        add(mainInputPanel, BorderLayout.CENTER);

        // --- 하단 버튼 및 로그 패널 ---
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        bottomPanel.setPreferredSize(new Dimension(0, 300));

        // 버튼 패널
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

    // [핵심 변경] 운동 입력 패널 생성 함수
    private JPanel createExercisePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new TitledBorder("오늘 운동량 (추가 후 공유)"));
        panel.setPreferredSize(new Dimension(0, 150)); // 높이 고정

        // 상단: 입력부 (운동명 + 시간선택 + 추가버튼)
        JPanel inputRow = new JPanel(new BorderLayout(5, 0));

        JTextField tfExName = new JTextField();

        // 시간 선택 콤보박스 (30분 단위 ~ 4시간)
        String[] times = {
            "30분", "1시간", "1시간 30분", "2시간",
            "2시간 30분", "3시간", "3시간 30분", "4시간"
        };
        JComboBox<String> cbTime = new JComboBox<>(times);

        JButton btnAdd = new JButton("추가");
        styleButton(btnAdd, new Color(100, 100, 100));
        btnAdd.setFont(new Font("Malgun Gothic", Font.BOLD, 12));

        // 리스트 모델 초기화
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

            // 리스트에 "운동명 : 시간" 형식으로 추가
            String entry = name + " : " + time;
            exerciseListModel.addElement(entry);

            // 입력창 초기화 및 포커스
            tfExName.setText("");
            tfExName.requestFocus();
        });

        // 엔터키로도 추가되게 설정
        tfExName.addActionListener(e -> btnAdd.doClick());

        // 레이아웃 조립
        JPanel rightBox = new JPanel(new BorderLayout());
        rightBox.add(cbTime, BorderLayout.CENTER);
        rightBox.add(btnAdd, BorderLayout.EAST);

        inputRow.add(new JLabel(" 종목: "), BorderLayout.WEST);
        inputRow.add(tfExName, BorderLayout.CENTER);
        inputRow.add(rightBox, BorderLayout.EAST);

        // 하단: 추가된 목록 리스트
        JScrollPane listScroll = new JScrollPane(exerciseList);
        listScroll.setBorder(new TitledBorder("추가된 운동 목록 (더블클릭시 삭제)"));

        // 리스트 항목 더블클릭 시 삭제 기능
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
        JTextArea ta = new JTextArea(4, 20); // 행 개수 지정
        ta.setLineWrap(true);
        ta.setBorder(new TitledBorder(title));
        ta.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        return ta;
    }

    // 데이터 전송 로직 수정
    private void sendHealthData() {
        StringBuilder exBuilder = new StringBuilder();
        for (int i = 0; i < exerciseListModel.size(); i++) {
            if (i > 0) exBuilder.append(", "); // 구분자 콤마
            exBuilder.append(exerciseListModel.get(i));
        }
        String ex = exBuilder.toString();

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

            // 전송 후 입력창 초기화
            exerciseListModel.clear(); // 운동 목록 비우기
            txtDiet.setText("");
            txtPlan.setText("");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // [추가] 로그 파일 저장 기능
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