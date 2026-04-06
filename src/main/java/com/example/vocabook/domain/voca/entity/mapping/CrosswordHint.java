package com.example.vocabook.domain.voca.entity.mapping;

import com.example.vocabook.domain.voca.entity.Crossword;
import com.example.vocabook.domain.voca.entity.Word;
import com.example.vocabook.domain.voca.enums.ClueType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "crossword_hint")
public class CrosswordHint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crossword_hint_id")
    private Long id;

    @Column(name = "clue_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ClueType clueType;

    @Column(name = "clue_content", nullable = false)
    private String clueContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crossword_id", nullable = false)
    private Crossword crossword;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;
}
