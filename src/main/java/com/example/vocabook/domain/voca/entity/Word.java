package com.example.vocabook.domain.voca.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "word")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Long id;

    @Column(name = "english_word", nullable = false)
    private String englishWord;

    @Column(name = "meaning", nullable = false)
    private String meaning;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voca_id")
    private Voca voca;

    public void update(String englishWord, String meaning, Voca voca) {
        if (englishWord != null) {
            this.englishWord = englishWord;
        }
        if (meaning != null) {
            this.meaning = meaning;
        }
        this.voca = voca;
    }

    public void delete() {
        this.voca = null;
    }
}
