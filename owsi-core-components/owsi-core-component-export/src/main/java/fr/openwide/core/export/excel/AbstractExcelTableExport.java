/*
 * Copyright (C) 2009-2010 Open Wide
 * Contact: contact@openwide.fr
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.openwide.core.export.excel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * <p>Classe abstraite permettant de construire des tableaux Excel.</p>
 *
 * @author Open Wide
 */
public abstract class AbstractExcelTableExport extends AbstractExcelExport {
	
	/**
	 * Index de la couleur des bordures
	 */
	protected static final short BORDER_COLOR_INDEX = (short) 35;

	/**
	 * Index de la couleur de fond du header
	 */
	protected static final short HEADER_BACKGROUND_COLOR_INDEX = (short) 36;

	/**
	 * Index de la couleur du texte du header
	 */
	protected static final short HEADER_FONT_COLOR_INDEX = (short) 37;

	/**
	 * Index de la couleur des lignes paires
	 */
	protected static final short EVEN_ROW_BACKGROUND_COLOR_INDEX = (short) 38;
	
	/**
	 * Index de la couleur de la police des liens
	 */
	protected static final short LINK_FONT_COLOR_INDEX = (short) 39;

	/**
	 * Ratio pour redimensionner les colonnes correctement
	 */
	protected static final float COLUMN_RESIZE_RATIO = 1.25f;

	/**
	 * Taille maximale autorisée pour une colonne
	 */
	protected static final int ABSOLUTE_MAX_COLUMN_WIDTH = 65000;

	protected static final String FONT_NORMAL_NAME = "fontNormal";
	protected static final String FONT_LINK_NAME = "fontLink";
	protected static final String FONT_HEADER_NAME = "fontHeader";
	protected static final String ROW_ODD_NAME = "Odd";
	protected static final String ROW_EVEN_NAME = "Even";
	protected static final String STYLE_DEFAULT_NAME = "default";
	protected static final String STYLE_HEADER_NAME = "header";
	protected static final String STYLE_STANDARD_NAME = "standard";
	protected static final String STYLE_INTEGER_NAME = "integer";
	protected static final String STYLE_DECIMAL_NAME = "decimal";
	protected static final String STYLE_DATE_NAME = "date";
	protected static final String STYLE_DATE_TIME_NAME = "datetime";
	protected static final String STYLE_PERCENT_NAME = "percent";
	protected static final String STYLE_LINK_NAME = "link";
	protected static final String STYLE_FILE_SIZE_NAME = "fileSize";

	/**
	 * Police utilisée dans le document
	 */
	private String fontName = "Verdana";
	
	/**
	 * Taille de police utilisée dans le corps d'une table
	 */
	private short normalFontHeight = 9;
	
	/**
	 * Taille de police utilisée dans le header d'une table
	 */
	private short headerFontHeight = 9;

	/**
	 * Couleur des bordures
	 */
	private String borderColor = "#D1D1D1";

	/**
	 * Couleur de fond du header
	 */
	private String headerBackgroundColor = "#007CAF";

	/**
	 * Couleur du texte du header
	 */
	private String headerFontColor = "#000000";
	
	/**
	 * Couleur des liens
	 */
	private String linkFontColor = "#0077CC";
	
	/**
	 * Couleur des lignes paires
	 */
	private String evenRowBackgroundColor = "#EEEEEE";
	
	/**
	 * Format des pour les nombres entiers
	 */
	private String integerDataFormat = "# ### ### ### ##0";
	
	/**
	 * Format des pour les nombres décimaux
	 */
	private String decimalDataFormat = "# ### ### ### ##0.##";
	
	/**
	 * Format des dates
	 */
	private String dateDataFormat = "DD/MM/YYYY";
	
	/**
	 * Format des dates avec heure
	 */
	private String dateTimeDataFormat = "DD/MM/YYYY HH:mm";
	
	/**
	 * Format des pourcentages
	 */
	private String percentDataFormat = "0.00%";
	
	/**
	 * Format de taille de fichier
	 *
	 * J'aurai bien mis en octets aussi, malheureusement Excel n'accepte visiblement que deux conditions... pas trois.
	 */
	private String fileSizeDataFormat = "[<1000000]0.0,\" Ko\";[<1000000000]0.0,,\" Mo\";0.0,,,\" Go\"";
	
	/**
	 * Constructeur
	 */
	public AbstractExcelTableExport(Workbook workbook) {
		super(workbook);
	}

