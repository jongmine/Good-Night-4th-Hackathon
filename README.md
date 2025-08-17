# Good-Night-4th-Hackathon
공연 좌석 예매 시스템 풀스택 구현

## 안내사항

- 본 레포지토리를 **fork**하여 과제를 수행하고, 완료시 PR을 보냅니다.
- 과제의 소스코드는 본인의 GitHub 레포지토리에 **Public**으로 올려주세요.
- 진행 간 문의사항은 이 레포지토리의 Issue로 등록해주세요.
- 구현 내용은 README.md 하단에 이어서 작성해 주세요.

## 과제 목표

공연 좌석 예매 시스템 구현

### 기술 스택

- Backend: 자유 선택 (Spring Boot, Node.js, Django, FastAPI 등)
- Frontend: 자유 선택 (React, Vue, Svelte, Vanilla JS 등)
- Database: 자유 선택
- 선택한 기술 스택에 대한 이유를 README에 간단히 설명해주세요

### 평가 항목

- 문제 해결 접근 방식에서 기술적 의사결정
- 구현 완성도와 문서화 수준

## 요구사항

### 최소 요구사항
> 아래 목표들을 달성하기 위한 구현 방법은 자유롭게 선택하세요.

1.  **좌석 현황 표시**
    - 3x3 격자 형태로 총 9개의 좌석 표시
    - 각 좌석의 예약 가능/불가능 상태를 시각적으로 구분
2.  **좌석 예약 기능**
    - 사용자가 빈 좌석을 클릭하여 선택
    - 페이지를 이동하여 예약자 정보 입력
    - 정보 입력 완료 후 예약 확정 시도
        - 99% 확률로 예약 성공 처리
        - 1% 확률로 의도적 실패 처리
    - 예약 성공/실패에 대한 명확한 피드백 제공
3.  **API 엔드포인트**
    - 좌석 목록 조회 API
    - 좌석 예약 요청 API
    - HTTP 통신을 통한 데이터 교환
4.  **코드 품질 보장**
    - 테스트 코드
    - 타입 체크
    - 린팅

### 기본 요구사항

> 아래 목표들을 달성하기 위한 구현 방법은 자유롭게 선택하세요.

> 각 목표를 어떻게 해결했는지 README에 설명해주세요.

1.  **사용자 경험 개선**
    - **목표**: 사용자가 서비스를 이용할 때 발생할 수 있는 불편함 최소화
    - **예시**
        - 직관적인 UI
        - 네트워크 지연이 발생했을 때 편의성
        - 예약이 실패했을 때 편의성
        - 모바일에서 접속했을 때 편의성
2.  **안정적인 서비스 운영**
    - **목표**: 예상치 못한 상황에서도 서비스가 안정적으로 동작
    - **예시**
        - 잘못된 요청이 들어왔을 때
        - 존재하지 않는 좌석을 예약하려 할 때
        - 서버 에러가 발생했을 때
        - 데이터 정합성 보장

### 심화 요구사항

> 아래 목표들을 달성하기 윈한 구현 방법은 자유롭게 선택하세요.

> 각 목표를 달성했음을 검증할 방법을 마련하세요.

> 시도한 방법이 어떤 방식으로 문제를 해결했으며 보유한 한계점에 대해 상세히 README에 설명해주세요.


1.  **동시성 제어**
    - **상황**: 여러 사용자가 동시에 같은 좌석을 예약하려고 시도하는 경우
    - **목표**: 한 좌석에 대해 단 한 명만 예약에 성공하도록 보장
2.  **실시간 좌석 상태 동기화**
    - 상황: UI에서 사용자들이 이미 선택된 좌석을 선택하게 되는 경우
    - 목표: 실시간 좌석 예약 상태를 확인할 수 있도록 실시간 동기화 제공
3.  **선택한 좌석에 대한 우선순위 제공**
    - 상황 : 좌석 선택 후 예약자 정보를 입력하는 동안 다른 사용자가 좌석을 예약하게 되는 경우
    - 목표: 동일 좌석에 대해 먼저 선택을 한 사용자에게 예약 우선순위 제공

## 참고사항

### 진행 방식

