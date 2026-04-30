package com.example.vocabook.domain.member.repository;

import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.FriendState;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query(
            value = "SELECT * " +
                    "FROM friend " +
                    "WHERE (to_id = :toId AND friend_state = :friendState) AND friend_id < :cursor " +
                    "ORDER BY friend_id DESC ",
            nativeQuery = true
    )
    Slice<Friend> findFriendRequestListWithCursor(Long toId, String friendState, Long cursor, PageRequest pageRequest);

    @Query(
            value = "SELECT * " +
                    "FROM friend " +
                    "WHERE to_id = :toId AND friend_state = :friendState " +
                    "ORDER BY friend_id DESC ",
            nativeQuery = true
    )
    Slice<Friend> findFriendRequestListWithoutCursor(Long toId, String friendState, PageRequest pageRequest);

    boolean existsByFromMemberAndToMember(Member member, Member friend);

    boolean existsByFromMemberAndToMemberAndFriendStateIs(Member friend, Member member, FriendState friendState);

    Optional<Friend> findByFromMemberAndToMember(Member fromMember, Member member);

    @Query(
            value = "SELECT * " +
                    "FROM friend " +
                    "WHERE (from_id = :fromId AND friend_state = :friendState) AND friend_id < :cursor " +
                    "ORDER BY friend_id DESC ",
            nativeQuery = true
    )
    Slice<Friend> findFriendListWithCursor(Long fromId, String friendState, Long cursor, PageRequest pageRequest);

    @Query(
            value = "SELECT * " +
                    "FROM friend " +
                    "WHERE from_id = :fromId AND friend_state = :friendState " +
                    "ORDER BY friend_id DESC ",
            nativeQuery = true
    )
    Slice<Friend> findFriendListWithoutCursor(Long fromId, String friendState, PageRequest pageRequest);

}
