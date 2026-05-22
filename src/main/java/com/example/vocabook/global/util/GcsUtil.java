package com.example.vocabook.global.util;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.exception.MemberException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.HttpMethod;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GcsUtil {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket-name}")
    public String bucketName;

    // Signed Url 생성 (15분 제한시간)
    public String createSignedUrl(
            String fileName,
            String folderName
    ) {
        Map<String, String> extensionHeaders = new HashMap<>();
        extensionHeaders.put("Content-Type", getContentType(fileName));

        BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, folderName+fileName).build();

        return storage.signUrl(blobInfo,
                15,
                TimeUnit.MINUTES,
                Storage.SignUrlOption.httpMethod(HttpMethod.PUT),
                Storage.SignUrlOption.withExtHeaders(extensionHeaders),
                Storage.SignUrlOption.withV4Signature()).toString();
    }

    // GCS에서 객체 찾기
    public Blob findObject(
            String fileName,
            String folderName
    ) {
        return storage.get(bucketName, folderName+fileName);
    }

    // Content-Type 추출
    private String getContentType(String fileName) {
        String format = fileName.substring(fileName.lastIndexOf(".") + 1);

        return switch (format.toLowerCase()) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "svg" -> "image/svg+xml";
            default -> throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
        };
    }
}
