package fr.openwide.core.imports.table.apache.poi.mapping.column.builder;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import com.google.common.base.Function;

import fr.openwide.core.commons.util.functional.SerializableFunction;
import fr.openwide.core.imports.table.common.event.ITableImportEventHandler;
import fr.openwide.core.imports.table.common.location.ITableImportNavigator;
import fr.openwide.core.imports.table.common.mapping.column.builder.ITableImportColumnMapper;

/*package*/ class StaticIndexApachePoiImportColumnMapper implements ITableImportColumnMapper<Sheet, Row, Cell, CellReference> {
	
	private final int columnIndex;

	public StaticIndexApachePoiImportColumnMapper(int columnIndex) {
		super();
		
		this.columnIndex = columnIndex;
	}
	
	@Override
	public Function<? super Row, CellReference> tryMap(Sheet sheet, ITableImportNavigator<Sheet, Row, Cell, CellReference> navigator, ITableImportEventHandler eventHandler) {
		return new SerializableFunction<Row, CellReference>() {
			private static final long serialVersionUID = 1L;
			@Override
			public CellReference apply(Row row) {
				return row == null ? null : new CellReference(row.getRowNum(), columnIndex);
			}
		};
	}
}
