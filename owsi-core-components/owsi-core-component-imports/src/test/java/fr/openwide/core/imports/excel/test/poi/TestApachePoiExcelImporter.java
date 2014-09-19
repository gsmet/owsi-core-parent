package fr.openwide.core.imports.excel.test.poi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.javatuples.Quartet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import fr.openwide.core.imports.table.apache.poi.mapping.ApachePoiImportColumnSet;
import fr.openwide.core.imports.table.apache.poi.scanner.ApachePoiImportFileScanner;
import fr.openwide.core.imports.table.common.event.ITableImportEventHandler;
import fr.openwide.core.imports.table.common.event.LoggerTableImportEventHandler;
import fr.openwide.core.imports.table.common.event.TableImportNonFatalErrorHandling;
import fr.openwide.core.imports.table.common.event.exception.TableImportException;
import fr.openwide.core.imports.table.common.excel.scanner.IExcelImportFileScanner.IExcelImportFileVisitor;
import fr.openwide.core.imports.table.common.excel.scanner.IExcelImportFileScanner.SheetSelection;
import fr.openwide.core.imports.table.common.location.ITableImportNavigator;
import fr.openwide.core.imports.table.common.mapping.column.builder.MappingConstraint;

public class TestApachePoiExcelImporter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TestApachePoiExcelImporter.class);

	private static class Columns extends ApachePoiImportColumnSet {
		final Column<Date> dateColumn = withIndex(0).asDate().build();
		final Column<Boolean> booleanColumn = withIndex(1).asString().toBoolean(new Function<String, Boolean>() {
					@Override
					public Boolean apply(String input) {
						return "true".equals(input) ? true : false;
					}
				}).build();
		final Column<String> stringColumn = withHeader("StringColumn", 2, MappingConstraint.REQUIRED).asString().clean().build();
		final Column<Integer> integerColumn = withHeader("IntegerColumn").asInteger().build();
		final Column<Integer> missingColumn = withHeader("MissingColumn", MappingConstraint.OPTIONAL).asInteger().build();
	}

	private static final Columns COLUMNS = new Columns();
	private static final ApachePoiImportFileScanner SCANNER = new ApachePoiImportFileScanner();

	public List<Quartet<Date, Boolean, String, Integer>> doImport(InputStream stream, String filename) throws TableImportException {
		final List<Quartet<Date, Boolean, String, Integer>> results = Lists.newArrayList();
		
		SCANNER.scan(stream, filename, SheetSelection.ALL, new IExcelImportFileVisitor<Workbook, Sheet, Row, Cell, CellReference>() {
			@Override
			public void visitSheet(ITableImportNavigator<Sheet, Row, Cell, CellReference> navigator, Workbook workbook, Sheet sheet)
					throws TableImportException {
				ITableImportEventHandler eventHandler = new LoggerTableImportEventHandler(TableImportNonFatalErrorHandling.THROW_IMMEDIATELY, LOGGER);
				
				Columns.TableContext sheetContext = COLUMNS.map(sheet, navigator, eventHandler);
				
				assertTrue(sheetContext.column(COLUMNS.dateColumn).exists());
				assertTrue(sheetContext.column(COLUMNS.integerColumn).exists());
				assertTrue(sheetContext.column(COLUMNS.booleanColumn).exists());
				assertFalse(sheetContext.column(COLUMNS.missingColumn).exists());
				
				for (Columns.RowContext rowContext : Iterables.skip(sheetContext, 1)) {
					Quartet<Date, Boolean, String, Integer> result = Quartet.with(
							rowContext.cell(COLUMNS.dateColumn).get(),
							rowContext.cell(COLUMNS.booleanColumn).get(),
							rowContext.cell(COLUMNS.stringColumn).get(),
							rowContext.cell(COLUMNS.integerColumn).get()
					);
					
					results.add(result);
				}
			}
		});
		
		return results;
	}

}
