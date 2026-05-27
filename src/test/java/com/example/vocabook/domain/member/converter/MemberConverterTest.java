package com.example.vocabook.domain.member.converter;

import com.example.vocabook.domain.member.dto.req.AuthReqDTO;
import com.example.vocabook.domain.member.entity.Member;
import com.example.vocabook.domain.member.entity.mapping.MemberActiveProfile;
import com.example.vocabook.domain.store.entity.Item;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MemberConverterTest {

    @Test
    @DisplayName("AuthReqDTO.SignUp을 Member로 변환 성공")
    void toMember_Success() {
        // given
        AuthReqDTO.SignUp dto = new AuthReqDTO.SignUp("Tester", "test@example.com", "password");
        String encodedPassword = "encodedPassword";

        // when
        Member member = MemberConverter.toMember(dto, encodedPassword);

        // then
        assertThat(member).isNotNull();
        assertThat(member.getEmail()).isEqualTo("test@example.com");
        assertThat(member.getPassword()).isEqualTo("encodedPassword");
        assertThat(member.getNickname()).isEqualTo("Tester");
    }

    @Test
    @DisplayName("Member와 Item을 MemberActiveProfile로 변환 성공")
    void toMemberActiveProfile_Success() {
        // given
        Member member = Member.builder().id(1L).email("test@example.com").build();
        Item item = Item.builder().id(1L).name("아이템").build();

        // when
        MemberActiveProfile profile = MemberConverter.toMemberActiveProfile(member, item);

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getMember().getId()).isEqualTo(1L);
        assertThat(profile.getItem().getId()).isEqualTo(1L);
    }
}
