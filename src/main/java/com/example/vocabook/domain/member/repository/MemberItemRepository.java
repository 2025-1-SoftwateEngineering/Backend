package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberItemRepository extends JpaRepository<MemberItem, Long> {
    Optional<MemberItem> findFirstByMemberAndItem_ItemType(Member member, ItemType itemType);

    @EntityGraph(attributePaths = {"item"})
    List<MemberItem> findByMember(Member member);

    @EntityGraph(attributePaths = {"item"})
    Optional<MemberItem> findWithItemById(Long id);

    Optional<MemberItem> findFirstByMemberAndItem(Member member, Item item);
    long countByMemberAndItem(Member member, Item item);
    long countByMemberAndItem_ItemType(Member member, ItemType itemType);
}
