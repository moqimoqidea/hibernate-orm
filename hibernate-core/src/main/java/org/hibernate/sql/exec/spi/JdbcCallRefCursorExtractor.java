/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.sql.exec.spi;

import java.sql.CallableStatement;
import java.sql.ResultSet;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * @author Steve Ebersole
 */
public interface JdbcCallRefCursorExtractor {
	ResultSet extractResultSet(CallableStatement callableStatement, SharedSessionContractImplementor session);
}
