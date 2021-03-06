package fr.openwide.core.jpa.more.util.init.service;

import static fr.openwide.core.jpa.more.property.JpaMorePropertyIds.DATABASE_INITIALIZED;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.schlichtherle.truezip.file.TFileInputStream;
import fr.openwide.core.commons.util.FileUtils;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.exception.SecurityServiceException;
import fr.openwide.core.jpa.exception.ServiceException;
import fr.openwide.core.jpa.more.business.generic.model.GenericListItem;
import fr.openwide.core.jpa.more.business.generic.model.GenericLocalizedGenericListItem;
import fr.openwide.core.jpa.more.util.init.dao.IImportDataDao;
import fr.openwide.core.jpa.more.util.init.util.GenericEntityConverter;
import fr.openwide.core.jpa.more.util.init.util.WorkbookUtils;
import fr.openwide.core.spring.property.service.IPropertyService;
import fr.openwide.core.spring.util.ReflectionUtils;
import fr.openwide.core.spring.util.SpringBeanUtils;
import fr.openwide.core.spring.util.StringUtils;

public abstract class AbstractImportDataServiceImpl implements IImportDataService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImportDataServiceImpl.class);
	
	protected static final String REFERENCE_DATA_FILE = "reference_data.xls";
	
	protected static final String BUSINESS_DATA_FILE = "business_data.xls";
	
	private static final String ID_FIELD_NAME = "id";
	
	private static final String CREATION_DATE_FIELD_NAME = "creationDate";
	
	private static final String LAST_UPDATE_DATE_FIELD_NAME = "lastUpdateDate";
	
	private static final String PASSWORD_FIELD_NAME = "password";
	
	private static final String PASSWORD_HASH_FIELD_NAME = "passwordHash";
	
	private static final Map<Class<?>, List<Class<?>>> ADDITIONAL_CLASS_MAPPINGS = new HashMap<Class<?>, List<Class<?>>>();
	
	private static final Map<Class<?>, String> SHEET_NAME_MAPPING = new HashMap<Class<?>, String>();
	
	@Autowired
	private IImportDataDao importDataDao;
	
	@Autowired
	private IPropertyService propertyService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public void importDirectory(File directory) throws ServiceException, SecurityServiceException, FileNotFoundException, IOException {
		Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping = new HashMap<String, Map<String, GenericEntity<Long, ?>>>();
		
		importBeforeReferenceData(directory, idsMapping);
		
		LOGGER.info("Importing {}", REFERENCE_DATA_FILE);
		Workbook genericListItemWorkbook = new HSSFWorkbook(new TFileInputStream(FileUtils.getFile(directory, REFERENCE_DATA_FILE)));
		importGenericListItems(idsMapping, genericListItemWorkbook);
		LOGGER.info("Import of {} complete", REFERENCE_DATA_FILE);
		
		importAfterReferenceData(directory, idsMapping);
		
		importBeforeBusinessData(directory, idsMapping);

		LOGGER.info("Importing {}", BUSINESS_DATA_FILE);
		Workbook businessItemWorkbook = new HSSFWorkbook(new TFileInputStream(FileUtils.getFile(directory, BUSINESS_DATA_FILE)));
		importMainBusinessItems(idsMapping, businessItemWorkbook);
		LOGGER.info("Import of {} complete", BUSINESS_DATA_FILE);
		
		importAfterBusinessData(directory, idsMapping);
		
		importFiles(directory, idsMapping);
		
		propertyService.set(DATABASE_INITIALIZED, true);
		
		LOGGER.info("Import complete");
	}
	
	protected void importBeforeReferenceData(File directory, Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping)
			throws ServiceException, SecurityServiceException, FileNotFoundException, IOException {
		// nothing, override if necessary
	}
	
	protected void importAfterReferenceData(File directory, Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping)
			throws ServiceException, SecurityServiceException, FileNotFoundException, IOException {
		// nothing, override if necessary
	}
	
	protected void importBeforeBusinessData(File directory, Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping)
			throws ServiceException, SecurityServiceException, FileNotFoundException, IOException {
		// nothing, override if necessary
	}
	
	protected void importAfterBusinessData(File directory, Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping)
			throws ServiceException, SecurityServiceException, FileNotFoundException, IOException {
		// nothing, override if necessary
	}
	
	protected abstract List<String> getGenericListItemPackagesToScan();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void importGenericListItems(Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping, Workbook workbook) {
		for (String packageToScan : getGenericListItemPackagesToScan()) {
			Set<Class<? extends GenericEntity>> classes = Sets.newHashSet();
			classes.addAll(ReflectionUtils.findAssignableClasses(packageToScan, GenericListItem.class));
			classes.addAll(ReflectionUtils.findAssignableClasses(packageToScan, GenericLocalizedGenericListItem.class));
			Map<Integer, Class<? extends GenericEntity>> orderedClasses = Maps.newTreeMap();
			
			for (Class<? extends GenericEntity> genericListItemClass : classes) {
				Sheet sheet = workbook.getSheet(getSheetName(genericListItemClass));
				
				if (sheet != null) {
					orderedClasses.put(sheet.getWorkbook().getSheetIndex(sheet), genericListItemClass);
				}
			}
			
			for (Class<? extends GenericEntity> genericListItemClass : orderedClasses.values()) {
				doImportItem(idsMapping, workbook, genericListItemClass);
			}
		}
	}
	
	protected abstract void importMainBusinessItems(Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping, Workbook workbook);
	
	protected void importFiles(File directory, Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping) 
			throws ServiceException, SecurityServiceException {
		// nothing, override if necessary
	}
	
	protected <E extends GenericEntity<Long, ?>> void doImportItem(Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping,
				Workbook workbook, Class<E> clazz) {
		Sheet sheet = workbook.getSheet(getSheetName(clazz));
		if (sheet != null) {
			GenericEntityConverter converter = new GenericEntityConverter(importDataDao, workbook,
					new HashMap<Class<?>, Class<?>>(0), idsMapping);
			GenericConversionService conversionService = getConversionService(converter);
			converter.setConversionService(conversionService);
			
			Map<String, GenericEntity<Long, ?>> idsMappingForClass = idsMapping.get(clazz.getName());
			if (idsMappingForClass == null) {
				idsMappingForClass = new HashMap<String, GenericEntity<Long, ?>>();
				idsMapping.put(clazz.getName(), idsMappingForClass);
			}
			
			for (Class<?> referencedClass : getOtherReferencedClasses(clazz)) {
				if (!idsMapping.containsKey(referencedClass.getName())) {
					idsMapping.put(referencedClass.getName(), new HashMap<String, GenericEntity<Long, ?>>());
				}
			}
			
			for (Map<String, Object> line : WorkbookUtils.getSheetContent(sheet)) {
				E item = BeanUtils.instantiateClass(clazz);
				
				String importId = StringUtils.trimWhitespace(Objects.toString(line.get(ID_FIELD_NAME), null));
				line.remove(ID_FIELD_NAME);
				
				doFilterLine(clazz, line);
				
				BeanWrapper wrapper = SpringBeanUtils.getBeanWrapper(item);
				wrapper.setConversionService(conversionService);
				wrapper.setPropertyValues(new MutablePropertyValues(line), true);
				
				importDataDao.create(item);
				
				afterImportItem(item);
				
				idsMappingForClass.put(importId, item);
				
				for (Class<?> referencedClass : getOtherReferencedClasses(clazz)) {
					idsMapping.get(referencedClass.getName()).put(importId, item);
				}
			}
			
			LOGGER.info("Imported " + idsMappingForClass.size() + " objects for class: " + clazz.getSimpleName());
		} else {
			LOGGER.info("Nothing to do for class: " + clazz.getSimpleName());
		}
	}
	
	protected <E extends GenericEntity<Long, ?>> void doFilterLine(Class<E> clazz, Map<String, Object> line) {
		Date creationDate = new Date();
		if (!line.containsKey(CREATION_DATE_FIELD_NAME)) {
			line.put(CREATION_DATE_FIELD_NAME, creationDate);
		}
		if (!line.containsKey(LAST_UPDATE_DATE_FIELD_NAME)) {
			line.put(LAST_UPDATE_DATE_FIELD_NAME, creationDate);
		}
		
		if (line.containsKey(PASSWORD_FIELD_NAME)) {
			line.put(PASSWORD_HASH_FIELD_NAME, passwordEncoder.encode(line.get(PASSWORD_FIELD_NAME).toString()));
		}
	}
	
	protected void addAdditionalClassMapping(Class<?> sourceClass, Class<?> targetClass) {
		if (!ADDITIONAL_CLASS_MAPPINGS.containsKey(sourceClass)) {
			ADDITIONAL_CLASS_MAPPINGS.put(sourceClass, new ArrayList<Class<?>>());
		}
		ADDITIONAL_CLASS_MAPPINGS.get(sourceClass).add(targetClass);
	}
	
	protected List<Class<?>> getOtherReferencedClasses(Class<?> sourceClass) {
		if (ADDITIONAL_CLASS_MAPPINGS.containsKey(sourceClass)) {
			return ADDITIONAL_CLASS_MAPPINGS.get(sourceClass);
		} else {
			return new ArrayList<Class<?>>(0);
		}
	}
	
	protected void setSheetNameMapping(Class<?> clazz, String sheetName) {
		SHEET_NAME_MAPPING.put(clazz, sheetName);
	}
	
	protected String getSheetName(Class<?> clazz) {
		if (SHEET_NAME_MAPPING.containsKey(clazz)) {
			return SHEET_NAME_MAPPING.get(clazz);
		} else {
			return clazz.getSimpleName();
		}
	}
	
	private GenericConversionService getConversionService(GenericConverter... converters) {
		GenericConversionService service = new GenericConversionService();
		
		for (GenericConverter converter : converters) {
			service.addConverter(converter);
		}
		
		DefaultConversionService.addDefaultConverters(service);
		
		return service;
	}

	protected <E extends GenericEntity<Long, ?>> void afterImportItem(E item) {
	}
}
