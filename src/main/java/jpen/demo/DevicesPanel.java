/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.demo;

import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import jpen.PenManager;

class DevicesPanel{

	final DevicesTableModel devicesTableModel;
	private final JScrollPane scrollPane;
	final JComponent panel;

	DevicesPanel(){
		this.devicesTableModel=new DevicesTableModel();
		JTable table=new JTable(devicesTableModel){
									 @Override
									 public TableCellEditor getCellEditor(int row, int col){
										 TableCellEditor cellEditor=devicesTableModel.getCellEditor(
													 /*convertRowIndexToModel(*/row/*)*/,
													 convertColumnIndexToModel(col));
										 return cellEditor==null? super.getCellEditor(row, col): cellEditor;
									 }
									 @Override
									 public TableCellRenderer getCellRenderer(int row, int col){
										 TableCellRenderer cellRenderer=devicesTableModel.getCellRenderer(
													 /*convertRowIndexToModel(*/row/*)*/,
													 convertColumnIndexToModel(col));
										 return cellRenderer==null? super.getCellRenderer(row, col): cellRenderer;
									 }
								 };
		this.scrollPane=new JScrollPane(table);
		scrollPane.setBorder(BorderFactory.createEmptyBorder(3, 3, 2, 2));
		scrollPane.setPreferredSize(new Dimension(100, 100));
		this.panel=scrollPane;
	}
}