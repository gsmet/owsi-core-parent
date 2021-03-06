package fr.openwide.core.basicapp.core.business.user.dao;

import org.springframework.stereotype.Repository;

import fr.openwide.core.basicapp.core.business.user.model.User;
import fr.openwide.core.basicapp.core.business.user.model.UserGroup;
import fr.openwide.core.jpa.security.business.person.dao.GenericUserGroupDaoImpl;

@Repository("personGroupDao")
public class UserGroupDaoImpl extends GenericUserGroupDaoImpl<UserGroup, User> implements IUserGroupDao {

	public UserGroupDaoImpl() {
		super();
	}

}
