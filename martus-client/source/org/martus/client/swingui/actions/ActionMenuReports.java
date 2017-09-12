/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2006-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
package org.martus.client.swingui.actions;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;

import org.json.JSONObject;
import org.martus.client.bulletinstore.ExportBulletinsWithSpecifiedFields;
import org.martus.client.core.MartusApp;
import org.martus.client.core.SortableBulletinList;
import org.martus.client.reports.PageReportBuilder;
import org.martus.client.reports.ReportAnswers;
import org.martus.client.reports.ReportAnswers.ReportType;
import org.martus.client.reports.ReportFormat;
import org.martus.client.reports.ReportFormatFilter;
import org.martus.client.reports.ReportOutput;
import org.martus.client.reports.ReportRunner;
import org.martus.client.reports.RunReportOptions;
import org.martus.client.reports.TabularReportBuilder;
import org.martus.client.search.FieldChooserSpecBuilder;
import org.martus.client.search.PageReportFieldChooserSpecBuilder;
import org.martus.client.search.SearchTreeNode;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.WorkerThread;
import org.martus.client.swingui.dialogs.PreviewDlgInterface;
import org.martus.client.swingui.dialogs.PushButtonsDlgInterface;
import org.martus.client.swingui.dialogs.ReportFieldDlgInterface;
import org.martus.client.swingui.dialogs.SortFieldsDlgInterface;
import org.martus.client.swingui.filefilters.CsvFileFilter;
import org.martus.clientside.FormatFilter;
import org.martus.common.EnglishCommonStrings;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeBoolean;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.util.UnicodeWriter;


public class ActionMenuReports extends ActionPrint implements ActionDoer
{
	private static final boolean SHOULD_EXPORT_ALL_VERSIONS = false;
	private static final boolean SHOULD_EXPORT_ATTACHMENTS = false;
	private static final boolean ALWAYS_EXPORT_PRIVATE_DATA = true;

