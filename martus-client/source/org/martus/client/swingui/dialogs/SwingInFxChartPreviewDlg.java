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

import org.jfree.chart.JFreeChart;
import org.martus.client.swingui.UiMainWindow;

public class SwingInFxChartPreviewDlg extends SwingInFxDialog implements PreviewDlgInterface
{
	public SwingInFxChartPreviewDlg(UiMainWindow mainWindowToUse, JFreeChart chartToUse)
	{
		mainWindow = mainWindowToUse;
		chart = chartToUse;

		createAndSetSwingContent();
	}

	@Override
	public JComponent createSwingContent()
	{
		previewDlg = new ChartPreviewDlg(mainWindow, chart);
		return previewDlg.getMainContent();
	}

	class ChartPreviewDlg extends UiChartPreviewDlg
	{
		public ChartPreviewDlg(UiMainWindow mainWindowToUse, JFreeChart chart)
		{
			super(mainWindowToUse, chart);
		}

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
		return 800;
	}

	@Override
	public boolean wantsPrintToDisk()
	{
		return previewDlg.wantsPrintToDisk();
	}

	@Override
	public boolean wantsExportToCsv()
	{
		return previewDlg.wantsExportToCsv();
	}

	@Override
	public boolean wasCancelButtonPressed()
	{
		return previewDlg.wasCancelButtonPressed();
	}

	@Override
	public void showDialog()
	{
		showAndWait();
	}

	private UiMainWindow mainWindow;
	private JFreeChart chart;

	private UiPreviewDlg previewDlg;
}
