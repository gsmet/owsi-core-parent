package fr.openwide.core.jpa.more.business.link.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.bindgen.Bindable;
import org.hibernate.search.annotations.DocumentId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

import fr.openwide.core.commons.util.CloneUtils;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;

@Bindable
@Cacheable
@Entity
public class ExternalLinkWrapper extends GenericEntity<Long, ExternalLinkWrapper> {

	private static final long serialVersionUID = -4558332826839419557L;
	
	private static final ExternalLinkWrapperBinding BINDING = new ExternalLinkWrapperBinding();

	@Id
	@GeneratedValue
	@DocumentId
	private Long id;
	
	@Column(nullable = false)
	private String url;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ExternalLinkStatus status = ExternalLinkStatus.ONLINE;
	
	@Column(nullable = false)
	private int consecutiveFailures;
	
	@Column
	private Integer lastStatusCode;
	
	@Column
	private Date lastCheckDate;
	
	protected ExternalLinkWrapper() {
	}
	
	public ExternalLinkWrapper(String url) {
		this.url = url;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public ExternalLinkStatus getStatus() {
		return status;
	}
	
	public void setStatus(ExternalLinkStatus status) {
		this.status = status;
	}
	
	public int getConsecutiveFailures() {
		return consecutiveFailures;
	}
	
	public void setConsecutiveFailures(int consecutiveFailures) {
		this.consecutiveFailures = consecutiveFailures;
	}
	
	public Integer getLastStatusCode() {
		return lastStatusCode;
	}
	
	public void setLastStatusCode(Integer lastStatusCode) {
		this.lastStatusCode = lastStatusCode;
	}
	
	public Date getLastCheckDate() {
		return CloneUtils.clone(lastCheckDate);
	}
	
	public void setLastCheckDate(Date lastCheckDate) {
		this.lastCheckDate = CloneUtils.clone(lastCheckDate);
	}

	@Override
	@JsonIgnore
	@org.codehaus.jackson.annotate.JsonIgnore
	public String getNameForToString() {
		return url;
	}

	@Override
	@JsonIgnore
	@org.codehaus.jackson.annotate.JsonIgnore
	public String getDisplayName() {
		return url;
	}
	
	@Override
	public int compareTo(ExternalLinkWrapper other) {
		if (this.equals(other)) {
			return 0;
		}
		return this.url.compareTo(other.getUrl());
	}
	
	@Transient
	public Map<String, Object> getResetStatusPropertyValues() {
		Map<String, Object> changes = Maps.newHashMap();
		changes.put(BINDING.status().getPath(), ExternalLinkStatus.ONLINE);
		changes.put(BINDING.consecutiveFailures().getPath(), 0);
		changes.put(BINDING.lastStatusCode().getPath(), null);
		changes.put(BINDING.lastCheckDate().getPath(), null);
		
		return changes;
	}
}