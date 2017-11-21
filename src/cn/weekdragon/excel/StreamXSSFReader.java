package cn.weekdragon.excel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class StreamXSSFReader {

	private OPCPackage container;
	private XSSFReader.SheetIterator iter;
	private ReadOnlySharedStringsTable strings;
	private XSSFReader xssfReader;

	private List<Map<Integer, String>> data = null;
	private Map<Integer, String> temp;

	public void init(String fileName) throws Exception {
		container = OPCPackage.open(new File(fileName));
		strings = new ReadOnlySharedStringsTable(container);
		xssfReader = new XSSFReader(container);
		iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
	}

	public boolean nextSheet() throws Exception {
		if (iter.hasNext()) {
			InputStream stream = iter.next();
			StylesTable styles = xssfReader.getStylesTable();
			processSheet(styles, strings, stream);
			stream.close();
			return true;
		}
		return false;
	}

	protected void processSheet(StylesTable styles, ReadOnlySharedStringsTable strings, InputStream sheetInputStream) throws IOException, SAXException {
		data = new ArrayList<>();

		InputSource sheetSource = new InputSource(sheetInputStream);
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		try {
			SAXParser saxParser = saxFactory.newSAXParser();
			XMLReader sheetParser = saxParser.getXMLReader();
			ContentHandler handler = new XSSFSheetXMLHandler(styles, strings, new SheetContentsHandler() {

				@Override
				public void startRow(int rowNum) {
					temp = new HashMap<>();
					data.add(temp);
				}

				@Override
				public void endRow() {
					temp = null;
				}

				@Override
				public void cell(String cellReference, String formattedValue) {
					temp.put(getExcelColToInt(cellReference), formattedValue);
				}

				@Override
				public void headerFooter(String text, boolean isHeader, String tagName) {
				}

			}, false// means result instead of formula
			);
			sheetParser.setContentHandler(handler);
			sheetParser.parse(sheetSource);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
		}
	}

	static int getExcelColToInt(String s) {
		int r = 0;
		int length = s.length();
		for (int i = 0; i < length; i++) {
			char v = s.charAt(i);
			if (!(v >= 'A' && v <= 'Z')) {
				return r;
			}
			r = r * 26 + v - 'A' + 1;
		}
		return r;
	}

	public void dispose() {
		container.revert();
	}

	public List<Map<Integer, String>> getData() {
		return data;
	}

}
