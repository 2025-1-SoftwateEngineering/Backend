package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.res.MemberResDTO;
import com.example.vocabook.domain.member.entity.Friend;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.enums.FriendState;

public class FriendConverter {

    public static Friend toFriend(
            Member member,
            Member friend
    ) {
        return Friend.builder()
                .fromMember(member)
                .toMember(friend)
                .build();
    }

    public static Friend toFriend(
            Member member,
            Member friend,
            FriendState state
    ) {
        return Friend.builder()
                .fromMember(member)
                .toMember(friend)
                .friendState(state)
                .build();
    }

    public static MemberResDTO.SendFriendRequest toFriendRequest(
            Member friend
    ) {
        return MemberResDTO.SendFriendRequest.builder()
                .id(friend.getId())
                .nickname(friend.getNickname())
                .build();
    }
}
