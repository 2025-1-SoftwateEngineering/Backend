package com.example.vocabook.domain.member.entity.mapping;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.voca.entity.Choice;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member_choice")
public class MemberChoice {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "member_choice_id")
    private Long id;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    @Column(name = "correct_cnt", nullable = false)
    @Builder.Default
    private Long correctCnt = 0L;

    @Column(name = "score", nullable = false)
    @Builder.Default
    private Long score = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;
}
