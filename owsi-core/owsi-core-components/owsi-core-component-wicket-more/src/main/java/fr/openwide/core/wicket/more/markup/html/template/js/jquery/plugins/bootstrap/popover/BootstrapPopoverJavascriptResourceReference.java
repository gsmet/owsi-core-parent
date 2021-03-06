package fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.bootstrap.popover;

import java.util.List;

import org.apache.wicket.markup.head.HeaderItem;

import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.bootstrap.tooltip.BootstrapTooltipJavascriptResourceReference;
import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.util.AbstractCoreJQueryPluginResourceReference;

public final class BootstrapPopoverJavascriptResourceReference extends AbstractCoreJQueryPluginResourceReference {

	private static final long serialVersionUID = 7455873661174214964L;

	private static final BootstrapPopoverJavascriptResourceReference INSTANCE = new BootstrapPopoverJavascriptResourceReference();

	private BootstrapPopoverJavascriptResourceReference() {
		super(BootstrapPopoverJavascriptResourceReference.class, "popover.js");
	}

	@Override
	protected List<HeaderItem> getPluginDependencies() {
		return forReferences(BootstrapTooltipJavascriptResourceReference.get());
	}

	public static BootstrapPopoverJavascriptResourceReference get() {
		return INSTANCE;
	}

}
