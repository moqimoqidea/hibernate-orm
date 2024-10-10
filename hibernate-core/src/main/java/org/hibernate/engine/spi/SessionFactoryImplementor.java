/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.engine.spi;

import java.util.Collection;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.spi.CacheImplementor;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.profile.FetchProfile;
import org.hibernate.event.spi.EventEngine;
import org.hibernate.graph.spi.RootGraphImplementor;
import org.hibernate.internal.FastSessionServices;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.metamodel.spi.RuntimeMetamodelsImplementor;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.query.spi.QueryParameterBindingTypeResolver;
import org.hibernate.query.sqm.spi.SqmCreationContext;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.sql.ast.spi.SqlAstCreationContext;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.generator.Generator;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.MappingContext;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Defines the internal contract between the {@link SessionFactory} and the internal
 * implementation of Hibernate.
 *
 * @see SessionFactory
 * @see org.hibernate.internal.SessionFactoryImpl
 *
 * @author Gavin King
 * @author Steve Ebersole
 */
public interface SessionFactoryImplementor
		extends MappingContext, SessionFactory, SqmCreationContext, SqlAstCreationContext,
				QueryParameterBindingTypeResolver { //deprecated extension, use MappingMetamodel
	/**
	 * Get the UUID for this {@code SessionFactory}.
	 * <p>
	 * The value is generated as a {@link java.util.UUID}, but kept as a String.
	 *
	 * @return The UUID for this {@code SessionFactory}.
	 *
	 * @see org.hibernate.internal.SessionFactoryRegistry#getSessionFactory
	 */
	String getUuid();

	/**
	 * Access to the name (if one) assigned to the {@code SessionFactory}
	 *
	 * @return The name for the {@code SessionFactory}
	 */
	@Override
	String getName();

	/**
	 * Overrides {@link SessionFactory#openSession()} to widen the return type:
	 * this is useful for internal code depending on {@link SessionFactoryImplementor}
	 * as it would otherwise need to frequently resort to casting to the internal contract.
	 *
	 * @return the opened {@code Session}.
	 */
	@Override
	SessionImplementor openSession();

	@Override
	TypeConfiguration getTypeConfiguration();

	@Override
	default SessionFactoryImplementor getSessionFactory() {
		return this;
	}

	@Override
	default MappingMetamodelImplementor getMappingMetamodel() {
		return getRuntimeMetamodels().getMappingMetamodel();
	}

	@Override
	SessionBuilderImplementor withOptions();

	/**
	 * Get a non-transactional "current" session (used by hibernate-envers)
	 */
	SessionImplementor openTemporarySession() throws HibernateException;

	@Override
	CacheImplementor getCache();

	@Override
	StatisticsImplementor getStatistics();

	RuntimeMetamodelsImplementor getRuntimeMetamodels();

	/**
	 * Access to the {@code ServiceRegistry} for this {@code SessionFactory}.
	 *
	 * @return The factory's ServiceRegistry
	 */
	ServiceRegistryImplementor getServiceRegistry();

	/**
	 * Get the EventEngine associated with this SessionFactory
	 */
	EventEngine getEventEngine();

	/**
	 * Retrieve fetch profile by name.
	 *
	 * @param name The name of the profile to retrieve.
	 * @return The profile definition
	 */
	FetchProfile getFetchProfile(String name);

	/**
	 * Get the identifier generator for the hierarchy
	 *
	 * @deprecated Only used in one place, will be removed
	 */
	@Deprecated(since = "7", forRemoval = true)
	Generator getGenerator(String rootEntityName);

	EntityNotFoundDelegate getEntityNotFoundDelegate();

	void addObserver(SessionFactoryObserver observer);

	//todo make a Service ?
	CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy();

	//todo make a Service ?
	CurrentTenantIdentifierResolver<Object> getCurrentTenantIdentifierResolver();

	/**
	 * The java type to use for a tenant identifier.
	 *
	 * @since 6.4
	 */
	JavaType<Object> getTenantIdentifierJavaType();

	/**
	 * @return the {@link FastSessionServices} instance associated with this SessionFactory
	 */
	FastSessionServices getFastSessionServices();

	WrapperOptions getWrapperOptions();

	@Override
	SessionFactoryOptions getSessionFactoryOptions();

	@Override
	FilterDefinition getFilterDefinition(String filterName);

	Collection<FilterDefinition> getAutoEnabledFilters();

	JdbcServices getJdbcServices();

	SqlStringGenerationContext getSqlStringGenerationContext();

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// map these to Metamodel

	@Override
	RootGraphImplementor<?> findEntityGraphByName(String name);

	/**
	 * The best guess entity name for an entity not in an association
	 */
	String bestGuessEntityName(Object object);

}
