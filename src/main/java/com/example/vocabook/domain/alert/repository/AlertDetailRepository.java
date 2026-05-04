package com.example.vocabook.domain.alert.repository;

import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AlertDetailRepository extends JpaRepository<AlertDetail, Long> {
    boolean existsByContentAndAlertAndRepeatAndAlertedAt(String content, Alert alert, Repeat repeat, LocalDateTime alertedAt);
}
