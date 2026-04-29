package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.converter.FriendConverter;
import com.example.vocabook.domain.member.converter.MemberConverter;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.FriendRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.global.apiPayload.converter.PagingConverter;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;

    // 내 프로필 조회
    public MemberResDTO.MyProfile getMyProfile(
            AuthMember auth
    ) {

        Member member = auth.getMember();

        return MemberConverter.toMyProfile(member);
    }

    // 친구 요청 목록 조회
    public PagingResDTO.Cursor<MemberResDTO.FriendRequestList> getFriendRequestList(
            String cursor,
            Integer pageSize,
            AuthMember auth
    ) {

        // PageRequest 생성
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        // cursor 검증 & 커서 페이지네이션
        Slice<Friend> friendList;
        if (!cursor.equals("-1")) {
            try {
                friendList = friendRepository.findFriendRequestListWithCursor(
                        auth.getMember().getId(),
                        FriendState.ACCEPTED,
                        Long.parseLong(cursor),
                        pageRequest
                );
            } catch (NumberFormatException e) {
                throw new MemberException(MemberErrorCode.INVADED_CURSOR);
            }
        } else {
            friendList = friendRepository.findFriendRequestListWithoutCursor(
                    auth.getMember().getId(),
                    FriendState.ACCEPTED,
                    pageRequest
            );
        }

        if (friendList.isEmpty()) {
            return PagingConverter.toCursor(
                    null,
                    null,
                    false,
                    friendList.getNumberOfElements()
            );
        }
        // 다음 커서 제작
        String nextCursor = friendList.getContent().getLast().getId().toString();

        // 조회한 DTO 포장
        return PagingConverter.toCursor(
                friendList.getContent().stream()
                        .map(MemberConverter::toFriendRequestList)
                        .toList(),
                nextCursor,
                friendList.hasNext(),
                friendList.getNumberOfElements()
        );
    }

    // 친구 요청 보내기 (알림 전송해야 함)
    @Transactional
    public MemberResDTO.SendFriendRequest sendFriendRequest(
            AuthMember auth,
            Long friendId
    ) {
        // 요청보낼 친구가 존재하는지 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 이미 친구인지 or 친구 요청을 보냈는지 확인
        if (friendRepository.existsByFromMemberAndToMember(auth.getMember(), friend)){
            throw new MemberException(MemberErrorCode.EXISTS_FRIEND_REQUEST);
        }

        // 친구가 나를 차단했는지 확인
        if (friendRepository.existsByFromMemberAndFriendStateIs(friend, FriendState.BLOCKED)){
            throw new MemberException(MemberErrorCode.BLOCKING);
        }

        // 친구 요청 생성
        friendRepository.save(FriendConverter.toFriend(auth.getMember(), friend));

        return FriendConverter.toFriendRequest(friend);
    }

    // 친구 요청 수락 or 거절
    @Transactional
    public MemberResDTO.UpdateFriendRequest updateFriendRequest(
            AuthMember auth,
            Long fromMemberId,
            MemberReqDTO.UpdateFriendRequest dto
    ) {

        // 요청 보낸 사용자 존재 여부 확인
        Member fromMember = memberRepository.findById(fromMemberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 존재하는 친구 요청인지 확인
        // fromFriendRequest = (상대 -> 나)
        // toFriendRequest = (나 -> 상대)
        Friend fromFriendRequest = friendRepository.findByFromMemberAndToMember(fromMember, auth.getMember())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_FRIEND_REQUEST));

        // 친구 요청 상태인지 확인 (WAITING)
        if (!fromFriendRequest.getFriendState().equals(FriendState.WAITING)){
            throw new MemberException(MemberErrorCode.NOT_REQUEST);
        }

        // 유효한 상태 범위인지 확인 -> 상태 변경
        switch (dto.state().toLowerCase()) {
            case "accept":
                fromFriendRequest.updateState(FriendState.ACCEPTED);

                // 친구 관계 양방향 설정
                friendRepository.save(FriendConverter.toFriend(auth.getMember(), fromMember, FriendState.ACCEPTED));
                break;
            case "reject":
                fromFriendRequest.updateState(FriendState.REJECTED);

                // 친구 관계 양방향 설정
                friendRepository.save(FriendConverter.toFriend(auth.getMember(), fromMember, FriendState.REJECTED));
                break;
            default:
                throw new MemberException(MemberErrorCode.INVADED_STATE);
        }

        return MemberConverter.toUpdateFriendRequest(
                fromFriendRequest.getFromMember(),
                fromFriendRequest.getFriendState()
        );
    }

    // 사용자 단순 조회
    public MemberResDTO.SearchMember searchMember(
            AuthMember auth,
            MemberReqDTO.SearchMember dto
    ) {
        Member member = memberRepository.findByEmail(dto.email())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));
        return MemberConverter.toSearchMember(member);
    }

    // 친구 목록 조회
    public PagingResDTO.Cursor<MemberResDTO.FriendList> getFriendList(
            AuthMember auth,
            String cursor,
            Integer pageSize
    ) {

        // PageRequest 생성
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        // cursor 검증 & 커서 페이지네이션
        Slice<Friend> friendList;
        if (!cursor.equals("-1")) {
            try {
                friendList = friendRepository.findFriendListWithCursor(
                        auth.getMember().getId(),
                        FriendState.ACCEPTED,
                        Long.parseLong(cursor),
                        pageRequest
                );
            } catch (NumberFormatException e) {
                throw new MemberException(MemberErrorCode.INVADED_CURSOR);
            }
        } else {
            friendList = friendRepository.findFriendListWithoutCursor(
                    auth.getMember().getId(),
                    FriendState.ACCEPTED,
                    pageRequest
            );
        }

        if (friendList.isEmpty()) {
            return PagingConverter.toCursor(
                    null,
                    null,
                    false,
                    friendList.getNumberOfElements()
            );
        }
        // 다음 커서 제작
        String nextCursor = friendList.getContent().getLast().getId().toString();

        // 조회한 DTO 포장
        return PagingConverter.toCursor(
                friendList.getContent().stream()
                        .map(MemberConverter::toFriendList)
                        .toList(),
                nextCursor,
                friendList.hasNext(),
                friendList.getNumberOfElements()
        );
    }

    // 친구 프로필 조회
    public MemberResDTO.FriendProfile getFriendProfile(
            AuthMember auth,
            Long friendId
    ) {
        // 사용자가 존재하는지 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 사용자와 친구인지 확인
        if (!friendRepository.existsByFromMemberAndToMember(auth.getMember(), friend)){
            throw new MemberException(MemberErrorCode.NOT_FRIEND);
        }

        // 사용자가 차단했는지 확인
        if (friendRepository.existsByFromMemberAndFriendStateIs(friend, FriendState.BLOCKED)){
            throw new MemberException(MemberErrorCode.BLOCKING);
        }

        return MemberConverter.toFriendProfile(friend);
    }

    // 사용자 차단
    @Transactional
    public MemberResDTO.Blocking blockMember(
            AuthMember auth,
            Long friendId
    ) {
        // 차단할 사용자와 친구인지 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // fromMemberFriend = (나 -> 상대)
        // toMemberFriend = (상대 -> 나)
        Friend fromMemberFriend = friendRepository.findByFromMemberAndToMember(auth.getMember(), friend)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FRIEND));
        Friend toMemberFriend = friendRepository.findByFromMemberAndToMember(friend, auth.getMember())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FRIEND));

        if (!fromMemberFriend.getFriendState().equals(FriendState.ACCEPTED)){
            throw new MemberException(MemberErrorCode.NOT_FRIEND);
        }

        // 사용자 관계 상태 변경
        fromMemberFriend.updateState(FriendState.BLOCKED);
        toMemberFriend.updateState(FriendState.BLOCKED);

        return MemberConverter.toBlocking(friend);
    }
}
