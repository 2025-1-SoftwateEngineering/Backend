package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements BaseErrorCode {

        NOT_FOUND(HttpStatus.NOT_FOUND,
                        "MEMBER404_1",
                        "해당 사용자를 찾지 못했습니다."),
        NOT_FOUND_FRIEND_REQUEST(HttpStatus.NOT_FOUND,
                        "MEMBER404_2",
                        "친구 요청을 찾지 못했습니다."),
        INVADED_CURSOR(HttpStatus.BAD_REQUEST,
                        "MEMBER400_1",
                        "잘못된 커서값을 입력했습니다."),
        INVADED_STATE(HttpStatus.BAD_REQUEST,
                        "MEMBER400_2",
                        "잘못된 친구 요청 상태입니다. (accept, reject)"),
        EXISTS_FRIEND_REQUEST(HttpStatus.CONFLICT,
                        "MEMBER409_1",
                        "이미 친구 요청을 보냈거나 이미 친구입니다."),
        BLOCKING(HttpStatus.FORBIDDEN,
                        "MEMBER403_1",
                        "대상 사용자가 차단했습니다."),
        NOT_REQUEST(HttpStatus.BAD_REQUEST,
                        "MEMBER400_3",
                        "친구 요청 대기 상태가 아닙니다."),
        NOT_FRIEND(HttpStatus.BAD_REQUEST,
                        "MEMBER400_4",
                        "대상과 친구가 아닙니다."),
        EXISTS_SELF_REQUEST(HttpStatus.CONFLICT,
                        "MEMBER409_2",
                        "자기 자신에게 친구 요청을 보낼 수 없습니다.");

        private final HttpStatus status;
        private final String code;
        private final String message;
}