	/**
	 * Initilisation générale
	 */
	public void init() {
		initColors();
		initFonts();
		initStyles();
	}
	
	/**
	 * Initialisation des couleurs
	 */
	protected void initColors() {
		registerColor(BORDER_COLOR_INDEX, borderColor);
		registerColor(HEADER_BACKGROUND_COLOR_INDEX, headerBackgroundColor);
		registerColor(HEADER_FONT_COLOR_INDEX, headerFontColor);
		registerColor(EVEN_ROW_BACKGROUND_COLOR_INDEX, evenRowBackgroundColor);
		registerColor(LINK_FONT_COLOR_INDEX, linkFontColor);
	}

	/**
	 * Initialisation des polices
	 */
	protected void initFonts() {
		Font fontHeader = workbook.createFont();
		fontHeader.setFontHeightInPoints(getHeaderFontHeight());
		fontHeader.setFontName(getFontName());
		fontHeader.setBoldweight(Font.BOLDWEIGHT_BOLD);
		setFontColor(fontHeader, colorRegistry, HEADER_FONT_COLOR_INDEX);
		registerFont(FONT_HEADER_NAME, fontHeader);

		Font fontNormal = workbook.createFont();
		fontNormal.setFontHeightInPoints(getNormalFontHeight());
		fontNormal.setFontName(getFontName());
		registerFont(FONT_NORMAL_NAME, fontNormal);
		
		Font fontLink = workbook.createFont();
		fontLink.setFontHeightInPoints(getNormalFontHeight());
		fontLink.setFontName(getFontName());
		fontLink.setUnderline(Font.U_SINGLE);
		setFontColor(fontLink, colorRegistry, LINK_FONT_COLOR_INDEX);
		registerFont(FONT_LINK_NAME, fontLink);
	}
	
