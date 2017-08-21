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
package org.martus.client.swingui.jfx.setupwizard.step2;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusApp.SaveConfigInfoException;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxInSwingWizardStage;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.step3.FxSetupBackupYourKeyController;
import org.martus.client.swingui.jfx.setupwizard.tasks.GetServerPublicKeyTask;
import org.martus.common.Exceptions.ServerNotAvailableException;
import org.martus.common.MartusLogger;
import org.martus.common.crypto.MartusSecurity;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

public class FxSetupStorageServerController extends FxSetupWizardAbstractServerSetupController
{
	public FxSetupStorageServerController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}
	
	@Override
	public void initializeMainContentPane()
	{
		destination = null;
		
		getWizardNavigationHandler().getBackButton().setVisible(false);
		getWizardNavigationHandler().getNextButton().getStyleClass().add("server-settings-next-connect-later-button");
		getWizardNavigationHandler().getNextButton().setText(getLocalization().getButtonLabel("ConnectLater"));

		getWizardNavigationHandler().getOptionalNextButton().getStyleClass().add("server-settings-next-connect-now-button");
		getWizardNavigationHandler().getOptionalNextButton().setText(getLocalization().getButtonLabel("ConnectNow"));
		getWizardNavigationHandler().getOptionalNextButton().setOnAction(new OptionalNextHandler());

		serverConnectionChoiceToggleGroup.selectToggle(defaultServerButton);
		hideAdvancedServerConnectionFields();


		ipAddressField.textProperty().addListener(new TextFieldChangeHandler());
		publicCodeField.textProperty().addListener(new TextFieldChangeHandler());
		ConfigInfo config = getApp().getConfigInfo();
		ipAddressField.setText(config.getServerName());
		String serverKey = config.getServerPublicKey();
		if(serverKey.length() > 0)
		{
			try
			{
				String publicCode = MartusSecurity.computeFormattedPublicCode40(serverKey);
				publicCodeField.setText(publicCode);
			}
			catch(Exception e)
			{
				MartusLogger.logException(e);
				// TODO: Should we display an error here, or just be silent?
			}
		}

		getWizardNavigationHandler().getOptionalNextButton().setDisable(false);
	}
	
	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step2/SetupStorageServer.fxml";
	}
	
	@Override
	public String getSidebarFxmlLocation()
	{
		return "setupwizard/step2/SetupStorageServerSidebar.fxml";
	}
	
		
	@FXML
	public void laterButton()
	{
		setupServerLater();
		getWizardNavigationHandler().doNext();
	}

	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		if(destination != null)
			return destination;

		try
		{
			return new FxSetupBackupYourKeyController(getMainWindow());
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
			
			return this;
		}
	}
	
	@FXML
	public void setupServerLater()
	{
		FxInSwingWizardStage wizardStage = getWizardStage();
		try
		{
			getApp().setServerInfo("", "", "");
			wizardStage.setCurrentServerIsAvailable(false);
		} 
		catch (SaveConfigInfoException e)
		{
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingConfig");
		}
		getWizardNavigationHandler().doNext();
	}
	
	@FXML
	public void useDefaultServer()
	{
		hideAdvancedServerConnectionFields();
		defaultServerButton.setSelected(true);
	}
	
	private String getDefaultServerIp()
	{
		return IP_FOR_SL1_IE;		
	}

	private String getDefaultServerPublicKey()
	{
		return PUBLIC_KEY_FOR_SL1_IE;		
	}

	@FXML
	public void advancedServerSettings()
	{
		showAdvancedServerConnectionFields();
		advancedServerSettingsButton.setSelected(true);
	}

	private void showAdvancedServerConnectionFields()
	{
		advancedServerConnectionFields.setVisible(true);
	}
	
	private void hideAdvancedServerConnectionFields()
	{
		advancedServerConnectionFields.setVisible(false);
	}
	
	private void connect()
	{
		RadioButton selectedToggle = (RadioButton) serverConnectionChoiceToggleGroup.getSelectedToggle();
		if (selectedToggle == null || selectedToggle.getId().equals(defaultServerButton.getId()))
			attempToConnectToDefaultSettings();
		else	
			attemptToConnectWithAdvancedSettings();
	}
	
	private void attempToConnectToDefaultSettings()
	{
		attemptToConnectNew(getDefaultServerIp(), getDefaultServerPublicKey(), false);
	}

	private void attemptToConnectWithAdvancedSettings()
	{
		attemptToConnectNew(ipAddressField.getText(), publicCodeField.getText(), true);
	}

	private void attemptToConnectNew(String ip, String userEnteredPublicCode, boolean askComplianceAcceptance)
	{
		try
		{
			GetServerPublicKeyTask task = new GetServerPublicKeyTask(getApp(), ip);
			showTimeoutDialog(getLocalization().getFieldLabel("GettingServerInformation"), task);
			
			String serverKey = task.getPublicKey();
			String magicWord = magicWordField.getText();
			attemptToConnect(ip, serverKey, askComplianceAcceptance, magicWord);

			FxInSwingWizardStage wizardStage = getWizardStage();
			if(wizardStage.checkIfCurrentServerIsAvailable())
				getWizardNavigationHandler().doNext();
		} 
		catch(UserCancelledException e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
		}
		catch (SaveConfigInfoException e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
			MartusLogger.logException(e);
			showNotifyDialog("ErrorSavingFile");
		}
		catch (ServerNotAvailableException e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
			MartusLogger.logException(e);
			showNotifyDialog("AdvanceServerNotResponding");
		}
		catch (Exception e)
		{
			getWizardStage().setCurrentServerIsAvailable(false);
			MartusLogger.logException(e);
			showNotifyDialog("UnexpectedError");
		}
		finally
		{
			updateButtonStates();
		}
	}
	
	protected class TextFieldChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2)
		{
			updateButtonStates();
		}
	}
	
	protected void updateButtonStates()
	{
	}
	
	protected class OptionalNextHandler implements EventHandler<ActionEvent>
	{
		@Override
		public void handle(ActionEvent event)
		{
			connect();
		}
	}
	
	private void showError(String errorTag)
	{
		showNotifyDialog(errorTag);
	}
	
	public static final String IP_FOR_SL1_IE = "54.72.26.74";
	public static final String PUBLIC_KEY_FOR_SL1_IE = 
			"MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAgzYTaocXQARAW5df4"
			+ "nvUYc6Sk2v9pQlMTB1v6/dc0nNamZAUaI5Z3ImPjnxCH/oATverq/Dsm8Gl"
			+ "MFOloHpXJwlPJyp3YUQ+wR9+MhhzG9qUsTNl6Iu8+f/GH6v6Sv1SXmUmS9E"
			+ "1jALpQqvCyBAbX+USyWo3P1uFmCYzlESPNoI8DUFCZ0XwTqQ3RmRrXYtVM9"
			+ "gIncknrcFwt14uf1UnVe0mIGyRUORGG3Pbl0hrMOopF2Ur/Z+bIFE535yF6"
			+ "Vpc+nFw+2nxBOpVgTvpt7LAtbxnxCzSO1KgAvUczBaQa4hXQ3dIlW//E9vK"
			+ "akQ85USbqXsxzr0scfkOxC7K+ZvYm0Porggn1W2b8dCGCUPNQAQRBFE7Czg"
			+ "b5EnmeumeJoLFon8El2idXRYcUBpY/FzHU4FM16guj85DWx7LEZ1LPFZXJv"
			+ "0u+DVd7KZfG4ovudn+ETKcskN4o6x/O6+KutVtTtIwmoIAam+lU/y8lZ+VC"
			+ "EqVxMiKkn2dp9nmvp780FOvAgMBAAE=";

	// NOTE: Keeping sl1-dev stuff here so we can add it back for testing if we want 
