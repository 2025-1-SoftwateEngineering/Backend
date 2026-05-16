package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Choice;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    @Query(
            value = "select c " +
                    "from Choice c " +
                    "order by c.id "
    )
    Slice<Choice> findChoiceWithoutCursor(PageRequest pageRequest);

    @Query(
            value = "select c " +
                    "from Choice c " +
                    "where c.id < :idCursor " +
                    "order by c.id "
    )
    Slice<Choice> findChoiceWithCursor(Long idCursor, PageRequest pageRequest);

}
