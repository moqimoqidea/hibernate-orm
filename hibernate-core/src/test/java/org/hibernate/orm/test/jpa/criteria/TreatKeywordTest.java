/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.orm.test.jpa.criteria;

import java.util.Arrays;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;

import org.hibernate.orm.test.jpa.BaseEntityManagerFunctionalTestCase;
import org.hibernate.orm.test.jpa.metamodel.Thing;
import org.hibernate.orm.test.jpa.metamodel.ThingWithQuantity;
import org.hibernate.orm.test.jpa.metamodel.ThingWithQuantity_;
import org.hibernate.orm.test.jpa.ql.TreatKeywordTest.JoinedEntity;
import org.hibernate.orm.test.jpa.ql.TreatKeywordTest.JoinedEntitySubSubclass;
import org.hibernate.orm.test.jpa.ql.TreatKeywordTest.JoinedEntitySubSubclass2;
import org.hibernate.orm.test.jpa.ql.TreatKeywordTest.JoinedEntitySubclass;
import org.hibernate.orm.test.jpa.ql.TreatKeywordTest.JoinedEntitySubclass2;

import org.hibernate.testing.orm.junit.JiraKey;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Steve Ebersole
 */
public class TreatKeywordTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class[] {
				JoinedEntity.class, JoinedEntitySubclass.class, JoinedEntitySubSubclass.class,
				JoinedEntitySubclass2.class, JoinedEntitySubSubclass2.class,
				Animal.class, Elephant.class, Human.class, Thing.class, ThingWithQuantity.class,
				TreatAnimal.class, Dog.class, Dachshund.class, Greyhound.class
		};
	}

	@Test
	public void basicTest() {
		EntityManager em = getOrCreateEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Thing> criteria = builder.createQuery( Thing.class );
		Root<Thing> root = criteria.from( Thing.class );
		criteria.select( root );
		criteria.where(
				builder.equal(
						builder.treat( root, ThingWithQuantity.class ).get( ThingWithQuantity_.quantity ),
						2
				)
		);
		em.createQuery( criteria ).getResultList();
		em.close();
	}

	@Test
	public void basicTest2() {
		EntityManager em = getOrCreateEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Animal> criteria = builder.createQuery( Animal.class );
		Root<Animal> root = criteria.from( Animal.class );
		criteria.select( root );
		criteria.where(
				builder.equal(
						builder.treat( root, Human.class ).get( "name" ),
						"2"
				)
		);
		em.createQuery( criteria ).getResultList();
		em.close();
	}

	@Test
	public void treatPathClassTest() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		Animal animal = new Animal();
		animal.setId(100L);
		animal.setName("2");
		em.persist(animal);
		Human human = new Human();
		human.setId(200L);
		human.setName("2");
		em.persist(human);
		Elephant elephant = new Elephant();
		elephant.setId( 300L );
		elephant.setName( "2" );
		em.persist( elephant );
		em.getTransaction().commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<String> criteria = builder.createQuery( String.class );
		Root<Animal> root = criteria.from( Animal.class );
		EntityType<Animal> Animal_ = em.getMetamodel().entity(Animal.class);
		criteria.select(root.get(Animal_.getSingularAttribute("name", String.class)));

		criteria.where(builder.like(builder.treat(root, Human.class).get( Human_.name ), "2%"));
		List<String> animalList = em.createQuery( criteria ).getResultList();
		Assert.assertEquals("treat(Animal as Human) was ignored",1, animalList.size());

		CriteriaQuery<Long> idCriteria = builder.createQuery( Long.class );
		Root<Animal> idRoot = idCriteria.from( Animal.class );
		idCriteria.select( idRoot.get( Animal_.getSingularAttribute( "id", Long.class ) ) );

		idCriteria.where(
				builder.like(
						builder.treat( idRoot, Human.class )
								.get( Human_.name ), "2%"
				)
		);
		List<Long> animalIdList = em.createQuery( idCriteria ).getResultList();
		Assert.assertEquals( "treat(Animal as Human) was ignored", 1, animalIdList.size() );
		Assert.assertEquals( 200L, animalIdList.get( 0 ).longValue() );

		em.close();
	}

	@Test
	public void treatPathClassTestHqlControl() {
		EntityManager em = getOrCreateEntityManager();
		em.getTransaction().begin();
		Animal animal = new Animal();
		animal.setId(100L);
		animal.setName("2");
		em.persist( animal );
		Human human = new Human();
		human.setId(200L);
		human.setName("2");
		em.persist(human);
		Elephant elephant = new Elephant();
		elephant.setId( 300L );
		elephant.setName( "2" );
		em.persist( elephant );
		em.getTransaction().commit();

		List<String> animalList = em.createQuery( "select a.name from Animal a where treat (a as Human).name like '2%'" ).getResultList();
		Assert.assertEquals( "treat(Animal as Human) was ignored", 1, animalList.size() );

		List<Long> animalIdList = em.createQuery( "select a.id from Animal a where treat (a as Human).name like '2%'" ).getResultList();
		Assert.assertEquals("treat(Animal as Human) was ignored",1, animalList.size());
		Assert.assertEquals( 200L, animalIdList.get( 0 ).longValue() );

		em.close();
	}

	@Test
	@JiraKey( value = "HHH-9549" )
	public void treatRoot() {
		EntityManager em = getOrCreateEntityManager();

		em.getTransaction().begin();
		Animal animal = new Animal();
		animal.setId(100L);
		animal.setName("2");
		em.persist(animal);
		Human human = new Human();
		human.setId(200L);
		human.setName("2");
		em.persist(human);
		Elephant elephant = new Elephant();
		elephant.setId( 300L );
		elephant.setName( "2" );
		em.persist( elephant );
		em.getTransaction().commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Human> criteria = builder.createQuery( Human.class );
		Root<Animal> root = criteria.from( Animal.class );
		criteria.select( builder.treat( root, Human.class ) );
		List<Human> humans = em.createQuery( criteria ).getResultList();
		Assert.assertEquals( 1, humans.size() );

		em.close();
	}

	@Test
	@JiraKey( value = "HHH-9549" )
	public void treatRootReturnSuperclass() {
		EntityManager em = getOrCreateEntityManager();

		em.getTransaction().begin();
		Animal animal = new Animal();
		animal.setId(100L);
		animal.setName("2");
		em.persist(animal);
		Human human = new Human();
		human.setId(200L);
		human.setName("2");
		em.persist(human);
		Elephant elephant = new Elephant();
		elephant.setId( 300L );
		elephant.setName( "2" );
		em.persist( elephant );
		em.getTransaction().commit();

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Animal> criteria = builder.createQuery( Animal.class );
		Root<Animal> root = criteria.from( Animal.class );
		criteria.select( builder.treat( root, Human.class ) );
		List<Animal> animalsThatAreHuman = em.createQuery( criteria ).getResultList();
		Assert.assertEquals( 1, animalsThatAreHuman.size() );
		Assert.assertTrue( Human.class.isInstance( animalsThatAreHuman.get( 0 ) ) );

		em.close();
	}

	@Test
	public void testSelectSubclassPropertyFromDowncast() {
		EntityManager em = getOrCreateEntityManager();
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Integer> criteria = builder.createQuery( Integer.class );
		Root<Thing> root = criteria.from( Thing.class );
		Root<ThingWithQuantity> subroot = builder.treat( root, ThingWithQuantity.class );
		criteria.select( subroot.<Integer>get( "quantity" ) );
		em.createQuery( criteria ).getResultList();
		em.close();
	}


	@Test
	@JiraKey(value = "HHH-9411")
	public void testTreatWithRestrictionOnAbstractClass() {
		EntityManager em = getOrCreateEntityManager();
		EntityTransaction entityTransaction = em.getTransaction();
		entityTransaction.begin();

		Greyhound greyhound = new Greyhound();
		Dachshund dachshund = new Dachshund();
		em.persist( greyhound );
		em.persist( dachshund );

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TreatAnimal> criteriaQuery = cb.createQuery( TreatAnimal.class );

		Root<TreatAnimal> animal = criteriaQuery.from( TreatAnimal.class );

		Root<Dog> dog = cb.treat( animal, Dog.class );

		// only fast dogs
		criteriaQuery.where( cb.isTrue( dog.<Boolean>get( "fast" ) ) );

		List<TreatAnimal> results = em.createQuery( criteriaQuery ).getResultList();

		// we should only have a single Greyhound here, not slow long dogs!
		assertEquals( Arrays.asList( greyhound ), results );

		entityTransaction.commit();
		em.close();
	}

	@Test
	@JiraKey(value = "HHH-16657")
	public void testTypeFilterInSubquery() {
		EntityManager em = getOrCreateEntityManager();
		EntityTransaction entityTransaction = em.getTransaction();
		entityTransaction.begin();

		JoinedEntitySubclass2 child1 = new JoinedEntitySubclass2( 3, "child1");
		JoinedEntitySubSubclass2 child2 = new JoinedEntitySubSubclass2( 4, "child2");
		JoinedEntitySubclass root1 = new JoinedEntitySubclass( 1, "root1", child1);
		JoinedEntitySubSubclass root2 = new JoinedEntitySubSubclass( 2, "root2", child2);
		em.persist( child1 );
		em.persist( child2 );
		em.persist( root1 );
		em.persist( root2 );

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery( String.class );
		Root<JoinedEntitySubclass> root = query.from( JoinedEntitySubclass.class );
		query.orderBy( cb.asc( root.get( "id" ) ) );
		Subquery<String> subquery = query.subquery( String.class );
		Root<JoinedEntitySubclass> subqueryRoot = subquery.correlate( root );
		Join<Object, Object> other = subqueryRoot.join( "other" );
		subquery.select( other.get( "name" ) );
		subquery.where( cb.equal( root.type(), cb.literal( JoinedEntitySubclass.class ) ) );
		query.select( subquery );

		List<String> results = em.createQuery(
				"select (select o.name from j.other o where type(j) = JoinedEntitySubSubclass) from JoinedEntitySubclass j order by j.id",
				String.class
		).getResultList();

		assertEquals( 2, results.size() );
		assertNull( results.get( 0 ) );
		assertEquals( "child2", results.get( 1 ) );

		entityTransaction.commit();
		em.close();
	}

	@Entity(name = "TreatAnimal")
	public static abstract class TreatAnimal {
		@Id
		@GeneratedValue
		private Long id;
	}

	@Entity(name = "Dog")
	public static abstract class Dog extends TreatAnimal {
		private boolean fast;

		protected Dog(boolean fast) {
			this.fast = fast;
		}

		public boolean isFast() {
			return fast;
		}

		public void setFast(boolean fast) {
			this.fast = fast;
		}
	}

	@Entity(name = "Dachshund")
	public static class Dachshund extends Dog {
		public Dachshund() {
			super( false );
		}
	}

	@Entity(name = "Greyhound")
	public static class Greyhound extends Dog {
		public Greyhound() {
			super( true );
		}
	}
}
