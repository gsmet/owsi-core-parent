package fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.modal.behavior;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.odlabs.wiquery.core.events.EventLabel;
import org.odlabs.wiquery.core.events.WiQueryAjaxEventBehavior;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.core.options.Options;

import fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.modal.component.IAjaxModalPopupPanel;

/**
 * Permet de provoquer l'ouverture d'un popup via une requête ajax. L'ordre des événements est le suivant :
 * 
 * 1. ouverture et mise en attente du popup
 * 
 * 2. requête ajax
 * 
 * 3. arrêt du mode attente et affichage du popup avec la réponse
 */
public abstract class AjaxOpenModalBehavior extends WiQueryAjaxEventBehavior {

	private static final long serialVersionUID = 3299212684157849227L;

	private IAjaxModalPopupPanel modal;

	public AjaxOpenModalBehavior(IAjaxModalPopupPanel modalPopupPanel, EventLabel... events) {
		super(events);
		this.modal = modalPopupPanel;
	}

	@Override
	protected final void onEvent(AjaxRequestTarget target) {
		onShow(target);
		modal.show(target);
	}

	protected abstract void onShow(AjaxRequestTarget target);

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		attributes.getAjaxCallListeners().add(getOpenModalCallListener());
		
		super.updateAjaxAttributes(attributes);
	}

	protected IAjaxCallListener getOpenModalCallListener() {
		Options options = new Options();
		options.putLiteral("href", selector(modal));
		options.put("displayWait", Boolean.TRUE.toString());
		options.put("showCloseButton", false);
		
		AjaxCallListener openModalListener = new AjaxCallListener();
		
		openModalListener.onBefore(new JsStatement().$().chain("fancybox", "''", options.getJavaScriptOptions()).append(";").render());
		openModalListener.onSuccess(new JsStatement().$().chain("fancybox.resize").append(";").render());
		openModalListener.onFailure(new JsStatement().$().chain("fancybox.close").append(";").render());
		
		return openModalListener;
		
		// TODO migration Wicket 6 : tester l'ouverture des modals avant de supprimer l'ancien code.
//		return new IAjaxCallListener() {
//			
//			private static final long serialVersionUID = 8595086274005315530L;
//
//			@Override
//			public CharSequence decorateScript(Component component, CharSequence script) {
//				// on montre le fancybox en état d'attente
//				Options options = new Options();
//				options.putLiteral("href", selector(modal));
//				options.put("displayWait", Boolean.TRUE.toString());
//				options.put("showCloseButton", false);
//				return new JsStatement().$().chain("fancybox", "''", options.getJavaScriptOptions()).append(";")
//						.append(script).render();
//			}
//			
//			@Override
//			public CharSequence decorateOnSuccessScript(Component component, CharSequence script) {
//				return new JsStatement().$().chain("fancybox.resize").append(";").append(script).render();
//			}
//			
//			@Override
//			public CharSequence decorateOnFailureScript(Component component, CharSequence script) {
//				return new JsStatement().$().chain("fancybox.close").append(";").append(script).render();
//			}
//		};
	}

	protected String selector(IAjaxModalPopupPanel modal) {
		return "#" + modal.getContainerMarkupId();
	}

}
