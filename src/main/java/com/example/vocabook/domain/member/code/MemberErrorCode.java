package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

        ALREADY_REPORT(HttpStatus.ALREADY_REPORTED,
            "MEMBER208_1",
            "이미 해당 사용자를 신고했습니다."),

        INVADED_STATE(HttpStatus.BAD_REQUEST,
            "MEMBER400_1",
            "잘못된 친구 요청 상태입니다. (accept, reject)"),
        NOT_REQUEST(HttpStatus.BAD_REQUEST,
            "MEMBER400_2",
            "친구 요청 대기 상태가 아닙니다."),
        NOT_FRIEND(HttpStatus.BAD_REQUEST,
            "MEMBER400_3",
            "대상과 친구가 아닙니다."),
        SELF_REQUEST(HttpStatus.BAD_REQUEST,
            "MEMBER400_4",
            "자기 자신에게 친구 요청을 보낼 수 없습니다."),
        SELF_REPORT(HttpStatus.BAD_REQUEST,
            "MEMBER400_5",
            "자기 자신을 신고할 수 없습니다."),
        INVADE_PHOTO_TYPE(HttpStatus.BAD_REQUEST,
            "MEMBER400_6",
            "해당 사진 타입을 업로드할 수 없습니다."),
        NOT_NULL_TARGET_ID(HttpStatus.BAD_REQUEST,
                "MEMBER400_7",
                "조회할 대상 ID는 필수입니다."),

        BLOCKING(HttpStatus.FORBIDDEN,
            "MEMBER403_1",
            "대상 사용자가 차단했습니다."),
        SUSPENDED(HttpStatus.FORBIDDEN,
            "MEMBER403_2",
            "해당 사용자는 영구 정지된 상태입니다. 관리자에게 문의해주세요."),

        NOT_FOUND(HttpStatus.NOT_FOUND,
                        "MEMBER404_1",
                        "해당 사용자를 찾지 못했습니다."),
        NOT_FOUND_FRIEND_REQUEST(HttpStatus.NOT_FOUND,
                        "MEMBER404_2",
                        "친구 요청을 찾지 못했습니다."),
        NOT_UPLOAD_PROFILE(HttpStatus.NOT_FOUND,
                "MEMBER404_3",
                "아직 프로필을 업로드하지 않았습니다."),
        PET_NOT_FOUND(HttpStatus.NOT_FOUND,
                "MEMBER404_4",
                "해당 애완동물을 찾지 못했습니다."),


        EXISTS_FRIEND_REQUEST(HttpStatus.CONFLICT,
                        "MEMBER409_1",
                        "이미 친구 요청을 보냈거나 이미 친구입니다."),
        ;

        private final HttpStatus status;
        private final String code;
        private final String message;
}
