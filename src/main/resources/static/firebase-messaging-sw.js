importScripts("https://www.gstatic.com/firebasejs/12.12.1/firebase-app-compat.js");
importScripts("https://www.gstatic.com/firebasejs/12.12.1/firebase-messaging-compat.js");

const firebaseConfig = {
    apiKey: "AIzaSyC6mFrSXdyKG-hrzreQEcskt88r31HEHgA",
    authDomain: "project-9afb2849-a8f7-481e-a41.firebaseapp.com",
    projectId: "project-9afb2849-a8f7-481e-a41",
    storageBucket: "project-9afb2849-a8f7-481e-a41.firebasestorage.app",
    messagingSenderId: "14062205846",
    appId: "1:14062205846:web:04eb325b56063459147bb7",
    measurementId: "G-QHM8KX0VTW"
};

firebase.initializeApp(firebaseConfig);
const messaging = firebase.messaging();

messaging.onBackgroundMessage(function(payload) {
  console.log('[firebase-messaging-sw.js] Received background message ', payload);
  const notificationTitle = payload.data?.title || payload.notification?.title || '알림';
  const notificationOptions = {
    body: payload.data?.body || payload.notification?.body || '내용 없음',
  };
  self.registration.showNotification(notificationTitle, notificationOptions);
});

self.addEventListener('notificationclick', function(event) {
  console.log('[firebase-messaging-sw.js] Notification click Received.', event);
  event.notification.close();
  
  // 알림 클릭 시 메인 페이지나 특정 페이지로 포커스/이동
  event.waitUntil(
    clients.openWindow('http://localhost:8080/api/v1/alerts/test-page')
  );
});