- 최소 요구사항을 먼저 완성한 후 기본 기능을 구현해주세요
- 심화 요구사항은 구현에 실패해도 고민한 해결 방법이 있으면 작성해주세요.

### 필수 제출 항목

- **README.md**: 다음 내용을 반드시 포함
    - 프로젝트 실행 방법 (상세하게)
    - 기술 스택 선택 이유
    - 구현한 요구사항 체크리스트
    - 각 요구사항별 해결 방법 설명

### 선택 제출 항목

- 아키텍처 다이어그램
- 시연 영상 또는 GIF

---

<!-- 구현 내용 작성 -->

## API Specifications

### SeatController

Base URL: `/api`

1.  **GET /api/seats**
    *   **Description**: Retrieves a list of all seats with their current status.
    *   **Method**: `GET`
    *   **Headers**:
        *   `X-Client-Token` (Optional): Client token for identifying held seats.
    *   **Response**: `200 OK`
        ```json
        [
            {
                "id": Long,
                "label": String,
                "status": "AVAILABLE" | "HELD" | "RESERVED",
                "holdExpiresAt": String (ISO 8601 datetime) | null,
                "heldByMe": Boolean
            }
        ]
        ```

2.  **POST /api/seats/{id}/hold**
    *   **Description**: Attempts to hold a specific seat.
    *   **Method**: `POST`
    *   **Path Parameters**:
        *   `id` (Long): The ID of the seat to hold.
    *   **Headers**:
        *   `X-Client-Token` (Required): Client token for identifying the holder.
    *   **Response**: `200 OK`
        ```json
        {
            "id": Long,
            "label": String,
            "status": "HELD",
            "holdExpiresAt": String (ISO 8601 datetime),
            "heldByMe": true
        }
        ```
    *   **Error Responses**:
        *   `409 Conflict`: If the seat is already held or reserved.

3.  **POST /api/seats/{id}/heartbeat**
    *   **Description**: Extends the hold time for a previously held seat.
    *   **Method**: `POST`
    *   **Path Parameters**:
        *   `id` (Long): The ID of the seat to extend hold for.
    *   **Headers**:
        *   `X-Client-Token` (Required): Client token of the current holder.
    *   **Response**: `200 OK`
        ```json
        {
            "id": Long,
            "label": String,
            "status": "HELD",
            "holdExpiresAt": String (ISO 8601 datetime),
            "heldByMe": true
        }
        ```
    *   **Error Responses**:
        *   `404 Not Found`: If the seat is not found or not held by the provided token.

4.  **POST /api/seats/{id}/reserve**
    *   **Description**: Reserves a previously held seat.
    *   **Method**: `POST`
    *   **Path Parameters**:
        *   `id` (Long): The ID of the seat to reserve.
    *   **Headers**:
        *   `X-Client-Token` (Required): Client token of the current holder.
        *   `Content-Type`: `application/json`
    *   **Request Body**:
        ```json
        {
            "name": String,
            "phone": String
        }
        ```
    *   **Response**: `200 OK`
        ```json
        {
            "seatId": Long,
            "name": String,
            "reservedAt": String (ISO 8601 datetime)
        }
        ```
    *   **Error Responses**:
        *   `400 Bad Request`: If `name` or `phone` are missing/invalid.
        *   `404 Not Found`: If the seat is not found or not held by the provided token.
        *   `409 Conflict`: If the reservation fails (e.g., 1% chance of failure).

### SeatStreamController

Base URL: `/stream`

1.  **GET /stream/seats**
    *   **Description**: Establishes a Server-Sent Events (SSE) connection to stream real-time seat status updates.
    *   **Method**: `GET`
    *   **Headers**:
        *   `X-Client-Token` (Optional): Client token for identifying held seats.
    *   **Response**: `text/event-stream`
        *   Sends `seat_update` events with updated seat DTOs.
        *   Sends `ping` events periodically for keep-alive.
        ```
        event: seat_update
        data: { "id": Long, "label": String, "status": "AVAILABLE" | "HELD" | "RESERVED", "holdExpiresAt": String | null, "heldByMe": Boolean }

        event: ping
        data: hello
        ```