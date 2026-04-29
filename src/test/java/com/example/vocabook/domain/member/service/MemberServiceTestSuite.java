package com.example.vocabook.domain.member.service;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("MemberService 통합 단위 테스트 스위트")
@SelectClasses({
        MemberServiceProfileSearchTest.class,
        MemberServiceFriendRequestTest.class,
        MemberServiceFriendManagementTest.class
})
public class MemberServiceTestSuite {
    // 이 클래스는 내부 테스트 코드가 없으며, 위에 지정된 클래스들을 한 번에 실행하기 위한 스위트(Suite) 역할만 수행합니다.
}
