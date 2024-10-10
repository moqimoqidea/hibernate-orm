/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.metamodel.spi;

import org.hibernate.metamodel.RuntimeMetamodels;
import org.hibernate.metamodel.model.domain.spi.JpaMetamodelImplementor;

/**
 * SPI extending {@link RuntimeMetamodels}.
 *
 * @author Steve Ebersole
 */
public interface RuntimeMetamodelsImplementor extends RuntimeMetamodels {
	@Override
	MappingMetamodelImplementor getMappingMetamodel();

	@Override
	JpaMetamodelImplementor getJpaMetamodel();
}
