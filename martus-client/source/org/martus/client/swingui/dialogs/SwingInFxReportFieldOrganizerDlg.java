package org.martus.client.swingui.dialogs;

import javax.swing.JComponent;

import org.martus.client.swingui.UiMainWindow;
import org.martus.common.fieldspec.FieldSpec;

public class SwingInFxReportFieldOrganizerDlg extends SwingInFxDialog implements ReportFieldDlgInterface
{
	public SwingInFxReportFieldOrganizerDlg(UiMainWindow mainWindowToUse)
	{
		mainWindow = mainWindowToUse;

		createAndSetSwingContent();
	}

	@Override
	public JComponent createSwingContent()
	{
		reportFieldDlg = new SwingReportFieldOrganizerDlg(mainWindow);
		return reportFieldDlg.getMainContent();
	}

	class SwingReportFieldOrganizerDlg extends UiReportFieldOrganizerDlg
	{
		public SwingReportFieldOrganizerDlg(UiMainWindow mainWindowToUse)
		{
			super(mainWindowToUse);
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
		return 600;
	}

	@Override
	public int getDialogHeight()
	{
		return 600;
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
	private UiMainWindow mainWindow;
}
