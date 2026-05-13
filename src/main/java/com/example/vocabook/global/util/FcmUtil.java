package com.example.vocabook.global.util;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FcmUtil {

    private final FirebaseMessaging firebaseMessaging;

    public void sendAlert(
            String title,
            String body,
            String token
    ) throws FirebaseMessagingException {

        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        firebaseMessaging.send(message);
    }
}
