package com.example.vocabook.domain.store;

import com.example.vocabook.domain.store.service.ChoiceTimeBonusStrategyTest;
import com.example.vocabook.domain.store.service.CrosswordHintStrategyTest;
import com.example.vocabook.domain.store.service.PetFoodStrategyTest;
import com.example.vocabook.domain.store.service.PetWaterStrategyTest;
import com.example.vocabook.domain.store.service.ProfileDecorationStrategyTest;
import com.example.vocabook.domain.store.service.StoreServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Store 도메인 전체 단위 테스트 스위트")
@SelectClasses({
		StoreServiceTest.class,
		ChoiceTimeBonusStrategyTest.class,
		CrosswordHintStrategyTest.class,
		PetFoodStrategyTest.class,
		PetWaterStrategyTest.class,
		ProfileDecorationStrategyTest.class
})
public class StoreTestSuite {
}
