package fr.openwide.core.basicapp.web.application.common.typedescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import org.javatuples.Pair;

import com.google.common.collect.Maps;

import fr.openwide.core.basicapp.web.application.common.util.ResourceKeyGenerator;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.util.HibernateUtils;

public abstract class AbstractGenericEntityTypeDescriptor<T extends AbstractGenericEntityTypeDescriptor<?, E>, E extends GenericEntity<?, ?>> implements Serializable {

	private static final long serialVersionUID = 4551490933539106231L;

	private static final Map<Pair<Class<?>, Class<?>>, AbstractGenericEntityTypeDescriptor<?, ?>> ALL = Maps.newHashMap();

	private final Class<E> clazz;

	private final ResourceKeyGenerator resourceKeyGenerator;

	protected AbstractGenericEntityTypeDescriptor(Class<?> typeDescriptorClass, Class<E> clazz, String name) {
		ALL.put(Pair.<Class<?>, Class<?>>with(typeDescriptorClass, clazz), this);
		this.clazz = checkNotNull(clazz);
		this.resourceKeyGenerator = ResourceKeyGenerator.of(checkNotNull(name));
	}

	@SuppressWarnings("unchecked")
	protected static final <T extends AbstractGenericEntityTypeDescriptor<?, E>, E extends GenericEntity<?, ?>> T get(Class<?> typeDescriptorClass, E entity) {
		if (entity == null) {
			return null;
		}
		
		AbstractGenericEntityTypeDescriptor<?, ?> specificTypeDescriptor = ALL.get(Pair.<Class<?>, Class<?>>with(typeDescriptorClass, HibernateUtils.getClass(entity)));
		if (specificTypeDescriptor != null) {
			return (T) specificTypeDescriptor;
		}
		
		// fallback
		for (Entry<Pair<Class<?>, Class<?>>, AbstractGenericEntityTypeDescriptor<?, ?>> entry : ALL.entrySet()) {
			if (entry.getKey().getValue0().equals(typeDescriptorClass) && entry.getKey().getValue1().isInstance(entity)) {
				return (T) entry.getValue();
			}
		}
		
		throw new IllegalStateException("Unknown type for entity " + entity);
	}

	public Class<E> getEntityClass() {
		return clazz;
	}

	public ResourceKeyGenerator business() {
		return resourceKeyGenerator.withPrefix("business");
	}

	public ResourceKeyGenerator resourceKeyGenerator() {
		return resourceKeyGenerator;
	}

	protected abstract Object readResolve();

}
