package com.example.vocabook.domain.member.service;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.dto.req.MemberReqDTO;
import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.FriendState;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.member.repository.FriendRepository;
import com.example.vocabook.domain.member.repository.MemberRepository;
import com.example.vocabook.domain.member.repository.MemberVocaRepository;
import com.example.vocabook.global.security.entity.AuthMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 프로필 및 검색 테스트")
class MemberServiceProfileSearchTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private FriendRepository friendRepository;

	@Mock
	private MemberVocaRepository memberVocaRepository;

	@InjectMocks
	private MemberService memberService;

	private AuthMember authMember;
	private Member myMember;

	@BeforeEach
	void setUp() {
		myMember = Member.builder()
				.id(1L)
				.email("me@example.com")
				.build();
		authMember = new AuthMember(myMember);
	}

	@Test
	@DisplayName("사용자 단순 조회 실패 - 이메일에 해당하는 사용자 없음 (NOT_FOUND)")
	void searchMember_Fail_NotFound() {
		// given
		String email = "unknown@example.com";
		given(memberRepository.findByEmail(email)).willReturn(Optional.empty());

		MemberReqDTO.SearchMember dto = new MemberReqDTO.SearchMember(email);

		// when
		MemberException exception = assertThrows(MemberException.class, () ->
				memberService.searchMember(dto)
		);

		// then
		assertEquals(MemberErrorCode.NOT_FOUND, exception.getCode());
	}

	@Test
	@DisplayName("내 프로필 조회 성공 - totalStudyDays 반환 검증")
	void getMyProfile_Success_ReturnsTotalStudyDays() {
		// given
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		for (int i = 0; i < 5; i++) myMember.updateStreak(today);

		// when
		MemberResDTO.MyProfile result = memberService.getMyProfile(authMember);

		// then
		assertEquals(5L, result.totalStudyDays());
	}

	@Test
	@DisplayName("친구 프로필 조회 성공 - totalStudyDays 반환 검증")
	void getFriendProfile_Success_ReturnsTotalStudyDays() {
		// given
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Member friend = Member.builder()
				.id(2L).email("friend@example.com").nickname("친구")
				.password("pw").refreshToken("rt").build();
		for (int i = 0; i < 10; i++) friend.updateStreak(today);

		given(memberRepository.findById(2L)).willReturn(Optional.of(friend));
		given(friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(
				myMember, friend, FriendState.ACCEPTED)).willReturn(true);
		given(friendRepository.existsByFromMemberAndToMemberAndFriendStateIs(
				friend, myMember, FriendState.BLOCKED)).willReturn(false);
		given(memberVocaRepository.findAllByMember(friend)).willReturn(List.of());

		// when
		MemberResDTO.FriendProfile result = memberService.getFriendProfile(authMember, 2L);

		// then
		assertEquals(10L, result.totalStudyDays());
	}

	@Test
	@DisplayName("updateStreak - totalStudyDays +1, resetStreak 후에도 유지")
	void updateStreak_IncrementsTotalStudyDays() {
		// given
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Member m = Member.builder().id(99L).email("e@e.com").nickname("n").password("p").refreshToken("r").build();

		// when 1
		m.updateStreak(today);

		// then 1
		assertEquals(1L, m.getStreak());
		assertEquals(1L, m.getTotalStudyDays());
		assertEquals(today, m.getLastStudiedAt());

		// when 2
		m.resetStreak();

		// then 2
		assertEquals(0L, m.getStreak());
		assertEquals(1L, m.getTotalStudyDays()); // 유지
	}

	@Test
	@DisplayName("updateStreak - 리셋 후 재학습 시 totalStudyDays 누적 유지")
	void updateStreak_AfterReset_TotalStudyDaysAccumulates() {
		// given
		LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
		Member m = Member.builder().id(99L).email("e@e.com").nickname("n").password("p").refreshToken("r").build();

		// when
		for (int i = 0; i < 3; i++) m.updateStreak(today);
		m.resetStreak();
		m.updateStreak(today);

		// then
		assertEquals(1L, m.getStreak());
		assertEquals(4L, m.getTotalStudyDays());
	}

	@Test
	@DisplayName("updateStreak - 파라미터 today가 lastStudiedAt에 그대로 반영됨 (자정 버그 방지)")
	void updateStreak_AroundMidnight_LastStudiedAtMatchesToday() {
		// given
		LocalDate fixedDate = LocalDate.of(2026, 5, 25);
		Member m = Member.builder().id(99L).email("e@e.com").nickname("n").password("p").refreshToken("r").build();

		// when
		m.updateStreak(fixedDate);

		// then
		assertEquals(fixedDate, m.getLastStudiedAt());
	}
}
