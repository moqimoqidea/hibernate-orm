/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.internal.util;

import jakarta.persistence.LockModeType;

import org.hibernate.AssertionFailure;
import org.hibernate.LockMode;

/**
 * Helper to deal with conversions (both directions) between {@link LockMode} and
 * {@link jakarta.persistence.LockModeType}.
 *
 * @author Steve Ebersole
 */
public final class LockModeConverter {
	private LockModeConverter() {
	}

	/**
	 * Convert from the Hibernate-specific {@link LockMode} to the JPA defined {@link LockModeType}.
	 *
	 * @param lockMode The Hibernate {@link LockMode}.
	 * @return The JPA {@link LockModeType}
	 */
	public static LockModeType convertToLockModeType(LockMode lockMode) {
		switch (lockMode) {
			case NONE:
			case READ: // no exact equivalent in JPA
				return LockModeType.NONE;
			case OPTIMISTIC:
				return LockModeType.OPTIMISTIC;
			case OPTIMISTIC_FORCE_INCREMENT:
				return LockModeType.OPTIMISTIC_FORCE_INCREMENT;
			case PESSIMISTIC_READ:
				return LockModeType.PESSIMISTIC_READ;
			case PESSIMISTIC_WRITE:
			case UPGRADE_NOWAIT:
			case UPGRADE_SKIPLOCKED:
				return LockModeType.PESSIMISTIC_WRITE;
			case WRITE: // no exact equivalent in JPA
			case PESSIMISTIC_FORCE_INCREMENT:
				return LockModeType.PESSIMISTIC_FORCE_INCREMENT;
			default:
				throw new AssertionFailure( "unhandled lock mode " + lockMode );
		}
	}


	/**
	 * Convert from JPA defined {@link LockModeType} to Hibernate-specific {@link LockMode}.
	 *
	 * @param lockModeType The JPA {@link LockModeType}
	 * @return The Hibernate {@link LockMode}.
	 */
	public static LockMode convertToLockMode(LockModeType lockModeType) {
		switch ( lockModeType ) {
			case NONE:
				return LockMode.NONE;
			case READ:
			case OPTIMISTIC:
				return LockMode.OPTIMISTIC;
			case WRITE:
			case OPTIMISTIC_FORCE_INCREMENT:
				return LockMode.OPTIMISTIC_FORCE_INCREMENT;
			case PESSIMISTIC_READ:
				return LockMode.PESSIMISTIC_READ;
			case PESSIMISTIC_WRITE:
				return LockMode.PESSIMISTIC_WRITE;
			case PESSIMISTIC_FORCE_INCREMENT:
				return LockMode.PESSIMISTIC_FORCE_INCREMENT;
			default:
				throw new AssertionFailure( "Unknown LockModeType: " + lockModeType );
		}
	}
}
