package fr.openwide.core.wicket.more.model;

import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import fr.openwide.core.commons.util.collections.PartitionDiscreteDomain;

public class ContiguousSetModel<C extends Comparable<?>> extends AbstractReadOnlyModel<ContiguousSet<C>> {
	
	private static final long serialVersionUID = 6151543937199662328L;
	
	public static <C extends Comparable<C>> ContiguousSetModel<C> create(
			IModel<? extends Range<C>> rangeModel, DiscreteDomain<C> discreteDomain) {
		return new ContiguousSetModel<>(rangeModel, discreteDomain);
	}

	private final IModel<? extends Range<C>> rangeModel;
	
	private final DiscreteDomain<C> discreteDomain;

	public ContiguousSetModel(IModel<? extends Range<C>> rangeModel, DiscreteDomain<C> discreteDomain) {
		super();
		this.rangeModel = rangeModel;
		this.discreteDomain = discreteDomain;
	}

	@Override
	public ContiguousSet<C> getObject() {
		Range<C> range = rangeModel.getObject();
		if (range == null) {
			return null;
		} else {
			if (discreteDomain instanceof PartitionDiscreteDomain) {
				range = ((PartitionDiscreteDomain<C>) discreteDomain).alignOut(range);
			}
			return ContiguousSet.create(range, discreteDomain);
		}
	}
}