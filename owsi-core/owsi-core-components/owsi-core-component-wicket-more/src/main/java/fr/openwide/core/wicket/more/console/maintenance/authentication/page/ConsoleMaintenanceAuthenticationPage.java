package fr.openwide.core.wicket.more.console.maintenance.authentication.page;

import org.apache.wicket.Application;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;

import fr.openwide.core.jpa.security.business.person.model.GenericUser;
import fr.openwide.core.jpa.security.business.person.service.IGenericUserService;
import fr.openwide.core.spring.util.StringUtils;
import fr.openwide.core.wicket.more.AbstractCoreSession;
import fr.openwide.core.wicket.more.console.maintenance.template.ConsoleMaintenanceTemplate;
import fr.openwide.core.wicket.more.console.template.ConsoleTemplate;
import fr.openwide.core.wicket.more.console.template.style.ConsoleSignInLessCssResourceReference;
import fr.openwide.core.wicket.more.link.descriptor.IPageLinkDescriptor;
import fr.openwide.core.wicket.more.link.descriptor.builder.LinkDescriptorBuilder;
import fr.openwide.core.wicket.more.markup.html.form.LabelPlaceholderBehavior;
import fr.openwide.core.wicket.more.security.page.LoginSuccessPage;

public class ConsoleMaintenanceAuthenticationPage<U extends GenericUser<U, ?>> extends ConsoleMaintenanceTemplate {

	private static final long serialVersionUID = 3401416708867386953L;

	@SpringBean
	private IGenericUserService<U> genericUserService;

	public static final IPageLinkDescriptor linkDescriptor() {
		return new LinkDescriptorBuilder()
				.page(ConsoleMaintenanceAuthenticationPage.class)
				.build();
	}
	
	private FormComponent<String> userNameField;
	
	public ConsoleMaintenanceAuthenticationPage(PageParameters parameters) {
		super(parameters);
		
		addHeadPageTitleKey("console.maintenance.authentication");
		
		Form<Void> signInForm = new Form<Void>("signInForm") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit() {
				if (StringUtils.hasText(userNameField.getModelObject())) {
					U genericUser = genericUserService.getByUserName(userNameField.getModelObject());
					
					if (genericUser != null) {
						AbstractCoreSession.get().signInAs(userNameField.getModelObject());
						AbstractCoreSession.get().success(new StringResourceModel("console.authentication.success")
								.setParameters(userNameField.getModelObject()).getObject());
					} else {
						AbstractCoreSession.get().error(getString("signIn.error.unknown"));
					}
				} else {
					AbstractCoreSession.get().error(getString("signIn.error.unknown"));
				}
				throw LoginSuccessPage.linkDescriptor().newRestartResponseException();
			}
		};
		add(signInForm);
		
		userNameField = new RequiredTextField<String>("userName", Model.of(""));
		userNameField.setLabel(new ResourceModel("console.signIn.userName"));
		userNameField.add(new LabelPlaceholderBehavior());
		userNameField.setOutputMarkupId(true);
		signInForm.add(userNameField);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(Application.get().getJavaScriptLibrarySettings().getJQueryReference()));
		response.render(CssHeaderItem.forReference(ConsoleSignInLessCssResourceReference.get()));
	}

	@Override
	protected Class<? extends ConsoleTemplate> getMenuItemPageClass() {
		return ConsoleMaintenanceAuthenticationPage.class;
	}

}
