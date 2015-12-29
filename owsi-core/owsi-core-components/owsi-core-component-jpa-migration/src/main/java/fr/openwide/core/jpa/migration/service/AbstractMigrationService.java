package fr.openwide.core.jpa.migration.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.business.generic.model.QGenericEntity;
import fr.openwide.core.jpa.migration.util.IPreloadAwareMigrationInformation;
import fr.openwide.core.jpa.util.EntityManagerUtils;
import fr.openwide.core.spring.property.service.IPropertyService;

public abstract class AbstractMigrationService {

	public static final int DEFAULT_TIMEOUT = 15;

	private static final String SQL_COUNT_ROWS = "SELECT count(*) FROM %1$s";

	public static final String SQL_UPDATE_SEQUENCE = "SELECT setval('%1$s_id_seq', (SELECT max(id) FROM %1$s))";

	@Autowired
	protected IPropertyService propertyService;

	@Autowired
	protected ConfigurableApplicationContext applicationContext;

	@Autowired
	protected IMigrationUtilsService migrationUtilsService;

	@Autowired
	protected EntityManagerUtils entityManagerUtils;

	protected int getDefaultTimeoutInMinutes() {
		return DEFAULT_TIMEOUT;
	}

	protected abstract JdbcTemplate getJdbcTemplate();

	protected abstract NamedParameterJdbcTemplate getNamedParameterJdbcTemplate();

	protected void updateSequence(Class<? extends GenericEntity<Long, ?>> genericEntityClass) {
		migrationUtilsService.updateSequence(genericEntityClass);
	} 

	protected Long countRows(String sqlCountRows) {
		Long rowCount = getJdbcTemplate().queryForObject(sqlCountRows, Long.class);
		if (rowCount != null) {
			return rowCount;
		} else {
			return 0L;
		}
	}

	protected Long countRowsTable(String tableName) {
		return countRows(String.format(SQL_COUNT_ROWS, tableName));
	}
	
	protected void preload(List<Long> entityIds, IPreloadAwareMigrationInformation migrationInformation) {
		Map<Class<? extends GenericEntity<Long, ?>>, String> preloadRequests = migrationInformation.getPreloadRequests();
		if (preloadRequests != null) {
			for (Class<? extends GenericEntity<Long, ?>> preloadedClass : preloadRequests.keySet()) {
				String sqlPreloadRequest = preloadRequests.get(preloadedClass);
				if (sqlPreloadRequest == null) {
					listEntitiesByIds(preloadedClass, entityIds);
				} else {
					preloadLinkedEntities(preloadedClass,
							sqlPreloadRequest,
							migrationInformation.getParameterIds(),
							entityIds);
				}
			}
		}
	}

	public final <E extends GenericEntity<Long, ?>> void preloadLinkedEntities(Class<E> clazz,
			String queryForLinkedEntityIds, String partitionIdsParamName, Collection<Long> partitionIds) {
		MapSqlParameterSource linkedEntityIdsParameterSource = new MapSqlParameterSource();
		linkedEntityIdsParameterSource.addValue(partitionIdsParamName, partitionIds);
		
		List<Long> linkedEntitiesIds = ImmutableList.copyOf(getNamedParameterJdbcTemplate().queryForList(queryForLinkedEntityIds,
				linkedEntityIdsParameterSource, Long.class));
		
		if (linkedEntitiesIds.isEmpty()) {
			return;
		}
		
		listEntitiesByIds(clazz, linkedEntitiesIds);
	}

	public final <E extends GenericEntity<Long, ?>> List<E> listEntitiesByIds(Class<E> clazz, Collection<Long> entityIds) {
		PathBuilder<E> path = new PathBuilder<E>(clazz, clazz.getSimpleName());
		QGenericEntity qGenericEntity = new QGenericEntity(path);
		
		return new JPAQuery<E>(entityManagerUtils.getEntityManager()).select(path)
				.from(path)
				.where(qGenericEntity.id.in(entityIds))
				.fetch();
	}

}