//	public static final String IP_FOR_SL1_DEV = "54.213.152.140";
//	public static final String PUBLIC_KEY_FOR_SL1_DEV = 
//			"MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAjIX0yCfct1/WQptimL"
//			+ "jK35F3wsW/SEQ8DGdxfMBTZX1GVoOD6zg0d71Ns1ij4FdnOUsD4QCN4Kiay"
//			+ "Q+l28eIU8LL8L5oJClFwsVqgNDvPn8jR/CAbPy9NL0gKHevvX/dciVVCSrg"
//			+ "Oyyc9p9MP05qyekXqVIfLoZNkcXL5tQKrEiqVdJaDEPepPIkQpBgFwF0QZl"
//			+ "J7NdgF4T5wSyEt+fxL7qnZOCqchF8aVbSzAaGLRQEJEtFYTa9mOUCdCLtcn"
//			+ "sdgnj+lLftaV5+8o8ZeUTbyH5H/NlLddboxlI8rNalY7E5f3DltOOmTyjMh"
//			+ "KSaxl9lfIxpfKoeLdYb5bA74BV1AjbwnxahlN4KRZm/7i0RkapKIXZ0Hqus"
//			+ "4JKUG5CJcIybS64ppt8ufCvAEERrZUzrrIDNwv+qob9PYFdiMq1xg+VNrxm"
//			+ "/0RXfjwgXxNjDS07MTQc2w/z1egtsDLSi4dALw69nefS0hbZwbv8dIrN23i"
//			+ "Hn0FNdbz81l1FrELGyh1hRAgMBAAE=";

	@FXML
	private RadioButton advancedServerSettingsButton;

	@FXML
	private RadioButton defaultServerButton;

	@FXML
	private Hyperlink advancedHyperlink;
	
	@FXML
	private GridPane advancedServerConnectionFields;
	
	@FXML
	private ToggleGroup serverConnectionChoiceToggleGroup;
	
	@FXML
	private TextField ipAddressField;
	
	@FXML
	private TextField publicCodeField;
	
	@FXML
	private TextField magicWordField;
	
	private AbstractFxSetupWizardContentController destination;
}
