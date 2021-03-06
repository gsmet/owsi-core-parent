package fr.openwide.core.spring.config.spring.annotation;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class DefaultConfigurationLocationProvider implements IConfigurationLocationProvider {

	public static final String CONFIGURER_APPLICATION_NAME_PLACEHOLDER = "${applicationName}";

	public static final String CONFIGURER_USER_PLACEHOLDER = "${user}";

	@Override
	public List<String> getLocations(String applicationName, String... locationPatterns) {
		List<String> locations = Lists.newArrayList();
		String applicationNameEscapedPattern = Pattern.quote(CONFIGURER_APPLICATION_NAME_PLACEHOLDER);
		String userEscapedPattern = Pattern.quote(CONFIGURER_USER_PLACEHOLDER);
		for (String location : locationPatterns) {
			location = location.replaceAll(applicationNameEscapedPattern, applicationName);
			
			String username = System.getProperty("user.name");
			if (StringUtils.isNotEmpty(username)) {
				location = location.replaceAll(userEscapedPattern, username);
			}
			
			locations.add(location);
		}
		return locations;
	}

}
