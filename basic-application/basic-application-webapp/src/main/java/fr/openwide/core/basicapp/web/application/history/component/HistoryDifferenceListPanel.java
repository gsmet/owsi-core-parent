package fr.openwide.core.basicapp.web.application.history.component;

import java.util.List;

import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import fr.openwide.core.basicapp.core.business.history.model.HistoryDifference;
import fr.openwide.core.basicapp.web.application.history.component.factory.IHistoryComponentFactory;
import fr.openwide.core.basicapp.web.application.history.renderer.HistoryDifferencePathRenderer;
import fr.openwide.core.wicket.markup.html.basic.CoreLabel;
import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.markup.html.collection.GenericEntityCollectionView;

public class HistoryDifferenceListPanel extends GenericPanel<List<HistoryDifference>> {

	private static final long serialVersionUID = 1188689543635870482L;
	
	private final IHistoryComponentFactory historyComponentFactory;

	public HistoryDifferenceListPanel(String id, IModel<List<HistoryDifference>> model,
			IHistoryComponentFactory historyComponentFactory) {
		super(id, model);
		this.historyComponentFactory = historyComponentFactory;
		add(Condition.collectionModelNotEmpty(getModel()).thenShowInternal());
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(new GenericEntityCollectionView<HistoryDifference>("item", getModel()) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(Item<HistoryDifference> item) {
				item.add(
						new CoreLabel("path", HistoryDifferencePathRenderer.get().asModel(item.getModel())),
						historyComponentFactory.create("difference", item.getModel())
				);
			}
		});
	}
}