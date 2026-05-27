package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberActiveProfile;
import com.example.vocabook.domain.store.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberActiveProfileRepository extends JpaRepository<MemberActiveProfile, Long> {

    Optional<MemberActiveProfile> findByMemberAndItem_ItemType(Member member, ItemType itemItemType);

    List<MemberActiveProfile> findAllByMemberAndItem_ItemTypeIn(Member member, Collection<ItemType> itemItemTypes);
}
