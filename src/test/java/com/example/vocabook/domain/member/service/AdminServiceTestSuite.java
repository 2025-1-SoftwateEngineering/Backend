package com.example.vocabook.domain.member.service;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AdminServiceSuccessTest.class,
        AdminServiceExceptionTest.class
})
public class AdminServiceTestSuite {
    // AdminService 관련 정상 및 예외 테스트를 한 번에 실행하는 Suite 클래스입니다.
}
