package fr.openwide.core.wicket.more.link.descriptor.builder.state;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;

import fr.openwide.core.wicket.more.link.descriptor.generator.IPageLinkGenerator;

public interface IBasePageInstanceState {
	
	IPageInstanceState<IPageLinkGenerator> pageInstance(Page pageInstance);

	IPageInstanceState<IPageLinkGenerator> pageInstance(IModel<? extends Page> pageInstanceModel);

}
