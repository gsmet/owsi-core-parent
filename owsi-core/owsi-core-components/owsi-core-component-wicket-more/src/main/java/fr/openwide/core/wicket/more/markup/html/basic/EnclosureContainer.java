package fr.openwide.core.wicket.more.markup.html.basic;

import fr.openwide.core.wicket.more.markup.html.basic.impl.AbstractConfigurableComponentBooleanPropertyContainer;

public class EnclosureContainer extends AbstractConfigurableComponentBooleanPropertyContainer<EnclosureContainer> {

	private static final long serialVersionUID = 8163938380844150417L;

	public EnclosureContainer(String id) {
		this(id, ComponentBooleanProperty.VISIBLE);
	}

	public EnclosureContainer(String id, ComponentBooleanProperty property) {
		super(id, new EnclosureBehavior(property));
	}
	
	@Override
	protected EnclosureContainer thisAsT() {
		return this;
	}

}
