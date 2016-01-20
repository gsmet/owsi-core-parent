package fr.openwide.core.jpa.more.config.spring;

import static fr.openwide.core.jpa.more.property.JpaMorePropertyIds.DATABASE_INITIALIZED;
import static fr.openwide.core.jpa.more.property.JpaMorePropertyIds.DATA_UPGRADE_DONE_TEMPLATE;
import static fr.openwide.core.jpa.more.property.JpaMorePropertyIds.IMAGE_MAGICK_CONVERT_BINARY_PATH;
import static fr.openwide.core.jpa.more.property.JpaMorePropertyIds.MAINTENANCE;

import java.io.File;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.google.common.base.Function;

import fr.openwide.core.spring.config.spring.AbstractApplicationPropertyRegistryConfig;
import fr.openwide.core.spring.property.service.IPropertyRegistry;
import fr.openwide.core.spring.util.StringUtils;

@Import(JpaMoreTaskApplicationPropertyRegistryConfig.class)
@Configuration
public class JpaMoreApplicationPropertyRegistryConfig extends AbstractApplicationPropertyRegistryConfig {

	@Override
	protected void register(IPropertyRegistry registry) {
		registry.registerBoolean(DATABASE_INITIALIZED, false);
		registry.registerBoolean(DATA_UPGRADE_DONE_TEMPLATE, false);
		registry.registerBoolean(MAINTENANCE, false);
		
		registry.register(
				IMAGE_MAGICK_CONVERT_BINARY_PATH,
				new Function<String, File>() {
					@Override
					public File apply(String input) {
						if (!StringUtils.hasText(input)) {
							return null;
						}
						return new File(input);
					}
				},
				new File("/usr/bin/convert")
		);
	}

}
