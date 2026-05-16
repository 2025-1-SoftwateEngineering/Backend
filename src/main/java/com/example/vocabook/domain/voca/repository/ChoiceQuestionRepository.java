package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Choice;
import com.example.vocabook.domain.voca.entity.mapping.ChoiceQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ChoiceQuestionRepository extends JpaRepository<ChoiceQuestion, Long> {

    @Query(
            value = "select q " +
                    "from ChoiceQuestion q " +
                    "where q.choice.id = :choiceId and q.id > :current " +
                    "order by q.id asc " +
                    "limit 1"
    )
    Optional<ChoiceQuestion> findByChoiceWithCurrent(Long choiceId, Long current);

    @Query(
            value = "select q " +
                    "from ChoiceQuestion q " +
                    "where q.id > :choiceQuestionId " +
                    "order by q.id asc " +
                    "limit 1"
    )
    Optional<ChoiceQuestion> findNextQuestion(Long choiceQuestionId);
}
