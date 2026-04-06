# 프로젝트명 미정 (영어 단어 학습 Web App 개발)

## 💡 Project Overview

## 🎯 주요 기능
- 로그인 기능(회원, 관리자)
- 영단어 학습 기능
  
## 👥 Contributors


### ⚙️ 기술 스택
- Java 21
- Spring Boot 4.0.4
- MySQL
- Swagger 2.7.0

### 📋 Commit Message Convention
|     Gitmoji     | Description |
|:---------------:| - |
|   `✨ feat: `   | 새로운 기능 추가 |
|   `🐛 fix: `    | 버그 수정 |
|   `📝 docs: `   | 문서 추가, 수정, 삭제 |
|   `✅ test: `   | 테스트 코드 추가, 수정, 삭제 |
|  `💄 style: `   | 코드 형식 변경 |
| `♻️ refactor: ` | 코드 리팩토링 |
|   `⚡️ perf: `   | 성능 개선 |
|    `💚 ci: `    | CI 관련 설정 수정 |
|  `🚀 chore: `   | 기타 변경사항 |
|  `🔥 remove:`️   | 코드 및 파일 제거 |

### 아키텍처 구조
도메인 아키텍쳐 (DDD)
```
└── java/com/example/voca/
    ├── domain
    |   ├── auth        # 인증 관련
    |   ├── member      # 마이페이지 관련
    |   ├── alert       # 알림 관련
    |   └── voca        # 단어장 관련
    ├── global
    |   ├── apiPayload  # 응답 통일
    |   ├── security    # 시큐리티
    |   ├── config      # 각종 설정
    |   ├── util        # 유틸
    └── VocaBookApplication
```
### 서버 아키텍처
<img width="878" height="673" alt="스크린샷 2026-04-06 14 47 25" src="https://github.com/user-attachments/assets/ab5990fd-5cd5-47ec-bb73-4b205d1e545c" />
