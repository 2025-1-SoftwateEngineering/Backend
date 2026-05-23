package com.example.vocabook;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.cloud.storage.Storage;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class VocaBookApplicationTests {

    @MockitoBean
    private FirebaseApp firebaseApp;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private Storage storage;

    @Test
    void contextLoads() {
    }
}
