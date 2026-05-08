package com.example.vocabook.domain.alert.service;

import com.example.vocabook.domain.alert.code.AlertErrorCode;
import com.example.vocabook.domain.alert.converter.AlertConverter;
import com.example.vocabook.domain.alert.dto.AlertReqDTO;
import com.example.vocabook.domain.alert.dto.AlertResDTO;
import com.example.vocabook.domain.alert.entity.Alert;
import com.example.vocabook.domain.alert.entity.AlertDetail;
import com.example.vocabook.domain.alert.enums.Repeat;
import com.example.vocabook.domain.alert.exception.AlertException;
import com.example.vocabook.domain.alert.repository.AlertDetailRepository;
import com.example.vocabook.domain.alert.repository.AlertRepository;
import com.example.vocabook.global.apiPayload.code.GeneralErrorCode;
import com.example.vocabook.global.apiPayload.converter.PagingConverter;
import com.example.vocabook.global.apiPayload.dto.PagingResDTO;
import com.example.vocabook.global.security.entity.AuthMember;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;
    private final AlertDetailRepository alertDetailRepository;
    private final AlertScheduleService alertScheduleService;

    // FCM 토큰 등록
    @Transactional
    public AlertResDTO.RegisterFcm registerFcm(
            AuthMember auth,
            AlertReqDTO.RegisterFcm dto
    ) {
        Optional<Alert> optionalAlert = alertRepository.findByMember(auth.getMember());
        Alert alert;

        if (optionalAlert.isEmpty()) {
            alert = alertRepository.save(AlertConverter.toAlert(dto.fcmToken(), auth.getMember()));
        } else {
            alert = optionalAlert.get();

            alert.updateFcmToken(dto.fcmToken());
        }

        return AlertConverter.toRegisterFcm(alert);
    }

    // 알림 커스텀
    @Transactional
    public AlertResDTO.CustomAlert customAlert(
            AuthMember auth,
            AlertReqDTO.CustomAlert dto
    ) {
        // 알림 가져오기
        Alert alert = alertRepository.findByMember(auth.getMember())
                .orElseThrow(() -> new AlertException(AlertErrorCode.NOT_FOUND_FCM));

        // 완벽히 동일한 알림이 있는지 확인
        if (alertDetailRepository
                .existsByContentAndAlertAndRepeatAndAlertedAt(
                        dto.message(),
                        alert,
                        dto.repeat(),
                        dto.alertedAt().atZoneSameInstant(java.time.ZoneId.of("Asia/Seoul")).toLocalDateTime()
                )
        ){
            throw new AlertException(AlertErrorCode.ALREADY_SET_ALERT);
        }

        // 알림 일시가 과거인지 확인
        if (dto.alertedAt().isBefore(OffsetDateTime.now()) && dto.repeat().equals(Repeat.NONE)){
            throw new AlertException(AlertErrorCode.INVADED_ALERT_TIME);
        }

        // 알림 제작 & 저장
        AlertDetail alertDetail = AlertConverter.toAlertDetail(alert, dto);
        alertDetailRepository.save(alertDetail);

        // 스케쥴러 등록
        try {
            alertScheduleService.schedule(alertDetail);
        } catch (SchedulerException e) {
            throw new AlertException(AlertErrorCode.FAILED_SET_ALERT);
        }

        return AlertConverter.toCustomAlert(alertDetail);
    }

    // 알림 삭제
    @Transactional
    public AlertResDTO.DeleteAlert deleteAlert(
            AuthMember auth,
            Long alertId
    ) {
        AlertDetail alertDetail = alertDetailRepository.findById(alertId)
                .orElseThrow(() -> new AlertException(AlertErrorCode.NOT_FOUND));

        // 알림 설정한 사용자와 일치하는가 확인 (N+1 문제)
        if (!alertDetail.getAlert().getMember().getId().equals(auth.getMember().getId())){
            throw new AlertException(AlertErrorCode.MISMATCH_USER);
        }

        alertDetailRepository.delete(alertDetail);

        return AlertConverter.toDeleteAlert(alertDetail);
    }

    // 알림 리스트 조회
    public PagingResDTO.Cursor<AlertResDTO.AlertList> getAlertList(
            AuthMember auth,
            String cursor,
            Integer pageSize
    ) {
        PageRequest pageRequest = PageRequest.ofSize(pageSize);

        Slice<AlertDetail> alertDetailList;
        if (!cursor.equals("-1")){
            Long idCursor;
            try {
                idCursor = Long.parseLong(cursor);
            } catch (NumberFormatException e) {
                throw new AlertException(GeneralErrorCode.INVADED_CURSOR);
            }

            alertDetailList = alertDetailRepository.findAlertListWithCursor(
                    auth.getMember(),
                    idCursor,
                    pageRequest
            );
        } else {
            alertDetailList = alertDetailRepository.findAlertListWithoutCursor(
                    auth.getMember(),
                    pageRequest
            );
        }

        // 만약 알림 리스트가 없는 경우
        if (alertDetailList.isEmpty()){
            return PagingConverter.toCursor(null, null, false, 0);
        }

        // 다음 커서 제작
        String nextCursor = alertDetailList.getContent().getLast().getId().toString();

        return PagingConverter.toCursor(
                alertDetailList.getContent().stream()
                        .map(AlertConverter::toAlertList)
                        .toList(),
                nextCursor,
                alertDetailList.hasNext(),
                alertDetailList.getNumberOfElements()
        );
    }
}
