package fr.openwide.core.wicket.more.markup.html.factory;

import org.apache.wicket.Component;

public abstract class AbstractDecoratingParameterizedComponentFactory<C extends Component, P> extends AbstractParameterizedComponentFactory<C, P> {
	
	private static final long serialVersionUID = -6411443989774223672L;
	
	private final IParameterizedComponentFactory<? extends C, ? super P> delegate;

	public AbstractDecoratingParameterizedComponentFactory(IParameterizedComponentFactory<? extends C, ? super P> delegate) {
		super();
		this.delegate = delegate;
	}
	
	@Override
	public C create(String wicketId, P parameter) {
		C result = delegate.create(wicketId, parameter);
		return decorate(result, parameter);
	}

	protected abstract C decorate(C component, P parameter);

	@Override
	public void detach() {
		delegate.detach();
	}

}
