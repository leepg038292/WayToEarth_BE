# WebSocket 인증 가이드

## 🔒 보안 토큰 전송 방법

### ✅ **권장 방법 1: Authorization 헤더 사용**

```javascript
// JavaScript WebSocket 연결
const ws = new WebSocket('ws://localhost:8080/ws/crew/123/chat', [], {
    headers: {
        'Authorization': 'Bearer ' + yourJwtToken
    }
});
```

### ✅ **권장 방법 2: Sec-WebSocket-Protocol 헤더 사용 (SockJS 호환)**

```javascript
// SockJS 연결 시
const socket = new SockJS('/ws/crew/123/chat', [], {
    protocols_whitelist: ['Bearer.' + yourJwtToken]
});
```

### ❌ **금지된 방법: URL 쿼리 파라미터**

```javascript
// 보안상 위험 - 사용 금지
const ws = new WebSocket('ws://localhost:8080/ws/crew/123/chat?token=' + yourJwtToken);
```

## 🛡️ **보안 개선사항**

1. **로그 노출 방지**: 토큰이 URL에 포함되지 않아 서버 로그에 노출되지 않음
2. **히스토리 보호**: 브라우저 히스토리에 토큰이 저장되지 않음
3. **Referrer 보호**: HTTP Referrer 헤더를 통한 토큰 누출 방지

## 📱 **클라이언트 구현 예제**

### React Native 예제
```javascript
const connectWebSocket = (crewId, token) => {
    const ws = new WebSocket(`ws://your-server.com/ws/crew/${crewId}/chat`, [], {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    ws.onopen = () => console.log('WebSocket 연결 성공');
    ws.onerror = (error) => console.error('WebSocket 오류:', error);

    return ws;
};
```

### Flutter 예제
```dart
import 'package:web_socket_channel/web_socket_channel.dart';

WebSocketChannel connectWebSocket(int crewId, String token) {
  return WebSocketChannel.connect(
    Uri.parse('ws://your-server.com/ws/crew/$crewId/chat'),
    protocols: ['Bearer.$token']
  );
}
```

## 🔧 **서버 설정**

서버에서는 다음과 같이 토큰을 안전하게 검증합니다:

1. **Authorization 헤더 우선 검증**
2. **Sec-WebSocket-Protocol 헤더 백업 검증**
3. **URL 쿼리 파라미터 사용 금지**

이제 모든 WebSocket 연결에서 토큰이 안전하게 전송됩니다.