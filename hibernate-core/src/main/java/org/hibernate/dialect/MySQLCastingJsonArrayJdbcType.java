/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.dialect;

import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.type.descriptor.jdbc.JsonArrayJdbcType;

/**
 * @author Christian Beikov
 */
public class MySQLCastingJsonArrayJdbcType extends JsonArrayJdbcType {
	/**
	 * Singleton access
	 */
	public static final JsonArrayJdbcType INSTANCE = new MySQLCastingJsonArrayJdbcType();

	@Override
	public void appendWriteExpression(
			String writeExpression,
			SqlAppender appender,
			Dialect dialect) {
		appender.append( "cast(" );
		appender.append( writeExpression );
		appender.append( " as json)" );
	}
}
