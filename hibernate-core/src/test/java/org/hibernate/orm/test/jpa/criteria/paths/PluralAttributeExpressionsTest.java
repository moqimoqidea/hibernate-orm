/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.jpa.criteria.paths;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.hibernate.orm.test.jpa.metamodel.AbstractMetamodelSpecificTest;
import org.hibernate.orm.test.jpa.metamodel.Address;
import org.hibernate.orm.test.jpa.metamodel.Address_;
import org.hibernate.orm.test.jpa.metamodel.Article;
import org.hibernate.orm.test.jpa.metamodel.Article_;
import org.hibernate.orm.test.jpa.metamodel.MapEntity;
import org.hibernate.orm.test.jpa.metamodel.MapEntityLocal;
import org.hibernate.orm.test.jpa.metamodel.MapEntity_;
import org.hibernate.orm.test.jpa.metamodel.Translation;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaExpression;

import org.hibernate.testing.orm.junit.JiraKey;

import org.junit.jupiter.api.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

/**
 * @author Steve Ebersole
 */
public class PluralAttributeExpressionsTest extends AbstractMetamodelSpecificTest {
	@Override
	public Class[] getAnnotatedClasses() {
		List<Class> classes = new ArrayList<>();
		Collections.addAll( classes, super.getAnnotatedClasses() );
		classes.add( MapEntity.class );
		classes.add( MapEntityLocal.class );
		classes.add( Article.class );
		classes.add( Translation.class );

		return classes.toArray( new Class[ classes.size() ] );
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// IS [NOT] EMPTY

	@Test
	public void testCollectionIsEmptyHql() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.createQuery( "select a from Address a where a.phones is empty" ).getResultList();
		});
	}

	@Test
	public void testCollectionIsEmptyCriteria() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();

			final CriteriaQuery<Address> criteria = cb.createQuery( Address.class );
			final Root<Address> root = criteria.from( Address.class);

			criteria.select( root )
					.where( cb.isEmpty( root.get( Address_.phones ) ) );

			entityManager.createQuery( criteria ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testElementMapIsEmptyHql() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.createQuery( "select m from MapEntity m where m.localized is empty" ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testElementMapIsEmptyCriteria() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();

			final CriteriaQuery<MapEntity> criteria = cb.createQuery( MapEntity.class );
			final Root<MapEntity> root = criteria.from( MapEntity.class);

			criteria.select( root )
					.where( cb.isMapEmpty( (JpaExpression) root.get( MapEntity_.localized ) ) );

			entityManager.createQuery( criteria ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testEntityMapIsEmptyHql() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.createQuery( "select a from Article a where a.translations is empty" ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testEntityMapIsEmptyCriteria() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();

			final CriteriaQuery<Article> criteria = cb.createQuery( Article.class );
			final Root<Article> root = criteria.from( Article.class);

			criteria.select( root )
					.where( cb.isEmpty( root.get( Article_.translations ) ) );

			entityManager.createQuery( criteria ).getResultList();
		});
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// SIZE

	@Test
	public void testCollectionSizeHql() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.createQuery( "select a from Address a where size(a.phones) > 1" ).getResultList();
		});
	}

	@Test
	public void testCollectionSizeCriteria() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();

			final CriteriaQuery<Address> criteria = cb.createQuery( Address.class );
			final Root<Address> root = criteria.from( Address.class);

			criteria.select( root )
					.where( cb.gt( cb.size( root.get( Address_.phones ) ), 1 ) );

			entityManager.createQuery( criteria ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testElementMapSizeHql() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.createQuery( "select m from MapEntity m where size( m.localized ) > 1" ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testElementMapSizeCriteria() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();

			final CriteriaQuery<MapEntity> criteria = cb.createQuery( MapEntity.class );
			final Root<MapEntity> root = criteria.from( MapEntity.class);

			criteria.select( root )
					.where( cb.gt( cb.mapSize( (JpaExpression) root.get( MapEntity_.localized ) ), 1 ) );

			entityManager.createQuery( criteria ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testEntityMapSizeHql() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			entityManager.createQuery( "select a from Article a where size(a.translations) > 1" ).getResultList();
		});
	}

	@Test
	@JiraKey( value = "HHH-11225" )
	public void testEntityMapSizeCriteria() {
		doInJPA( this::entityManagerFactory, entityManager -> {
			final HibernateCriteriaBuilder cb = (HibernateCriteriaBuilder) entityManager.getCriteriaBuilder();

			final CriteriaQuery<Article> criteria = cb.createQuery( Article.class );
			final Root<Article> root = criteria.from( Article.class);

			criteria.select( root )
					.where( cb.gt( cb.mapSize( (JpaExpression) root.get( Article_.translations ) ), 1 ) );

			entityManager.createQuery( criteria ).getResultList();
		});
	}

}
