/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.jpa.callbacks;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import jakarta.persistence.Basic;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.Session;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.persister.entity.EntityPersister;

import org.hibernate.testing.orm.junit.JiraKey;
import org.hibernate.testing.junit4.BaseNonConfigCoreFunctionalTestCase;
import org.junit.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@JiraKey(value = "HHH-12718")
public class PreUpdateCustomEntityDirtinessStrategyTest
		extends BaseNonConfigCoreFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Person.class };
	}

	@Test
	public void testPreUpdateModifications() {
		Person person = new Person();

		doInHibernate( this::sessionFactory, session -> {
			session.persist( person );
		} );

		doInHibernate( this::sessionFactory, session -> {
			Person p = session.find( Person.class, person.id );
			assertNotNull( p );
			assertNotNull( p.createdAt );
			assertNull( p.lastUpdatedAt );

			p.setName( "Changed Name" );
		} );

		doInHibernate( this::sessionFactory, session -> {
			Person p = session.find( Person.class, person.id );
			assertNotNull( p.lastUpdatedAt );
		} );

		assertTrue( DefaultCustomEntityDirtinessStrategy.INSTANCE.isPersonNameChanged() );
		assertTrue( DefaultCustomEntityDirtinessStrategy.INSTANCE.isPersonLastUpdatedAtChanged() );
	}

	@Override
	protected void addSettings(Map<String,Object> settings) {
		settings.put( AvailableSettings.CUSTOM_ENTITY_DIRTINESS_STRATEGY, DefaultCustomEntityDirtinessStrategy.INSTANCE );
	}

	public static class DefaultCustomEntityDirtinessStrategy
			implements CustomEntityDirtinessStrategy {
		private static final DefaultCustomEntityDirtinessStrategy INSTANCE =
				new DefaultCustomEntityDirtinessStrategy();

		private boolean personNameChanged = false;
		private boolean personLastUpdatedAtChanged = false;

		@Override
		public boolean canDirtyCheck(Object entity, EntityPersister persister, Session session) {
			return true;
		}

		@Override
		public boolean isDirty(Object entity, EntityPersister persister, Session session) {
			Person person = (Person) entity;
			if ( !personNameChanged ) {
				personNameChanged = person.getName() != null;
				return personNameChanged;
			}
			if ( !personLastUpdatedAtChanged ) {
				personLastUpdatedAtChanged = person.getLastUpdatedAt() != null;
				return personLastUpdatedAtChanged;
			}
			return false;
		}

		@Override
		public void resetDirty(Object entity, EntityPersister persister, Session session) {
		}

		@Override
		public void findDirty(
				Object entity,
				EntityPersister persister,
				Session session,
				DirtyCheckContext dirtyCheckContext) {
		}

		public boolean isPersonNameChanged() {
			return personNameChanged;
		}

		public boolean isPersonLastUpdatedAtChanged() {
			return personLastUpdatedAtChanged;
		}
	}

	@Entity(name = "Person")
	@DynamicUpdate
	private static class Person {
		@Id
		@GeneratedValue
		private int id;

		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		private Instant createdAt;

		public Instant getCreatedAt() {
			return createdAt;
		}

		public void setCreatedAt(Instant createdAt) {
			this.createdAt = createdAt;
		}

		private Instant lastUpdatedAt;

		public Instant getLastUpdatedAt() {
			return lastUpdatedAt;
		}

		public void setLastUpdatedAt(Instant lastUpdatedAt) {
			this.lastUpdatedAt = lastUpdatedAt;
		}

		@ElementCollection
		private List<String> tags;

		@Lob
		@Basic(fetch = FetchType.LAZY)
		private byte[] image;

		@PrePersist
		void beforeCreate() {
			this.setCreatedAt( Instant.now() );
		}

		@PreUpdate
		void beforeUpdate() {
			this.setLastUpdatedAt( Instant.now() );
		}
	}
}
