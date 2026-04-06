package com.example.vocabook.domain.member.entity;

import com.example.vocabook.domain.member.enums.Authorize;
import com.example.vocabook.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

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

    @Column(name = "refresh_token", columnDefinition = "text", nullable = false)
    private String refreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public void updateLoginAt() {
        this.loginAt = LocalDateTime.now();
    }
}
