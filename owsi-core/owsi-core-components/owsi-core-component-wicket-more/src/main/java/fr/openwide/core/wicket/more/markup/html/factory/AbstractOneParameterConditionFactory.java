package fr.openwide.core.wicket.more.markup.html.factory;

/**
 * @deprecated Use {@link AbstractDetachableFactory AbstractDetachableFactory&lt;T, Condition&gt;} instead.
 */
@Deprecated
public abstract class AbstractOneParameterConditionFactory<T> implements IOneParameterConditionFactory<T> {

	private static final long serialVersionUID = 712071199355219097L;

	@Override
	public void detach() {
		// nothing to do
	}

}
