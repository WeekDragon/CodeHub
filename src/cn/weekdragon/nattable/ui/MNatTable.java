package cn.weekdragon.nattable.ui;

import static org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes.CELL_PAINTER;
import static org.eclipse.nebula.widgets.nattable.grid.GridRegion.FILTER_ROW;
import static org.eclipse.nebula.widgets.nattable.style.DisplayMode.NORMAL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfiguration;
import org.eclipse.nebula.widgets.nattable.config.NullComparator;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.config.DefaultExportBindings;
import org.eclipse.nebula.widgets.nattable.export.excel.DefaultExportFormatter;
import org.eclipse.nebula.widgets.nattable.export.excel.ExcelExporter;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.filterrow.DefaultGlazedListsFilterStrategy;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterIconPainter;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowHeaderComposite;
import org.eclipse.nebula.widgets.nattable.filterrow.FilterRowPainter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionBindings;
import org.eclipse.nebula.widgets.nattable.sort.SortConfigAttributes;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import cn.weekdragon.nattable.config.StyledColumnHeaderConfiguration;
import cn.weekdragon.nattable.config.StyledRowHeaderConfiguration;

public class MNatTable<T> extends Composite {

	private static final String CUSTOM_COMPARATOR_LABEL = "customComparatorLabel";
	private static final String NO_SORT_LABEL = "noSortLabel";
	private static final String PROPERTIES_FILE = "natTable.properties";

	private NatTable natTable;
	private T selected;
	private EventList<T> eventList;
	private SortedList<T> sortedlist;
	private FilterList<T> filterList;
	private GlazedListsColumnHeaderLayerStack<T> columnHeaderLayerStack;
	private IConfigRegistry configRegistry;
	private GridLayer gridLayer;
	private RowSelectionProvider<T> rowSelectionProvider;
	private ListDataProvider<T> bodyDataProvider;
	// private String[] propertyNames =
	// {"fileName","fileExtName","size","sharePerson","shareTime","downloadCount"};

	public MNatTable(Composite parent, int style, List<T> datalist,String[] propertyNames, Map<String, String> propertyToColumnName) {
		super(parent, style);
		this.setLayout(new FillLayout());
		eventList =  GlazedLists.eventList(datalist);
		//transformedList = GlazedLists.threadSafeList(eventList);
		sortedlist = new SortedList<>(eventList,null);
		filterList = new FilterList<>(sortedlist);
		init(propertyNames, propertyToColumnName);
		CreateNatable();
	}
	public MNatTable(Composite parent, int style, List<T> datalist,String[] propertyNames) {
		this(parent, style, datalist, propertyNames, null);
	}

	private void init(String[] propertyNames, Map<String, String> propertyToColumnName) {

		configRegistry = new ConfigRegistry();

		// body layer
		IColumnPropertyAccessor<T> columnPropertyAccessor = new ReflectiveColumnPropertyAccessor<T>(propertyNames);
		bodyDataProvider = new ListDataProvider<T>(filterList, columnPropertyAccessor);
		DataLayer bodyDataLayer = new DataLayer(bodyDataProvider);
		DefaultBodyLayerStack bodyLayerStack = new DefaultBodyLayerStack(bodyDataLayer);
		
		initLabels(bodyLayerStack);
		initSelectionProvider(bodyLayerStack);


		// 最底层的数据提供者
		IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames,
				propertyToColumnName);

