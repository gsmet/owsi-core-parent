package fr.openwide.core.basicapp.init;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import fr.openwide.core.basicapp.init.config.spring.BasicApplicationInitConfig;
import fr.openwide.core.basicapp.init.util.SpringContextWrapper;
import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;

public final class BasicApplicationInitFromExcelMain {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasicApplicationInitFromExcelMain.class);

	public static void main(String[] args) throws ServiceException, SecurityServiceException, IOException {
		int returnStatus = 0;
		ConfigurableApplicationContext context = null;
		try {
			context = new AnnotationConfigApplicationContext(BasicApplicationInitConfig.class);
			
			SpringContextWrapper contextWrapper = context.getBean("springContextWrapper",
					SpringContextWrapper.class);
			
			contextWrapper.openEntityManager();
			contextWrapper.importDirectory(new File("src/main/resources/init"));
			
			contextWrapper.reindexAll();
			
			LOGGER.info("Initialization complete");
		} catch(Throwable e) {
			LOGGER.error("Error during initialization", e);
			returnStatus = 1;
		} finally {
			if (context != null) {
				context.close();
			}
			System.exit(returnStatus);
		}
	}
	
	private BasicApplicationInitFromExcelMain() {
	}
}
