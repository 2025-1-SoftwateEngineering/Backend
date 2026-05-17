package com.example.vocabook.domain.voca.repository;

import com.example.vocabook.domain.voca.entity.Crossword;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CrosswordRepository extends JpaRepository<Crossword, Long> {

    @Query(
            value = "select c " +
                    "from Crossword c " +
                    "order by c.id desc"
    )
    Slice<Crossword> findCrosswordWithoutCursor(PageRequest pageRequest);

    @Query(
            value = "select c " +
                    "from Crossword c " +
                    "where c.id < :idCursor " +
                    "order by c.id desc"
    )
    Slice<Crossword> findCrosswordWithCursor(Long idCursor, PageRequest pageRequest);
}
