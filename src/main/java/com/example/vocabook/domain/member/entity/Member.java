package com.example.vocabook.domain.member.entity;

import com.example.vocabook.domain.member.code.MemberErrorCode;
import com.example.vocabook.domain.member.enums.Authorize;
import com.example.vocabook.domain.member.exception.MemberException;
import com.example.vocabook.domain.store.enums.ItemType;
import com.example.vocabook.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member")
@SQLDelete(sql = "UPDATE member SET deleted_at = now() WHERE member_id = ?")
@SQLRestriction(value = "deleted_at is null")
public class Member extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long id;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "nickname", nullable = false)
	private String nickname;

	@Column(name = "authorize", nullable = false)
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Authorize authorize = Authorize.ROLE_USER;

	@Column(name = "login_at", nullable = false)
	@Builder.Default
	private LocalDateTime loginAt = LocalDateTime.now();

	@Column(name = "streak", nullable = false)
	@Builder.Default
	private Long streak = 0L;

	@Column(name = "coin", nullable = false)
	@Builder.Default
	private Long coin = 0L;

	@Column(name = "last_studied_at")
	private LocalDate lastStudiedAt;

	@Column(name = "total_study_days", nullable = false)
	@Builder.Default
	private Long totalStudyDays = 0L;

	@Column(name = "refresh_token", columnDefinition = "text", nullable = false)
	private String refreshToken;

	@Column(name = "is_suspended", nullable = false)
	@Builder.Default
	private boolean isSuspended = false;

	@Column(name = "choice_higher", nullable = false)
	@Builder.Default
	private Long choiceHigher = 0L;

	@Column(name = "crossword_higher")
    @Builder.Default
	private Duration crosswordHigher = Duration.ZERO;

	public void updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void updateLoginAt() {
		this.loginAt = LocalDateTime.now();
	}

	public void addCoin(long amount) {
		this.coin += amount;
	}

	public void spendCoin(long amount) {
		if (this.coin < amount) {
			throw new MemberException(MemberErrorCode.NOT_ENOUGH_COIN);
		}
		this.coin -= amount;
	}

	public void suspend() {
		this.isSuspended = true;
		this.nickname = "정지 당한 사용자 " + UUID.randomUUID();
		this.streak = 0L;
		this.coin = 0L;
		this.refreshToken = "";
	}

	public void updateStreak(LocalDate today) {
		this.streak += 1;
		this.totalStudyDays += 1;
		this.lastStudiedAt = today;
	}

	public void resetStreak() {
		this.streak = 0L;
	}

	public void updateChoiceHigher(Long higher) {
		this.choiceHigher = higher;
	}

	public void updateCrosswordHigher(Duration higher) {
		this.crosswordHigher = higher;
	}

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateEmail(String email) {
        this.email = email;
    }
}
