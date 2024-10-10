/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.bytecode.enhancement.lazy.proxy;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * @author Steve Ebersole
 */
@Entity(name = "DomesticCustomer")
@Table(name = "domestic_cust")
public class DomesticCustomer extends Customer {
	private String taxId;

	public DomesticCustomer() {
	}

	public DomesticCustomer(
			Integer oid,
			String name,
			String taxId,
			Address address,
			Customer parentCustomer) {
		super( oid, name, address, parentCustomer );
		this.taxId = taxId;
	}

	public String getTaxId() {
		return taxId;
	}

	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}
}
