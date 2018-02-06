package cn.weekdragon.excel;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SimpleLargeExcelReader implements IExcelReader {
	private String fileName;
	private StreamXSSFReader reader;
	private List<Map<Integer, String>> data;
	private int row;
	private Map<String, Integer> colMap = new HashMap<String, Integer>();
	private Map<Integer, String> nowRow;
	private int numSheet;

	public SimpleLargeExcelReader(String fileName) throws Exception {
		init(fileName);
	}

	protected void init(String fileName) throws Exception {
		this.fileName = fileName;
		numSheet = 0;

		reader = new StreamXSSFReader();
		reader.init(fileName);
	}

	@Override
	public boolean next() throws Exception {
		if (row < data.size()) {
			nowRow = data.get(row);
			row++;
			return true;
		}
		return false;
	}

	@Override
	public boolean nextSheet() throws Exception {
		boolean res = reader.nextSheet();
		if (res) {
			data = reader.getData();
			numSheet++;
			row = 0;
		}
		return res;
	}

	@Override
	public boolean readerHeaders() throws Exception {
		while (next()) {
			colMap.clear();
			boolean isData = false;
			Map<Integer, String> map = nowRow;
			Set<Entry<Integer, String>> set = map.entrySet();
			for (Entry<Integer, String> entry : set) {
				String colName = entry.getValue();
				if (!colName.trim().isEmpty()) {
					isData = true;
				}
				colMap.put(colName, entry.getKey());
			}
			if (isData) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getCellValue(String colName, boolean check) throws Exception {
		Integer index = colMap.get(colName);
		String xssfCell = index != null ? nowRow.get(index) : null;
		if (xssfCell == null) {
			if (check)
				throw new Exception(String.format("can't find colName[%s] value in row[%d] of file[%s] sheetIndex[%d]", colName, row, fileName, numSheet));
			return null;
		}
		return xssfCell;
	}

	@Override
	public void close() throws IOException {
		reader.dispose();
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
	    for(int i=0;i<10&&readerHeaders();i++){
	        if(isNowRowContainsTheHeaders(headerNames)) {
                return true;
            }
	    }
		return false;
	}

}
