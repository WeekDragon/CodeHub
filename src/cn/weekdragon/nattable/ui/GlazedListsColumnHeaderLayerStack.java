/*******************************************************************************
 * Copyright (c) 2012, 2013 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package cn.weekdragon.nattable.ui;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.SortedList;

public class GlazedListsColumnHeaderLayerStack<T> extends
        AbstractLayerTransform {
    private IDataProvider columnHeaderDataProvider;
    private DefaultColumnHeaderDataLayer columnHeaderDataLayer;
    private ColumnHeaderLayer columnHeaderLayer;

    public GlazedListsColumnHeaderLayerStack(String[] propertyNames,
            Map<String, String> propertyToLabelMap, SortedList<T> sortedList,
            IColumnPropertyAccessor<T> columnPropertyAccessor,
            IConfigRegistry configRegistry, DefaultBodyLayerStack bodyLayerStack) {

        this(new DefaultColumnHeaderDataProvider(propertyNames,
                propertyToLabelMap), sortedList, columnPropertyAccessor,
                configRegistry, bodyLayerStack);
    }

    public GlazedListsColumnHeaderLayerStack(IDataProvider columnHeaderDataProvider,
            SortedList<T> sortedList,
            IColumnPropertyAccessor<T> columnPropertyAccessor,
            IConfigRegistry configRegistry, DefaultBodyLayerStack bodyLayerStack) {

        this.columnHeaderDataProvider = columnHeaderDataProvider;
        this.columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        this.columnHeaderLayer = new ColumnHeaderLayer(this.columnHeaderDataLayer, bodyLayerStack,bodyLayerStack.getSelectionLayer());

        SortHeaderLayer<T> sortHeaderLayer = new SortHeaderLayer<>(
                this.columnHeaderLayer, new GlazedListsSortModel<>(sortedList,
                        columnPropertyAccessor, configRegistry, this.columnHeaderDataLayer),
                false);
        sortHeaderLayer.addConfiguration(new SingleClickSortConfiguration());
        setUnderlyingLayer(sortHeaderLayer);
        
    }

    @Override
    public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
        super.setClientAreaProvider(clientAreaProvider);
    }

    public DataLayer getDataLayer() {
        return this.columnHeaderDataLayer;
    }

    public IDataProvider getDataProvider() {
        return this.columnHeaderDataProvider;
    }

    public ColumnHeaderLayer getColumnHeaderLayer() {
        return this.columnHeaderLayer;
    }
}
