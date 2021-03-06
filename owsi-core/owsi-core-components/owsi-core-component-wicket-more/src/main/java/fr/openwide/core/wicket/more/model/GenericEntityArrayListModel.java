package fr.openwide.core.wicket.more.model;

import java.io.Serializable;
import java.util.List;

import fr.openwide.core.commons.util.functional.Suppliers2;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;

/**
 * @deprecated use {@link CollectionCopyModel} with {@link Suppliers2} and {@link GenericEntityModel} instead : 
 * <pre>
 * {@code 
 * GenericEntityArrayListModel.of(Class<E>);
 * ->
 * CollectionCopyModel.custom(Suppliers2.<E>arrayList[AsList](), GenericEntityModel.<E>factory());
 * }
 * </pre>
 */
@Deprecated
public class GenericEntityArrayListModel<K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>>
		extends AbstractGenericEntityCollectionModel<K, E, List<E>> {
	
	private static final long serialVersionUID = 7130088291928970625L;
	
	public static <K extends Serializable & Comparable<K>, E extends GenericEntity<K, ?>>
			GenericEntityArrayListModel<K, E> of(Class<E> clazz) {
		return new GenericEntityArrayListModel<K, E>(clazz);
	}

	protected GenericEntityArrayListModel(Class<E> clazz) {
		super(clazz, Suppliers2.<E>arrayList());
	}

}
