package fr.openwide.core.wicket.more.markup.html.bootstrap.label.component;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import fr.openwide.core.commons.util.functional.Predicates2;
import fr.openwide.core.wicket.behavior.ClassAttributeAppender;
import fr.openwide.core.wicket.more.markup.html.basic.ComponentBooleanProperty;
import fr.openwide.core.wicket.more.markup.html.basic.EnclosureBehavior;
import fr.openwide.core.wicket.more.markup.html.bootstrap.label.behavior.BootstrapColorBehavior;
import fr.openwide.core.wicket.more.markup.html.bootstrap.label.renderer.BootstrapLabelRenderer;

public class BootstrapLabel<T> extends GenericPanel<T> {

	private static final long serialVersionUID = -7040646675697285281L;

	public BootstrapLabel(String id, IModel<T> model, final BootstrapLabelRenderer<? super T> renderer) {
		super(id, model);
		
		IModel<String> labelModel = renderer.asModel(model);
		
		add(
				new WebMarkupContainer("icon")
						.add(new ClassAttributeAppender(renderer.asIconCssClassModel(model))),
				new Label("label", labelModel)
		);
		
		add(
				BootstrapColorBehavior.label(renderer.asColorModel(model)),
				new EnclosureBehavior(ComponentBooleanProperty.VISIBLE).model(Predicates2.hasText(), labelModel)
		);
		
	}

}
