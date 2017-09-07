/*

Martus(TM) is a trademark of Beneficent Technology, Inc.
This software is (c) Copyright 2001-2017, Beneficent Technology, Inc.

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

import javax.swing.JComponent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.MiniFieldSpec;

public class SwingInFxSortFieldsDlg extends SwingInFxDialog implements SortFieldsDlgInterface
{
	public SwingInFxSortFieldsDlg(UiMainWindow mainWindowToUse, MiniFieldSpec[] specsToAllowToUse)
	{
		mainWindow = mainWindowToUse;
		specsToAllow = specsToAllowToUse;

		createAndSetSwingContent();
	}

	@Override
	public JComponent createSwingContent()
	{
		uiSortFieldsDlg = new SortFieldsDlg(mainWindow, specsToAllow);
		return uiSortFieldsDlg.getMainContent();
	}

	class SortFieldsDlg extends UiSortFieldsDlg
	{
		public SortFieldsDlg(UiMainWindow mainWindow, MiniFieldSpec[] specsToAllow)
		{
			super(mainWindow, specsToAllow);
		}

		@Override
		public void dispose()
		{
			closeDialog();
		}
	}

	@Override
	public int getDialogWidth()
	{
		return 900;
	}

	@Override
	public int getDialogHeight()
	{
		return 250;
	}

	@Override
	public boolean ok()
	{
		return uiSortFieldsDlg.ok();
	}

	@Override
	public MiniFieldSpec[] getSelectedMiniFieldSpecs()
	{
		return uiSortFieldsDlg.getSelectedMiniFieldSpecs();
	}

	@Override
	public boolean getPrintBreaks()
	{
		return uiSortFieldsDlg.getPrintBreaks();
	}

	@Override
	public boolean getHideDetail()
	{
		return uiSortFieldsDlg.getHideDetail();
	}

	@Override
	public void showDialog()
	{
		showAndWait();
	}

	private UiMainWindow mainWindow;
	private MiniFieldSpec[] specsToAllow;

	private UiSortFieldsDlg uiSortFieldsDlg;
}
