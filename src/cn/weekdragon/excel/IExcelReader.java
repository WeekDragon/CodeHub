package cn.weekdragon.excel;

import java.io.IOException;

public interface IExcelReader {
	public boolean next() throws Exception;

	public boolean nextSheet() throws Exception;

	public boolean readerHeaders() throws Exception;

	public Object getCellValue(String colName, boolean check) throws Exception;

	public void close() throws IOException;
	
	public boolean isNowRowContainsTheHeaders(String... headName);
	
	public boolean readerHeaders(String ...headerNames) throws Exception;

}
