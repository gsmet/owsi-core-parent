package fr.openwide.core.wicket.more.markup.repeater.table.builder.toolbar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

import fr.openwide.core.jpa.more.business.sort.ISort;
import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.markup.html.basic.EnclosureBehavior;
import fr.openwide.core.wicket.more.markup.html.factory.IOneParameterComponentFactory;
import fr.openwide.core.wicket.more.markup.repeater.table.CoreDataTable;
import fr.openwide.core.wicket.more.util.model.Detachables;

public class CustomizableToolbarElementBuilder<T, S extends ISort<?>>
		implements IOneParameterComponentFactory<Component, CoreDataTable<T, S>> {

	private static final long serialVersionUID = 8327298869880437772L;

	private final IOneParameterComponentFactory<Component, CoreDataTable<T, S>> factory;

	private final int colspanAccumulation;

	private Integer colspan = 1;

	private final Set<String> cssClasses = Sets.newHashSet();

	private Condition condition = Condition.alwaysTrue();

	public CustomizableToolbarElementBuilder(int colspanAccumulation, IOneParameterComponentFactory<Component, CoreDataTable<T, S>> factory) {
		super();
		this.colspanAccumulation = checkNotNull(colspanAccumulation);
		this.factory = checkNotNull(factory);
	}

	@Override
	public Component create(String wicketId, final CoreDataTable<T, S> dataTable) {
		IModel<Integer> computedColspanModel = new LoadableDetachableModel<Integer>() {
			private static final long serialVersionUID = 1L;
			@Override
			protected Integer load() {
				if (colspan == null) {
					dataTable.configure(); // Update the displayed columns
					return dataTable.getDisplayedColumns().size() - colspanAccumulation;
				}
				
				Map<IColumn<T, S>, Condition> columnToConditionMap = dataTable.getColumnToConditionMap();
				
				FluentIterable<Entry<IColumn<T, S>, Condition>> columnEntryIterable =
						FluentIterable.from(columnToConditionMap .entrySet())
						.skip(colspanAccumulation);
				
				// Make sure we don't span further than the table width
				int cappedColspan;
				int dataTableColumnsSize = columnToConditionMap.size();
				if (colspanAccumulation + colspan > dataTableColumnsSize) {
					cappedColspan = dataTableColumnsSize - colspanAccumulation;
				} else {
					cappedColspan = colspan;
				}
				
				// Make sure we don't span further than the column width
				columnEntryIterable = columnEntryIterable.limit(cappedColspan);
				
				int colspanWithoutHiddenColumns = cappedColspan;
				for (Entry<IColumn<T, S>, Condition> column : columnEntryIterable) {
					Condition columnCondition = column.getValue();
					if (columnCondition != null && !columnCondition.applies()) {
						--colspanWithoutHiddenColumns;
					}
				}
				
				return Math.max(colspanWithoutHiddenColumns, 0);
			}
		};
		
		return factory.create(wicketId, dataTable)
				.add(
						new AttributeModifier("colspan", computedColspanModel),
						new EnclosureBehavior().condition(
								condition
								.and(
										Condition.predicate(computedColspanModel, Range.atLeast(1))
								)
						),
						new AttributeModifier("class", Joiner.on(" ").join(cssClasses))
				);
	}

	public Integer getColspan() {
		return colspan;
	}

	public void colspan(Integer colspan) {
		this.colspan = colspan;
	}

	public void full() {
		this.colspan = null;
	}

	public void when(Condition condition) {
		this.condition = condition;
	}

	public void withClass(String cssClass) {
		this.cssClasses.add(cssClass);
	}

	@Override
	public void detach() {
		Detachables.detach(factory, condition);
	}

}
