package com.example.vocabook.domain.member.entity;

import com.example.vocabook.domain.member.enums.FriendState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Table(name = "friend")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    @Column(name = "friend_state", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FriendState friendState = FriendState.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_id", nullable = false)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_id", nullable = false)
    private Member toMember;

    public void updateState(FriendState friendState) {
        this.friendState = friendState;
    }
}
