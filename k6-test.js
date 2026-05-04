import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '5s', target: 20 },  // 5초 동안 VUs(Virtual Users)를 20명으로 서서히 증가
    { duration: '10s', target: 20 }, // 10초 동안 20명 유지
    { duration: '5s', target: 0 },   // 5초 동안 0명으로 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%의 요청이 500ms 이내에 완료되어야 함
  },
};

export default function () {
  // 정적 리소스(HTML) 응답 속도 테스트
  const res1 = http.get('http://localhost:8080/fcm-test.html');
  check(res1, {
    'fcm-test.html status is 200': (r) => r.status === 200,
  });

  // API 테스트 (리다이렉션 응답)
  const res2 = http.get('http://localhost:8080/api/v1/alerts/test-page');
  check(res2, {
    'test-page redirects correctly': (r) => r.status === 200 || r.status === 302,
  });

  sleep(1); // 1초 대기
}
