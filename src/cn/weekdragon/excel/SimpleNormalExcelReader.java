package cn.weekdragon.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SimpleNormalExcelReader implements IExcelReader {
	private String fileName;
	private Workbook wb;
	private Sheet sheet;
	private Row row;
	private int rowIndex;
	private File file;
	private OPCPackage pkg;
	private FileInputStream in;
	private Map<String, Integer> colMap = new HashMap<String, Integer>();

	private int numSheet = 0;

	public SimpleNormalExcelReader(String fileName) throws Exception {
		this.fileName = fileName;

		init();
	}

	public boolean nextSheet() {
		sheet = null;
		for (; numSheet < wb.getNumberOfSheets();) {
			sheet = wb.getSheetAt(numSheet);
			numSheet++;
			if (sheet != null) {
				break;
			}
		}
		if (sheet == null)
			return false;
		rowIndex = 0;
		return true;
	}

	public boolean readerHeaders() throws Exception {
		while (true) {
			colMap.clear();
			// 循环行Row
			for (; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				row = sheet.getRow(rowIndex);
				if (row != null) {
					break;
				}
			}
			if (row == null)
				return false;
			boolean isData = false;
			// 循环列Cell
			for (int cellNum = 0; cellNum <= row.getLastCellNum(); cellNum++) {
				Cell xssfCell = row.getCell(cellNum);
				if (xssfCell == null) {
					continue;
				}
				String colName = formatColName(getValue(xssfCell));
				if (!colName.trim().isEmpty()) {
					isData = true;
				}
				colMap.put(colName, cellNum);
			}
			if (isData) {
				return true;
			}
		}
	}

	protected String formatColName(String colName) {
		colName = colName.replace("（", "(");
		colName = colName.replace("）", ")");
		return colName;
	}

	protected void init() throws Exception {
		file = new File(fileName);
		if (!file.exists()) {
			throw new Exception(String.format("fileName[%s] not exist", file.getName()));
		}
		try {
			in = new FileInputStream(fileName);
			wb = new HSSFWorkbook(in);
		} catch (Exception e) {
			pkg = OPCPackage.open(fileName);
			wb = new XSSFWorkbook(pkg);
		}
	}

	public boolean next() throws Exception {
		rowIndex++;
		if (rowIndex > sheet.getLastRowNum())
			return false;
		row = sheet.getRow(rowIndex);
		if (row == null)
			return false;
		return true;
	}

	public String getCellValue(String colName) throws Exception {
		return getCellValue(colName, true);
	}

	public String getCellValue(String colName, boolean check) throws Exception {
		Integer index = colMap.get(formatColName(colName));
		Cell xssfCell = index != null ? row.getCell(index) : null;
		if (xssfCell == null) {
			if (check)
				throw new Exception(
						String.format("can't find colName[%s] value in rowIndex[%d] of file[%s] sheetIndex[%d]", colName, rowIndex, file.getName(), numSheet));
			return null;
		}
		return getValue(xssfCell);
	}

	private String getValue(Cell hssfCell) {
		if (hssfCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(hssfCell.getBooleanCellValue());
		} else if (hssfCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			return String.valueOf(hssfCell.getNumericCellValue());
		} else {
			return String.valueOf(hssfCell.getStringCellValue());
		}
	}

	public void close() throws IOException {
		if (pkg != null) {
			pkg.revert();
			pkg = null;
		}
		if (in != null) {
			in.close();
			in = null;
		}
		if (wb != null) {
			file = null;
			wb = null;
			sheet = null;
		}
	}

	@Override
	public boolean isNowRowContainsTheHeaders(String... headNames) {
		for(String headName:headNames) {
			if(!colMap.containsKey(headName)) return false;
		}
		return true;
	}

	@Override
	public boolean readerHeaders(String... headerNames) throws Exception {
		while(readerHeaders()) {
			if(isNowRowContainsTheHeaders(headerNames)) {
				return true;
			}
		}
		return false;
	}
}
