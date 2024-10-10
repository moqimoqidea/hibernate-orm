/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.testing.envers;

import jakarta.persistence.EntityManagerFactory;

/**
 * Envers contract for something that can build an EntityManagerFactory based on an audit strategy.
 *
 * @author Chris Cranford
 */
public interface EnversEntityManagerFactoryProducer {
	EntityManagerFactory produceEntityManagerFactory(String auditStrategyName);
}
