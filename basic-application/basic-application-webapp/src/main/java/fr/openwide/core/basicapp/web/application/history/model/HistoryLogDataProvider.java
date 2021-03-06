package fr.openwide.core.basicapp.web.application.history.model;

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import fr.openwide.core.basicapp.core.business.history.model.HistoryLog;
import fr.openwide.core.basicapp.core.business.history.model.atomic.HistoryEventType;
import fr.openwide.core.basicapp.core.business.history.search.IHistoryLogSearchQuery;
import fr.openwide.core.basicapp.core.business.user.model.User;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.more.business.history.search.HistoryLogSort;
import fr.openwide.core.jpa.more.business.search.query.ISearchQuery;
import fr.openwide.core.wicket.more.markup.html.sort.model.CompositeSortModel;
import fr.openwide.core.wicket.more.markup.html.sort.model.CompositeSortModel.CompositingStrategy;
import fr.openwide.core.wicket.more.model.AbstractSearchQueryDataProvider;
import fr.openwide.core.wicket.more.model.GenericEntityModel;
import fr.openwide.core.wicket.more.util.model.Detachables;

public class HistoryLogDataProvider extends AbstractSearchQueryDataProvider<HistoryLog, HistoryLogSort> {

	private static final long serialVersionUID = 1604966591810765209L;

	private final IModel<? extends User> subjectModel;
	
	private final IModel<Date> dateMinModel = new Model<Date>();
	private final IModel<Date> dateMaxModel = new Model<Date>();
	
	private final IModel<? extends GenericEntity<?, ?>> objectModel;
	
	private final CompositeSortModel<HistoryLogSort> sortModel =
			new CompositeSortModel<>(CompositingStrategy.LAST_ONLY, HistoryLogSort.DATE);
	
	private Set<HistoryEventType> mandatoryDifferencesEventTypes = EnumSet.noneOf(HistoryEventType.class);
	
	public static HistoryLogDataProvider subject(IModel<? extends User> subjectModel) {
		return new HistoryLogDataProvider(subjectModel, new GenericEntityModel<>());
	}

	public static HistoryLogDataProvider object(IModel<? extends GenericEntity<?, ?>> objectModel) {
		return new HistoryLogDataProvider(new GenericEntityModel<Long, User>(), objectModel);
	}

	public HistoryLogDataProvider(IModel<? extends User> subjectModel, IModel<? extends GenericEntity<?, ?>> objectModel) {
		this.subjectModel = subjectModel;
		this.objectModel = objectModel;
	}

	@Override
	public IModel<HistoryLog> model(HistoryLog object) {
		return GenericEntityModel.of(object);
	}
	
	@Override
	protected ISearchQuery<HistoryLog, HistoryLogSort> getSearchQuery() {
		return createSearchQuery(IHistoryLogSearchQuery.class)
				.subject(subjectModel.getObject())
				.date(dateMinModel.getObject(), dateMaxModel.getObject())
				.object(objectModel.getObject())
				.differencesMandatoryFor(mandatoryDifferencesEventTypes)
				.sort(sortModel.getObject());
	}
	
	@Override
	public void detach() {
		super.detach();
		Detachables.detach(subjectModel, dateMinModel, dateMaxModel, objectModel, sortModel);
	}
	
	public IModel<Date> getDateMinModel() {
		return dateMinModel;
	}
	
	public IModel<Date> getDateMaxModel() {
		return dateMaxModel;
	}
	
	public CompositeSortModel<HistoryLogSort> getSortModel() {
		return sortModel;
	}
	
	public HistoryLogDataProvider addMandatoryDifferenceEventType(HistoryEventType eventType) {
		mandatoryDifferencesEventTypes.add(eventType);
		return this;
	}
}
