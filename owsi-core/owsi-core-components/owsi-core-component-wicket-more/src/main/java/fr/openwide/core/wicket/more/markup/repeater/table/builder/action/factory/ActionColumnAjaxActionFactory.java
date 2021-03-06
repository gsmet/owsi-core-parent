package fr.openwide.core.wicket.more.markup.repeater.table.builder.action.factory;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.model.IModel;

import fr.openwide.core.wicket.more.markup.html.action.IOneParameterAjaxAction;
import fr.openwide.core.wicket.more.markup.html.basic.ComponentBooleanProperty;
import fr.openwide.core.wicket.more.markup.html.basic.EnclosureBehavior;
import fr.openwide.core.wicket.more.markup.html.factory.IOneParameterComponentFactory;

public class ActionColumnAjaxActionFactory<T> implements IOneParameterComponentFactory<AjaxLink<T>, IModel<T>> {

	private static final long serialVersionUID = 1L;
	
	private final IOneParameterAjaxAction<? super IModel<T>> action;
	
	public ActionColumnAjaxActionFactory(IOneParameterAjaxAction<? super IModel<T>> action) {
		super();
		this.action = action;
	}
	
	@Override
	public AjaxLink<T> create(String wicketId, final IModel<T> parameter) {
		AjaxLink<T> link = new AjaxLink<T>(wicketId, parameter) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				action.updateAjaxAttributes(attributes, parameter);
			}
			@Override
			public void onClick(AjaxRequestTarget target) {
				action.execute(target, parameter);
			}
		};
		
		link.add(
				new EnclosureBehavior(ComponentBooleanProperty.VISIBLE)
						.condition(action.getActionAvailableCondition(parameter))
		);
		
		return link;
	}
	
	@Override
	public void detach() {
		action.detach();
	}

}
