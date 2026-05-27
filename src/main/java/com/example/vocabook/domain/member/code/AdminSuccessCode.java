package com.example.vocabook.domain.member.code;

import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AdminSuccessCode implements BaseSuccessCode {

    REPORT_LIST(HttpStatus.OK,
            "ADMIN200_1",
            "성공적으로 신고 목록을 조회했습니다."),
    SUSPEND(HttpStatus.OK,
            "ADMIN200_2",
            "성공적으로 해당 유저를 영구 차단했습니다."),
    ADD_VOCABULARY(HttpStatus.OK,
            "ADMIN200_3",
            "성공적으로 단어장을 추가했습니다."),
    GET_VOCABULARY_LIST(HttpStatus.OK,
            "ADMIN200_4",
            "성공적으로 단어장 목록을 조회했습니다."),
    ADD_WORD(HttpStatus.OK,
            "ADMIN200_5",
            "성공적으로 단어를 추가했습니다."),
    SEARCH_WORD(HttpStatus.OK,
            "ADMIN200_6",
            "성공적으로 단어를 찾았습니다."),
    NOT_ADD_VOCABULARY(HttpStatus.NO_CONTENT,
            "ADMIN204_1",
            "추가될 단어장이 없습니다."),
    UPDATE_VOCABULARY(HttpStatus.OK,
            "ADMIN200_7",
            "성공적으로 단어장을 수정했습니다."),
    DELETE_VOCABULARY(HttpStatus.OK,
            "ADMIN200_8",
            "성공적으로 단어장을 삭제했습니다."),
    UPDATE_WORD(HttpStatus.OK,
            "ADMIN200_9",
            "성공적으로 단어를 수정했습니다."),
    DELETE_WORD(HttpStatus.OK,
            "ADMIN200_10",
            "성공적으로 단어를 삭제했습니다."),
    CREATE_CHOICE(HttpStatus.OK,
            "ADMIN200_11",
            "성공적으로 사지선다를 생성했습니다."),
    CREATE_CROSSWORD(HttpStatus.OK,
            "ADMIN200_12",
            "성공적으로 십자말풀이를 생성했습니다."),
    CREATE_SIGNED_URI(HttpStatus.OK,
            "ADMIN200_13",
            "성공적으로 Signed URL을 생성했습니다."),
    UPLOAD_IMAGE(HttpStatus.OK,
            "ADMIN200_14",
            "성공적으로 사진을 업로드했습니다."),
    CREATE_ITEM(HttpStatus.OK,
            "ADMIN200_15",
            "성공적으로 아이템을 생성했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
