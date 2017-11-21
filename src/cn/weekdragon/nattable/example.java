package cn.weekdragon.nattable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import cn.weekdragon.nattable.model.SFile;
import cn.weekdragon.nattable.ui.MNatTable;

public class example {

	protected Shell shell;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			example window = new example();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("SWT Application");
		shell.setLayout(new FillLayout());
		List<SFile> datalist = new ArrayList<>();
		datalist.add(new SFile(1, "文件1", "1mb"));
		datalist.add(new SFile(2, "文件3", "1mb"));
		datalist.add(new SFile(4, "文件5", "1mb"));
		datalist.add(new SFile(3, "文件2", "1mb"));
		
		new MNatTable<SFile>(shell, SWT.NONE, datalist, new String[] {"id","name","size"});

	}

}
