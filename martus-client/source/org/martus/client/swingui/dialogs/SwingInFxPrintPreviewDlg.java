package org.martus.client.swingui.dialogs;

import javax.swing.JComponent;

import org.martus.client.reports.ReportOutput;
import org.martus.client.swingui.UiMainWindow;

public class SwingInFxPrintPreviewDlg extends SwingInFxDialog implements PreviewDlgInterface
{
	public SwingInFxPrintPreviewDlg(UiMainWindow mainWindowToUse, ReportOutput outputToUse)
	{
		mainWindow = mainWindowToUse;
		output = outputToUse;

		createAndSetSwingContent();
	}

	@Override
	public JComponent createSwingContent()
	{
		previewDlg = new PrintPreviewDlg(mainWindow, output);
		return previewDlg.getMainContent();
	}

	class PrintPreviewDlg extends UiPrintPreviewDlg
	{
		public PrintPreviewDlg(UiMainWindow mainWindowToUse, ReportOutput output)
		{
			super(mainWindowToUse, output);
		}

		public void dispose()
		{
			closeDialog();
		}
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
	private ReportOutput output;

	private UiPreviewDlg previewDlg;
}
