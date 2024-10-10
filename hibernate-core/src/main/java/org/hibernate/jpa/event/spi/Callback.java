/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.jpa.event.spi;

import java.io.Serializable;

/**
 * Represents a JPA event callback (the method).
 *
 * Generally there are 2 flavors of this; either an annotated method on the entity itself
 * or an annotated method on a separate "listener" class.  This contract presents
 * a unified abstraction for both cases
 *
 * @author Kabir Khan
 * @author Steve Ebersole
 */
public interface Callback extends Serializable {
	/**
	 * The type of callback (pre-update, pre-persist, etc) handled
	 */
	CallbackType getCallbackType();

	/**
	 * Contract for performing the callback
	 *
	 * @param entity Reference to the entity for which the callback is triggered.
	 *
	 * @return Did a callback actually happen?
	 */
	boolean performCallback(Object entity);
}
