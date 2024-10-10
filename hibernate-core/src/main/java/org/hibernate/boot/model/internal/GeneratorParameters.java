/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.boot.model.internal;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;

import org.hibernate.Internal;
import org.hibernate.boot.model.IdentifierGeneratorDefinition;
import org.hibernate.boot.spi.MetadataBuildingContext;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.config.spi.StandardConverters;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.id.Configurable;
import org.hibernate.id.OptimizableGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.enhanced.LegacyNamingStrategy;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.id.enhanced.SingleNamingStrategy;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Table;

import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import static org.hibernate.cfg.MappingSettings.ID_DB_STRUCTURE_NAMING_STRATEGY;
import static org.hibernate.id.IdentifierGenerator.CONTRIBUTOR_NAME;
import static org.hibernate.id.IdentifierGenerator.ENTITY_NAME;
import static org.hibernate.id.IdentifierGenerator.JPA_ENTITY_NAME;
import static org.hibernate.id.OptimizableGenerator.DEFAULT_INITIAL_VALUE;
import static org.hibernate.id.OptimizableGenerator.IMPLICIT_NAME_BASE;
import static org.hibernate.id.OptimizableGenerator.INCREMENT_PARAM;
import static org.hibernate.id.OptimizableGenerator.INITIAL_PARAM;
import static org.hibernate.id.PersistentIdentifierGenerator.PK;
import static org.hibernate.id.PersistentIdentifierGenerator.TABLE;
import static org.hibernate.id.PersistentIdentifierGenerator.TABLES;

/**
 * Responsible for setting up the parameters which are passed to
 * {@link Configurable#configure(GeneratorCreationContext, Properties)}
 * when a {@link Configurable} generator is instantiated.
 *
 * @since 7.0
 *
 * @author Gavin King
 */
public class GeneratorParameters {

	private static final CoreMessageLogger LOG = CoreLogging.messageLogger( GeneratorBinder.class );

	/**
	 * Collect the parameters which should be passed to
	 * {@link Configurable#configure(GeneratorCreationContext, Properties)}.
	 */
	public static Properties collectParameters(
			SimpleValue identifierValue,
			Dialect dialect,
			RootClass rootClass) {
		final Properties params = new Properties();
		collectParameters( identifierValue, dialect, rootClass, params::put );
		return params;
	}

	/**
	 * Collect the parameters which should be passed to
	 * {@link Configurable#configure(GeneratorCreationContext, Properties)}.
	 */
	public static Properties collectParameters(
			SimpleValue identifierValue,
			Dialect dialect,
			RootClass rootClass,
			Map<String, Object> configuration) {
		final Properties params = collectParameters( identifierValue, dialect, rootClass );
		if ( configuration != null ) {
			params.putAll( configuration );
		}
		return params;
	}

	public static void collectParameters(
			SimpleValue identifierValue,
			Dialect dialect,
			RootClass rootClass,
			BiConsumer<String,String> parameterCollector) {

		final ConfigurationService configService = identifierValue
				.getMetadata()
				.getMetadataBuildingOptions()
				.getServiceRegistry()
				.requireService( ConfigurationService.class );

		// default initial value and allocation size per-JPA defaults
		parameterCollector.accept( INITIAL_PARAM, String.valueOf( DEFAULT_INITIAL_VALUE ) );
		parameterCollector.accept( INCREMENT_PARAM,	String.valueOf( defaultIncrement( configService ) ) );

		collectBaselineProperties( identifierValue, dialect, rootClass, parameterCollector );
	}

	public static int fallbackAllocationSize(Annotation generatorAnnotation, MetadataBuildingContext buildingContext) {
		if ( generatorAnnotation == null ) {
			final ConfigurationService configService = buildingContext
					.getBootstrapContext()
					.getServiceRegistry()
					.requireService( ConfigurationService.class );
			final String idNamingStrategy = configService.getSetting( ID_DB_STRUCTURE_NAMING_STRATEGY, StandardConverters.STRING );
			if ( LegacyNamingStrategy.STRATEGY_NAME.equals( idNamingStrategy )
					|| LegacyNamingStrategy.class.getName().equals( idNamingStrategy )
					|| SingleNamingStrategy.STRATEGY_NAME.equals( idNamingStrategy )
					|| SingleNamingStrategy.class.getName().equals( idNamingStrategy ) ) {
				return 1;
			}
		}

		return OptimizableGenerator.DEFAULT_INCREMENT_SIZE;
	}

