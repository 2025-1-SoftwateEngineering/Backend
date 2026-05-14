package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberItemRepository extends JpaRepository<MemberItem, Long> {
    // 아이템 보유 확인
    Optional<MemberItem> findFirstByMemberAndItem_ItemType(Member member, ItemType itemType);
}
