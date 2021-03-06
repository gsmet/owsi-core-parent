package fr.openwide.core.basicapp.web.application.administration.component;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import fr.openwide.core.basicapp.core.business.user.model.User;
import fr.openwide.core.basicapp.core.business.user.service.IUserService;
import fr.openwide.core.basicapp.core.util.binding.Bindings;
import fr.openwide.core.basicapp.web.application.BasicApplicationSession;
import fr.openwide.core.basicapp.web.application.common.typedescriptor.user.UserTypeDescriptor;
import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.wicket.markup.html.link.EmailLink;
import fr.openwide.core.wicket.more.link.descriptor.generator.IPageLinkGenerator;
import fr.openwide.core.wicket.more.link.model.PageModel;
import fr.openwide.core.wicket.more.markup.html.image.BooleanIcon;
import fr.openwide.core.wicket.more.model.BindingModel;
import fr.openwide.core.wicket.more.model.ReadOnlyModel;

public class UserPortfolioPanel<U extends User> extends AbstractUserPortfolioPanel<U> {

	private static final long serialVersionUID = 6030960404037116497L;

	@SpringBean
	private IUserService userService;

	private final UserTypeDescriptor<U> typeDescriptor;

	public UserPortfolioPanel(String id, IDataProvider<U> dataProvider, UserTypeDescriptor<U> typeDescriptor, int itemsPerPage) {
		super(id, dataProvider, itemsPerPage, "administration.user.count");
		this.typeDescriptor = typeDescriptor;
	}

	protected IPageLinkGenerator getPageLinkGenerator(IModel<U> userModel) {
		return typeDescriptor.administrationTypeDescriptor().description(userModel, PageModel.of(getPage()));
	}

	@Override
	protected void addItemColumns(Item<U> item, IModel<? extends U> itemModel) {
		item.add(
				getPageLinkGenerator(item.getModel()).link("userNameLink").setBody(BindingModel.of(itemModel, Bindings.user().userName())),
				new Label("firstName", BindingModel.of(itemModel, Bindings.user().firstName())),
				new Label("lastName", BindingModel.of(itemModel, Bindings.user().lastName())),
				new BooleanIcon("active", BindingModel.of(itemModel, Bindings.user().active())).hideIfNullOrFalse(),
				new EmailLink("email", BindingModel.of(itemModel, Bindings.user().email()))
		);
	}

	@Override
	protected boolean hasWritePermissionOn(IModel<? extends U> itemModel) {
		return BasicApplicationSession.get().hasRoleAdmin();
	}

	@Override
	protected void doDeleteItem(IModel<? extends U> itemModel) throws ServiceException, SecurityServiceException {
		userService.delete(itemModel.getObject()); 
	}
	
	@Override
	protected IModel<String> getDeleteConfirmationTitleModel(IModel<? extends U> userModel) {
		return new StringResourceModel("administration.user.delete.confirmation.title")
				.setParameters(userModel.getObject().getFullName());
	}

	@Override
	protected IModel<String> getDeleteConfirmationTextModel(IModel<? extends U> userModel) {
		return new StringResourceModel("administration.user.delete.confirmation.text")
				.setParameters(userModel.getObject().getFullName());
	}

	@Override
	protected boolean isActionAvailable() {
		return true;
	}

	@Override 
	protected MarkupContainer getActionLink(String id, IModel<? extends U> userModel) { 
		return getPageLinkGenerator(ReadOnlyModel.of(userModel)).link(id); 
	}

	@Override
	protected boolean isDeleteAvailable() {
		return BasicApplicationSession.get().hasRoleAdmin();
	}

	@Override
	protected boolean isEditAvailable() {
		return false;
	}

}
