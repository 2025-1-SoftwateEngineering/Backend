package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.Report;
import com.example.vocabook.domain.member.entity.dto.ReportDTO;
import com.example.vocabook.domain.member.enums.ReportReason;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findAllByTargetMember(Member targetMember);

    List<Report> findAllByReportReasonAndTargetMember(ReportReason reportReason, Member targetMember);


    @Query("SELECT new com.example.vocabook.domain.member.entity.dto.ReportDTO$ReportCnt(" +
           "r, (SELECT COUNT(r2) FROM Report r2 WHERE r2.targetMember = r.targetMember)) " +
           "FROM Report r " +
           "ORDER BY (SELECT COUNT(r2) FROM Report r2 WHERE r2.targetMember = r.targetMember) DESC, r.id DESC")
    Slice<ReportDTO.ReportCnt> findReportListWithoutCursor(Pageable pageable);

    @Query("SELECT new com.example.vocabook.domain.member.entity.dto.ReportDTO$ReportCnt(" +
           "r, (SELECT COUNT(r2) FROM Report r2 WHERE r2.targetMember = r.targetMember)) " +
           "FROM Report r " +
           "WHERE (SELECT COUNT(r2) FROM Report r2 WHERE r2.targetMember = r.targetMember) < :countCursor OR " +
           "((SELECT COUNT(r2) FROM Report r2 WHERE r2.targetMember = r.targetMember) = :countCursor AND r.id < :idCursor) " +
           "ORDER BY (SELECT COUNT(r2) FROM Report r2 WHERE r2.targetMember = r.targetMember) DESC, r.id DESC")
    Slice<ReportDTO.ReportCnt> findReportListWithCursor(Long countCursor, Long idCursor, Pageable pageable);
}
