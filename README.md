# 🏙️ 안양 스마트도시 통합센터 관리자 페이지

안양시 스마트도시 통합센터의 시민 서비스 관리 시스템입니다.

## 📋 주요 기능

### 시민 서비스 관리
- **공지사항 관리**: 분류별(일반/행사/긴급/점검) 검색, 상태별 필터링, 게시 기간 설정
- **보도자료 관리**: 제목/작성자 검색, 파일 첨부, 조회수 관리
- **방문사진 관리**: 이미지 업로드, 상태별 관리
- **홍보영상 관리**: 영상 파일 업로드, 간편 검색

### 공통 기능
- 상단 고정 게시글
- 파일 첨부 (최대 5개, 90MB)
- 게시 상태 자동 관리 (게시 중/예약/게시 종료)
- 조회수 자동 증가
- CRUD 전체 지원

## 🛠️ 기술 스택

- **Backend**: Java 17, Spring Boot 3.x
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Template Engine**: Thymeleaf
- **Build Tool**: Maven
- **Version Control**: Git

## 📦 설치 및 실행 방법

### 1. 필수 요구사항
- Java 17 이상
- MySQL 8.0 이상
- Maven 3.6 이상

### 2. 데이터베이스 설정
```sql
CREATE DATABASE smart_city_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 프로젝트 클론
```bash
git clone https://github.com/YOUR_USERNAME/anyang-smart-city-admin.git
cd anyang-smart-city-admin
```

### 4. application.properties 설정
`src/main/resources/application.properties` 파일에서 DB 정보 수정:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_city_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 5. 프로젝트 빌드 및 실행
```bash
# Maven으로 빌드
mvn clean install

# 스프링 부트 실행
mvn spring-boot:run
```

### 6. 접속
```
http://localhost:8080
```

## 📂 프로젝트 구조
```
demo/
├── src/
│   └── main/
│       ├── java/com/example/demo/
│       │   ├── controller/      # 컨트롤러
│       │   ├── entity/          # 엔티티
│       │   ├── repository/      # 리포지토리
│       │   └── service/         # 서비스
│       └── resources/
│           ├── templates/       # Thymeleaf 템플릿
│           │   ├── notice/
│           │   ├── press/
│           │   ├── visit/
│           │   └── video/
│           └── application.properties
└── pom.xml
```

## 🔧 주요 API 엔드포인트

### 공지사항 (Notice)
- `GET /notice/list` - 목록 조회
- `GET /notice/write` - 작성 페이지
- `GET /notice/edit/{id}` - 수정 페이지
- `GET /notice/detail/{id}` - 상세 조회
- `POST /notice/api/save` - 등록/수정
- `DELETE /notice/api/delete/{id}` - 삭제

### 보도자료 (Press)
- `GET /press/list` - 목록 조회
- `GET /press/write` - 작성 페이지
- `GET /press/edit/{id}` - 수정 페이지
- `GET /press/detail/{id}` - 상세 조회
- `POST /press/api/save` - 등록/수정
- `DELETE /press/api/delete/{id}` - 삭제

### 방문사진 (Visit)
- `GET /visit/list` - 목록 조회
- `GET /visit/write` - 작성 페이지
- `GET /visit/edit/{id}` - 수정 페이지
- `GET /visit/detail/{id}` - 상세 조회
- `POST /visit/api/save` - 등록/수정
- `DELETE /visit/api/delete/{id}` - 삭제

### 홍보영상 (Video)
- `GET /video/list` - 목록 조회
- `GET /video/write` - 작성 페이지
- `GET /video/edit/{id}` - 수정 페이지
- `GET /video/detail/{id}` - 상세 조회
- `POST /video/api/save` - 등록/수정
- `DELETE /video/api/delete/{id}` - 삭제

