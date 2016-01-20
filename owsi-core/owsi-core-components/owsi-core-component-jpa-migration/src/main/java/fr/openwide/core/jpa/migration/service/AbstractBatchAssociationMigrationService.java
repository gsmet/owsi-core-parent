package fr.openwide.core.jpa.migration.service;

import java.util.List;
import java.util.Map;

import org.hibernate.PropertyValueException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.google.common.collect.ImmutableList;

import fr.openwide.core.jpa.batch.executor.BatchExecutorCreator;
import fr.openwide.core.jpa.batch.executor.MultithreadedBatchExecutor;
import fr.openwide.core.jpa.batch.monitor.ProcessorMonitorContext;
import fr.openwide.core.jpa.batch.runnable.AbstractBatchRunnable;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.business.generic.service.IGenericEntityService;
import fr.openwide.core.jpa.migration.rowmapper.AbstractResultRowMapper;
import fr.openwide.core.jpa.migration.util.IBatchAssociationMigrationInformation;

/**
 * An abstract base for migration services that import associations from an entity to other elements
 * (@OneToMany, @OneToOne, @ManyToOne, @ManyToMany, but also @ElementCollection)
 * 
 * @param Owning The type of the entity on the owning (non "mappedBy") side of the association
 * @param Owned The type of the owning entity association member (e.g. Collection&lt;AnotherType&gt;)
 */
public abstract class AbstractBatchAssociationMigrationService<Owning extends GenericEntity<Long, Owning>, Owned>
		extends AbstractMigrationService {
	
	private static final int DEFAULT_VALUES_PER_KEY = 3;
	
	@Autowired
	private BatchExecutorCreator batchCreator;

	public void importAllEntities() {
		List<Long> entityIds = ImmutableList.copyOf(getJdbcTemplate().queryForList(getMigrationInformation().getSqlAllIds(), Long.class));

		MultithreadedBatchExecutor executor = batchCreator.newMultithreadedBatchExecutor();
		executor.threads(4).batchSize(100);
		executor.run(getMigrationInformation().getAssociationName(), entityIds, new AbstractBatchRunnable<Long>() {
			@Override
			public void executePartition(List<Long> partition) {
				importBatch(partition);
			}
		});
	}

	private void importBatch(List<Long> entityIds) throws PropertyValueException {
		preload(entityIds, getMigrationInformation());
		
		try {
			MapSqlParameterSource entityIdsParameterSource = new MapSqlParameterSource();
			entityIdsParameterSource.addValue(getMigrationInformation().getParameterIds(), entityIds);
			AutowireCapableBeanFactory autowire = applicationContext.getAutowireCapableBeanFactory();
			
			AbstractResultRowMapper<? extends Map<Owning, Owned>> rowMapper = getMigrationInformation().newRowMapper(entityIds.size(), DEFAULT_VALUES_PER_KEY);
			
			autowire.autowireBean(rowMapper);
			autowire.initializeBean(rowMapper, rowMapper.getClass().getSimpleName());
			prepareRowMapper(rowMapper, entityIds);
			getNamedParameterJdbcTemplate().query(getMigrationInformation().getSqlRequest(), entityIdsParameterSource, rowMapper);
			
			for (Map.Entry<Owning, Owned> entry : rowMapper.getResults().entrySet()) {
				Owning entity = entry.getKey();
				getMigrationInformation().addToAssociation(entity, entry.getValue());
				if (getEntityService() != null) {
					getEntityService().update(entity);
				} else {
					entityManagerUtils.getEntityManager().persist(entity);
				}
			}
			
		} catch (Exception e) {
			getLogger().error("Error during the persistence of {} items. {} cancelled creations.",
					getMigrationInformation().getAssociationName(), entityIds.size(), e);
			ProcessorMonitorContext.get().getDoneItems().addAndGet(-1 * entityIds.size());
		}
	}

	protected void prepareRowMapper(RowMapper<?> rowMapper, List<Long> entityIds) {
	}

	protected abstract IBatchAssociationMigrationInformation<Owning, Owned> getMigrationInformation();

	protected abstract Logger getLogger();

	/**
	 * Override this if you want to use a GenericEntityService when creating entities.
	 * <p><strong>Note:</strong>This is not recommended, since these services generally assume that they are really
	 * creating a brand new row, not importing it. They may, for instance, set the "creationdate" attribute of an
	 * entity to the current time, which is probably wrong when migrating.
	 */
	protected IGenericEntityService<Long, Owning> getEntityService() {
		return null;
	}

	protected Integer getPartitionSize() {
		return 100;
	}
}
