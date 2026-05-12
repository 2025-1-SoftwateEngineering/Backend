package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.converter.FriendConverter;
import com.example.vocabook.domain.member.converter.MemberConverter;
import com.example.vocabook.domain.member.converter.ReportConverter;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.FriendRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.ReportRepository;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.apiPayload.converter.PagingConverter;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.FcmUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final ReportRepository reportRepository;
    private final FcmUtil fcmUtil;
    private final AlertRepository alertRepository;

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
                        FriendState.WAITING.name(),
                        Long.parseLong(cursor),
                        pageRequest
                );
            } catch (NumberFormatException e) {
                throw new MemberException(GeneralErrorCode.INVADED_CURSOR);
            }
        } else {
            friendList = friendRepository.findFriendRequestListWithoutCursor(
                    auth.getMember().getId(),
                    FriendState.WAITING.name(),
                    pageRequest);
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

    // 친구 요청 보내기
    @Transactional
    public MemberResDTO.SendFriendRequest sendFriendRequest(
            AuthMember auth,
            Long friendId
    ) {
        // 요청보낼 친구가 존재하는지 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 요청 보낼 친구가 영구 정지 상태인지 확인
        if (friend.isSuspended()) {
            throw new MemberException(MemberErrorCode.SUSPENDED);
        }

        // 자기 자신인지 확인
        if (auth.getMember().getId().equals(friendId)) {
            throw new MemberException(MemberErrorCode.SELF_REQUEST);
        }

        // 이미 친구인지 or 친구 요청을 보냈는지 or 상대방이 먼저 요청을 보냈는지 확인
        if (friendRepository.existsByFromMemberAndToMember(friend, auth.getMember())
                || friendRepository.existsByFromMemberAndToMember(auth.getMember(), friend)
        ) {
            throw new MemberException(MemberErrorCode.EXISTS_FRIEND_REQUEST);
        }

        // 친구가 나를 차단했는지 확인
        if (friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(
                friend,
                auth.getMember(),
                FriendState.BLOCKED
        )) {
            throw new MemberException(MemberErrorCode.BLOCKING);
        }

        // 친구 요청 생성
        friendRepository.save(FriendConverter.toFriend(auth.getMember(), friend));

        // 친구 FCM 토큰 조회
        String friendFCM = alertRepository.findByMember(friend)
                .orElseThrow(() -> new MemberException(AlertErrorCode.NOT_FOUND_FCM))
                .getFcmToken();

        // 알림 전송
        try {
            String title = "보카 버디";
            String body = auth.getMember().getNickname() + "님이 친구 요청을 보냈습니다. 확인해주세요 ";
            fcmUtil.sendAlert(title, body, friendFCM);
        } catch (FirebaseMessagingException e) {
            throw new MemberException(AlertErrorCode.FAILED_SEND_ALERT);
        }

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

        // 요청 보낸 사용자가 영구 정지 상태인지 확인
        if (fromMember.isSuspended()) {
            throw new MemberException(MemberErrorCode.SUSPENDED);
        }

        // 존재하는 친구 요청인지 확인
        // fromFriendRequest = (상대 -> 나)
        // toFriendRequest = (나 -> 상대)
        Friend fromFriendRequest = friendRepository.findByFromMemberAndToMember(fromMember, auth.getMember())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND_FRIEND_REQUEST));

        // 친구 요청 상태인지 확인 (WAITING)
        if (!fromFriendRequest.getFriendState().equals(FriendState.WAITING)) {
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

        // 영구 정지 상태인지 확인
        if (member.isSuspended()) {
            throw new MemberException(MemberErrorCode.SUSPENDED);
        }

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
                        FriendState.ACCEPTED.name(),
                        Long.parseLong(cursor),
                        pageRequest);
            } catch (NumberFormatException e) {
                throw new MemberException(GeneralErrorCode.INVADED_CURSOR);
            }
        } else {
            friendList = friendRepository.findFriendListWithoutCursor(
                    auth.getMember().getId(),
                    FriendState.ACCEPTED.name(),
                    pageRequest);
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
        if (!friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(
                auth.getMember(),
                friend,
                FriendState.ACCEPTED
        )) {
            throw new MemberException(MemberErrorCode.NOT_FRIEND);
        }

        // 사용자가 차단했는지 확인
        if (friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(
                friend,
                auth.getMember(),
                FriendState.BLOCKED
        )) {
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
        // 차단할 사용자 확인
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 나 -> 상대방: 관계가 없다면 새로 생성, 있다면 가져오기
        Friend fromMemberFriend = friendRepository.findByFromMemberAndToMember(auth.getMember(), friend)
                .orElseGet(() -> FriendConverter.toFriend(auth.getMember(), friend));

        // 사용자 관계 상태 변경 (나 -> 상대)
        fromMemberFriend.updateState(FriendState.BLOCKED);
        friendRepository.save(fromMemberFriend);

        // 상대방 -> 나: 요청이 존재한다면 거절(차단) 상태로 변경하여 무효화
        friendRepository.findByFromMemberAndToMember(friend, auth.getMember()).ifPresent(toMemberFriend -> {
            toMemberFriend.updateState(FriendState.BLOCKED);
            friendRepository.save(toMemberFriend);
        });

        return MemberConverter.toBlocking(friend);
    }

    // 사용자 신고
    @Transactional
    public MemberResDTO.ReportMember reportMember(
            AuthMember auth,
            Long memberId,
            MemberReqDTO.ReportMember dto
    ) {

        // 신고할 사용자랑 로그인한 사용자가 동일한지 확인
        if (memberId.equals(auth.getMember().getId())){
            throw new MemberException(MemberErrorCode.SELF_REPORT);
        }

        // 신고당할 사용자 조회
        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        List<Report> alreadyReportList = reportRepository.findAllByReportReasonAndTargetMember(dto.reportReason(), targetMember);
        Report report;

        // 이미 같은 유형으로 신고했는지 확인
        if (alreadyReportList.stream().anyMatch(r -> r.
                getReportMember().getId().equals(auth.getMember().getId()))
        ) {
            throw new MemberException(MemberErrorCode.ALREADY_REPORT);
        } else {
            report = reportRepository.save(ReportConverter.toReport(
                    auth.getMember(),
                    targetMember,
                    dto.reportReason(),
                    dto.detailReason())
            );
        }

        return MemberConverter.toReportMember(report);
    }
}
