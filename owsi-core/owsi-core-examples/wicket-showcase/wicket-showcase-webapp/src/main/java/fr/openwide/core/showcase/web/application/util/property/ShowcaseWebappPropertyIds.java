package fr.openwide.core.showcase.web.application.util.property;

import fr.openwide.core.spring.property.model.AbstractPropertyIds;
import fr.openwide.core.spring.property.model.ImmutablePropertyId;

public final class ShowcaseWebappPropertyIds extends AbstractPropertyIds {
	
	private ShowcaseWebappPropertyIds() {
	}

	public static final ImmutablePropertyId<Integer> PORTFOLIO_ITEMS_PER_PAGE = immutable("portfolio.itemsPerPage");
	public static final ImmutablePropertyId<Integer> PORTFOLIO_ITEMS_PER_PAGE_USER = immutable("portfolio.itemsPerPage.user");

}
