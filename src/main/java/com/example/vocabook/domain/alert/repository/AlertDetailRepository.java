package com.example.vocabook.domain.alert.repository;

import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface AlertDetailRepository extends JpaRepository<AlertDetail, Long> {
    boolean existsByContentAndAlertAndRepeatAndAlertedAt(String content, Alert alert, Repeat repeat, LocalDateTime alertedAt);

    @Query(
            value = "SELECT ad " +
                    "FROM AlertDetail ad " +
                    "LEFT JOIN ad.alert a " +
                    "WHERE a.member = :member AND ad.id < :cursor " +
                    "ORDER BY ad.id DESC "
    )
    Slice<AlertDetail> findAlertListWithCursor(Member member, Long cursor, Pageable pageRequest);

    @Query(
            value = "SELECT ad " +
                    "FROM AlertDetail ad " +
                    "LEFT JOIN ad.alert a " +
                    "WHERE a.member = :member " +
                    "ORDER BY ad.id DESC "
    )
    Slice<AlertDetail> findAlertListWithoutCursor(Member member, Pageable pageRequest);
}