	public ActionMenuReports(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse, "Reports");
	}

	public boolean isEnabled()
	{
		return true;
	}

	public void actionPerformed(ActionEvent ae)
	{
		doAction();
	}

	public void doAction()
	{
		try
		{
			MiniLocalization localization = mainWindow.getLocalization();
			
			String runButtonLabel = localization.getButtonLabel("RunReport");
			String createTabularReportButtonLabel = localization.getButtonLabel("CreateTabularReport");
			String createPageReportButtonLabel = localization.getButtonLabel("CreatePageReport");
			String cancelButtonLabel = localization.getButtonLabel(EnglishCommonStrings.CANCEL);
			String[] buttonLabels = {runButtonLabel, createTabularReportButtonLabel, createPageReportButtonLabel, cancelButtonLabel, };
			String title = mainWindow.getLocalization().getWindowTitle("RunOrCreateReport");
			PushButtonsDlgInterface runOrCreate = mainWindow.createPushButtonsDlg(title, buttonLabels);
			runOrCreate.showDialog();
			String pressed = runOrCreate.getPressedButtonLabel();
			if(pressed == null || pressed.equals(cancelButtonLabel))
				return;
			
			ReportAnswers answers = null;
			if(pressed.equals(runButtonLabel))
			{
				answers = chooseAndLoad();
			}
			if(pressed.equals(createTabularReportButtonLabel))
			{
				answers = createAndSave(ReportAnswers.TABULAR_REPORT);
			}
			if(pressed.equals(createPageReportButtonLabel))
			{
				answers = createAndSave(ReportAnswers.PAGE_REPORT);
			}

			if(answers == null)
				return;
			
			runReport(answers);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			mainWindow.notifyDlgBeep("UnexpectedError");
		}
	}
	
	ReportAnswers chooseAndLoad() throws Exception
	{
		ReportAnswers answers = chooseReport();
		if(answers == null)
			return null;
		
		int version = answers.getVersion();
		
		if(version < ReportAnswers.EXPECTED_VERSION)
			mainWindow.notifyDlg("ReportFormatIsOld");
		else if(version > ReportAnswers.EXPECTED_VERSION)
			mainWindow.notifyDlg("ReportFormatIsTooNew");
		
		String language = answers.getLanguageCode();
		if(!language.equals(getLocalization().getCurrentLanguageCode()))
		{
			mainWindow.notifyDlg("ReportFormatDifferentLanguage");
		}
		return answers;
	}
	
	ReportAnswers createAndSave(ReportType reportType) throws Exception
	{
		MiniFieldSpec[] specs = askUserWhichFieldsToInclude(reportType);
		if(specs == null)
			return null;
		ReportAnswers answers = new ReportAnswers(reportType, specs, getLocalization());
		
		File file = askForReportFileToSaveTo();
		if(file == null)
			return null;
		
		getSecurity().saveEncryptedStringToFile(file, answers.toJson().toString());
		
		return answers;
	}

	private MartusCrypto getSecurity()
	{
		return mainWindow.getApp().getSecurity();
	}

	private ReportFormat buildReportFormat(ReportAnswers answers)
	{
		if(answers.isPageReport())
		{
			PageReportBuilder builder = new PageReportBuilder(getLocalization());
			return builder.createPageReport(answers.getSpecs());
		}
		else if(answers.isTabularReport())
		{
			TabularReportBuilder builder = new TabularReportBuilder(getLocalization());
			return builder.createTabular(answers.getSpecs());
		}
		
		return null;
	}
	
	ReportAnswers chooseReport() throws Exception
	{
		FormatFilter filter = new ReportFormatFilter(getLocalization());
		File chosenFile = mainWindow.showFileOpenDialog("SelectReport", filter);
		if(chosenFile == null)
			return null;
	
		try
		{
			String reportAnswersText = getSecurity().loadEncryptedStringFromFile(chosenFile);
			JSONObject json = new JSONObject(reportAnswersText);
			if(json.optString(ReportAnswers.TAG_JSON_TYPE).equals(ReportAnswers.JSON_TYPE))
				return new ReportAnswers(json);
		} 
		catch (IOException e)
		{
			// fall through to the code below
		}

		mainWindow.notifyDlg("NotValidReportFormat");
		return null;
	}

	File askForReportFileToSaveTo()
	{
		FormatFilter filter = new ReportFormatFilter(getLocalization());
		return mainWindow.showFileSaveDialog("SaveReportFormat", filter);
	}

	void runReport(ReportAnswers answers) throws Exception
	{
		SearchTreeNode searchTree = mainWindow.askUserForSearchCriteria();
		if(searchTree == null)
			return;

		SortFieldsDlgInterface sortDlg = mainWindow.createSortFieldsDlg(answers.getSpecs());
		sortDlg.showDialog();
		if(!sortDlg.ok())
			return;
		
		MiniFieldSpec[] sortTags = sortDlg.getSelectedMiniFieldSpecs();
		FieldSpec allPrivateSpec = FieldSpec.createStandardField(Bulletin.PSEUDOFIELD_ALL_PRIVATE, new FieldTypeBoolean());
		MiniFieldSpec allPrivateMiniSpec = new MiniFieldSpec(allPrivateSpec);
		MiniFieldSpec[] extraTags = {allPrivateMiniSpec};
		SortableBulletinList sortableList = doSearch(searchTree, sortTags, extraTags, "ReportSearchProgress");
		if(sortableList == null)
			return;
		
		if(sortableList.size() == 0)
		{
			mainWindow.notifyDlg("SearchFailed");
			return;
		}
	
		RunReportOptions options = new RunReportOptions();
		options.printBreaks = sortDlg.getPrintBreaks();
		options.hideDetail = sortDlg.getHideDetail();
		options.includePrivate = ALWAYS_EXPORT_PRIVATE_DATA;

		ReportFormat rf = buildReportFormat(answers);

		ReportOutput result = new ReportOutput();
		printToWriter(result, rf, sortableList, options);
		result.close();

		PreviewDlgInterface printPreview = mainWindow.createPrintPreviewDlg(result);
		printPreview.showDialog();
		if(printPreview.wasCancelButtonPressed())
			return;
		boolean sendToDisk = printPreview.wantsPrintToDisk();
		boolean exportToCsv = printPreview.wantsExportToCsv();
		
		boolean didPrint;
		if(sendToDisk)
			didPrint = printToDisk(result);
		else if (exportToCsv)
		{
			File destFile = chooseDestinationFile(new CsvFileFilter(getLocalization()));
			didPrint = exportToCsv(destFile, options.includePrivate, sortableList, answers.getSpecs());
		}
		else
			didPrint = printToPrinter(result);
			
		if(didPrint)
			mainWindow.notifyDlg("PrintCompleted");
			
	}

	private boolean exportToCsv(File destFile, boolean includePrivate, SortableBulletinList list, MiniFieldSpec[] specs) throws Exception
	{
		Vector bulletinsToExport = mainWindow.getBulletins(list.getSortedUniversalIds());
		ExportBulletinsWithSpecifiedFields exporter = new ExportBulletinsWithSpecifiedFields(mainWindow, specs);
		exporter.setExportPrivate(includePrivate);
		exporter.setExportAttachments(SHOULD_EXPORT_ATTACHMENTS);
		exporter.setExportAllVersions(SHOULD_EXPORT_ALL_VERSIONS);
		exporter.doExport(destFile, bulletinsToExport);

		return !exporter.didErrorOccur();
	}

	boolean printToDisk(ReportOutput output) throws Exception
	{
		File destFile = chooseDestinationFile();
		if(destFile == null)
			return false;

		UnicodeWriter destination = new UnicodeWriter(destFile);
		destination.write(output.getPrintableDocument());
		destination.close();
		return true;
	}

	static class BackgroundPrinter extends WorkerThread
	{
		public BackgroundPrinter(UiMainWindow mainWindowToUse, ReportOutput whereToPrint, ReportFormat reportFormatToUse, 
				SortableBulletinList listToPrint, RunReportOptions optionsToUse)
		{
			mainWindow = mainWindowToUse;
			destination = whereToPrint;
			rf = reportFormatToUse;
			list = listToPrint;
			options = optionsToUse;
		}
		
		public void doTheWorkWithNO_SWING_CALLS() throws Exception
		{
			MartusApp app = mainWindow.getApp();
			ReportRunner rr = new ReportRunner(app);
			rr.runReport(rf, mainWindow.getStore().getDatabase(), list, destination, options, app.getStore().getAllReusableChoiceLists());
		}
		
		UiMainWindow mainWindow;
		ReportOutput destination;
		ReportFormat rf;
		SortableBulletinList list;
		RunReportOptions options;
	}

	private void printToWriter(ReportOutput destination, ReportFormat rf, SortableBulletinList list, RunReportOptions options) throws Exception
	{
		BackgroundPrinter worker = new BackgroundPrinter(mainWindow, destination, rf, list, options);
		mainWindow.doBackgroundWork(worker, "BackgroundPrinting");
	}
	
	boolean printToPrinter(ReportOutput output) throws Exception
	{
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(output);
		HashPrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
		if(!printJob.printDialog(attributes))
			return false;
		
		printJob.print(attributes);
		return true;
	}

	//TODO: Instead of passing in a constant pass in a Factory instead which will create the correct dialog
	MiniFieldSpec[] askUserWhichFieldsToInclude(ReportAnswers.ReportType reportType)
	{
		while(true)
		{
			ReportFieldDlgInterface dlg;
			if(reportType.isPage())
			{
				FieldChooserSpecBuilder fieldChooserSpecBuilder = new PageReportFieldChooserSpecBuilder(mainWindow.getLocalization());
				FieldSpec[] availableFieldSpecs = fieldChooserSpecBuilder.createFieldSpecArray(mainWindow.getStore());
				dlg = mainWindow.createReportFieldChooserDlg(availableFieldSpecs);
			}
			else if(reportType.isTabular())
			{
				dlg = mainWindow.createReportFieldOrganizerDlg();
			}
			else
			{
				return null;
			}
			dlg.showDialog();
			FieldSpec[] selectedSpecs = dlg.getSelectedSpecs();
			if(selectedSpecs == null)
				return null;
			if(selectedSpecs.length == 0)
			{
				mainWindow.notifyDlg("NoReportFieldsSelected");
				continue;
			}
			MiniFieldSpec[] specs = new MiniFieldSpec[selectedSpecs.length];
			for(int i = 0; i < specs.length; ++i)
			{
				selectedSpecs[i].setLabel(fontHelper.getStorable(selectedSpecs[i].getLabel()));
				specs[i] = new MiniFieldSpec(selectedSpecs[i]);
			}
			return specs;
		}
	}

}

