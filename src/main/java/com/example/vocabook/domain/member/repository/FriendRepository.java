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
    Slice<Friend> findFriendRequestListWithCursor(Long toId, FriendState friendState, Long cursor, PageRequest pageRequest);

    @Query(
            value = "SELECT * " +
                    "FROM friend " +
                    "WHERE to_id = :toId AND friend_state = :friendState " +
                    "ORDER BY friend_id DESC ",
            nativeQuery = true
    )
    Slice<Friend> findFriendRequestListWithoutCursor(Long toId, FriendState friendState, PageRequest pageRequest);

    boolean existsByFromMemberAndToMember(Member member, Member friend);

    boolean existsByFromMemberAndToMemberAndFriendStateIs(Member friend, Member member, FriendState friendState);

    Optional<Friend> findByFromMemberAndToMember(Member fromMember, Member member);

    @Query(
            value = "SELECT f " +
                    "FROM Friend f " +
                    "WHERE (f.fromMember.id = :fromId AND f.friendState = :friendState) AND f.id < :cursor AND f.toMember.isSuspended is false " +
                    "ORDER BY f.id DESC "
    )
    Slice<Friend> findFriendListWithCursor(Long fromId, FriendState friendState, Long cursor, PageRequest pageRequest);

    @Query(
            value = "SELECT f " +
                    "FROM Friend f " +
                    "WHERE f.fromMember.id = :fromId AND f.friendState = :friendState AND f.toMember.isSuspended is false " +
                    "ORDER BY f.id DESC "
    )
    Slice<Friend> findFriendListWithoutCursor(Long fromId, FriendState friendState, PageRequest pageRequest);

    Optional<Friend> findByFromMemberAndToMemberAndFriendState(Member fromMember, Member toMember, FriendState friendState);
}
