package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK,
            "MEMBER200_1",
            "성공적으로 사용자 프로필을 조회했습니다."),
    FRIEND_REQUEST_LIST(HttpStatus.OK,
            "MEMBER200_2",
            "성공적으로 친구 요청 목록을 조회했습니다."),
    FRIEND_LIST(HttpStatus.OK,
            "MEMBER200_2",
            "성공적으로 친구 목록을 조회했습니다."),
    FRIEND_REQUEST(HttpStatus.OK,
            "MEMBER200_3",
            "성공적으로 친구 요청을 보냈습니다."),
    UPDATED_FRIEND_REQUEST(HttpStatus.OK,
            "MEMBER200_4",
            "성공적으로 친구 요청 상태를 변경했습니다."),
    SEARCH_MEMBER(HttpStatus.OK,
            "MEMBER200_5",
            "성공적으로 사용자를 조회했습니다."),
    BLOCKING(HttpStatus.OK,
            "MEMBER200_6",
            "성공적으로 사용자를 차단했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
