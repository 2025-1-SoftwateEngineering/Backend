package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberWord;
import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface MemberWordRepository extends JpaRepository<MemberWord, Long> {

    @Query("SELECT mw.word.id FROM MemberWord mw WHERE mw.member = :member AND mw.word.voca.id = :vocaId")
    List<Long> findWordIdsByMemberAndVocaId(@Param("member") Member member, @Param("vocaId") Long vocaId);

    @Query("SELECT mw.word.id FROM MemberWord mw WHERE mw.member = :member AND mw.word IN :words")
    Set<Long> findWordIdsByMemberAndWordIn(@Param("member") Member member, @Param("words") List<Word> words);

    @Query("SELECT mw.word.voca.id, COUNT(mw) FROM MemberWord mw WHERE mw.member = :member AND mw.word.voca IN :vocas GROUP BY mw.word.voca.id")
    List<Object[]> countByMemberGroupByVoca(@Param("member") Member member, @Param("vocas") List<Voca> vocas);
}
