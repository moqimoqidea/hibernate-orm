/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.testing.orm;

import java.util.ArrayList;

import org.hibernate.tool.schema.internal.exec.GenerationTarget;

/**
 * @author Steve Ebersole
 */
public class JournalingGenerationTarget implements GenerationTarget {
	private final ArrayList<String> commands = new ArrayList<>();

	@Override
	public void prepare() {
	}

	@Override
	public void accept(String command) {
		commands.add( command );
	}

	public ArrayList<String> getCommands() {
		return commands;
	}

	@Override
	public void release() {
	}

	public void clear() {
		commands.clear();
	}
}
