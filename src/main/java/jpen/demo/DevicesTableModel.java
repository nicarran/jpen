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

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import jpen.event.PenManagerListener;
import jpen.owner.multiAwt.AwtPenToolkit;
import jpen.PenDevice;
import jpen.PenManager;
import jpen.PenProvider;
import jpen.PKind;

class DevicesTableModel
			extends AbstractTableModel
	implements PenManagerListener{

	private final List<PenDevice> devices=new ArrayList<PenDevice>();
	final PKindTypeNumberCombo kindTypeNumberCombo=new PKindTypeNumberCombo();
	private boolean supportCustomPKinds;

	private final Column[] columns=new Column[]{
				new Column<String>("Name (Physical Id)", String.class){
					@Override
					Object getValue(PenDevice device){
						return device.getName()+" ("+device.getPhysicalId()+")";
					}
				},
				/*new Column<String>("Provider", String.class){
				 @Override
				 Object getValue(PenDevice device){
				  return device.getProvider().getConstructor().getName();
				 }
			},*/
				/*new Column<Boolean>("Enabled", Boolean.class, true){
				 @Override
				 Object getValue(PenDevice device){
				  return device.getEnabled();
				 }
				 @Override
				 void setValue(Boolean val, PenDevice device){
				  device.setEnabled(val);
				 }
			},*/	
				new Column<Integer>("Kind", Integer.class, true){
					private final MyCellRenderer myCellRenderer=new MyCellRenderer();
					private final TableCellEditor myCellEditor=new DefaultCellEditor(kindTypeNumberCombo.comboBox);

					class MyCellRenderer
						extends DefaultTableCellRenderer{
						{
							setHorizontalAlignment(JLabel.CENTER);
						}
						@Override
						public Component 	getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
							super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
							setText(PKindTypeNumberCombo.getPKindTypeStringValue((Integer)value));
							return this;
						}
					}

					@Override
					Object getValue(PenDevice device){
						return device.getKindTypeNumber();
					}

					@Override
					void setValue(Integer val, PenDevice device){
						try{
							if(device.getKindTypeNumber()==val)
							return;
							device.setKindTypeNumber(val);
						}catch(Exception ex){
							JOptionPane.showMessageDialog(null, ex.getMessage());
						}
						updateKindTypeNumberCombo();
					}

					@Override
					TableCellRenderer getCellRenderer(){
						return myCellRenderer;
					}

					@Override
					TableCellEditor getCellEditor(){
						return myCellEditor;
					}
				},
			};

	private abstract class Column<C>{
		final String name;
		final Class<C> clazz;
		final boolean isCellEditable;
		Column(String name, Class<C> clazz){
			this(name, clazz, false);
		}
		Column(String name, Class<C> clazz, boolean isCellEditable){
			this.name=name;
			this.clazz=clazz;
			this.isCellEditable=isCellEditable;
		}

		Object getValueAt(int row){
			return getValue(devices.get(row));
		}

		abstract Object getValue(PenDevice device);

		void setValueAt(Object val, int row){
			setValue(clazz.cast(val), devices.get(row));
		}

		void setValue(C val, PenDevice device){}

		TableCellEditor getCellEditor(){
			return null;
		}

		TableCellRenderer getCellRenderer(){
			return null;
		}
	}

	DevicesTableModel(){
		AwtPenToolkit.getPenManager().addListener(this);
		updateDevices();
	}

	void setSupportCustomPKinds(boolean supportCustomPKinds){
		this.supportCustomPKinds=supportCustomPKinds;
		updateKindTypeNumberCombo();
	}

	private void updateDevices(){
		devices.clear();
		devices.addAll(AwtPenToolkit.getPenManager().getDevices());
		updateKindTypeNumberCombo();
		fireTableDataChanged();
	}

	private void updateKindTypeNumberCombo(){
		int devicesMax=0;
		for(PenDevice device: devices)
			if(devicesMax<device.getKindTypeNumber())
				devicesMax=device.getKindTypeNumber();
		if(supportCustomPKinds){
			if(++devicesMax<PKind.Type.VALUES.size())
				devicesMax=PKind.Type.VALUES.size(); // to show at least one custom PKind
		}
		if(devicesMax>kindTypeNumberCombo.getMaxPKindTypeNumber())
			kindTypeNumberCombo.setMaxPKindTypeNumber(devicesMax);
	}

	//@Override
	public void 	penDeviceAdded(PenProvider.Constructor providerConstructor, PenDevice penDevice){
		updateDevices();
	}

	//@Override
	public void 	penDeviceRemoved(PenProvider.Constructor providerConstructor, PenDevice penDevice){
		updateDevices();
	}

	//@Override
	public int getRowCount(){
		return devices.size();
	}
	//@Override
	public int getColumnCount(){
		return columns.length;
	}
	//@Override
	public Object getValueAt(int row, int column){
		return columns[column].getValueAt(row);
	}
	@Override
	public String getColumnName(int col){
		return columns[col].name;
	}
	@Override
	public Class<?> getColumnClass(int col){
		return columns[col].clazz;
	}
	@Override
	public boolean isCellEditable(int row, int col){
		return columns[col].isCellEditable;
	}
	@Override
	public void 	setValueAt(Object aValue, int row, int col){
		columns[col].setValueAt(aValue, row);
	}

	public TableCellEditor getCellEditor(int row, int col){
		return columns[col].getCellEditor();
	}

	public TableCellRenderer getCellRenderer(int row, int col){
		return columns[col].getCellRenderer();
	}
}