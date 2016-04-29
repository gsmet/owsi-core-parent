package fr.openwide.core.showcase.web.application.task.page;

import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.openwide.core.jpa.more.business.task.model.QueuedTaskHolder;
import fr.openwide.core.jpa.more.business.task.model.QueuedTaskHolderBinding;
import fr.openwide.core.jpa.more.business.task.service.IQueuedTaskHolderManager;
import fr.openwide.core.jpa.more.business.task.service.IQueuedTaskHolderService;
import fr.openwide.core.showcase.core.business.task.model.FailedTask;
import fr.openwide.core.showcase.core.business.task.model.FailedWithBusinessExceptionTask;
import fr.openwide.core.showcase.core.business.task.model.ShowcaseTaskQueueId;
import fr.openwide.core.showcase.core.business.task.model.SuccessTask;
import fr.openwide.core.showcase.core.business.task.model.SuccessWithAlertTask;
import fr.openwide.core.showcase.core.business.task.model.SuccessWithErrorTask;
import fr.openwide.core.showcase.web.application.task.component.ShowcaseTaskQueueIdDropDownChoice;
import fr.openwide.core.showcase.web.application.task.component.TaskPortfolioPanel;
import fr.openwide.core.showcase.web.application.task.component.TaskSearchPanel;
import fr.openwide.core.showcase.web.application.task.model.TaskDataProvider;
import fr.openwide.core.showcase.web.application.util.property.ShowcaseWebappPropertyIds;
import fr.openwide.core.showcase.web.application.util.template.MainTemplate;
import fr.openwide.core.wicket.behavior.ClassAttributeAppender;
import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.link.descriptor.IPageLinkDescriptor;
import fr.openwide.core.wicket.more.link.descriptor.builder.LinkDescriptorBuilder;
import fr.openwide.core.wicket.more.markup.html.basic.PlaceholderContainer;
import fr.openwide.core.wicket.more.markup.html.feedback.FeedbackUtils;
import fr.openwide.core.wicket.more.markup.html.form.LabelPlaceholderBehavior;
import fr.openwide.core.wicket.more.model.ApplicationPropertyModel;
import fr.openwide.core.wicket.more.model.BindingModel;

public class TaskMainPage extends MainTemplate {