	public static void collectBaselineProperties(
			SimpleValue identifierValue,
			Dialect dialect,
			RootClass rootClass,
			BiConsumer<String,String> parameterCollector) {

		final ConfigurationService configService = identifierValue
				.getMetadata()
				.getMetadataBuildingOptions()
				.getServiceRegistry()
				.requireService( ConfigurationService.class );

		//init the table here instead of earlier, so that we can get a quoted table name
		//TODO: would it be better to simply pass the qualified table name, instead of
		//	  splitting it up into schema/catalog/table names
		final String tableName = identifierValue.getTable().getQuotedName( dialect );
		parameterCollector.accept( TABLE, tableName );

		//pass the column name (a generated id almost always has a single column)
		if ( identifierValue.getColumnSpan() == 1 ) {
			final Column column = (Column) identifierValue.getSelectables().get( 0 );
			final String columnName = column.getQuotedName( dialect );
			parameterCollector.accept( PK, columnName );
		}

		//pass the entity-name, if not a collection-id
		if ( rootClass != null ) {
			parameterCollector.accept( ENTITY_NAME, rootClass.getEntityName() );
			parameterCollector.accept( JPA_ENTITY_NAME, rootClass.getJpaEntityName() );
			// The table name is not really a good default for subselect entities,
			// so use the JPA entity name which is short
			parameterCollector.accept(
					IMPLICIT_NAME_BASE,
					identifierValue.getTable().isSubselect()
							? rootClass.getJpaEntityName()
							: identifierValue.getTable().getName()
			);

			parameterCollector.accept( TABLES, identityTablesString( dialect, rootClass ) );
		}
		else {
			parameterCollector.accept( TABLES, tableName );
			parameterCollector.accept( IMPLICIT_NAME_BASE, tableName );
		}

		parameterCollector.accept( CONTRIBUTOR_NAME, identifierValue.getBuildingContext().getCurrentContributorName() );

		final Map<String, Object> settings = configService.getSettings();
		if ( settings.containsKey( AvailableSettings.PREFERRED_POOLED_OPTIMIZER ) ) {
			parameterCollector.accept(
					AvailableSettings.PREFERRED_POOLED_OPTIMIZER,
					(String) settings.get( AvailableSettings.PREFERRED_POOLED_OPTIMIZER )
			);
		}

	}

	public static String identityTablesString(Dialect dialect, RootClass rootClass) {
		final StringBuilder tables = new StringBuilder();
		for ( Table table : rootClass.getIdentityTables() ) {
			tables.append( table.getQuotedName( dialect ) );
			if ( !tables.isEmpty() ) {
				tables.append( ", " );
			}
		}
		return tables.toString();
	}

	public static int defaultIncrement(ConfigurationService configService) {
		final String idNamingStrategy =
				configService.getSetting( AvailableSettings.ID_DB_STRUCTURE_NAMING_STRATEGY,
						StandardConverters.STRING, null );
		if ( LegacyNamingStrategy.STRATEGY_NAME.equals( idNamingStrategy )
				|| LegacyNamingStrategy.class.getName().equals( idNamingStrategy )
				|| SingleNamingStrategy.STRATEGY_NAME.equals( idNamingStrategy )
				|| SingleNamingStrategy.class.getName().equals( idNamingStrategy ) ) {
			return 1;
		}
		else {
			return OptimizableGenerator.DEFAULT_INCREMENT_SIZE;
		}
	}


