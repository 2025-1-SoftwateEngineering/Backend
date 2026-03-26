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
└── java/com/example/scoi/
    ├── domain
    |   ├── auth        # 인증 관련
    |   ├── member      # 마이페이지 관련
    |   └── voca        # 단어장 관련
    ├── global
    |   ├── apiPayload  # 응답 통일
    |   ├── security    # 시큐리티
    |   ├── config      # 각종 설정
    |   ├── util        # 유틸
    └── VocaBookApplication
```
### 서버 아키텍처
