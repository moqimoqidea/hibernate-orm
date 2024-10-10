/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.annotations.entity;

import jakarta.persistence.Convert;
import jakarta.persistence.MappedSuperclass;


@MappedSuperclass
public class FirstName {

	@Convert( converter = ToLowerConverter.class )
	private String firstName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String lowerCaseName) {
		this.firstName = lowerCaseName;
	}


}
