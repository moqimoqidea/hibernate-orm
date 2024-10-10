/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.sql.ast.spi;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.metamodel.spi.MappingMetamodelImplementor;
import org.hibernate.query.BindingContext;

/**
 * The "context" in which creation of SQL AST occurs.  Provides
 * access to generally needed when creating SQL AST nodes
 *
 * @author Steve Ebersole
 */
public interface SqlAstCreationContext extends BindingContext {
	/**
	 * The SessionFactory
	 */
	SessionFactoryImplementor getSessionFactory();

	/**
	 * The runtime MappingMetamodelImplementor
	 */
	MappingMetamodelImplementor getMappingMetamodel();

	/**
	 * When creating {@link org.hibernate.sql.results.graph.Fetch} references,
	 * defines a limit to how deep we should join for fetches.
	 */
	Integer getMaximumFetchDepth();
}
