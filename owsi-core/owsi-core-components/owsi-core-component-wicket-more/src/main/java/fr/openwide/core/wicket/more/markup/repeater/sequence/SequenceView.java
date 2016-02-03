package fr.openwide.core.wicket.more.markup.repeater.sequence;

import java.util.Iterator;

import org.apache.wicket.markup.repeater.AbstractPageableView;
import org.apache.wicket.model.IModel;

import fr.openwide.core.wicket.more.markup.repeater.IRefreshableOnDemandRepeater;
import fr.openwide.core.wicket.more.markup.repeater.sequence.ISequenceProvider;

public abstract class SequenceView<T> extends AbstractPageableView<T> implements IRefreshableOnDemandRepeater {

	private static final long serialVersionUID = 1L;
	
	private final ISequenceProvider<T> sequenceProvider;
	
	public SequenceView(String id, ISequenceProvider<T> sequenceProvider) {
		super(id);
		this.sequenceProvider = sequenceProvider;
	}
	
	@Override
	public void refreshItems() {
		onPopulate();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		sequenceProvider.detach();
	}
	
	@Override
	protected Iterator<IModel<T>> getItemModels(long offset, long size) {
		return new ExactTypeIterator<IModel<T>>(sequenceProvider.iterator(offset, size));
	}
	
	private static class ExactTypeIterator<T> implements Iterator<T>  {
		private final Iterator<? extends T> delegate;
		
		public ExactTypeIterator(Iterator<? extends T> delegate) {
			super();
			this.delegate = delegate;
		}

		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		@Override
		public T next() {
			return delegate.next();
		}

		@Override
		public void remove() {
			delegate.remove();
		}

	}
	
	@Override
	protected long internalGetItemCount() {
		return sequenceProvider.size();
	}
	
	public ISequenceProvider<T> getSequenceProvider() {
		return sequenceProvider;
	}

}
