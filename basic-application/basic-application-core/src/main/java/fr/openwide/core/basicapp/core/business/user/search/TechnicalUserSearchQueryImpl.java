package fr.openwide.core.basicapp.core.business.user.search;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import fr.openwide.core.basicapp.core.business.user.model.TechnicalUser;

@Component
@Scope("prototype")
public class TechnicalUserSearchQueryImpl extends AbstractUserSearchQueryImpl<TechnicalUser> implements ITechnicalUserSearchQuery {

	
	protected TechnicalUserSearchQueryImpl() {
		super(TechnicalUser.class);
	}
}