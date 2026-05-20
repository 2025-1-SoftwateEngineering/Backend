package com.example.vocabook.domain.voca;

import com.example.vocabook.domain.voca.service.VocaServiceCompleteTestTest;
import com.example.vocabook.domain.voca.service.VocaServiceGetTest;
import com.example.vocabook.domain.voca.service.VocaServiceMemorizeTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Voca 도메인 전체 단위 테스트 스위트")
@SelectClasses({
        VocaServiceGetTest.class,
        VocaServiceMemorizeTest.class,
        VocaServiceCompleteTestTest.class
})
public class VocaTestSuite {
    // 이 클래스는 내부 테스트 코드가 없으며, 위에 지정된 Voca 도메인 클래스들을 한 번에 실행하기 위한 스위트(Suite) 역할만 수행합니다.
}
