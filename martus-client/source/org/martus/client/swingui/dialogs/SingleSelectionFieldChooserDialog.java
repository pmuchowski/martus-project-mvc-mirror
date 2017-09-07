/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.client.swingui.dialogs;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.martus.client.reports.SpecTableModel;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.FieldSpec;

public class SingleSelectionFieldChooserDialog extends UiReportFieldChooserDlg
{
	public SingleSelectionFieldChooserDialog(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse)
	{
		super(mainWindowToUse, specsToUse);
		
		enableSingleSelection();
		
		getTableModel().addTableModelListener(new TableModelHandler());
	}

	public SingleSelectionFieldChooserDialog(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse, ResultsHandler resultsHandlerToUse)
	{
		super(mainWindowToUse, specsToUse, resultsHandlerToUse);

		enableSingleSelection();

		getTableModel().addTableModelListener(new TableModelHandler());
	}

	protected SpecTableModel getTableModel()
	{
		return selectedFieldsSelector.model;
	}
	
	public void enableSingleSelection()
	{
		fieldSelector.enableSingleSelection();
	}
	
	protected class TableModelHandler implements TableModelListener
	{
		@Override
		public void tableChanged(TableModelEvent e)
		{
			addFieldButton.setEnabled(hasEmptyRowCount());
		}

		private boolean hasEmptyRowCount()
		{
			return getTableModel().getRowCount() == 0;
		}
	}
}
