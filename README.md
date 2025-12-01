🌿 Mint Talk (Java Chat Application)

Java Swing과 Socket 통신을 활용한 실시간 멀티 채팅 메신저 프로젝트입니다. 카카오톡과 유사한 UI/UX를 목표로 하여 프로필 사진 변경, 이미지 전송, 이모티콘, 실시간 상태 동기화 기능을 구현했습니다.

📸 Screenshots

로그인 화면

친구 목록

채팅방 (이미지/이모티콘)

프로필 상세

<img src="screenshots/login.png" width="200">

<img src="screenshots/friendlist.png" width="200">

<img src="screenshots/chatting.png" width="200">

<img src="screenshots/profile.png" width="200">

실행 화면 스크린샷을 screenshots 폴더에 추가하면 여기에 표시됩니다.

✨ Key Features (주요 기능)

1. 사용자 관리 & 커스터마이징

실시간 접속자 확인: 서버에 접속한 사용자 목록이 실시간으로 갱신됩니다.

프로필 변경: 내 프로필 사진, 배경화면, 상태 메시지를 변경할 수 있으며 다른 사용자에게 즉시 반영됩니다.

이미지 동기화: 프로필 이미지는 ID.jpg 형태로 로컬에 저장되어 채팅방에서도 연동됩니다.

2. 실시간 채팅 (1:N)

채팅방 생성: 원하는 친구들을 선택하여 그룹 채팅방을 만들 수 있습니다.

UI 디자인: 나(오른쪽/노란색), 상대방(왼쪽/흰색/프로필사진)으로 구분된 말풍선 UI를 제공합니다. (카카오톡 스타일)

멀티미디어 전송:

+ 버튼을 통한 이미지 파일 전송 (리사이징 및 Base64 인코딩 처리)

이모티콘 전송 기능

3. 기술적 특징

Socket Programming: ServerSocket과 Socket을 이용한 TCP/IP 통신 구현.

Multi-Threading: UserVector를 활용하여 다중 클라이언트 접속 및 메시지 브로드캐스팅 처리.

Custom Swing Components: RoundedButton, RoundedTextField, ImagePanel, ChatCellRenderer 등 커스텀 컴포넌트로 세련된 UI 구현.

ImageIO: 안정적인 이미지 로딩 및 처리를 위한 ImageIO 활용.

🛠️ Project Structure (폴더 구조)

src/
├── chat/
│   ├── ChatMessage.java       # 메시지 데이터 객체 (텍스트, 이미지 포함)
│   └── ChatRoomInfo.java      # 채팅방 정보 객체
├── chatclient/
│   ├── JavaChatClientMain.java # 클라이언트 실행 (로그인)
│   └── JavaChatClientView.java # 채팅방 화면 UI 및 로직
├── friendlist/
│   ├── FriendList.java        # 친구 목록 메인 화면 (프로필 관리)
│   ├── ChatRoomList.java      # 채팅방 목록 화면
│   ├── ChatCellRenderer.java  # 채팅 리스트 렌더러 (말풍선, 프로필 사진)
│   ├── ProfileDetailDialog.java # 프로필 상세 보기 및 수정 창
│   └── HealthCare.java        # (부가기능) 헬스케어 기능
├── image/
│   ├── ImagePanel.java        # 배경 이미지를 그리는 패널
│   ├── RoundedButton.java     # 둥근 버튼 컴포넌트
│   └── RoundedTextField.java  # 둥근 입력창 컴포넌트
└── server/
    └── JavaChatServer.java    # 멀티 스레드 서버


🚀 How to Run (실행 방법)

이 프로젝트는 자바 개발 환경(JDK)이 필요합니다.

1. 필수 준비 사항

프로젝트 루트 경로(src와 같은 레벨)에 image 폴더가 있어야 하며, 기본 리소스 파일이 필요합니다.

image/mint.jpg, image/mint2.jpg, image/abc.jpg (배경화면)

image/profile.jpg (기본 프로필)

image/emoticon/ (이모티콘 폴더)

2. 서버 실행

먼저 서버를 구동하여 클라이언트를 받을 준비를 합니다.

// Run As Java Application
server.JavaChatServer


포트 번호(기본 30000)를 확인하고 Server Start 버튼을 누릅니다.

3. 클라이언트 실행

여러 개의 클라이언트를 실행하여 테스트할 수 있습니다.

// Run As Java Application
chatclient.JavaChatClientMain


IP Address: 로컬 테스트 시 127.0.0.1 입력

Port: 서버와 동일한 포트 입력 (30000)

ID: 서로 다른 아이디로 로그인

🐛 Troubleshooting

Q. 프로필 사진이 채팅방에 안 떠요!

A. FriendList에서 프로필 사진을 한 번 변경(업로드)해야 image 폴더에 ID.jpg 파일이 생성되어 채팅방에 연동됩니다.

Q. 이미지가 전송이 안 돼요.

A. 이미지 파일의 크기가 너무 크면 소켓 버퍼 제한으로 전송이 실패할 수 있습니다. 코드는 자동으로 리사이징하지만, 너무 고해상도 이미지는 피해주세요.

👨‍💻 Author

Developer: [본인 이름/ID]

Contact: [이메일 주소]

University: [학교 이름 (선택사항)]

This project was created for educational purposes to study Java Network Programming.