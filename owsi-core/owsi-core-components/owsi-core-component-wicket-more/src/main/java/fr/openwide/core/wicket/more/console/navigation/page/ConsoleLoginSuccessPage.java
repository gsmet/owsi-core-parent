package fr.openwide.core.wicket.more.console.navigation.page;

import org.apache.wicket.Page;

import fr.openwide.core.wicket.more.console.maintenance.search.page.ConsoleMaintenanceSearchPage;
import fr.openwide.core.wicket.more.security.page.LoginSuccessPage;

public class ConsoleLoginSuccessPage extends LoginSuccessPage {

	private static final long serialVersionUID = 1395214320773430444L;

	public ConsoleLoginSuccessPage() {
	}
	
	@Override
	protected Class<? extends Page> getDefaultRedirectPage() {
		return ConsoleMaintenanceSearchPage.class;
	}

}
