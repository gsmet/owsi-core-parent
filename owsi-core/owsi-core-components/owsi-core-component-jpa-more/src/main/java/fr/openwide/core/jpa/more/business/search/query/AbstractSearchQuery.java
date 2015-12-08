package fr.openwide.core.jpa.more.business.search.query;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import fr.openwide.core.jpa.more.business.sort.ISort;
import fr.openwide.core.jpa.more.business.sort.ISort.SortOrder;

public abstract class AbstractSearchQuery<T, S extends ISort<?>> implements ISearchQuery<T, S> /* NOT Serializable */ {
	
	@PersistenceContext
	protected EntityManager entityManager;
	
	protected List<S> defaultSorts;
	protected Map<S, SortOrder> sortMap;
	
	@SafeVarargs
	protected AbstractSearchQuery(S ... defaultSorts) {
		this.defaultSorts = ImmutableList.copyOf(defaultSorts);
	}
	
	// Sort
	@Override
	public ISearchQuery<T, S> sort(Map<S, SortOrder> sortMap) {
		this.sortMap = ImmutableMap.copyOf(sortMap);
		return this;
	}
	
	// May be overridden for performance purposes
	@Override
	@Transactional(readOnly = true)
	public List<T> fullList() {
		return list(0, Long.MAX_VALUE);
	}
}