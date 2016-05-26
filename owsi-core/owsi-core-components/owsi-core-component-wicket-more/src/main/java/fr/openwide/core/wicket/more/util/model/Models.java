package fr.openwide.core.wicket.more.util.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IComponentAssignedModel;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

import fr.openwide.core.commons.util.collections.Iterators2;
import fr.openwide.core.wicket.more.markup.repeater.collection.ICollectionModel;
import fr.openwide.core.wicket.more.markup.repeater.collection.IItemModelAwareCollectionModel;
import fr.openwide.core.wicket.more.markup.repeater.map.IMapModel;

public final class Models {

	private Models() {
	}

	@SuppressWarnings("unchecked") // SerializableModelFactory works for any T extending Serializable
	public static <T extends Serializable> Function<T, Model<T>> serializableModelFactory() {
		return (Function<T, Model<T>>) (Object) SerializableModelFactory.INSTANCE;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"}) // SerializableModelFactory works for any T extending Serializable
	private enum SerializableModelFactory implements Function<Serializable, Model> {
		INSTANCE;
		
		@Override
		public Model apply(Serializable input) {
			return new Model(input);
		}
	}

	@SuppressWarnings("unchecked") // ModelGetObjectFunction works for any T
	public static <T> Function<? super IModel<? extends T>, T> getObject() {
		return (Function<? super IModel<? extends T>, T>) ModelGetObjectFunction.INSTANCE;
	}
	
	@SuppressWarnings("rawtypes") // ModelGetObjectFunction works for any T
	private enum ModelGetObjectFunction implements Function<IModel, Object> {
		INSTANCE;
		
		@Override
		public Object apply(IModel input) {
			return input == null ? null : input.getObject();
		}
	}

	/**
	 * A "placeholder" model, for use when the actual behavior of getObject() and setObject() do not matter.
	 * 
	 * <p>{@link #getObject()} always returns null and {@link #setObject(Object)} and {@link #detach()} do nothing.
	 */
	@SuppressWarnings("unchecked") // Works for any T
	public static <T> IModel<T> placeholder() {
		return (IModel<T>) PlaceholderModel.INSTANCE;
	}
		
	private enum PlaceholderModel implements IModel<Object> {
		INSTANCE;
		private Object readResolve() {
			return INSTANCE;
		}
		@Override
		public Object getObject() {
			return null;
		}
		
		@Override
		public void setObject(Object object) {
			// Does nothing
		}
		
		@Override
		public void detach() {
			// Does nothing
		}
	};
	
	/**
	 * A static model of <code>Map<String, Object></code>. Useful as a data model for {@link StringResourceModel}.
	 */
	public static MapModelBuilder<String, Object> dataMap() {
		return new MapModelBuilder<String, Object>();
	}
	
	public static class MapModelBuilder<K, V> {
		private ImmutableMap.Builder<K, IModel<? extends V>> delegate = ImmutableMap.builder();
		public MapModelBuilder<K, V> put(K key, V value) {
			delegate.put(key, transientModel(value));
			return this;
		}
		public MapModelBuilder<K, V> put(K key, IModel<? extends V> valueModel) {
			delegate.put(key, valueModel);
			return this;
		}
		public IModel<Map<K, V>> build() {
			return new MapModelBuilderMap<>(delegate.build());
		}

		private static class MapModelBuilderMap<K, V> extends LoadableDetachableModel<Map<K, V>> {
			private static final long serialVersionUID = 1L;
			
			private Map<K, IModel<? extends V>> source;
			
			public MapModelBuilderMap(Map<K, IModel<? extends V>> source) {
				super();
				this.source = source;
			}
			
			@Override
			protected Map<K, V> load() {
				return Maps.transformValues(source, Models.<V>getObject());
			}
			
			@Override
			protected void onDetach() {
				super.onDetach();
				for (IDetachable detachable : source.values()) {
					detachable.detach();
				}
			}
		}
	}

	/**
	 * A constant, non-serializable model.
	 * <p>Useful when calling 
	 */
	public static <T> IModel<T> transientModel(T value) {
		return new TransientModel<>(value);
	}
	
	private static class TransientModel<T> extends AbstractReadOnlyModel<T> {
		
		private static final long serialVersionUID = -2160512073899616819L;
		
		private final T value;
		
		public TransientModel(T value) {
			super();
			this.value = value;
		}
		
		@Override
		public T getObject() {
			return value;
		}
		
	}

	/**
	 * @param model The model to be wrapped.
	 * @param component The component to be used as a wrapping parameter.
	 * @return The wrapped model, or the original model if it does not implement {@link IComponentAssignedModel}.
	 */
	public static <T> IModel<T> wrap(IModel<T> model, Component component) {
		if (model instanceof IComponentAssignedModel) {
			return ((IComponentAssignedModel<T>)model).wrapOnAssignment(component);
		} else {
			return model;
		}
	}
	
	public static <T, C extends Collection<T>, M extends IModel<T>> IItemModelAwareCollectionModel<T, C, M> filter(
			IItemModelAwareCollectionModel<T, C, M> unfiltered, Predicate<M> predicate) {
		return new FilteringItemModelAwareCollectionModel<>(unfiltered, predicate);
	}

	/**
	 * A utility method that provides a sensible default implementation of {@link IItemModelAwareCollectionModel#iterator()}.
	 * <p>In particular, this implementation defers the call to {@code iterator} on the underlying {@link Iterable}.
	 * This is necessary because of how RefreshingView works: it first gets the iterator and *then* detaches its items,
	 * which may indirectly detach the {@link ICollectionModel} and thus trigger changes to the
	 * underlying {@link Iterable}.
	 * @param iterable The model iterable.
	 * @return The model iterator.
	 */
	public static <T> Iterator<T> collectionModelIterator(Iterable<T> iterable) {
		Iterator<T> iterator = Iterators2.deferred(iterable);
		return Iterators.unmodifiableIterator(iterator);
	}

	/**
	 * A utility method that provides a sensible default implementation of {@link ICollectionModel#iterator(long, long)}.
	 * <p>In particular, this implementation defers the call to {@code iterator} on the underlying {@link Iterable}.
	 * This is necessary because of how RefreshingView works: it first gets the iterator and *then* detaches its items,
	 * which may indirectly detach the {@link ICollectionModel} and thus trigger changes to the
	 * underlying {@link Iterable}.
	 * @param iterable The model iterable.
	 * @param offset The offset the returned iterator should start from.
	 * @param limit The maximum number of items the returned iterator should provide.
	 * @return The model iterator.
	 */
	public static <T> Iterator<T> collectionModelIterator(Iterable<T> iterable, long offset, long limit) {
		Iterable<T> offsetedIterable = Iterables.skip(
				iterable, Ints.saturatedCast(offset)
		);
		return Iterators.limit(collectionModelIterator(offsetedIterable), Ints.saturatedCast(limit));
	}
	

	/**
	 * A utility method that provides a sensible default implementation for {@link IMapModel#valueModel(IModel)}.
	 */
	public static <K, V> IModel<V> mapModelValueModel(final IMapModel<K, V, ?> mapModel, final IModel<? extends K> keyModel) {
		return new IModel<V>() {
			private static final long serialVersionUID = 1L;
			@Override
			public void detach() {
				mapModel.detach();
				keyModel.detach();
			}
			@Override
			public V getObject() {
				Map<K, V> map = mapModel.getObject();
				return map == null ? null : map.get(keyModel.getObject());
			}
			@Override
			public void setObject(V object) {
				mapModel.put(keyModel.getObject(), object);
			}
		};
	}
}
