package fr.openwide.core.basicapp.core.security.service;

import static com.google.common.base.Preconditions.checkNotNull;
import static fr.openwide.core.spring.property.SpringSecurityPropertyIds.PASSWORD_EXPIRATION_DAYS;
import static fr.openwide.core.spring.property.SpringSecurityPropertyIds.PASSWORD_HISTORY_COUNT;
import static fr.openwide.core.spring.property.SpringSecurityPropertyIds.PASSWORD_RECOVERY_REQUEST_EXPIRATION_MINUTES;
import static fr.openwide.core.spring.property.SpringSecurityPropertyIds.PASSWORD_RECOVERY_REQUEST_TOKEN_RANDOM_COUNT;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.openwide.core.basicapp.core.business.history.model.atomic.HistoryEventType;
import fr.openwide.core.basicapp.core.business.history.model.bean.HistoryLogAdditionalInformationBean;
import fr.openwide.core.basicapp.core.business.history.service.IHistoryLogService;
import fr.openwide.core.basicapp.core.business.notification.service.INotificationService;
import fr.openwide.core.basicapp.core.business.user.model.User;
import fr.openwide.core.basicapp.core.business.user.model.atomic.UserPasswordRecoveryRequestInitiator;
import fr.openwide.core.basicapp.core.business.user.model.atomic.UserPasswordRecoveryRequestType;
import fr.openwide.core.basicapp.core.business.user.service.IUserService;
import fr.openwide.core.basicapp.core.security.model.SecurityOptions;
import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.jpa.security.business.person.model.GenericUser;
import fr.openwide.core.jpa.util.HibernateUtils;
import fr.openwide.core.spring.property.service.IPropertyService;
import fr.openwide.core.spring.util.StringUtils;

public class SecurityManagementServiceImpl implements ISecurityManagementService {

	private final Map<Class<? extends GenericUser<?, ?>>, SecurityOptions> optionsByUser = Maps.newHashMap();

	private SecurityOptions defaultOptions = SecurityOptions.DEFAULT;

	@Autowired
	private IUserService userService;

	@Autowired
	private INotificationService notificationService;

	@Autowired
	private IPropertyService propertyService;
	
	@Autowired
	private IHistoryLogService historyLogService;

	public SecurityManagementServiceImpl setOptions(Class<? extends User> clazz, SecurityOptions options) {
		checkNotNull(clazz);
		checkNotNull(options);
		
		optionsByUser.put(clazz, options);
		
		return this;
	}

	public SecurityManagementServiceImpl setDefaultOptions(SecurityOptions options) {
		checkNotNull(options);
		
		defaultOptions = options;
		
		return this;
	}

	@Override
	public SecurityOptions getOptions(Class<? extends User> clazz) {
		if (optionsByUser.containsKey(clazz)) {
			return optionsByUser.get(clazz);
		}
		return defaultOptions;
	}

	@Override
	public SecurityOptions getOptions(User user) {
		if (user == null) {
			return defaultOptions;
		}
		return getOptions(HibernateUtils.unwrap(user).getClass());
	}

	@Override
	public void initiatePasswordRecoveryRequest(User user, UserPasswordRecoveryRequestType type,
			UserPasswordRecoveryRequestInitiator initiator) throws ServiceException, SecurityServiceException {
		initiatePasswordRecoveryRequest(user, type, initiator, user);
	}

	@Override
	public void initiatePasswordRecoveryRequest(User user, UserPasswordRecoveryRequestType type,
			UserPasswordRecoveryRequestInitiator initiator, User author) throws ServiceException, SecurityServiceException {
		Date now = new Date();
		
		user.getPasswordRecoveryRequest().setToken(RandomStringUtils.randomAlphanumeric(propertyService.get(PASSWORD_RECOVERY_REQUEST_TOKEN_RANDOM_COUNT)));
		user.getPasswordRecoveryRequest().setCreationDate(now);
		user.getPasswordRecoveryRequest().setType(type);
		user.getPasswordRecoveryRequest().setInitiator(initiator);
		
		userService.update(user);
		
		notificationService.sendUserPasswordRecoveryRequest(user);
		
		switch (type) {
		case CREATION:
			historyLogService.log(HistoryEventType.PASSWORD_CREATION_REQUEST, user, HistoryLogAdditionalInformationBean.empty());
			break;
		case RESET:
			historyLogService.log(HistoryEventType.PASSWORD_RESET_REQUEST, user, HistoryLogAdditionalInformationBean.empty());
			break;
		default:
			break;
		}
	}

	@Override
	public boolean isPasswordExpired(User user) {
		if (user == null
				|| user.getPasswordInformation().getLastUpdateDate() == null
				|| !getOptions(user).isPasswordExpirationEnabled()) {
			return false;
		}
		
		Date expirationDate = DateUtils.addDays(user.getPasswordInformation().getLastUpdateDate(), propertyService.get(PASSWORD_EXPIRATION_DAYS));
		Date now = new Date();
		
		return now.after(expirationDate);
	}

	@Override
	public boolean isPasswordRecoveryRequestExpired(User user) {
		if (user == null
				|| user.getPasswordRecoveryRequest().getToken() == null
				|| user.getPasswordRecoveryRequest().getCreationDate() == null) {
			return true;
		}
		
		Date expirationDate = DateUtils.addMinutes(user.getPasswordRecoveryRequest().getCreationDate(), propertyService.get(PASSWORD_RECOVERY_REQUEST_EXPIRATION_MINUTES));
		Date now = new Date();
		
		return now.after(expirationDate);
	}

	@Override
	public void updatePassword(User user, String password) throws ServiceException, SecurityServiceException {
		updatePassword(user, password, user);
	}

	@Override
	public void updatePassword(User user, String password, User author) throws ServiceException, SecurityServiceException {
		if (user == null || !StringUtils.hasText(password)) {
			return;
		}
		
		userService.setPasswords(user, password);
		user.getPasswordInformation().setLastUpdateDate(new Date());
		
		if (getOptions(user).isPasswordHistoryEnabled()) {
			EvictingQueue<String> historyQueue = EvictingQueue.create(propertyService.get(PASSWORD_HISTORY_COUNT));
			
			for (String oldPassword : user.getPasswordInformation().getHistory()) {
				historyQueue.offer(oldPassword);
			}
			historyQueue.offer(user.getPasswordHash());
			
			user.getPasswordInformation().setHistory(Lists.newArrayList(historyQueue));
		}
		
		user.getPasswordRecoveryRequest().reset();
		userService.update(user);
		
		historyLogService.log(HistoryEventType.PASSWORD_UPDATE, user, HistoryLogAdditionalInformationBean.empty());
	}

}
