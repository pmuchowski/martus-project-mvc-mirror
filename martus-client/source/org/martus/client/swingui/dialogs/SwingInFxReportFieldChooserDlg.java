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
import org.martus.client.swingui.dialogs.UiReportFieldChooserDlg.ResultsHandler;
import org.martus.common.fieldspec.FieldSpec;

public class SwingInFxReportFieldChooserDlg extends SwingInFxDialog implements ReportFieldDlgInterface
{
	public SwingInFxReportFieldChooserDlg(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse, ResultsHandler resultsHandlerToUse)
	{
		this(mainWindowToUse, specsToUse);
		resultsHandler = resultsHandlerToUse;
	}

	public SwingInFxReportFieldChooserDlg(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse)
	{
		mainWindow = mainWindowToUse;
		specs = specsToUse;

		createAndSetSwingContent();
	}

	@Override
	public JComponent createSwingContent()
	{
		reportFieldDlg = createReportFieldChooserDlg(mainWindow, specs, resultsHandler);
		return reportFieldDlg.getMainContent();
	}

	class ReportFieldChooserDlg extends UiReportFieldChooserDlg
	{
		public ReportFieldChooserDlg(UiMainWindow mainWindowToUse, FieldSpec[] specsToUse, ResultsHandler resultsHandlerToUse)
		{
			super(mainWindowToUse, specsToUse, resultsHandlerToUse);
		}

		@Override
		public void dispose()
		{
			closeDialog();
		}
	}

	protected UIReportFieldDlg createReportFieldChooserDlg(UiMainWindow mainWindow, FieldSpec[] specs, ResultsHandler resultsHandler)
	{
		return new ReportFieldChooserDlg(mainWindow, specs, resultsHandler);
	}

	@Override
	public FieldSpec[] getSelectedSpecs()
	{
		return reportFieldDlg.getSelectedSpecs();
	}

	@Override
	public void showDialog()
	{
		showAndWait();
	}

	private UIReportFieldDlg reportFieldDlg;
	private FieldSpec[] specs;
	private ResultsHandler resultsHandler;
	private UiMainWindow mainWindow;
}
