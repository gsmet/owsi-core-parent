package fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.bootstrap.modal.statement;

import java.io.Serializable;

import org.wicketstuff.wiquery.core.javascript.ChainableStatement;
import org.wicketstuff.wiquery.core.javascript.JsUtils;
import org.wicketstuff.wiquery.core.options.LiteralOption;
import org.wicketstuff.wiquery.core.options.Options;

public final class BootstrapModal implements ChainableStatement, Serializable {

	private static final long serialVersionUID = 7943137804361253044L;

	public static final String BOOTSTRAP_MODAL_CHAIN_LABEL = "modal";

	public static final CharSequence METHOD_SHOW = new LiteralOption("show").getJavascriptOption();
	public static final CharSequence METHOD_HIDE = new LiteralOption("hide").getJavascriptOption();

	private static final String OPTION_BACKDROP = "backdrop";
	private static final String OPTION_MODAL_OVERFLOW = "modalOverflow";
	private static final String OPTION_WIDTH = "width";
	private static final String OPTION_HEIGHT = "height";
	private static final String OPTION_FOCUS_ON = "focusOn";
	private static final String OPTION_ATTENTION_ANIMATION = "attentionAnimation";
	private static final String OPTION_REPLACE = "replace";
	private static final String OPTION_SPINNER = "spinner";
	private static final String OPTION_KEYBOARD = "keyboard";

	private final CharSequence method;

	/**
	 * Contrôle l'affichage du fond et la possibilité de fermer la modale par clic sur le fond
	 */
	private BootstrapModalBackdrop backdrop;

	/**
	 * Oblige un affichage en mode overflow (haut de l'écran)
	 */
	private Boolean modalOverflow;

	/**
	 * Détermine la largeur (il faut préférer les css)
	 */
	private String width;

	/**
	 * Détermine la hauteur (il faut préférer les css)
	 */
	private String height;

	/**
	 * Sélecteur pour l'élément devant recevoir le focus
	 */
	private String focusOn;

	/**
	 * Détermine le nom pour l'animation d'attention
	 */
	private String attentionAnimation;

	/**
	 * Détermine si la fenêtre modale doit remplacer la précédente ou s'afficher au dessus
	 */
	private Boolean replace;

	/**
	 * URL pour une image de chargement
	 */
	private String spinner;

	/**
	 * Si true, active la sortie de la popup par la touche escape (défaut)
	 */
	private Boolean keyboard;

	private BootstrapModal() {
		this(null);
	}

	private BootstrapModal(CharSequence method) {
		super();
		this.method = method;
	}

	@Override
	public String chainLabel() {
		return BOOTSTRAP_MODAL_CHAIN_LABEL;
	}

	@Override
	public CharSequence[] statementArgs() {
		Options options = new Options();
		if (backdrop != null) {
			options.put(OPTION_BACKDROP, backdrop);
		}
		if (modalOverflow != null) {
			options.put(OPTION_MODAL_OVERFLOW, modalOverflow);
		}
		if (width != null) {
			options.put(OPTION_WIDTH, JsUtils.quotes(width, true));
		}
		if (height != null) {
			options.put(OPTION_HEIGHT,  JsUtils.quotes(height, true));
		}
		if (focusOn != null) {
			options.put(OPTION_FOCUS_ON,  JsUtils.quotes(focusOn, true));
		}
		if (attentionAnimation != null) {
			options.put(OPTION_ATTENTION_ANIMATION,  JsUtils.quotes(attentionAnimation, true));
		}
		if (replace != null) {
			options.put(OPTION_REPLACE, replace);
		}
		if (spinner != null) {
			options.put(OPTION_SPINNER,  JsUtils.quotes(spinner, true));
		}
		if (keyboard != null) {
			options.put(OPTION_KEYBOARD, keyboard);
		}
		
		CharSequence[] args;
		if (method != null) {
			args = new CharSequence[1];
			args[0] = method;
		} else {
			args = new CharSequence[1];
			args[0] = options.getJavaScriptOptions();
		}
		return args;
	}

	public BootstrapModalBackdrop getBackdrop() {
		return backdrop;
	}

	public BootstrapModal setBackdrop(BootstrapModalBackdrop backdrop) {
		this.backdrop = backdrop;
		return this;
	}

	public Boolean getModalOverflow() {
		return modalOverflow;
	}

	public BootstrapModal setModalOverflow(Boolean modalOverflow) {
		this.modalOverflow = modalOverflow;
		return this;
	}

	public String getWidth() {
		return width;
	}

	public BootstrapModal setWidth(String width) {
		this.width = width;
		return this;
	}

	public String getHeight() {
		return height;
	}

	public BootstrapModal setHeight(String height) {
		this.height = height;
		return this;
	}

	public String getFocusOn() {
		return focusOn;
	}

	/**
	 * Sélecteur pour un focus sur le premier champ non caché de la popup.
	 */
	public BootstrapModal setFocusOnFirstNotHiddenInput() {
		this.focusOn = "input[type!='hidden']:first";
		return this;
	}

	public BootstrapModal setFocusOn(String focusOn) {
		this.focusOn = focusOn;
		return this;
	}

	public String getAttentionAnimation() {
		return attentionAnimation;
	}

	public BootstrapModal setAttentionAnimation(String attentionAnimation) {
		this.attentionAnimation = attentionAnimation;
		return this;
	}

	public Boolean getReplace() {
		return replace;
	}

	public BootstrapModal setReplace(Boolean replace) {
		this.replace = replace;
		return this;
	}

	public String getSpinner() {
		return spinner;
	}

	public BootstrapModal setSpinner(String spinner) {
		this.spinner = spinner;
		return this;
	}

	public Boolean getKeyboard() {
		return keyboard;
	}

	public BootstrapModal setKeyboard(Boolean keyboard) {
		this.keyboard = keyboard;
		return this;
	}

	public static final BootstrapModal modal() {
		return new BootstrapModal(null);
	}

	public static final BootstrapModal hide() {
		return new BootstrapModal(METHOD_HIDE);
	}

}
