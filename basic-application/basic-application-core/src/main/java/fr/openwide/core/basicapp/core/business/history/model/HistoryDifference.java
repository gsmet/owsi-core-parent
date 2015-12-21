package fr.openwide.core.basicapp.core.business.history.model;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.bindgen.Bindable;

import fr.openwide.core.jpa.more.business.history.model.AbstractHistoryDifference;

@Entity
@Bindable
@Cacheable
@Access(AccessType.FIELD)
@Table(
		indexes = {
				@Index(name="idx_HistoryDifference_parentLog", columnList = "parentLog_id"),
				@Index(name="idx_HistoryDifference_parentDifference", columnList = "parentDifference_id")
		}
)
public class HistoryDifference extends AbstractHistoryDifference<HistoryDifference, HistoryLog> {

	private static final long serialVersionUID = -8437788725042615126L;


}
