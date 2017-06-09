/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2014, Beneficent
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
package org.martus.client.swingui.jfx.landing;

import java.net.URL;
import java.util.ResourceBundle;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionDoer;
import org.martus.client.swingui.actions.ActionMenuCharts;
import org.martus.client.swingui.actions.ActionMenuCreateNewBulletin;
import org.martus.client.swingui.actions.ActionMenuManageContactsWithoutResignIn;
import org.martus.client.swingui.actions.ActionMenuQuickEraseDeleteMyData;
import org.martus.client.swingui.actions.ActionMenuQuickSearch;
import org.martus.client.swingui.actions.ActionMenuReports;
import org.martus.client.swingui.actions.ActionMenuSearch;
import org.martus.client.swingui.jfx.generic.DialogWithNoButtonsShellController;
import org.martus.client.swingui.jfx.generic.FxController;
import org.martus.client.swingui.jfx.generic.FxNonWizardShellController;
import org.martus.client.swingui.jfx.generic.FxTabbedShellController;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinListProvider;
import org.martus.client.swingui.jfx.landing.bulletins.BulletinsListController;
import org.martus.client.swingui.jfx.landing.cases.CaseListProvider;
import org.martus.client.swingui.jfx.landing.cases.FxCaseManagementController;
import org.martus.client.swingui.jfx.landing.general.AccountController;
import org.martus.client.swingui.jfx.landing.general.HelpController;
import org.martus.client.swingui.jfx.landing.general.ManageServerSyncRecordsController;
import org.martus.client.swingui.jfx.landing.general.ManageTemplatesController;
import org.martus.client.swingui.jfx.landing.general.SettingsController;

