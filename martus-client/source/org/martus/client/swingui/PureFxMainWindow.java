/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2015, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.client.swingui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;

import org.martus.client.core.BulletinGetterThread;
import org.martus.client.reports.ReportOutput;
import org.martus.client.swingui.dialogs.FancySearchDialogInterface;
import org.martus.client.swingui.dialogs.ModelessBusyDlg;
import org.martus.client.swingui.dialogs.PreviewDlgInterface;
import org.martus.client.swingui.dialogs.ProgressMeterDialogInterface;
import org.martus.client.swingui.dialogs.PureFxBulletinModifyDialog;
import org.martus.client.swingui.dialogs.PureFxModelessBusyDlg;
import org.martus.client.swingui.dialogs.PureFxNotifyDlg;
import org.martus.client.swingui.dialogs.PureFxProgressWithCancelDlg;
import org.martus.client.swingui.dialogs.PureFxPushButtonsDlg;
import org.martus.client.swingui.dialogs.PureFxScrollableTextDlg;
import org.martus.client.swingui.dialogs.PureFxStringInputDlg;
import org.martus.client.swingui.dialogs.PureFxUtilities;
import org.martus.client.swingui.dialogs.PureFxWarningMessageDlg;
import org.martus.client.swingui.dialogs.PushButtonsDlgInterface;
import org.martus.client.swingui.dialogs.ReportFieldDlgInterface;
import org.martus.client.swingui.dialogs.SortFieldsDlgInterface;
import org.martus.client.swingui.dialogs.SwingInFxPrintPreviewDlg;
import org.martus.client.swingui.dialogs.SwingInFxReportFieldChooserDlg;
import org.martus.client.swingui.dialogs.SwingInFxReportFieldOrganizerDlg;
import org.martus.client.swingui.dialogs.SwingInFxSingleSelectionFieldChooserDlg;
import org.martus.client.swingui.dialogs.SwingInFxSortFieldsDlg;
import org.martus.client.swingui.dialogs.UiAboutDlg;
import org.martus.client.swingui.dialogs.UiBulletinModifyDlg;
import org.martus.client.swingui.dialogs.UiReportFieldChooserDlg.ResultsHandler;
import org.martus.client.swingui.jfx.contacts.PureFxContactsStage;
import org.martus.client.swingui.jfx.generic.FancySearchDialogController;
import org.martus.client.swingui.jfx.generic.FxShellController;
import org.martus.client.swingui.jfx.generic.FxStatusBar;
import org.martus.client.swingui.jfx.generic.PureFxDialogStage;
import org.martus.client.swingui.jfx.generic.PureFxStage;
import org.martus.client.swingui.jfx.generic.VirtualStage;
import org.martus.client.swingui.jfx.landing.FxMainStage;
import org.martus.client.swingui.jfx.landing.PureFxMainStage;
import org.martus.client.swingui.jfx.setupwizard.PureFxCreateNewAccountWizardStage;
import org.martus.client.swingui.jfx.setupwizard.PureFxSetupWizardStage;
import org.martus.clientside.FormatFilter;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.martus.common.packet.UniversalId;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class PureFxMainWindow extends UiMainWindow
{
	public PureFxMainWindow() throws Exception
	{
		super();
	}

	public StatusBar createStatusBar()
	{
		return new FxStatusBar(getLocalization());
	}

	@Override
	public JFrame getSwingFrame()
	{
		return null;
	}

	@Override
	public UiMainPane getMainPane()
	{
		return null;
	}
	
	@Override
	public FxMainStage getMainStage()
	{
		return mainStage;
	}

	@Override
	protected void hideMainWindow()
	{
		realStage.hide();
	}
	
	@Override
	protected void initializeFrame() throws Exception
	{
		setStatusBar(createStatusBar());
		PureFxMainStage fxStage = new PureFxMainStage(this, realStage);
		mainStage = fxStage;
		fxStage.showCurrentPage();
		restoreWindowSizeAndState();
		realStage.setTitle(getLocalization().getWindowTitle("main"));
		realStage.show();
	}
	
	@Override
	public void restoreWindowSizeAndState()
	{
		Dimension appDimension = getUiState().getCurrentAppDimension();
		Point appPosition = getUiState().getCurrentAppPosition();
		boolean showMaximized = getUiState().isCurrentAppMaximized();

		if (appDimension.getHeight() == 0 || appDimension.getWidth() == 0)
		{
			showMaximized = true;
		}
		else
		{
			realStage.setWidth(appDimension.getWidth());
			realStage.setHeight(appDimension.getHeight());
		}

		realStage.setX(appPosition.getX());
		realStage.setY(appPosition.getY());
		realStage.setMaximized(showMaximized);

		getUiState().setCurrentAppDimension(getMainWindowSize());
	}

	@Override
	public void rawError(String errorText)
	{
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("ERROR");
		alert.setHeaderText(null);
		alert.setContentText(errorText);

		alert.showAndWait();
	}

	@Override
	public void rawSetCursor(Object newCursor)
	{
		realStage.getScene().setCursor((Cursor) newCursor);
	}

	@Override
	public Object getWaitCursor()
	{
		return Cursor.WAIT;
	}

	@Override
	public Object getExistingCursor()
	{
		return realStage.getScene().getCursor();
	}

	@Override
	protected void showMainWindow()
	{
		realStage.show();
	}
	
	@Override
	protected void obscureMainWindow()
	{
		// FIXME: PureFX: We need to support this
	}
	
	public static void setStage(Stage stage)
	{
		PureFxMainWindow.realStage = stage;
	}
	
	@Override
	public void createAndShowLargeModalDialog(VirtualStage stageToShow) throws Exception
	{
		PureFxStage fxStage = (PureFxStage)stageToShow;
		fxStage.showCurrentPage();
		fxStage.showAndWait();
	}

	@Override
	public void createAndShowModalDialog(FxShellController controller, Dimension preferedDimension, String titleTag) throws Exception
	{
		PureFxDialogStage dialogStage = new PureFxDialogStage(this, controller); 
		dialogStage.showCurrentPage();
		dialogStage.showAndWait();
	}
	
	@Override
	public void createAndShowContactsDialog() throws Exception
	{
		createAndShowLargeModalDialog(new PureFxContactsStage(this));
	}
	
	@Override
	public void createAndShowSetupWizard() throws Exception
	{
		createAndShowLargeModalDialog(new PureFxCreateNewAccountWizardStage(this));
	}

	@Override
	public void createAndShowLegacySetupWizard() throws Exception
	{
		createAndShowLargeModalDialog(new PureFxSetupWizardStage(this));
	}
	
	public Dimension getMainWindowSize()
	{
		int width = (int)realStage.getWidth();
		int height = (int)realStage.getHeight();
		return new Dimension(width, height);
	}

	public Point getMainWindowLocation()
	{
		int x = (int)realStage.getX();
		int y = (int)realStage.getY();
		return new Point(x, y);
	}

	public boolean isMainWindowMaximized()
	{
		return realStage.isMaximized();
	}
	
	@Override
	public void modifyBulletin(Bulletin b) throws Exception
	{
		getCurrentUiState().setModifyingBulletin(true);
		UiBulletinModifyDlg dlg = null;
		try
		{
			dlg = new PureFxBulletinModifyDialog(b, this);
			hideMainWindow();
			setCurrentActiveFrame(dlg);
			dlg.restoreFrameState();
			dlg.setVisible(true);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			if(dlg != null)
				dlg.dispose();
			doneModifyingBulletin();
			throw(e);
		}
	}

	public ModelessBusyDlg createSplashScreen()
	{
		return new PureFxModelessBusyDlg(new Image(UiAboutDlg.class.getResourceAsStream("Martus-logo-black-text-160x72.png")));
	}

	public ModelessBusyDlg createBulletinLoadScreen()
	{
		return new PureFxModelessBusyDlg(getLocalization().getFieldLabel("waitingForBulletinsToLoad"));
	}

	public void showMessageDialog(String message)
	{
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Message");
		alert.setHeaderText(null);
		alert.setContentText(message);

		alert.showAndWait();
	}

	protected void initializationErrorExitMartusDlg(String message)
	{
		String title = "Error Starting Martus";
		String cause = "Unable to start Martus: \n" + message;

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(cause);

		alert.showAndWait();

		System.exit(1);
	}

	protected void showWarningMessageDlg(JFrame owner, String title, String okButtonLabel, String warningMessageLtoR, String warningMessageRtoL)
	{
		new PureFxWarningMessageDlg(title, okButtonLabel, warningMessageLtoR, warningMessageRtoL);
	}

	public boolean confirmDlg(JFrame parent, String baseTag)
	{
		return PureFxUtilities.confirmDlg(getLocalization(), parent, baseTag);
	}

	public boolean confirmDlg(JFrame parent, String baseTag, Map tokenReplacement)
	{
		return PureFxUtilities.confirmDlg(getLocalization(), parent, baseTag, tokenReplacement);
	}

	public boolean confirmDlg(JFrame parent, String title, String[] contents)
	{
		return PureFxUtilities.confirmDlg(getLocalization(), parent, title, contents);
	}

	public boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons)
	{
		return PureFxUtilities.confirmDlg(parent, title, contents, buttons);
	}

	public boolean confirmDlg(String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		return PureFxUtilities.confirmDlg(getCurrentActiveFrame().getSwingFrame(), title, contents, buttons, tokenReplacement);
	}

	protected boolean confirmDlg(JFrame parent, String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		return PureFxUtilities.confirmDlg(parent, title, contents, buttons, tokenReplacement);
	}

	protected void notifyDlg(JFrame parent, String baseTag, String titleTag, Map tokenReplacement)
	{
		PureFxUtilities.notifyDlg(getLocalization(), parent, baseTag, titleTag, tokenReplacement);
	}

	public void notifyDlg(String title, String[] contents, String[] buttons)
	{
		new PureFxNotifyDlg(title, contents, buttons);
	}

	public void messageDlg(JFrame parent, String baseTag, String message, Map tokenReplacement)
	{
		PureFxUtilities.messageDlg(getLocalization(), parent, baseTag, message, tokenReplacement);
	}

	protected void notifyDlg(Frame owner, String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		new PureFxNotifyDlg(title, contents, buttons, tokenReplacement);
	}

	protected void notifyDlg(String title, String[] contents, String[] buttons, Map tokenReplacement)
	{
		new PureFxNotifyDlg(title, contents, buttons, tokenReplacement);
	}

	public File showFileOpenDialog(String title, String okButtonLabel, File directory, FormatFilter filter)
	{
		FileChooser fileChooser = createFileChooser(title, directory, filter);

		return fileChooser.showOpenDialog(getActiveStage());
	}

	protected File showFileSaveDialog(String title, File directory, String defaultFilename, FormatFilter filter)
	{
		FileChooser fileChooser = createFileChooser(title, directory, filter);
		fileChooser.setInitialFileName(defaultFilename);

		return fileChooser.showSaveDialog(getActiveStage());
	}

	public File showChooseDirectoryDialog(String windowTitle)
	{
		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle(windowTitle);

		return chooser.showDialog(getActiveStage());
	}

	private FileChooser createFileChooser(String title, File directory, FormatFilter... filters)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setInitialDirectory(directory);

		if (filters != null)
			fileChooser.getExtensionFilters().addAll(createExtensionFilters(filters));

		return fileChooser;
	}

	private List<FileChooser.ExtensionFilter> createExtensionFilters(FormatFilter... filters)
	{
		List<FileChooser.ExtensionFilter> extensionFilters = new ArrayList<>();

		for (FormatFilter filter : filters)
			if (filter != null)
				extensionFilters.add(new FileChooser.ExtensionFilter(filter.getDescription(), getExtensionsWithWildcards(filter.getExtensions())));

		return extensionFilters;
	}

	private List<String> getExtensionsWithWildcards(String[] extensions)
	{
		List<String> extensionsWithWildcards = new ArrayList<>();

		for (String extension : extensions)
			extensionsWithWildcards.add("*" + extension);

		return extensionsWithWildcards;
	}

	protected File showFileSaveDialog(String title, File directory, Vector<FormatFilter> filters)
	{
		FileChooser fileChooser = createFileChooser(title, directory, filters.toArray(new FormatFilter[filters.size()]));

		File selectedFile = fileChooser.showSaveDialog(getActiveStage());

		if (selectedFile == null)
			return null;

		List<String> extensions = fileChooser.getSelectedExtensionFilter().getExtensions();
		String extension = extensions.get(0).replace("*", "");
		selectedFile = getFileWithExtension(selectedFile, extension);

		return selectedFile;
	}

	public File showFileOpenDialog(String title, File directory, Vector<FormatFilter> filters)
	{
		FileChooser fileChooser = createFileChooser(title, directory, filters.toArray(new FormatFilter[filters.size()]));

		return fileChooser.showOpenDialog(getActiveStage());
	}

	protected File[] showMultiFileOpenDialog(String title, File directory, Vector<FormatFilter> filters)
	{
		FileChooser fileChooser = createFileChooser(title, directory, filters.toArray(new FormatFilter[filters.size()]));
		List<File> files = fileChooser.showOpenMultipleDialog(getActiveStage());

		if (files == null)
			return new File[0];

		return files.toArray(new File[files.size()]);
	}

	public void runInUiThreadLater(Runnable toRun)
	{
		Platform.runLater(toRun);
	}

	public void runInUiThreadAndWait(final Runnable toRun) throws InterruptedException, InvocationTargetException
	{
		if (Platform.isFxApplicationThread())
		{
			toRun.run();
			return;
		}

		final CountDownLatch doneLatch = new CountDownLatch(1);
		Platform.runLater(() -> {
			try
			{
				toRun.run();
			}
			finally
			{
				doneLatch.countDown();
			}
		});

		doneLatch.await();
	}

	public String getStringInput(String baseTag, String descriptionTag, String rawDescriptionText, String defaultText)
	{
		PureFxStringInputDlg inputDlg = new PureFxStringInputDlg(this, baseTag, descriptionTag, rawDescriptionText, defaultText);

		return inputDlg.getResult();
	}

	public boolean showScrollableTextDlg(String titleTag, String okButtonTag, String cancelButtonTag, String descriptionTag, String text)
	{
		PureFxScrollableTextDlg dlg = new PureFxScrollableTextDlg(this, titleTag, okButtonTag, cancelButtonTag, descriptionTag, text);
		return dlg.getResult();
	}

	public void displayScrollableMessage(String titleTag, String message, String okButtonTag, Map tokenReplacement)
	{
		new PureFxScrollableTextDlg(this, titleTag, okButtonTag, MtfAwareLocalization.UNUSED_TAG, MtfAwareLocalization.UNUSED_TAG, message, tokenReplacement);
	}

	@Override
	public void setCurrentActiveStage(PureFxStage newActiveStage)
	{
		activeStage = newActiveStage;
	}

	@Override
	public PureFxStage getCurrentActiveStage()
	{
		return activeStage;
	}

	private Stage getActiveStage()
	{
		if (getCurrentActiveStage() == null)
			return realStage;
		else
			return getCurrentActiveStage().getActualStage();
	}

	protected ModalBusyDialogInterface createModalBusyDialog(String dialogTag)
	{
		return new PureFxModalBusyDialog(this, dialogTag);
	}

	public ProgressMeterDialogInterface createProgressMeter(String tagToUse)
	{
		return new PureFxProgressWithCancelDlg(this, tagToUse);
	}

	protected FancySearchDialogInterface createFancySearchDialog()
	{
		return new FancySearchDialogController(this);
	}

	protected void showFancySearchDialog(FancySearchDialogInterface fancySearchDialog)
	{
		try
		{
			createAndShowModalDialog((FancySearchDialogController) fancySearchDialog, null, null);
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}

	public Vector getBulletins(UniversalId[] uids) throws Exception
	{
		BulletinGetterThread thread = new BulletinGetterThread(getStore(), uids);
		runInUiThreadAndWait(() ->
		{
			try
			{
				doBackgroundWork(thread, "PreparingBulletins");
			}
			catch (Exception e)
			{
				MartusLogger.logException(e);
			}
		});
		return thread.getBulletins();
	}

	public PushButtonsDlgInterface createPushButtonsDlg(String title, String[] buttonLabels)
	{
		return new PureFxPushButtonsDlg(title, buttonLabels);
	}

	public ReportFieldDlgInterface createReportFieldChooserDlg(FieldSpec[] specs)
	{
		return new SwingInFxReportFieldChooserDlg(this, specs);
	}

	public ReportFieldDlgInterface createReportFieldChooserDlg(FieldSpec[] specs, ResultsHandler resultsHandler)
	{
		return new SwingInFxReportFieldChooserDlg(this, specs, resultsHandler);
	}

	public ReportFieldDlgInterface createReportFieldOrganizerDlg()
	{
		return new SwingInFxReportFieldOrganizerDlg(this);
	}

	public SortFieldsDlgInterface createSortFieldsDlg(MiniFieldSpec[] specsToAllow)
	{
		return new SwingInFxSortFieldsDlg(this, specsToAllow);
	}

	public PreviewDlgInterface createPrintPreviewDlg(ReportOutput output)
	{
		return new SwingInFxPrintPreviewDlg(this, output);
	}

	public ReportFieldDlgInterface createSingleSelectionFieldChooserDialog(FieldSpec[] specs, ResultsHandler resultsHandler)
	{
		return new SwingInFxSingleSelectionFieldChooserDlg(this, specs, resultsHandler);
	}

	private static Stage realStage;
	private FxMainStage mainStage;
	private PureFxStage activeStage;
}
