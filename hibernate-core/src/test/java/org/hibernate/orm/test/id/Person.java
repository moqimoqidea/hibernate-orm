/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.id;


public class Person {

	private long id;

	public Person() {
	}

	public long getId() {
		return id;
	}

	public void setId(final long id) {
		this.id = id;
	}

}
