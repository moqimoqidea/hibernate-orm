/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.bytecode.enhance.spi.interceptor;

import org.hibernate.engine.spi.PersistentAttributeInterceptor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Steve Ebersole
 */
public interface SessionAssociableInterceptor extends PersistentAttributeInterceptor {
	SharedSessionContractImplementor getLinkedSession();

	void setSession(SharedSessionContractImplementor session);

	void unsetSession();

	boolean allowLoadOutsideTransaction();

	String getSessionFactoryUuid();
}