	private static final long serialVersionUID = 4305540423473999086L;

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskMainPage.class);

	public static final IPageLinkDescriptor linkDescriptor() {
		return new LinkDescriptorBuilder()
				.page(TaskMainPage.class)
				.build();
	}

	@SpringBean
	private IQueuedTaskHolderManager queuedTaskHolderManager;

	@SpringBean
	private IQueuedTaskHolderService queuedTaskHolderService;

	public TaskMainPage(PageParameters parameters) {
		super(parameters);
		
		final IModel<ShowcaseTaskQueueId> queueIdModel = Model.of();
		
		Form<?> form = new Form<Void>("taskForm");
		add(form);
		
		form.add(
				new ShowcaseTaskQueueIdDropDownChoice("queueId", queueIdModel)
						.setLabel(new ResourceModel("tasks.queue"))
						.add(new LabelPlaceholderBehavior())
		);
		
		form.add(new AjaxSubmitLink("createSuccessTask") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					queuedTaskHolderManager.submit(new SuccessTask(queueIdModel.getObject()));
					
					Session.get().success(getString("tasks.createTask.add.success"));
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while adding a task", e);
					Session.get().error(getString("common.error.unexpected"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		});
		
		form.add(new AjaxSubmitLink("createSuccessWithAlertTask") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					queuedTaskHolderManager.submit(new SuccessWithAlertTask(queueIdModel.getObject()));
					
					Session.get().success(getString("tasks.createTask.add.success"));
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while adding a task", e);
					Session.get().error(getString("common.error.unexpected"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		});
		
		form.add(new AjaxSubmitLink("createSuccessWithErrorTask") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					queuedTaskHolderManager.submit(new SuccessWithErrorTask(queueIdModel.getObject()));
					
					Session.get().success(getString("tasks.createTask.add.success"));
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while adding a task", e);
					Session.get().error(getString("common.error.unexpected"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		});

		form.add(new AjaxSubmitLink("createFailedTask") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					queuedTaskHolderManager.submit(new FailedTask(queueIdModel.getObject()));

					Session.get().success(getString("tasks.createTask.add.success"));
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while adding a task", e);
					Session.get().error(getString("common.error.unexpected"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		});
		
		form.add(new AjaxSubmitLink("createFailedWithBusinessExceptionTask") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					queuedTaskHolderManager.submit(new FailedWithBusinessExceptionTask(queueIdModel.getObject()));
					
					Session.get().success(getString("tasks.createTask.add.success"));
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while adding a task", e);
					Session.get().error(getString("common.error.unexpected"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		});

		form.add(new AjaxSubmitLink("createStopQueueTask") {
			private static final long serialVersionUID = 1L;
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				try {
					queuedTaskHolderManager.submit(new SuccessTask(queueIdModel.getObject()));
					queuedTaskHolderManager.stop();

					Session.get().success(getString("tasks.createTask.add.success"));
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while adding a task", e);
					Session.get().error(getString("common.error.unexpected"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		});

		AjaxLink<Void> startManager = new AjaxLink<Void>("start") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				add(new ClassAttributeAppender(queuedTaskHolderManager.isActive() ? "disabled" : ""));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					if (queuedTaskHolderManager.isAvailableForAction()) {
						queuedTaskHolderManager.start();
						getSession().success(getString("tasks.manager.start.success"));
					} else {
						getSession().error(getString("tasks.manager.unavailable"));
					}

					FeedbackUtils.refreshFeedback(target, getPage());
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while trying to stop the queue.", e);
					Session.get().error(getString("common.error.unexpectedr"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		};
		add(startManager);

		AjaxLink<Void> stopManager = new AjaxLink<Void>("stop") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				add(new ClassAttributeAppender(queuedTaskHolderManager.isActive() ? "" : "disabled"));
			}

			@Override
			public void onClick(AjaxRequestTarget target) {
				try {
					if (queuedTaskHolderManager.isAvailableForAction()) {
						queuedTaskHolderManager.stop();
						getSession().success(getString("tasks.manager.stop.success"));
					} else {
						getSession().error(getString("tasks.manager.unavailable"));
					}

					FeedbackUtils.refreshFeedback(target, getPage());
					target.add(getPage());
				} catch (Exception e) {
					LOGGER.error("Unexpected error while trying to stop the queue.", e);
					Session.get().error(getString("common.error.unexpectedr"));
				}

				FeedbackUtils.refreshFeedback(target, getPage());
			}
		};
		add(stopManager);

		IModel<List<QueuedTaskHolder>> queuedTaskHoldersListModel = new LoadableDetachableModel<List<QueuedTaskHolder>>() {
			private static final long serialVersionUID = 1L;

			@Override
			protected List<QueuedTaskHolder> load() {
				return queuedTaskHolderService.list();
			}
		};

		add(new ListView<QueuedTaskHolder>("queuedTaskHolders", queuedTaskHoldersListModel) {
			private static final long serialVersionUID = 1L;

			QueuedTaskHolderBinding queuedTaskHolderBinding = new QueuedTaskHolderBinding();

			@Override
			protected void populateItem(ListItem<QueuedTaskHolder> item) {
				item.add(new Label("id", BindingModel.of(item.getModel(), queuedTaskHolderBinding.id())));
				item.add(new Label("name", BindingModel.of(item.getModel(), queuedTaskHolderBinding.name())));
				item.add(new Label("status", BindingModel.of(item.getModel(), queuedTaskHolderBinding.status())));
			}
		});

		add(new PlaceholderContainer("placeholder").condition(Condition.collectionModelNotEmpty(queuedTaskHoldersListModel)));
		
		// Search & porfolio
		TaskDataProvider dataProvider = new TaskDataProvider();
		
		TaskPortfolioPanel portfolio = new TaskPortfolioPanel("portfolio", dataProvider, ApplicationPropertyModel.of(ShowcaseWebappPropertyIds.PORTFOLIO_ITEMS_PER_PAGE).getObject());
		add(
				new TaskSearchPanel("search", portfolio.getPageable(), dataProvider),
				portfolio
		);
	}

	@Override
	protected Class<? extends WebPage> getFirstMenuPage() {
		return TaskMainPage.class;
	}

	@Override
	protected Class<? extends WebPage> getSecondMenuPage() {
		return null;
	}
}
