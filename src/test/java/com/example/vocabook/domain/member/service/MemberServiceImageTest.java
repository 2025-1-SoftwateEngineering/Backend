package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.Authorize;
import com.example.vocabook.domain.member.enums.PhotoType;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.GcsUtil;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 사진 업로드 및 URL 테스트")
class MemberServiceImageTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GcsUtil gcsUtil;

    @InjectMocks
    private MemberService memberService;

    private AuthMember authMember;
    private Member myMember;

    @BeforeEach
    void setUp() {
        myMember = Member.builder()
                .id(1L)
                .email("me@example.com")
                .authorize(Authorize.ROLE_USER)
                .profileUrl("https://storage.googleapis.com/vocabuddy-storage/profile/default-profile.png")
                .build();
        authMember = new AuthMember(myMember);
    }

    @Test
    @DisplayName("사진 업로드용 URL 발급 성공 - PROFILE (일반 유저)")
    void createSignedUri_Success() {
        // given
        String fileName = "test.png";
        PhotoType photoType = PhotoType.PROFILE;
        given(gcsUtil.createSignedUrl(anyString(), anyString())).willReturn("http://signed.url");

        // when
        MemberResDTO.CreateSignedUri res = memberService.createSignedUri(authMember, fileName, photoType);

        // then
        assertNotNull(res);
        assertEquals(PhotoType.PROFILE, res.photoType());
    }

    @Test
    @DisplayName("사진 업로드용 URL 발급 실패 - 일반 유저가 PROFILE이 아닌 타입 시도")
    void createSignedUri_Fail_InvadeType() {
        // given
        String fileName = "test.png";
        PhotoType photoType = PhotoType.ITEM;

        // when & then
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.createSignedUri(authMember, fileName, photoType)
        );
        assertEquals(MemberErrorCode.INVADE_PHOTO_TYPE, exception.getCode());
    }

    @Test
    @DisplayName("사진 업로드용 URL 발급 실패 - 확장자가 없는 파일명")
    void createSignedUri_Fail_NoExtension() {
        // given
        String fileName = "test"; // no dot
        PhotoType photoType = PhotoType.PROFILE;

        // when & then
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.createSignedUri(authMember, fileName, photoType)
        );
        assertEquals(MemberErrorCode.INVADE_PHOTO_TYPE, exception.getCode());
    }

    @Test
    @DisplayName("사진 업로드 완료 실패 - 대상 ID 누락 (ITEM 등 PROFILE 외 타입)")
    void uploadImage_Fail_NoTargetId() {
        // given
        String fileName = "uuid.png";
        PhotoType photoType = PhotoType.ITEM;
        Long targetId = null;

        // when & then
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.uploadImage(authMember, fileName, photoType, targetId)
        );
        assertEquals(MemberErrorCode.NOT_NULL_TARGET_ID, exception.getCode());
    }

    @Test
    @DisplayName("사진 업로드 완료 실패 - GCS에 파일이 없는 경우")
    void uploadImage_Fail_ObjectNotFound() {
        // given
        String fileName = "uuid.png";
        PhotoType photoType = PhotoType.PROFILE;
        Long targetId = null;

        given(gcsUtil.findObject(fileName, "profile/")).willReturn(null);

        // when & then
        MemberException exception = assertThrows(MemberException.class, () ->
                memberService.uploadImage(authMember, fileName, photoType, targetId)
        );
        assertEquals(MemberErrorCode.NOT_UPLOAD_PROFILE, exception.getCode());
    }

    @Test
    @DisplayName("사진 업로드 완료 성공 - PROFILE (기존 파일 삭제 포함)")
    void uploadImage_Success_Profile() {
        // given
        String fileName = "uuid.png";
        PhotoType photoType = PhotoType.PROFILE;
        Long targetId = null;

        Blob object = mock(Blob.class);
        BlobId blobId = mock(BlobId.class);
        given(gcsUtil.findObject(fileName, "profile/")).willReturn(object);
        given(object.getBlobId()).willReturn(blobId);
        given(blobId.getBucket()).willReturn("my-bucket");
        given(blobId.getName()).willReturn("profile/uuid.png");
        given(object.getUpdateTimeOffsetDateTime()).willReturn(OffsetDateTime.now());

        given(memberRepository.findById(myMember.getId())).willReturn(Optional.of(myMember));

        Blob oldObject = mock(Blob.class);
        given(gcsUtil.findObject("default-profile.png", "profile/")).willReturn(oldObject);

        // when
        MemberResDTO.UploadImage res = memberService.uploadImage(authMember, fileName, photoType, targetId);

        // then
        assertNotNull(res);
        assertEquals("https://storage.googleapis.com/my-bucket/profile/uuid.png", res.publicUrl());
        verify(oldObject).delete();
    }
}
