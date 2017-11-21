package cn.weekdragon.nattable.config;

import org.eclipse.nebula.widgets.nattable.layer.config.DefaultRowHeaderStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;

public class StyledRowHeaderConfiguration extends DefaultRowHeaderStyleConfiguration {

    public StyledRowHeaderConfiguration() {
        this.font = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));

        Image bgImage = GUIHelper.getImageByURL("rowHeaderBg",
                getClass().getResource("row_header_bg.png"));
        TextPainter txtPainter = new TextPainter(false, false);
        ICellPainter bgImagePainter =
                new BackgroundImagePainter(txtPainter, bgImage, null);
        this.cellPainter = bgImagePainter;
    }
}