	/**
	 * Initialisation des styles de cellule
	 */
	protected void initStyles() {
		CellStyle defaultStyle = workbook.createCellStyle();
		defaultStyle.setFont(getFont(FONT_NORMAL_NAME));
		setStyleFillForegroundColor(defaultStyle, colorRegistry, HSSFColor.WHITE.index);
		defaultStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		defaultStyle.setBorderBottom(CellStyle.BORDER_THIN);
		setStyleBottomBorderColor(defaultStyle, colorRegistry, BORDER_COLOR_INDEX);
		defaultStyle.setBorderLeft(CellStyle.BORDER_THIN);
		setStyleLeftBorderColor(defaultStyle, colorRegistry, BORDER_COLOR_INDEX);
		defaultStyle.setBorderRight(CellStyle.BORDER_THIN);
		setStyleRightBorderColor(defaultStyle, colorRegistry, BORDER_COLOR_INDEX);
		defaultStyle.setBorderTop(CellStyle.BORDER_THIN);
		setStyleTopBorderColor(defaultStyle, colorRegistry, BORDER_COLOR_INDEX);
		defaultStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		registerStyle(STYLE_DEFAULT_NAME, defaultStyle);

		CellStyle styleHeader = workbook.createCellStyle();
		styleHeader.setAlignment(CellStyle.ALIGN_CENTER);
		styleHeader.setFont(getFont(FONT_HEADER_NAME));
		setStyleFillForegroundColor(styleHeader, colorRegistry, HEADER_BACKGROUND_COLOR_INDEX);
		styleHeader.setFillPattern(CellStyle.SOLID_FOREGROUND);
		styleHeader.setBorderBottom(CellStyle.BORDER_THIN);
		setStyleBottomBorderColor(styleHeader, colorRegistry, BORDER_COLOR_INDEX);
		styleHeader.setBorderLeft(CellStyle.BORDER_THIN);
		setStyleLeftBorderColor(styleHeader, colorRegistry, BORDER_COLOR_INDEX);
		styleHeader.setBorderRight(CellStyle.BORDER_THIN);
		setStyleRightBorderColor(styleHeader, colorRegistry, BORDER_COLOR_INDEX);
		styleHeader.setBorderTop(CellStyle.BORDER_THIN);
		setStyleTopBorderColor(styleHeader, colorRegistry, BORDER_COLOR_INDEX);
		styleHeader.setDataFormat((short) 0);
		registerStyle(STYLE_HEADER_NAME, styleHeader);

		CellStyle styleOdd = cloneStyle(defaultStyle);
		registerStyle(STYLE_STANDARD_NAME + ROW_ODD_NAME, styleOdd);

		CellStyle styleEven = cloneStyle(styleOdd);
		setStyleFillForegroundColor(styleEven, colorRegistry, EVEN_ROW_BACKGROUND_COLOR_INDEX);
		registerStyle(STYLE_STANDARD_NAME + ROW_EVEN_NAME, styleEven);

		// styles pour les nombres entiers
		short integerFormatIndex = dataFormat.getFormat(integerDataFormat);

		CellStyle styleOddInteger = cloneStyle(styleOdd);
		styleOddInteger.setAlignment(CellStyle.ALIGN_RIGHT);
		styleOddInteger.setDataFormat(integerFormatIndex);
		registerStyle(STYLE_INTEGER_NAME + ROW_ODD_NAME, styleOddInteger);

		CellStyle styleEvenInteger = cloneStyle(styleEven);
		styleEvenInteger.setAlignment(CellStyle.ALIGN_RIGHT);
		styleEvenInteger.setDataFormat(integerFormatIndex);
		registerStyle(STYLE_INTEGER_NAME + ROW_EVEN_NAME, styleEvenInteger);
		
		// styles pour les nombres décimaux
		short decimalFormatIndex = dataFormat.getFormat(decimalDataFormat);

		CellStyle styleOddDecimal = cloneStyle(styleOdd);
		styleOddDecimal.setAlignment(CellStyle.ALIGN_RIGHT);
		styleOddDecimal.setDataFormat(decimalFormatIndex);
		registerStyle(STYLE_DECIMAL_NAME + ROW_ODD_NAME, styleOddDecimal);

		CellStyle styleEvenDecimal = cloneStyle(styleEven);
		styleEvenDecimal.setAlignment(CellStyle.ALIGN_RIGHT);
		styleEvenDecimal.setDataFormat(decimalFormatIndex);
		registerStyle(STYLE_DECIMAL_NAME + ROW_EVEN_NAME, styleEvenDecimal);

		// styles pour les dates
		short dateFormatIndex = dataFormat.getFormat(dateDataFormat);

		CellStyle styleOddDate = cloneStyle(styleOdd);
		styleOddDate.setDataFormat(dateFormatIndex);
		registerStyle(STYLE_DATE_NAME + ROW_ODD_NAME, styleOddDate);

		CellStyle styleEvenDate = cloneStyle(styleEven);
		styleEvenDate.setDataFormat(dateFormatIndex);
		registerStyle(STYLE_DATE_NAME + ROW_EVEN_NAME, styleEvenDate);

		// styles pour les dates avec heure
		short dateTimeFormatIndex = dataFormat.getFormat(dateTimeDataFormat);
		
		CellStyle styleOddDateTime = cloneStyle(styleOdd);
		styleOddDateTime.setDataFormat(dateTimeFormatIndex);
		registerStyle(STYLE_DATE_TIME_NAME + ROW_ODD_NAME, styleOddDateTime);
		
		CellStyle styleEvenDateTime = cloneStyle(styleEven);
		styleEvenDateTime.setDataFormat(dateTimeFormatIndex);
		registerStyle(STYLE_DATE_TIME_NAME + ROW_EVEN_NAME, styleEvenDateTime);
		
		// styles pour les pourcentages
		short percentFormatIndex = dataFormat.getFormat(percentDataFormat);

		CellStyle styleOddPercent = cloneStyle(styleOdd);
		styleOddPercent.setDataFormat(percentFormatIndex);
		registerStyle(STYLE_PERCENT_NAME + ROW_ODD_NAME, styleOddPercent);

		CellStyle styleEvenPercent = cloneStyle(styleEven);
		styleEvenPercent.setDataFormat(percentFormatIndex);
		registerStyle(STYLE_PERCENT_NAME + ROW_EVEN_NAME, styleEvenPercent);
		
		// styles pour les liens
		CellStyle styleOddLink = cloneStyle(styleOdd);
		styleOddLink.setFont(getFont(FONT_LINK_NAME));
		registerStyle(STYLE_LINK_NAME + ROW_ODD_NAME, styleOddLink);
		
		CellStyle styleEvenLink = cloneStyle(styleEven);
		styleEvenLink.setFont(getFont(FONT_LINK_NAME));
		registerStyle(STYLE_LINK_NAME + ROW_EVEN_NAME, styleEvenLink);
		
		// styles pour les tailles de fichiers
		short fileSizeFormatIndex = dataFormat.getFormat(fileSizeDataFormat);
		
		CellStyle styleOddFileSize = cloneStyle(styleOdd);
		styleOddFileSize.setDataFormat(fileSizeFormatIndex);
		registerStyle(STYLE_FILE_SIZE_NAME + ROW_ODD_NAME, styleOddFileSize);
		
		CellStyle styleEvenFileSize = cloneStyle(styleEven);
		styleEvenFileSize.setDataFormat(fileSizeFormatIndex);
		registerStyle(STYLE_FILE_SIZE_NAME + ROW_EVEN_NAME, styleEvenFileSize);
	}
	