	@Internal
	public static void interpretTableGenerator(
			TableGenerator tableGeneratorAnnotation,
			IdentifierGeneratorDefinition.Builder definitionBuilder) {
		definitionBuilder.setName( tableGeneratorAnnotation.name() );
		definitionBuilder.setStrategy( org.hibernate.id.enhanced.TableGenerator.class.getName() );
		definitionBuilder.addParam( org.hibernate.id.enhanced.TableGenerator.CONFIG_PREFER_SEGMENT_PER_ENTITY, "true" );

		final String catalog = tableGeneratorAnnotation.catalog();
		if ( StringHelper.isNotEmpty( catalog ) ) {
			definitionBuilder.addParam( PersistentIdentifierGenerator.CATALOG, catalog );
		}

		final String schema = tableGeneratorAnnotation.schema();
		if ( StringHelper.isNotEmpty( schema ) ) {
			definitionBuilder.addParam( PersistentIdentifierGenerator.SCHEMA, schema );
		}

		final String table = tableGeneratorAnnotation.table();
		if ( StringHelper.isNotEmpty( table ) ) {
			definitionBuilder.addParam( org.hibernate.id.enhanced.TableGenerator.TABLE_PARAM, table );
		}

		final String pkColumnName = tableGeneratorAnnotation.pkColumnName();
		if ( StringHelper.isNotEmpty( pkColumnName ) ) {
			definitionBuilder.addParam(
					org.hibernate.id.enhanced.TableGenerator.SEGMENT_COLUMN_PARAM,
					pkColumnName
			);
		}

		final String pkColumnValue = tableGeneratorAnnotation.pkColumnValue();
		if ( StringHelper.isNotEmpty( pkColumnValue ) ) {
			definitionBuilder.addParam(
					org.hibernate.id.enhanced.TableGenerator.SEGMENT_VALUE_PARAM,
					pkColumnValue
			);
		}

		final String valueColumnName = tableGeneratorAnnotation.valueColumnName();
		if ( StringHelper.isNotEmpty( valueColumnName ) ) {
			definitionBuilder.addParam(
					org.hibernate.id.enhanced.TableGenerator.VALUE_COLUMN_PARAM,
					valueColumnName
			);
		}

		final String options = tableGeneratorAnnotation.options();
		if ( StringHelper.isNotEmpty( options ) ) {
			definitionBuilder.addParam(
					PersistentIdentifierGenerator.OPTIONS,
					options
			);
		}

		definitionBuilder.addParam(
				INCREMENT_PARAM,
				String.valueOf( tableGeneratorAnnotation.allocationSize() )
		);

		// See comment on HHH-4884 wrt initialValue.  Basically initialValue is really the stated value + 1
		definitionBuilder.addParam(
				INITIAL_PARAM,
				String.valueOf( tableGeneratorAnnotation.initialValue() + 1 )
		);

		// TODO : implement unique-constraint support
		final UniqueConstraint[] uniqueConstraints = tableGeneratorAnnotation.uniqueConstraints();
		if ( CollectionHelper.isNotEmpty( uniqueConstraints ) ) {
			LOG.ignoringTableGeneratorConstraints( tableGeneratorAnnotation.name() );
		}
	}

	@Internal
	public static void interpretSequenceGenerator(
			SequenceGenerator sequenceGeneratorAnnotation,
			IdentifierGeneratorDefinition.Builder definitionBuilder) {
		definitionBuilder.setName( sequenceGeneratorAnnotation.name() );
		definitionBuilder.setStrategy( SequenceStyleGenerator.class.getName() );

		final String catalog = sequenceGeneratorAnnotation.catalog();
		if ( StringHelper.isNotEmpty( catalog ) ) {
			definitionBuilder.addParam( PersistentIdentifierGenerator.CATALOG, catalog );
		}

		final String schema = sequenceGeneratorAnnotation.schema();
		if ( StringHelper.isNotEmpty( schema ) ) {
			definitionBuilder.addParam( PersistentIdentifierGenerator.SCHEMA, schema );
		}

		final String sequenceName = sequenceGeneratorAnnotation.sequenceName();
		if ( StringHelper.isNotEmpty( sequenceName ) ) {
			definitionBuilder.addParam( SequenceStyleGenerator.SEQUENCE_PARAM, sequenceName );
		}

		definitionBuilder.addParam(
				INCREMENT_PARAM,
				String.valueOf( sequenceGeneratorAnnotation.allocationSize() )
		);
		definitionBuilder.addParam(
				INITIAL_PARAM,
				String.valueOf( sequenceGeneratorAnnotation.initialValue() )
		);

		final String options = sequenceGeneratorAnnotation.options();
		if ( StringHelper.isNotEmpty( options ) ) {
			definitionBuilder.addParam( PersistentIdentifierGenerator.OPTIONS, options );
		}
	}
}
