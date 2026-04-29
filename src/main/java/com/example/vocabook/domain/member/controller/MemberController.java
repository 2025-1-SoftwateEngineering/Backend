package com.example.vocabook.domain.member.controller;

import com.example.vocabook.domain.member.code.MemberSuccessCode;
import com.example.vocabook.domain.member.controller.docs.MemberControllerDocs;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.service.MemberService;
import com.example.vocabook.global.apiPayload.ApiResponse;
import com.example.vocabook.global.apiPayload.code.BaseSuccessCode;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Validated
public class MemberController implements MemberControllerDocs {

    private final MemberService memberService;

    // 내 프로필 조회
    @GetMapping("/v1/members/me")
    public ApiResponse<MemberResDTO.MyProfile> getMyProfile(
            @AuthenticationPrincipal AuthMember auth
    ) {
        BaseSuccessCode code = MemberSuccessCode.OK;
        return ApiResponse.onSuccess(code, memberService.getMyProfile(auth));
    }

    // 친구 요청 목록 조회
    @GetMapping("/v1/friends/request")
    public ApiResponse<PagingResDTO.Cursor<MemberResDTO.FriendRequestList>> getFriendRequestList(
            @RequestParam(defaultValue = "-1") @NotBlank(message = "커서는 빈칸일 수 없습니다.")
            String cursor,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "조회할 데이터 수는 양의 정수 범위로 입력해주세요")
            Integer pageSize,
            @AuthenticationPrincipal AuthMember auth
    ){
        BaseSuccessCode code = MemberSuccessCode.FRIEND_REQUEST_LIST;
        return ApiResponse.onSuccess(code, memberService.getFriendRequestList(cursor, pageSize, auth));
    }

    // 친구 요청 보내기 (알림 전송해야 함)
    @PostMapping("/v1/friends/{friendId}/request")
    public ApiResponse<MemberResDTO.SendFriendRequest> sendFriendRequest(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable @NotNull(message = "요청 보낼 친구 ID는 필수입니다.")
            Long friendId
    ){
        BaseSuccessCode code = MemberSuccessCode.FRIEND_REQUEST;
        return ApiResponse.onSuccess(code, memberService.sendFriendRequest(auth, friendId));
    }

    // 친구 요청 수락 토글
    @PatchMapping("/v1/friends/{friendId}")
    public ApiResponse<MemberResDTO.UpdateFriendRequest> updateFriendRequest(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable(name = "friendId") @NotNull(message = "요청 보낸 친구 ID는 필수입니다.")
            Long fromMemberId,
            @RequestBody @Valid MemberReqDTO.UpdateFriendRequest dto
    ){
        BaseSuccessCode code = MemberSuccessCode.UPDATED_FRIEND_REQUEST;
        return ApiResponse.onSuccess(code, memberService.updateFriendRequest(auth, fromMemberId, dto));
    }

    // 사용자 단순 조회
    @PostMapping("/v1/members/search")
    public ApiResponse<MemberResDTO.SearchMember> searchMember(
            @AuthenticationPrincipal AuthMember auth,
            @RequestBody @Valid MemberReqDTO.SearchMember dto
    ){
        BaseSuccessCode code = MemberSuccessCode.SEARCH_MEMBER;
        return ApiResponse.onSuccess(code, memberService.searchMember(auth, dto));
    }

    // 친구 목록 조회
    @GetMapping("/v1/friends")
    public ApiResponse<PagingResDTO.Cursor<MemberResDTO.FriendList>> getFriendList(
            @RequestParam(defaultValue = "-1") @NotBlank(message = "커서는 빈칸일 수 없습니다.")
            String cursor,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "조회할 데이터 수는 양의 정수 범위로 입력해주세요")
            Integer pageSize,
            @AuthenticationPrincipal AuthMember auth
    ){
        BaseSuccessCode code = MemberSuccessCode.FRIEND_LIST;
        return ApiResponse.onSuccess(code, memberService.getFriendList(auth, cursor, pageSize));
    }

    // 친구 프로필 조회
    @GetMapping("/v1/friends/{friendId}")
    public ApiResponse<MemberResDTO.FriendProfile> getFriendProfile(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long friendId
    ){
        BaseSuccessCode code = MemberSuccessCode.OK;
        return ApiResponse.onSuccess(code, memberService.getFriendProfile(auth, friendId));
    }

    // 친구 차단
    @PatchMapping("/v1/friends/{friendId}/block")
    public ApiResponse<MemberResDTO.Blocking> blockMember(
            @AuthenticationPrincipal AuthMember auth,
            @PathVariable Long friendId
    ){
        BaseSuccessCode code = MemberSuccessCode.BLOCKING;
        return ApiResponse.onSuccess(code, memberService.blockMember(auth, friendId));
    }
}
