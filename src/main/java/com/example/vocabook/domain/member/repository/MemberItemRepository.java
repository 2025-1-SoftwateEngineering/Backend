package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberItem;
import com.example.vocabook.domain.store.entity.Item;
import com.example.vocabook.domain.store.enums.ItemType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberItemRepository extends JpaRepository<MemberItem, Long> {

	Optional<MemberItem> findByMemberAndItem(Member member, Item item);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT mi FROM MemberItem mi WHERE mi.member = :member AND mi.item = :item")
	Optional<MemberItem> findByMemberAndItemWithLock(@Param("member") Member member, @Param("item") Item item);

	Optional<MemberItem> findByMemberAndItem_ItemType(Member member, ItemType itemType);

	@EntityGraph(attributePaths = {"item"})
	List<MemberItem> findByMember(Member member);

	long countByMemberAndItem_ItemType(Member member, ItemType itemType);
}
