/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.processor.test.collectionbasictype;

import jakarta.persistence.AttributeConverter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author helloztt
 */
public class StringToListConverter implements AttributeConverter<List<String>, String> {
	private static final String COMMA = ",";

	@Override
	public String convertToDatabaseColumn(List<String> attribute) {
		if (attribute == null || attribute.size() == 0) {
			return null;
		}
		return attribute.stream().collect(Collectors.joining(COMMA));
	}

	@Override
	public List<String> convertToEntityAttribute(String dbData) {
		if (dbData == null || dbData.length() == 0) {
			return null;
		}
		return Arrays.asList(dbData.split(COMMA));
	}
}
