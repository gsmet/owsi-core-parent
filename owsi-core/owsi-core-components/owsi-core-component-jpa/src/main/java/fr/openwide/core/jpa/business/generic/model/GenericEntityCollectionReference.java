package fr.openwide.core.jpa.business.generic.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.openwide.core.jpa.search.util.HibernateSearchAnalyzer;

public class GenericEntityCollectionReference<K extends Comparable<K> & Serializable, E extends GenericEntity<K, ?>>
		implements Serializable {
	
	private static final long serialVersionUID = 1357434247523209721L;

	@Column(nullable = true)
	private final Class<? extends E> entityClass;
	
	@Column(nullable = true)
	@Field(analyzer = @Analyzer(definition = HibernateSearchAnalyzer.KEYWORD))
	private final List<K> entityIdList;
	
	public static <K extends Comparable<K> & Serializable, E extends GenericEntity<K, ?>>
			GenericEntityCollectionReference<K, E> of(Class<E> entityClass, Collection<? extends E> entityCollection) {
		List<K> entityIdCollection = Lists.newArrayListWithExpectedSize(entityCollection.size());
		for (E entity : entityCollection) {
			Assert.state(!entity.isNew(), "None of the referenced entities must be transient");
			entityIdCollection.add(entity.getId());
		}
		return new GenericEntityCollectionReference<>(entityClass, entityIdCollection);
	}
	
	public GenericEntityCollectionReference(E object) {
		this(GenericEntityReference.of(object));
	}
	
	public GenericEntityCollectionReference(GenericEntityReference<K, E> reference) {
		this(reference.getEntityClass(), ImmutableList.of(reference.getEntityId()));
	}
	
	public GenericEntityCollectionReference(Class<? extends E> entityClass) {
		this(entityClass, ImmutableList.<K>of());
	}
	
	public GenericEntityCollectionReference(Class<? extends E> entityClass, Collection<K> entityIdCollection) {
		super();
		Assert.notNull(entityClass, "entityClass must not be null");
		Assert.notNull(entityIdCollection, "entityIdCollection must not be null");
		this.entityClass = entityClass;
		this.entityIdList = Collections.unmodifiableList(Lists.newArrayList(entityIdCollection));
	}

	public Class<? extends E> getEntityClass() {
		return entityClass;
	}

	public List<K> getEntityIdList() {
		return entityIdList;
	}

	@SuppressWarnings("unchecked")
	protected static <K extends Comparable<K> & Serializable> Class<? extends GenericEntity<K, ?>> getUpperEntityClass(Class<? extends GenericEntity<K, ?>> entityClass) {
		Class<?> currentClass = entityClass;
		while (currentClass != null && currentClass.getAnnotation(Entity.class) == null) {
			currentClass = currentClass.getSuperclass();
		}
		return (Class<? extends GenericEntity<K, ?>>) currentClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof GenericEntityCollectionReference)) {
			return false;
		}
		GenericEntityCollectionReference<?, ?> other = (GenericEntityCollectionReference<?, ?>) obj;
		return new EqualsBuilder()
				.append(getEntityIdList(), other.getEntityIdList())
				.append(getUpperEntityClass(getEntityClass()), getUpperEntityClass(other.getEntityClass()))
				.build();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(getEntityIdList())
				.append(getUpperEntityClass(getEntityClass()))
				.build();
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(ToStringStyle.SHORT_PREFIX_STYLE)
				.append("class", getEntityClass())
				.append("id", getEntityIdList())
				.build();
	}

}
