package com.example.vocabook.domain.alert;

import com.example.vocabook.domain.alert.controller.AlertControllerTest;
import com.example.vocabook.domain.alert.service.AlertServiceCustomAlertTest;
import com.example.vocabook.domain.alert.service.AlertServiceDeleteAlertTest;
import com.example.vocabook.domain.alert.service.AlertServiceGetAlertListTest;
import com.example.vocabook.domain.alert.service.AlertServiceRegisterFcmTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Alert 도메인 전체 단위 테스트 스위트")
@SelectClasses({
        AlertControllerTest.class,
        AlertServiceRegisterFcmTest.class,
        AlertServiceCustomAlertTest.class,
        AlertServiceGetAlertListTest.class,
        AlertServiceDeleteAlertTest.class
})
public class AlertTestSuite {
    // 이 클래스는 내부 테스트 코드가 없으며, 위에 지정된 Alert 도메인 클래스들을 한 번에 실행하기 위한 스위트(Suite) 역할만 수행합니다.
}
