package fr.openwide.core.basicapp.web.application.referencedata.component;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.wicketstuff.wiquery.core.events.StateEvent;

import fr.openwide.core.basicapp.web.application.referencedata.model.AbstractGenericListItemDataProvider;
import fr.openwide.core.jpa.more.business.generic.model.EnabledFilter;
import fr.openwide.core.jpa.more.business.generic.model.GenericListItem;
import fr.openwide.core.wicket.more.markup.html.feedback.FeedbackUtils;
import fr.openwide.core.wicket.more.markup.html.form.EnumDropDownSingleChoice;
import fr.openwide.core.wicket.more.markup.html.form.LabelPlaceholderBehavior;

public class SimpleGenericListItemSearchPanel<T extends GenericListItem<? super T>> extends Panel {
	
	private static final long serialVersionUID = 3027788723051745121L;

	public SimpleGenericListItemSearchPanel(String id, final AbstractGenericListItemDataProvider<T, ?> dataProvider,
			final Component table) {
		super(id);
		
		Form<Void> searchForm = new Form<Void>("searchForm");
		add(searchForm);
		
		searchForm.add(
				new AjaxFormSubmitBehavior(searchForm, StateEvent.CHANGE.getEventLabel()) {
					private static final long serialVersionUID = 1L;
					@Override
					protected void onSubmit(AjaxRequestTarget target) {
						// Just in case the dataProvider's content was loaded before search parameters changed
						dataProvider.detach();
						
						target.add(table);
						FeedbackUtils.refreshFeedback(target, getPage());
					}
				}
		);
		
		searchForm.add(
				new TextField<String>("label", dataProvider.getLabelModel())
						.setLabel(new ResourceModel("business.listItem.label"))
						.add(
								new LabelPlaceholderBehavior()
						),
				new EnumDropDownSingleChoice<EnabledFilter>("enabledFilter",
						dataProvider.getEnabledFilterModel(), EnabledFilter.class)
						.setLabel(new ResourceModel("business.listItem.enabledState"))
		);
	}
}