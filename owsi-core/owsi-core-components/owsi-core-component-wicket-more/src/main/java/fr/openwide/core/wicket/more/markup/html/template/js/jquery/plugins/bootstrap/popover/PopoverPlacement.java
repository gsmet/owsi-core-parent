package fr.openwide.core.wicket.more.markup.html.template.js.jquery.plugins.bootstrap.popover;

import java.util.Locale;

public enum PopoverPlacement {
	
	TOP("top"),
	BOTTOM("bottom"),
	LEFT("left"),
	RIGHT("right");
	
	private String value;
	
	private PopoverPlacement(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	private static String normalize(String string) {
		if (string == null) {
			return null;
		}
		return string.toLowerCase(Locale.ROOT);
	}
	
	public static PopoverPlacement fromString(String value) {
		for (PopoverPlacement type : values()) {
			if (type.getValue().equals(normalize(value))) {
				return type;
			}
		}
		return null;
	}
}