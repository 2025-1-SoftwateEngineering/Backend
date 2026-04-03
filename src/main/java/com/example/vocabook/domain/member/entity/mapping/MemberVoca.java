package com.example.vocabook.domain.member.entity.mapping;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.voca.entity.Voca;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "member_voca")
public class MemberVoca {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    @Column(name = "member_voca_id")
    private Long id;

    @Column(name = "learning_word_cnt", nullable = false)
    @Builder.Default
    private Long learningWordCnt = 0L;

    @Column(name = "correct_cnt", nullable = false)
    @Builder.Default
    private Long correctCnt = 0L;

    @Column(name = "solved_at")
    private LocalDateTime solvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voca_id", nullable = false)
    private Voca voca;
}
