package fr.openwide.core.jpa.business.generic.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;

import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.business.generic.model.GenericEntityCollectionReference;
import fr.openwide.core.jpa.business.generic.model.GenericEntityReference;
import fr.openwide.core.jpa.business.generic.model.QGenericEntity;

@Repository("entityDao")
public class EntityDaoImpl implements IEntityDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>> E getEntity(
			Class<E> clazz, K id) {
		return entityManager.find(clazz, id);
	}
	
	@Override
	public <E extends GenericEntity<?, ?>> E getEntity(GenericEntityReference<?, E> reference) {
		return entityManager.find(reference.getEntityClass(), reference.getEntityId());
	}
	
	@Override
	public <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>> List<E> listEntity(Class<E> clazz, Collection<K> ids) {
		return listEntityUnsafe(clazz, ids);
	}
	
	@Override
	public <E extends GenericEntity<?, ?>> List<E> listEntity(GenericEntityCollectionReference<?, E> reference) {
		return listEntityUnsafe(reference.getEntityClass(), reference.getEntityIdList());
	}
	
	@SuppressWarnings("unchecked")
	private <K extends Serializable & Comparable<K>, E extends GenericEntity<? extends K, ?>>
			List<E> listEntityUnsafe(Class<? extends E> clazz, Collection<?> ids) {
		if (ids == null || ids.isEmpty()) {
			return Lists.newArrayListWithCapacity(0);
		}
		
		PathBuilder<E> path = new PathBuilder<E>(clazz, clazz.getSimpleName());
		QGenericEntity qGenericEntity = new QGenericEntity(path);
		
		return new JPAQuery<E>(entityManager).select(path)
				.from(path)
				.where(qGenericEntity.id.in((Collection<? extends K>)ids))
				.orderBy(qGenericEntity.id.asc())
				.fetch();
	}
	
	@Override
	public void flush() {
		entityManager.flush();
	}
	
	@Override
	public void clear() {
		entityManager.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends GenericEntity<?, ?>> List<Class<? extends E>> listAssignableEntityTypes(Class<E> superclass) {
		List<Class<? extends E>> classes = Lists.newLinkedList();
		Metamodel metamodel = entityManager.getMetamodel();
		for (EntityType<?> entity : metamodel.getEntities()) {
			Class<?> clazz = entity.getBindableJavaType();
			if (superclass.isAssignableFrom(clazz)) {
				classes.add((Class<? extends E>) clazz);
			}
		}
		return classes;
	}

}