import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class FxLandingShellController extends FxNonWizardShellController
{
	private static final String TOGGLE_ON_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/toggle_on.png";
	private static final String TOGGLE_OFF_IMAGE_PATH = "/org/martus/client/swingui/jfx/images/toggle_off.png";
	private static final Image TOGGLE_ON_IMAGE = new Image(TOGGLE_ON_IMAGE_PATH);
	private static final Image TOGGLE_OFF_IMAGE = new Image(TOGGLE_OFF_IMAGE_PATH);

	public FxLandingShellController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		bulletinListProvider = new BulletinListProvider(getMainWindow());
		caseManagementController = new FxCaseManagementController(getMainWindow());
		bulletinsListController = new BulletinsListController(getMainWindow(), bulletinListProvider, caseManagementController);
	}
	
	public BulletinsListController getBulletinsListController()
	{
		return bulletinsListController;
	}
	
	public CaseListProvider getAllCaseListProvider()
	{
		return caseManagementController.getAllCaseListProvider();
	}
	
	public BooleanBinding getShowTrashBinding()
	{
		return bulletinsListController.getTrashNotBeingDisplayedBinding();
	}
	
	public FxCaseManagementController getCaseManager()
	{
		return caseManagementController;
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "landing/LandingShell.fxml";
	}
	
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);
		updateOnlineStatus();
		caseManagementController.addFolderSelectionListener(bulletinListProvider);
		caseManagementController.addFolderSelectionListener(bulletinsListController);
	}

	@Override
	public Parent createContents() throws Exception
	{
		Parent contents = super.createContents();
		loadControllerAndEmbedInPane(caseManagementController, sideContentPane);
		loadControllerAndEmbedInPane(bulletinsListController, mainContentPane);
		return contents;
	}

	private void onSettings(String tabToDisplayFirst)
	{
		FxTabbedShellController settingsController = new SettingsController(getMainWindow());
		setTab(tabToDisplayFirst, settingsController);
	}

	private void onAccount(String tabToDisplayFirst)
	{
		FxTabbedShellController settingsController = new AccountController(getMainWindow());
		setTab(tabToDisplayFirst, settingsController);
	}
	
	private void setTab(String tabToDisplayFirst, FxTabbedShellController settingsController)
	{
		settingsController.setFirstTabToDisplay(tabToDisplayFirst);
		DialogWithNoButtonsShellController shellController = new DialogWithNoButtonsShellController(getMainWindow(), settingsController);
		doAction(shellController);
	}

	@FXML
	private void onConfigureServer(ActionEvent event)
	{
		//TODO remove this Old Swing doAction(new ActionMenuSelectServer(getMainWindow()));
		onSettings(SettingsController.SERVER_TAB);
	}
	
	@FXML
	private void onSystemPreferences(ActionEvent event)
	{
		//TODO remove this Old Swing doAction(new ActionMenuPreferences(getMainWindow()));
		onSettings(SettingsController.SYSTEM_TAB);
	}

	@FXML
	private void onQuickSearch(ActionEvent event)
	{
		String displayableSearchText = searchText.getText();
		doAction(new  ActionMenuQuickSearch(getMainWindow(), displayableSearchText));
	}
	
	@FXML 
	private void onAdvanceSearch(ActionEvent event)
	{
		doAction(new ActionMenuSearch(getMainWindow()));
	}

	@FXML
	private void onCreateNewBulletin(ActionEvent event)
	{
		doAction(new ActionMenuCreateNewBulletin(getMainWindow()));
		caseManagementController.showAllCases();
	}
	
	@FXML 
	private void onReports(ActionEvent event)
	{
		doAction(new ActionMenuReports(getMainWindow()));
	}
		
	@FXML 
	private void onCharts(ActionEvent event)
	{
		doAction(new ActionMenuCharts(getMainWindow()));
	}
	
	@FXML
	private void onAccountInformation(ActionEvent event)
	{
		onAccount(AccountController.ACCOUNT_INFORMATION_TAB_CODE);
	}

	@FXML
	private void onContactInformation(ActionEvent event)
	{
		onAccount(AccountController.CONTACT_INFORMATION_TAB_CODE);
	}
	
	@FXML
	public void onManageContacts(ActionEvent event)
	{
		doAction(new ActionMenuManageContactsWithoutResignIn(getMainWindow()));
	}
	
	@FXML
	private void onBackupKeypair(ActionEvent event)
	{
		onAccount(AccountController.KEY_BACKUP_TAB_CODE);
	}

	@FXML
	public void onLogoClicked(MouseEvent mouseEvent) 
	{
		try
		{
			BulletinsListController bulletinListController = getBulletinsListController();
			bulletinListController.loadAllBulletinsAndSortByMostRecent();
		} 
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	public void onDeleteMyData(ActionEvent event)
	{
		if(showOkCancelConfirmationDialog("confirmDeleteMyData", "QuickEraseWillNotRemoveItems"))
		{
			doAction(new ActionMenuQuickEraseDeleteMyData(getMainWindow()));
		}
	}
	
	@FXML
	public void onHelpMenu(ActionEvent event)
	{
		showDialogWithClose("Help", new HelpController(getMainWindow()));	
	}
	
	@FXML
	public void onCloseCurrentView(ActionEvent event)
	{
		caseManagementController.showAllCases();
	}
	
	@FXML
	public void onManageTemplates(ActionEvent event)
	{
		try
		{
			FxController controller = new ManageTemplatesController(getMainWindow());
			ActionDoer shellController = new DialogWithNoButtonsShellController(getMainWindow(), controller);
			doAction(shellController);
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}
	
	@FXML
	public void onServerSync(ActionEvent event)
	{
		try
		{
			if(getMainWindow().isRetrieveInProgress())
			{
				showNotifyDialog("RetrieveInProgress");
				return;
			}
			if(!getApp().isSSLServerAvailable())
			{
				showNotifyDialog("retrievenoserver");
				return;
			}
			ManageServerSyncRecordsController controller = new ManageServerSyncRecordsController(getMainWindow());
			ActionDoer shellController = new DialogWithNoButtonsShellController(getMainWindow(), controller);
			doAction(shellController);
		}
		catch (UserCancelledException exitCleanly)
		{
		}
		catch (Exception e)
		{
			logAndNotifyUnexpectedError(e);
		}
	}

	@FXML
	private void onOnline(ActionEvent event)
	{
		boolean oldState = getApp().getTransport().isOnline();
		boolean newState = !oldState;
		getApp().turnNetworkOnOrOff(newState);
		updateOnlineStatus();
	}

	private void updateOnlineStatus()
	{
		boolean isOnline = getApp().getTransport().isOnline();
		toolbarImageViewOnline.setImage(getUpdatedOnOffStatusImage(isOnline));
		toolbarButtonOnline.setTooltip(getUpdatedToolTip(isOnline, "ServerCurrentlyOn", "ServerCurrentlyOff"));
		getMainWindow().updateServerStatusInStatusBar();
	}

	private Tooltip getUpdatedToolTip(boolean enabled, String onMessage, String offMessage)
	{
		Tooltip tooltip = new Tooltip();
		String tooltipMessage = getLocalization().getTooltipLabel(offMessage);
		if(enabled)
			tooltipMessage = getLocalization().getTooltipLabel(onMessage);
		tooltip.setText(tooltipMessage);
		return tooltip;
	}

	private Image getUpdatedOnOffStatusImage(boolean isOn)
	{
		if (isOn)
			return TOGGLE_ON_IMAGE;

		return TOGGLE_OFF_IMAGE;
	}

	@FXML
	private TextField searchText;
	
	@FXML
	private Button toolbarButtonOnline;
	
	@FXML
	private ImageView toolbarImageViewOnline;
	
	@FXML
	private Button toolbarButtonTor;
	
	@FXML
	private ImageView toolbarImageViewTor;

	@FXML
	private Pane sideContentPane;
	
	@FXML
	private ScrollPane mainContentPane;
	
	private BulletinsListController bulletinsListController;
	private BulletinListProvider bulletinListProvider;
	private FxCaseManagementController caseManagementController;
}

