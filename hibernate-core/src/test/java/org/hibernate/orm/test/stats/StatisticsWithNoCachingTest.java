/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.stats;

import org.hibernate.cache.internal.NoCachingRegionFactory;
import org.hibernate.cfg.AvailableSettings;

import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.ServiceRegistry;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.hibernate.testing.orm.junit.SettingProvider;
import org.junit.jupiter.api.Test;

/**
 * @author Gail Badner
 */
@DomainModel
@SessionFactory
@ServiceRegistry(
		settingProviders = @SettingProvider(provider = StatisticsWithNoCachingTest.RegionFacrotySettingProvider.class, settingName = AvailableSettings.CACHE_REGION_FACTORY)
)
public class StatisticsWithNoCachingTest {

	public static class RegionFacrotySettingProvider implements SettingProvider.Provider<String> {

		@Override
		public String getSetting() {
			return NoCachingRegionFactory.class.getName();
		}
	}

	@Test
	@JiraKey(value = "HHH-12508")
	public void testUncachedRegion(SessionFactoryScope scope) {
		scope.getSessionFactory().getStatistics().getCacheRegionStatistics( "hibernate.test.unknown" );
	}
}
