package fr.openwide.core.wicket.more.markup.html.form;

import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.retzlaff.select2.Select2Settings;

import fr.openwide.core.wicket.more.markup.html.select2.GenericSelect2DropDownSingleChoice;
import fr.openwide.core.wicket.more.markup.html.select2.util.Select2Utils;
import fr.openwide.core.wicket.more.util.convert.converters.HumanReadableEnumConverter;

public class EnumDropDownSingleChoice<E extends Enum<E>> extends GenericSelect2DropDownSingleChoice<E> {

	private static final long serialVersionUID = 6244269987751271782L;
	
	public EnumDropDownSingleChoice(String id, IModel<E> model, Class<E> clazz) {
		this(id, model, Model.ofList(EnumUtils.getEnumList(clazz)));
	}
	
	public EnumDropDownSingleChoice(String id, IModel<E> model, IModel<? extends List<? extends E>> choicesModel) {
		this(id, model, choicesModel, new ResourceKeyWithParameterEnumChoiceRenderer<E>());
	}

	protected EnumDropDownSingleChoice(String id, IModel<E> model, IModel<? extends List<? extends E>> choicesModel,
			IChoiceRenderer<? super E> renderer) {
		super(id, model, choicesModel, renderer);
	}

	protected EnumDropDownSingleChoice(String id, IModel<E> model, Class<E> clazz, IChoiceRenderer<? super E> renderer) {
		super(id, model, Model.ofList(EnumUtils.getEnumList(clazz)), renderer);
	}
	
	@Override
	protected void fillSelect2Settings(Select2Settings settings) {
		super.fillSelect2Settings(settings);
		Select2Utils.disableSearch(settings);
	}
	
	private static class ResourceKeyWithParameterEnumChoiceRenderer<E extends Enum<E>> extends EnumChoiceRenderer<E> {
		private static final long serialVersionUID = -5914319140045008140L;
		@Override
		public Object getDisplayValue(E object) {
			// This converter adds the enum value as a model parameter when rendering the resource key, which
			// enables the rendering of properties of the enum value
			// (DateEnum.TOMORROW being rendered as "Tomorrow (${date})" with DateEnum implementing getDate(), for instance).
			return HumanReadableEnumConverter.get().convertToString(object, Session.get().getLocale());
		}
	}

}
