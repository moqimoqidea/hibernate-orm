/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.internal.build;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to indicate to the Forbidden APIs library that a specific usage
 * of {@link System#out} is allowable.
 *
 * @author Steve Ebersole
 */
@Retention( RetentionPolicy.CLASS )
public @interface AllowSysOut {
}
