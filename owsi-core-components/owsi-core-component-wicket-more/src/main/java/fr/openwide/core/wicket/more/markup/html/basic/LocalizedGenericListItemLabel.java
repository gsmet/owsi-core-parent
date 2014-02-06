/*
 * Copyright (C) 2009-2011 Open Wide
 * Contact: contact@openwide.fr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.openwide.core.wicket.more.markup.html.basic;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.IConverter;

import fr.openwide.core.jpa.more.business.generic.model.GenericListItem;
import fr.openwide.core.jpa.more.business.generic.model.GenericLocalizedGenericListItem;

/**
 * Affichage sous forme de label d'un {@link GenericLocalizedGenericListItem}
 *
 */
public class LocalizedGenericListItemLabel extends Label {

	private static final long serialVersionUID = -902689514465301799L;
	
	public LocalizedGenericListItemLabel(String id, IModel<? extends GenericListItem<?>> listItemModel) {
		super(id, listItemModel);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <C> IConverter<C> getConverter(Class<C> type) {
		if (GenericListItem.class.isAssignableFrom(type)) {
			return (IConverter<C>) LocalizedGenericListItemConverter.get();
		} else {
			return super.getConverter(type);
		}
	}
	
}