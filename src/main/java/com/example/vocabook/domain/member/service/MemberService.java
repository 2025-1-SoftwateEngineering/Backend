package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.AuthErrorCode;
import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.converter.FriendConverter;
import com.example.vocabook.domain.member.converter.MemberConverter;
import com.example.vocabook.domain.member.converter.ReportConverter;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.entity.mapping.MemberVoca;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.enums.PhotoType;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.*;
import com.example.vocabook.domain.store.code.StoreErrorCode;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.domain.store.exception.StoreException;
import com.example.vocabook.domain.store.repository.ItemRepository;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.apiPayload.converter.PagingConverter;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import com.example.vocabook.global.util.GcsUtil;
import com.google.cloud.storage.Blob;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FriendRepository friendRepository;
    private final ReportRepository reportRepository;
    private final GcsUtil gcsUtil;
    private final ItemRepository itemRepository;
    private final MemberAlertService memberAlertService;
    private final MemberVocaRepository memberVocaRepository;
    private final PasswordEncoder passwordEncoder;

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
                        FriendState.WAITING,
                        Long.parseLong(cursor),
                        pageRequest
                );
            } catch (NumberFormatException e) {
                throw new MemberException(GeneralErrorCode.INVADED_CURSOR);
            }
        } else {
            friendList = friendRepository.findFriendRequestListWithoutCursor(
                    auth.getMember().getId(),
                    FriendState.WAITING,
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

        // 내(auth)가 친구에게 보낸 관계와 친구가 내게 보낸 관계 확인
        java.util.Optional<Friend> myToFriend = friendRepository.findByFromMemberAndToMember(auth.getMember(), friend);
        java.util.Optional<Friend> friendToMe = friendRepository.findByFromMemberAndToMember(friend, auth.getMember());

        // 차단 상태인지 확인 (내가 차단했거나, 상대가 차단했거나)
        if (myToFriend.map(Friend::getFriendState).orElse(null) == FriendState.BLOCKED ||
            friendToMe.map(Friend::getFriendState).orElse(null) == FriendState.BLOCKED) {
            throw new MemberException(MemberErrorCode.BLOCKING);
        }

        // 이미 친구이거나 요청 중인 상태인지 확인 (REJECTED 제외)
        if (myToFriend.map(Friend::getFriendState).orElse(null) == FriendState.ACCEPTED ||
            myToFriend.map(Friend::getFriendState).orElse(null) == FriendState.WAITING ||
            friendToMe.map(Friend::getFriendState).orElse(null) == FriendState.ACCEPTED ||
            friendToMe.map(Friend::getFriendState).orElse(null) == FriendState.WAITING) {
            throw new MemberException(MemberErrorCode.EXISTS_FRIEND_REQUEST);
        }

        // 상대방이 나를 거절했던 내역(REJECTED)이 있다면 삭제하여 양방향 리셋
        friendToMe.ifPresent(friendRepository::delete);

        // 내 요청이 기존에 REJECTED 등으로 남아있다면 상태만 WAITING으로 업데이트
        if (myToFriend.isPresent()) {
            myToFriend.get().updateState(FriendState.WAITING);
            friendRepository.save(myToFriend.get());
        } else {
            // 친구 요청 생성 (새 요청)
            friendRepository.save(FriendConverter.toFriend(auth.getMember(), friend));
        }

        // 알림 전송 비동기 처리
        memberAlertService.sendFriendRequestAlert(auth, friend);

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
                        FriendState.ACCEPTED,
                        Long.parseLong(cursor),
                        pageRequest);
            } catch (NumberFormatException e) {
                throw new MemberException(GeneralErrorCode.INVADED_CURSOR);
            }
        } else {
            friendList = friendRepository.findFriendListWithoutCursor(
                    auth.getMember().getId(),
                    FriendState.ACCEPTED,
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

        // 친구 학습한 단어 개수 카운트
        List<MemberVoca> friendVoca = memberVocaRepository.findAllByMember(friend);

        Integer totalWordLearned = 0;
        for (MemberVoca i : friendVoca){
            totalWordLearned += i.getLearningWordCnt().intValue();
        }

        return MemberConverter.toFriendProfile(friend, totalWordLearned);
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

    // 사진 업로드용 URL 발급
    public MemberResDTO.CreateSignedUri createSignedUri(
            AuthMember auth,
            String fileName,
            PhotoType photoType
    ) {
        // 확장자가 있는지 검증
        if (fileName.lastIndexOf(".") == -1) {
            throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
        }

        // 확장자 추출
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

        // UUID로 파일명 다시 조립
        String uuid = java.util.UUID.randomUUID().toString();

        // 권한 & 사진 타입에 따라 프리픽스 변경
        String prefix;
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a
                        .getAuthority().equals("ROLE_ADMIN"))
        ){
             prefix = switch (photoType) {
                case PROFILE -> "profile/";
                case ITEM -> "item/";
                case BACKGROUND -> "background/";
            };

        } else {
            if (!photoType.equals(PhotoType.PROFILE)) {
                throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
            }

            prefix = "profile/";
        }

        String url = gcsUtil.createSignedUrl(uuid+'.'+extension, prefix);

        return MemberConverter.toCreateSignedUri(uuid+"."+extension, url, photoType);
    }

    // 사진 업로드 완료
    @Transactional
    public MemberResDTO.UploadImage uploadImage(
            AuthMember auth,
            String fileName,
            PhotoType photoType,
            Long targetId
    ) {
        // 프로필 변경만 targetId NULL
        if (!photoType.equals(PhotoType.PROFILE) && targetId == null) {
            throw new MemberException(MemberErrorCode.NOT_NULL_TARGET_ID);
        }

        // 권한 & 사진 타입에 따라 프리픽스 변경
        String prefix;
        if (auth.getAuthorities().stream()
                .anyMatch(a -> a.
                        getAuthority().equals("ROLE_ADMIN"))
        ){
             prefix = switch (photoType) {
                case PROFILE -> "profile/";
                case ITEM -> "item/";
                case BACKGROUND -> "background/";
            };
        } else {
            if (!photoType.equals(PhotoType.PROFILE)) {
                throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
            }

            prefix = "profile/";
        }

        // GCS에서 객체 찾기
        Blob object = gcsUtil.findObject(fileName, prefix);

        if (object == null) {
            throw new MemberException(MemberErrorCode.NOT_UPLOAD_PROFILE);
        }

        // 공개 URL 제작
        String publicUrl = "https://storage.googleapis.com/" +
                object.getBlobId().getBucket() +
                "/"+object.getBlobId().getName();

        // 사진 타입에 따라 행동
        switch (photoType) {
            case PROFILE -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
                }

                Member member = memberRepository.findById(auth.getMember().getId())
                        .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

                // 기존 사진 객체 삭제
                if (!member.getProfileUrl().isBlank() && !member.getProfileUrl().contains("default-profile")) {
                    String oldObjectName = member.getProfileUrl().substring(member.getProfileUrl().lastIndexOf("/")+1);
                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);
                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                member.updateProfileUrl(publicUrl);
            }

            case ITEM -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
                }

                Item item = itemRepository.findById(targetId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

                // 기존 사진 객체 삭제
                if (!item.getImageUrl().isBlank()) {
                    String oldObjectName = item.getImageUrl().substring(item.getImageUrl().lastIndexOf("/")+1);
                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);
                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                item.updateImageUrl(publicUrl);
            }
            case BACKGROUND -> {
                // 프리픽스로 검증
                if (!object.getBlobId().getName().startsWith(prefix)) {
                    throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
                }

                Item item = itemRepository.findById(targetId)
                        .orElseThrow(() -> new StoreException(StoreErrorCode.ITEM_NOT_FOUND));

                // 해당 아이템 종류가 배경화면인지
                if (!item.getItemType().equals(ItemType.BACKGROUND)) {
                    throw new MemberException(MemberErrorCode.INVADE_PHOTO_TYPE);
                }

                // 기존 사진 객체 삭제
                if (!item.getImageUrl().isBlank()) {
                    String oldObjectName = item.getImageUrl().substring(item.getImageUrl().lastIndexOf("/")+1);
                    Blob oldObject = gcsUtil.findObject(oldObjectName, prefix);
                    if (oldObject != null) {
                        oldObject.delete();
                    }
                }

                item.updateImageUrl(publicUrl);
            }
        }

        return MemberConverter.toUploadImage(object, publicUrl);
    }

    // 프로필 수정
    @Transactional
    public MemberResDTO.UpdateProfile updateProfile(
            AuthMember auth,
            MemberReqDTO.UpdateProfile dto
    ) {
        Member member = memberRepository.findById(auth.getMember().getId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(dto.confirmPassword(), member.getPassword())){
            throw new MemberException(AuthErrorCode.WRONG_PASSWORD);
        }

        // 변경할 부분만 변경
        if (dto.nickname() != null && !dto.nickname().isBlank()){
            member.updateNickname(dto.nickname());
        }

        if (dto.email() != null && !dto.email().isBlank()){
            member.updateEmail(dto.email());

            // 이메일 변경 시 기존 Refresh Token 만료처리 (UUID로 예측 못하게 처리)
            member.updateRefreshToken(UUID.randomUUID().toString());
        }

        return MemberConverter.toUpdateProfile(member);
    }

    // 친구 삭제
    @Transactional
    public MemberResDTO.DeleteFriend deleteFriend(
            AuthMember auth,
            Long friendId
    ) {
        // 친구 조회
        Member friend = memberRepository.findById(friendId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        // 내(auth)가 보낸 관계 찾기 (상태 무관하게 조회)
        Friend friendship = friendRepository
                .findByFromMemberAndToMember(auth.getMember(), friend)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FRIEND));

        // 차단 상태면 삭제 불가 (차단 해제는 별도 처리)
        if (friendship.getFriendState() == FriendState.BLOCKED) {
            throw new MemberException(MemberErrorCode.NOT_FRIEND);
        }

        List<Friend> friendList = new ArrayList<>();
        friendList.add(friendship);

        // 상대방이 나에게 보낸 관계 찾기 (차단 상태 제외하고 같이 삭제)
        friendRepository
                .findByFromMemberAndToMember(friend, auth.getMember())
                .filter(f -> f.getFriendState() != FriendState.BLOCKED)
                .ifPresent(friendList::add);

        // 친구 삭제
        friendRepository.deleteAll(friendList);

        return MemberConverter.toDeleteFriend(friend);
    }
}
