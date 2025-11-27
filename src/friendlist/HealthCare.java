package friendlist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.DataOutputStream;
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
  private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");

  public HealthCare(String username, DataOutputStream out, FriendList parent) {
    this.username = username;
    this.out = out;
    this.parent = parent;

    setTitle("헬스케어 & 일정 공유 - " + username);
    setSize(500, 700);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    // 상단 입력 패널 (그리드 레이아웃으로 3등분)
    JPanel inputPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    inputPanel.setBorder(new EmptyBorder(10, 10, 0, 10));

    txtExercise = createTitledTextArea("🏋️ 오늘 운동량 (Exercise)");
    txtDiet = createTitledTextArea("🥗 오늘 식단 (Diet)");
    txtPlan = createTitledTextArea("📅 앞으로 계획/일정 (Plan)");

    inputPanel.add(new JScrollPane(txtExercise));
    inputPanel.add(new JScrollPane(txtDiet));
    inputPanel.add(new JScrollPane(txtPlan));

    add(inputPanel, BorderLayout.CENTER);

    // 하단 버튼 및 로그 패널
    JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
    bottomPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
    bottomPanel.setPreferredSize(new Dimension(0, 300));

    JButton btnShare = new JButton("기록 공유하기 (Share)");
    btnShare.setFont(new Font("Gothic", Font.BOLD, 16));
    btnShare.setBackground(new Color(100, 200, 100));
    btnShare.setForeground(Color.WHITE);
    btnShare.addActionListener(e -> sendHealthData());

    displayLog = new JTextArea();
    displayLog.setEditable(false);
    displayLog.setFont(new Font("Gothic", Font.PLAIN, 14));
    displayLog.setText("=== 공유된 헬스케어 기록 ===\n");

    bottomPanel.add(btnShare, BorderLayout.NORTH);
    bottomPanel.add(new JScrollPane(displayLog), BorderLayout.CENTER);

    add(bottomPanel, BorderLayout.SOUTH);

    setVisible(true);
  }

  // 입력창 생성 헬퍼 메서드
  private JTextArea createTitledTextArea(String title) {
    JTextArea ta = new JTextArea();
    ta.setLineWrap(true);
    ta.setBorder(new TitledBorder(title));
    return ta;
  }

  // 데이터 전송
  private void sendHealthData() {
    String ex = txtExercise.getText().replace("|", "/"); // 구분자 충돌 방지
    String dt = txtDiet.getText().replace("|", "/");
    String pl = txtPlan.getText().replace("|", "/");

    if (ex.isEmpty() && dt.isEmpty() && pl.isEmpty()) {
      JOptionPane.showMessageDialog(this, "내용을 하나라도 입력해주세요.");
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
      JOptionPane.showMessageDialog(this, "기록이 공유되었습니다!");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // 서버로부터 받은 데이터 처리
  public void processMessage(String msg) {
    // 프로토콜: HEALTH_BROADCAST:username:운동|식단|계획
    if (msg.startsWith("HEALTH_BROADCAST:")) {
      String[] parts = msg.split(":", 3);
      String sender = parts[1];
      String[] data = parts[2].split("\\|", -1); // -1은 빈 문자열도 포함

      String time = sdf.format(new Date());
      StringBuilder sb = new StringBuilder();
      sb.append("\n------------------------------------\n");
      sb.append("👤 [").append(sender).append("] 님의 기록 (").append(time).append(")\n");
      if(!data[0].isEmpty()) sb.append("🏋️ 운동: ").append(data[0]).append("\n");
      if(!data[1].isEmpty()) sb.append("🥗 식단: ").append(data[1]).append("\n");
      if(!data[2].isEmpty()) sb.append("📅 계획: ").append(data[2]).append("\n");

      displayLog.append(sb.toString());
      displayLog.setCaretPosition(displayLog.getDocument().getLength());
    }
  }
}