	/**
	 * Ajoute une cellule texte.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param text texte à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addTextCell(Row row, int columnIndex, String text) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellStyle(getRowStyle(STYLE_STANDARD_NAME, row.getRowNum()));
		cell.setCellType(Cell.CELL_TYPE_STRING);
		cell.setCellValue(creationHelper.createRichTextString(text));

		return cell;
	}

	/**
	 * Ajoute une cellule d'en-tête.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param text texte à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addHeaderCell(Row row, int columnIndex, String text) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellStyle(getStyle(STYLE_HEADER_NAME));
		cell.setCellType(Cell.CELL_TYPE_STRING);
		cell.setCellValue(creationHelper.createRichTextString(text));

		return cell;
	}

	/**
	 * Ajoute une cellule au format date.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param date date à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addDateCell(Row row, int columnIndex, Date date) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellStyle(getRowStyle(STYLE_DATE_NAME, row.getRowNum()));
		if (date != null) {
			cell.setCellValue(date);
		}

		return cell;
	}
	
	/**
	 * Ajoute une cellule au format date + heure.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param date date à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addDateTimeCell(Row row, int columnIndex, Date date) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellStyle(getRowStyle(STYLE_DATE_TIME_NAME, row.getRowNum()));
		if (date != null) {
			cell.setCellValue(date);
		}

		return cell;
	}

	/**
	 * Ajoute une cellule contenant un nombre entier.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param number nombre à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addIntegerCell(Row row, int columnIndex, Number number) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellStyle(getRowStyle(STYLE_INTEGER_NAME, row.getRowNum()));
		
		if (number != null) {
			cell.setCellValue(number.doubleValue());
		}

		return cell;
	}
	
	/**
	 * Ajoute une cellule contenant un nombre décimal.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param number nombre à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addDecimalCell(Row row, int columnIndex, Number number) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellStyle(getRowStyle(STYLE_DECIMAL_NAME, row.getRowNum()));

		if (number != null) {
			cell.setCellValue(number.doubleValue());
		}

		return cell;
	}

	/**
	 * Ajoute une cellule contenant un pourcentage.
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param number nombre à insérer dans la cellule
	 * @return cellule
	 */
	protected Cell addPercentCell(Row row, int columnIndex, Number number) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellStyle(getRowStyle(STYLE_PERCENT_NAME, row.getRowNum()));
		
		if (number != null) {
			cell.setCellValue(number.doubleValue());
		}