		columnHeaderLayerStack = new GlazedListsColumnHeaderLayerStack<>(columnHeaderDataProvider, sortedlist,
				columnPropertyAccessor, configRegistry, bodyLayerStack);
		FilterRowHeaderComposite<T> filterRowHeaderLayer = new FilterRowHeaderComposite<>(
				new DefaultGlazedListsFilterStrategy<>(filterList, columnPropertyAccessor, configRegistry),
				columnHeaderLayerStack, columnHeaderDataProvider, configRegistry);
		// Row header
		IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack,
				bodyLayerStack.getSelectionLayer());

		// Corner
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(
				columnHeaderLayerStack.getDataProvider(), rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, filterRowHeaderLayer);

		// 利用之前创建的四个层来创建网格组件层
		gridLayer = new GridLayer(bodyLayerStack, filterRowHeaderLayer, rowHeaderLayer, cornerLayer);
		final ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
		bodyDataLayer.setConfigLabelAccumulator(columnLabelAccumulator);
	}

	private void initSelectionProvider(DefaultBodyLayerStack bodyLayerStack) {
		SelectionLayer selectionLayer = bodyLayerStack.getSelectionLayer();
		RowSelectionProvider<T> rowSelectionProvider = new RowSelectionProvider<>(selectionLayer, bodyDataProvider,
				false);
	}

	private void initLabels(DefaultBodyLayerStack bodyLayerStack) {
		ColumnOverrideLabelAccumulator columnOverrideLabelAccumulator = new ColumnOverrideLabelAccumulator(
				bodyLayerStack);
		bodyLayerStack.setConfigLabelAccumulator(columnOverrideLabelAccumulator);
		IConfigLabelAccumulator distinguishFileNameExt = new IConfigLabelAccumulator() {
			@Override
			public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
				setLabels(configLabels, columnPosition, rowPosition);
			}
		};
		bodyLayerStack.setConfigLabelAccumulator(distinguishFileNameExt);
	}
	
	/**
	 * 重写此方法自定义标签
	 * @param configLabels
	 * @param columnPosition
	 * @param rowPosition
	 */
	public void setLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
		
	}

	private void CreateNatable() {
		natTable = new NatTable(this, gridLayer, false);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration()); // 添加默认配置
		natTable.addConfiguration(new RowOnlySelectionBindings());
		natTable.addConfiguration(new DefaultExportBindings() { // 配置导出 excel 的字符集为 uft-8
			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {
				ExcelExporter excelExporter = new ExcelExporter();
				excelExporter.setCharset("utf-8");
				configRegistry.registerConfigAttribute(ExportConfigAttributes.EXPORTER, excelExporter);
				configRegistry.registerConfigAttribute(ExportConfigAttributes.EXPORT_FORMATTER,
						new DefaultExportFormatter());
				configRegistry.registerConfigAttribute(ExportConfigAttributes.DATE_FORMAT, "m/d/yy h:mm"); //$NON-NLS-1$
			}
		});
		natTable.addConfiguration(getCustomConfiguration()); // 添加排序标签配置
		natTable.addConfiguration(new HeaderMenuConfiguration(natTable)); // 添加头部菜单
		addCustomStyleing(natTable);
		natTable.setConfigRegistry(configRegistry);
		natTable.configure();
		Menu menu = createPopMenu(getShell());
		natTable.setMenu(menu);
		natTable.addMenuDetectListener(new MenuDetectListener() { // 右键菜单

			@Override
			public void menuDetected(MenuDetectEvent e) {
				Object firstElement = ((IStructuredSelection) rowSelectionProvider.getSelection()).getFirstElement();
				if (firstElement != null && ((IStructuredSelection) rowSelectionProvider.getSelection()).size() == 1) {
					e.doit = true;
					selected = (T) firstElement;
				} else {
					e.doit = false;
				}
			}
		});
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(PROPERTIES_FILE)));
			natTable.loadState("", properties); // 加载状态
		} catch (FileNotFoundException e) {
			// No file found, oh well, move along
			System.out.println(PROPERTIES_FILE + " not found, skipping load");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		natTable.addDisposeListener(new DisposeListener() { // 保存状态

			@Override
			public void widgetDisposed(DisposeEvent e) {
				Properties properties = new Properties();
				if (natTable.isDisposed())
					return;
				natTable.saveState("", properties);
				try {
					System.out.println("Saving NatTable state to " + PROPERTIES_FILE);
					properties.store(new FileOutputStream(new File(PROPERTIES_FILE)), "NatTable state");
				} catch (Exception ee) {
					throw new RuntimeException(ee);
				}
			}
		});
	}

	/**
	 * 重写此方法自定义样式
	 * @param natTable
	 */
	public void addCustomStyleing(NatTable natTable) {
		// 配置行头和列头的样式
		natTable.addConfiguration(new StyledRowHeaderConfiguration());
		natTable.addConfiguration(new StyledColumnHeaderConfiguration());

		// 配置行斑马线样式
		DefaultRowStyleConfiguration rowStyleConfiguration = new DefaultRowStyleConfiguration();
		rowStyleConfiguration.oddRowBgColor = GUIHelper.getColor(254, 251, 243);
		rowStyleConfiguration.evenRowBgColor = GUIHelper.COLOR_WHITE;
		natTable.addConfiguration(rowStyleConfiguration);

		// 设置选中样式
		DefaultSelectionStyleConfiguration selectionStyleConfiguration = new DefaultSelectionStyleConfiguration();
		selectionStyleConfiguration.selectionBgColor = GUIHelper.getColor(176, 226, 255);
		selectionStyleConfiguration.anchorBgColor = GUIHelper.getColor(150, 200, 255);
		selectionStyleConfiguration.selectionFgColor = GUIHelper.COLOR_WHITE;
		selectionStyleConfiguration.selectionFont = natTable.getFont();
		natTable.addConfiguration(selectionStyleConfiguration);

	}

	/**
	 * 
	 * @param shell
	 * @return
	 */
	public Menu createPopMenu(final Shell shell) {
		return null;
	}

	public IConfiguration getCustomConfiguration() {
		return new AbstractRegistryConfiguration() {

			@Override
			public void configureRegistry(IConfigRegistry configRegistry) {

				{// 这里做表格美化的相关操作
					// 这里将分享者列颜色改变
					Style style = new Style();
					style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(176, 196, 250));
					style.setAttributeValue(CellStyleAttributes.FOREGROUND_COLOR, GUIHelper.COLOR_WHITE);
					BorderStyle borderStyle = new BorderStyle();
					borderStyle.setColor(GUIHelper.COLOR_DARK_GRAY);
					borderStyle.setLineStyle(BorderStyle.LineStyleEnum.DASHDOTDOT);
					style.setAttributeValue(CellStyleAttributes.BORDER_STYLE, borderStyle);
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
							style, // value of the attribute
							DisplayMode.NORMAL, // apply during normal rendering
							"WOYAOBIANSE"); // apply for all cells with this label

					// 这里将文件扩展名为 mp4 exe txt rar 和其他类型的文件夹的行的颜色区分开来
					final Style rowStyle1 = new Style();
					rowStyle1.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
							GUIHelper.getColor(255, 222, 173));
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle1,
							DisplayMode.NORMAL, "MP4");
					final Style rowStyle2 = new Style();
					rowStyle2.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(173, 255, 47));
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle2,
							DisplayMode.NORMAL, "EXE");
					final Style rowStyle3 = new Style();
					rowStyle3.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
							GUIHelper.getColor(222, 184, 135));
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle3,
							DisplayMode.NORMAL, "TXT");
					final Style rowStyle4 = new Style();
					rowStyle4.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
							GUIHelper.getColor(250, 128, 114));
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle4,
							DisplayMode.NORMAL, "RAR");
					final Style rowStyle5 = new Style();
					rowStyle5.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
							GUIHelper.getColor(220, 220, 220));
					configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle5,
							DisplayMode.NORMAL, "NONE");
				}
				// 这里是为了将过滤行的样式改变
				final Style rowStyle = new Style();
				rowStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.getColor(255, 222, 173));
				configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, rowStyle, DisplayMode.NORMAL,
						GridRegion.FILTER_ROW);

				// 这段代码可以给指定标签的表格 添加自定义的COMPARATOR
				// configRegistry.registerConfigAttribute(FilterRowConfigAttributes.FILTER_COMPARATOR,getCustomComparator(),
				// DisplayMode.NORMAL,FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 2);

				// 给过滤行添加 过滤的图标
				configRegistry.registerConfigAttribute(CELL_PAINTER,
						new FilterRowPainter(new FilterIconPainter(GUIHelper.getImage("filter"))), NORMAL, FILTER_ROW);

				// 如果 单元格的值 是格式化的数据类型 比如Double Date 等 需要为那一列注册 配置类型转换器 用于将用户输入的转换成 可比较的类型
				/*
				 * configRegistry.registerConfigAttribute(
				 * FilterRowConfigAttributes.FILTER_DISPLAY_CONVERTER,
				 * this.doubleDisplayConverter, //类型转换器 DisplayMode.NORMAL,
				 * FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);
				 * configRegistry.registerConfigAttribute(
				 * FilterRowConfigAttributes.TEXT_MATCHING_MODE,
				 * TextMatchingMode.REGULAR_EXPRESSION, DisplayMode.NORMAL,
				 * FilterRowDataLayer.FILTER_ROW_COLUMN_LABEL_PREFIX + 5);
				 */

				// Register custom comparator
				configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, getCustomComparator(),
						DisplayMode.NORMAL, CUSTOM_COMPARATOR_LABEL);

				// Register null comparator to disable sort
				configRegistry.registerConfigAttribute(SortConfigAttributes.SORT_COMPARATOR, new NullComparator(),
						DisplayMode.NORMAL, NO_SORT_LABEL);
			}
		};
	}

	public Comparator<String> getCustomComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		};
	}

}
