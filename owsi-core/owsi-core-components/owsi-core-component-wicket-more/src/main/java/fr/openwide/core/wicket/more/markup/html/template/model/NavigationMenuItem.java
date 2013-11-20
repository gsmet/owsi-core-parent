package fr.openwide.core.wicket.more.markup.html.template.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import fr.openwide.core.wicket.more.link.descriptor.generator.IPageLinkGenerator;

public class NavigationMenuItem implements IDetachable {

	private static final long serialVersionUID = -833923931725195545L;
	
	private IModel<String> labelModel;
	
	private IPageLinkGenerator pageLinkGenerator;
	
	private Class<? extends Page> pageClass;
	
	private PageParameters pageParameters;
	
	private final List<NavigationMenuItem> subMenuItems = Lists.newArrayList();
	
	protected NavigationMenuItem(NavigationMenuItem wrapped) {
		setLabelModel(wrapped.getLabelModel());
		setPageLinkGenerator(wrapped.getPageLinkGenerator());
		setPageClass(wrapped.getPageClass());
		setPageParameters(wrapped.getPageParameters());
		setSubMenuItems(wrapped.getSubMenuItems());
	}
	
	public NavigationMenuItem(IModel<String> labelModel) {
		this.labelModel = labelModel;
	}
	
	public NavigationMenuItem(IModel<String> labelModel, Class<? extends Page> pageClass, PageParameters pageParameters,
			IPageLinkGenerator pageLinkGenerator, Collection<NavigationMenuItem> subMenuItems) {
		this.labelModel = labelModel;
		this.pageClass = pageClass;
		this.pageParameters = pageParameters;
		this.pageLinkGenerator = pageLinkGenerator;
		this.subMenuItems.addAll(subMenuItems);
	}
	
	public NavigationMenuItem(IModel<String> labelModel,
			Class<? extends Page> pageClass, PageParameters pageParameters,
			IPageLinkGenerator pageLinkGenerator) {
		this(labelModel, pageClass, pageParameters, pageLinkGenerator,
				Lists.<NavigationMenuItem>newArrayListWithExpectedSize(0));
	}
	
	@Deprecated
	public NavigationMenuItem(IModel<String> labelModel, Class<? extends Page> pageClass) {
		this(labelModel, pageClass, null, null, Lists.<NavigationMenuItem>newArrayListWithExpectedSize(0));
	}
	
	@Deprecated
	public NavigationMenuItem(IModel<String> labelModel, Class<? extends Page> pageClass, PageParameters pageParameters) {
		this(labelModel, pageClass, pageParameters, null, Lists.<NavigationMenuItem>newArrayListWithExpectedSize(0));
	}
	
	@Deprecated
	public NavigationMenuItem(IModel<String> labelModel, Class<? extends Page> pageClass,
			Collection<NavigationMenuItem> subMenuItems) {
		this(labelModel, pageClass, null, null, subMenuItems);
	}
	
	@Deprecated
	public NavigationMenuItem(IModel<String> labelModel, Class<? extends Page> pageClass, PageParameters pageParameters,
			Collection<NavigationMenuItem> subMenuItems) {
		this(labelModel, pageClass, pageParameters, null, subMenuItems);
	}
	
	@Override
	public void detach() {
		if (labelModel != null) {
			labelModel.detach();
		}
		if (pageLinkGenerator != null) {
			pageLinkGenerator.detach();
		}
		
		for (NavigationMenuItem subMenuItem : subMenuItems) {
			subMenuItem.detach();
		}
	}

	public Link<Void> link(String wicketId) {
		if (pageLinkGenerator != null) {
			return pageLinkGenerator.link(wicketId);
		} else {
			return new BookmarkablePageLink<Void>(wicketId, pageClass, pageParameters);
		}
	}
	
	public boolean isActive(Class<? extends Page> selectedPage) {
		return pageClass.equals(selectedPage);
	}
	
	public boolean isAccessible() {
		if (pageLinkGenerator != null) {
			return pageLinkGenerator.isAccessible();
		} else {
			return Session.get().getAuthorizationStrategy().isInstantiationAuthorized(pageClass);
		}
	}
	
	public IModel<String> getLabelModel() {
		return labelModel;
	}
	
	public void setLabelModel(IModel<String> labelModel) {
		this.labelModel = labelModel;
	}
	
	protected IPageLinkGenerator getPageLinkGenerator() {
		return pageLinkGenerator;
	}

	protected void setPageLinkGenerator(IPageLinkGenerator pageLinkGenerator) {
		this.pageLinkGenerator = pageLinkGenerator;
	}

	protected Class<? extends Page> getPageClass() {
		return pageClass;
	}

	protected void setPageClass(Class<? extends Page> pageClass) {
		this.pageClass = pageClass;
	}

	protected PageParameters getPageParameters() {
		return pageParameters;
	}

	protected void setPageParameters(PageParameters pageParameters) {
		this.pageParameters = pageParameters;
	}
	
	public List<NavigationMenuItem> getSubMenuItems() {
		return Collections.unmodifiableList(subMenuItems);
	}
	
	public void addSubMenuItem(NavigationMenuItem subMenuItem) {
		this.subMenuItems.add(subMenuItem);
	}
	
	public void setSubMenuItems(Collection<NavigationMenuItem> subMenuItems) {
		Collection<NavigationMenuItem> menuItems = ImmutableList.copyOf(subMenuItems); // Handle collection views
		this.subMenuItems.clear();
		this.subMenuItems.addAll(menuItems);
	}

}