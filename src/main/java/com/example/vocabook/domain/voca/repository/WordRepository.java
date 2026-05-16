package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Voca;
import com.example.vocabook.domain.voca.entity.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WordRepository extends JpaRepository<Word, Long> {
	Page<Word> findByVoca(Voca voca, Pageable pageable);
	List<Word> findByVoca(Voca voca);
	List<Word> findByVocaIn(List<Voca> vocas);

	List<Word> findByEnglishWordIn(List<String> englishWords);
	Optional<Word> findFirstByEnglishWord(String englishWord);

    List<Word> findAllByEnglishWordInOrMeaningIn(Collection<String> englishWords, Collection<String> meanings);

    @Query(
            value = "SELECT * " +
                    "FROM word " +
                    "WHERE word_id != :excludeId " +
                    "ORDER BY RAND() " +
                    "LIMIT :limit ",
            nativeQuery = true)
    List<Word> findRandomExcluding(Long excludeId, int limit);
}