		return cell;
	}

	/**
	 * Ajoute un lien hypertexte sur la cellule.
	 * 
	 * @param cell cellule
	 * @param hyperlink lien à ajouter
	 * @return cellule
	 */
	protected Cell addLinkToCell(Cell cell, Hyperlink hyperlink) {
		cell.setHyperlink(hyperlink);
		cell.setCellStyle(getRowStyle(STYLE_LINK_NAME, cell.getRowIndex()));

		return cell;
	}

	/**
	 * Ajoute une cellule contenant une taille de fichier
	 * 
	 * @param row ligne
	 * @param columnIndex numéro de la colonne
	 * @param fileSizeInBytes taille de fichier en octets
	 * @return cellule
	 */
	protected Cell addFileSizeCell(Row row, int columnIndex, Long fileSizeInBytes) {
		Cell cell = row.createCell(columnIndex);
		cell.setCellType(Cell.CELL_TYPE_NUMERIC);
		cell.setCellStyle(getRowStyle(STYLE_FILE_SIZE_NAME, row.getRowNum()));
		
		if (fileSizeInBytes != null) {
			cell.setCellValue(fileSizeInBytes);
		}
		
		return cell;
	}
	
	/**
	 * Retourne le style de la ligne en tenant compte des alternances de
	 * couleurs pour les lignes paires/impaires.
	 * 
	 * @param prefix prefixe du nom du style auquel est ajouté Even ou Odd
	 * @param rowIndex numéro de la ligne
	 * @return style à utiliser pour la ligne
	 */
	protected CellStyle getRowStyle(String prefix, int rowIndex) {
		StringBuilder styleName = new StringBuilder(prefix);
		if (rowIndex % 2 == 0) {
			styleName.append(ROW_EVEN_NAME);
		} else {
			styleName.append(ROW_ODD_NAME);
		}
		return getStyle(styleName.toString());
	}
	
	/**
	 * Ajoute les en-têtes dans la feuille de calcul.
	 * 
	 * @param sheet feuille de calcul
	 * @param rowIndex numéro de la ligne
	 * @param headers en-têtes
	 */
	protected void addHeadersToSheet(Sheet sheet, int rowIndex, List<String> headers) {
		int columnIndex = 0;
		
		Row rowHeader = sheet.createRow(rowIndex);
		for (String header : headers) {
			addHeaderCell(rowHeader, columnIndex, getLocalizedLabel(header));
			columnIndex++;
		}
	}

	/**
	 * Ajoute les en-têtes dans la feuille de calcul et cache les colonnes qui doivent l'être.
	 * 
	 * @param sheet feuille de calcul
	 * @param rowIndex numéro de la ligne
	 * @param columnInfos map contenant l'en-tête et les informations d'une colonne
	 */
	protected void addHeadersToSheet(Sheet sheet, int rowIndex, Map<String, ColumnInformation> columnInfos) {
		int columnIndex = 0;

		Row rowHeader = sheet.createRow(rowIndex);
		for (Entry<String, ColumnInformation> entry : columnInfos.entrySet()) {
			sheet.setColumnHidden(columnIndex, entry.getValue().isHidden());
			addHeaderCell(rowHeader, columnIndex, getLocalizedLabel(entry.getKey()));
			columnIndex++;
		}
	}

	/**
	 * Finalise la création de la feuille de calcul, notamment en demandant le
	 * redimensionnement automatique des colonnes.
	 * 
	 * @param sheet feuilles de calcul
	 * @param headers en-têtes
	 * @param landscapePrintSetup définit si la feuille est imprimée en paysage ou non
	 */
	protected void finalizeSheet(Sheet sheet, List<String> headers, boolean landscapePrintSetup) {
		int nbColumns = headers.size();
		for (int i = 0; i < nbColumns; i++) {
			sheet.autoSizeColumn((short) i);
			int columnWidth = (int) (sheet.getColumnWidth(i) * COLUMN_RESIZE_RATIO);
			sheet.setColumnWidth(i, columnWidth < ABSOLUTE_MAX_COLUMN_WIDTH ? columnWidth : ABSOLUTE_MAX_COLUMN_WIDTH);
		}
		
		sheet.getPrintSetup().setLandscape(landscapePrintSetup);
	}

	protected void finalizeSheet(Sheet sheet, List<String> headers) {
		// Par défaut, le format d'impression est en paysage pour les tableaux Excel
		finalizeSheet(sheet, headers, true);
	}

	/**
	 * Finalise la création de la feuille de calcul, notamment en demandant le
	 * redimensionnement automatique des colonnes.
	 * 
	 * @param sheet feuilles de calcul
	 * @param columnInfos map contenant l'en-tête et les informations d'une colonne
	 * @param landscapePrintSetup définit si la feuille est imprimée en paysage ou non
	 */
	protected void finalizeSheet(Sheet sheet, Map<String, ColumnInformation> columnInfos, boolean landscapePrintSetup) {
		int columnIndex = 0;
		for (Entry<String, ColumnInformation> entry : columnInfos.entrySet()) {
			sheet.autoSizeColumn((short) columnIndex);
			
			// Détermination de la taille maximum de cette colonne
			int maxColumnWidth;
			if (entry.getValue().getColumnMaxWidth() != -1) {
				maxColumnWidth = entry.getValue().getColumnMaxWidth();
			} else {
				maxColumnWidth = ABSOLUTE_MAX_COLUMN_WIDTH;
			}
			
			// Détermination de la taille souhaitée pour la colonne
			int columnWidth;
			if (entry.getValue().getColumnWidth() != -1) {
				columnWidth = entry.getValue().getColumnWidth();
			} else {
				columnWidth = (int) (sheet.getColumnWidth(columnIndex) * COLUMN_RESIZE_RATIO);
			}
			
			// On redimmensionne la colonne
			sheet.setColumnWidth(columnIndex, columnWidth < maxColumnWidth ? columnWidth : maxColumnWidth);
			
			columnIndex++;
		}
		
		sheet.getPrintSetup().setLandscape(landscapePrintSetup);
	}

	protected void finalizeSheet(Sheet sheet, Map<String, ColumnInformation> columnInfos) {
		// Par défaut, le format d'impression est en paysage pour les tableaux Excel
		finalizeSheet(sheet, columnInfos, true);
	}

	/**
	 * Redimensionne les colonnes qui contiennent des régions fusionnées.
	 * 
	 * Dans POI, les régions fusionnées ne sont pas prises en compte dans le autoSizeColumn.
	 * Quand on fusionne des cellules sur une même colonne, on corrige la taille de cette colonne si nécessaire.
	 * 
	 * @param sheet feuille de calcul
	 * @param columns map contenant l'en-tête et les informations d'une colonne
	 */
	protected void resizeMergedColumns(Sheet sheet, Map<String, ColumnInformation> columns) {
		if (sheet.getNumMergedRegions() > 0) {
			List<ColumnInformation> columnsInfo = new ArrayList<ColumnInformation>(columns.values());
			
			for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
				CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
				
				if (mergedRegion.getFirstColumn() == mergedRegion.getLastColumn()) {
					int columnIndex = mergedRegion.getFirstColumn();
					
					String headerText = getLocalizedLabel(columnsInfo.get(columnIndex).getHeaderKey());
					
					int headerSize = (int) (headerText.length() * 300 * COLUMN_RESIZE_RATIO);
					if (sheet.getColumnWidth(columnIndex) < headerSize) {
						sheet.setColumnWidth(columnIndex, headerSize);
					}
				}
			}
		}
	}

	/**
	 * Retourne le message correspondant à la clé en fonction du Locale
	 * 
	 * @param key clé
	 * @return message
	 */
	protected abstract String getLocalizedLabel(String key);

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public short getNormalFontHeight() {
		return normalFontHeight;
	}

	public void setNormalFontHeight(short normalFontHeight) {
		this.normalFontHeight = normalFontHeight;
	}

	public short getHeaderFontHeight() {
		return headerFontHeight;
	}

	public void setHeaderFontHeight(short headerFontHeight) {
		this.headerFontHeight = headerFontHeight;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public String getHeaderBackgroundColor() {
		return headerBackgroundColor;
	}

	public void setHeaderBackgroundColor(String headerBackgroundColor) {
		this.headerBackgroundColor = headerBackgroundColor;
	}

	public String getHeaderFontColor() {
		return headerFontColor;
	}

	public void setHeaderFontColor(String headerFontColor) {
		this.headerFontColor = headerFontColor;
	}
	
	public String getLinkFontColor() {
		return linkFontColor;
	}

	public void setLinkFontColor(String linkFontColor) {
		this.linkFontColor = linkFontColor;
	}

	public String getEvenRowBackgroundColor() {
		return evenRowBackgroundColor;
	}

	public void setEvenRowBackgroundColor(String evenRoxBackgroundColor) {
		this.evenRowBackgroundColor = evenRoxBackgroundColor;
	}

	public String getIntegerDataFormat() {
		return integerDataFormat;
	}

	public void setIntegerDataFormat(String numericDataFormat) {
		this.integerDataFormat = numericDataFormat;
	}

	public String getDecimalDataFormat() {
		return decimalDataFormat;
	}

	public void setDecimalDataFormat(String decimalDataFormat) {
		this.decimalDataFormat = decimalDataFormat;
	}

	public String getDateDataFormat() {
		return dateDataFormat;
	}

	public void setDateDataFormat(String dateDataFormat) {
		this.dateDataFormat = dateDataFormat;
	}

	public String getDateTimeDataFormat() {
		return dateTimeDataFormat;
	}

	public void setDateTimeDataFormat(String dateTimeDataFormat) {
		this.dateTimeDataFormat = dateTimeDataFormat;
	}

	public String getPercentDataFormat() {
		return percentDataFormat;
	}

	public void setPercentDataFormat(String percentDataFormat) {
		this.percentDataFormat = percentDataFormat;
	}
}
