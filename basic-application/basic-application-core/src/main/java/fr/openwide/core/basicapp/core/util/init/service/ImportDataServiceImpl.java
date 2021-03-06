package fr.openwide.core.basicapp.core.util.init.service;

import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import fr.openwide.core.basicapp.core.business.BasicApplicationCoreCommonBusinessPackage;
import fr.openwide.core.basicapp.core.business.user.model.TechnicalUser;
import fr.openwide.core.basicapp.core.business.user.model.UserGroup;
import fr.openwide.core.jpa.business.generic.model.GenericEntity;
import fr.openwide.core.jpa.more.util.init.service.AbstractImportDataServiceImpl;
import fr.openwide.core.jpa.security.business.authority.model.Authority;

@Service("importDataService")
public class ImportDataServiceImpl extends AbstractImportDataServiceImpl {

	@Override
	protected List<String> getGenericListItemPackagesToScan() {
		return Lists.newArrayList(BasicApplicationCoreCommonBusinessPackage.class.getPackage().getName());
	}

	@Override
	protected void importMainBusinessItems(Map<String, Map<String, GenericEntity<Long, ?>>> idsMapping, Workbook workbook) {
		doImportItem(idsMapping, workbook, Authority.class);
		doImportItem(idsMapping, workbook, UserGroup.class);
		doImportItem(idsMapping, workbook, TechnicalUser.class);
	}
}