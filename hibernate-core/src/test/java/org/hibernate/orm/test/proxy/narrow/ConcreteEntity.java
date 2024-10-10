/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.proxy.narrow;

import jakarta.persistence.Entity;

/**
 * @author Yoann Rodière
 * @author Guillaume Smet
 */
@Entity
public class ConcreteEntity extends AbstractEntity {

	private String content = "text";

	public ConcreteEntity() {
		super();
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
