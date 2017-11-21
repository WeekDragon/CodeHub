package cn.weekdragon.excel;

import java.io.IOException;

public class SimpleExcelReader implements IExcelReader {

	private IExcelReader reader;

	public SimpleExcelReader(String fileName) throws Exception {
		try {
			reader = new SimpleLargeExcelReader(fileName);
		} catch (Exception e) {
			reader = new SimpleNormalExcelReader(fileName);
		}
	}

	@Override
	public boolean next() throws Exception {
		return reader.next();
	}

	@Override
	public boolean nextSheet() throws Exception {
		return reader.nextSheet();
	}

	@Override
	public boolean readerHeaders() throws Exception {
		return reader.readerHeaders();
	}

	@Override
	public String getCellValue(String colName, boolean check) throws Exception {
		return reader.getCellValue(colName, check);
	}

	@Override
	public void close() throws IOException {
		reader.close();
	}

	@Override
	public boolean readerHeaders(String... headerNames) throws Exception {
		return reader.readerHeaders(headerNames);
	}

	@Override
	public boolean isNowRowContainsTheHeaders(String... headName) {
		return reader.isNowRowContainsTheHeaders(headName);
	}


}
