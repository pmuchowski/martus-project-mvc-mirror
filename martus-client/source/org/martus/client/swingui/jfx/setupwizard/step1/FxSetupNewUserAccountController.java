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
package org.martus.client.swingui.jfx.setupwizard.step1;

import java.util.Arrays;

import org.martus.client.core.ConfigInfo;
import org.martus.client.core.MartusUserNameAndPassword;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiFontEncodingHelper;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.WizardNavigationButtonsInterface;
import org.martus.client.swingui.jfx.setupwizard.AbstractFxSetupWizardContentController;
import org.martus.client.swingui.jfx.setupwizard.StaticAccountCreationData;
import org.martus.client.swingui.jfx.setupwizard.step2.FxSetupStorageServerController;
import org.martus.client.swingui.jfx.setupwizard.tasks.CreateAccountTask;
import org.martus.common.MartusLogger;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class FxSetupNewUserAccountController extends FxStep1Controller
{
	public FxSetupNewUserAccountController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
		
		info = getApp().getConfigInfo();
		fontHelper = new UiFontEncodingHelper(getConfigInfo().getDoZawgyiConversion());
	}
	
	@Override
	public void initializeMainContentPane()
	{
		setupContactInforFields();
		setupLoginFields();
		setupConfirmationFields();
		getWizardNavigationHandler().getOptionalNextButton().setVisible(false);
		getWizardNavigationHandler().getBackButton().setText(getLocalization().getButtonLabel("Cancel"));
	}

	@Override
	public AbstractFxSetupWizardContentController getNextController()
	{
		return new FxSetupStorageServerController(getMainWindow());
	}
	
	@Override
	public void nextWasPressed() throws Exception
	{
		createAccount();
		getWizardNavigationHandler().getBackButton().setVisible(false);
		getConfigInfo().setAuthor(getFontHelper().getStorable(authorField.getText()));
		getConfigInfo().setOrganization(getFontHelper().getStorable(organizationField.getText()));
		getApp().saveConfigInfo();
		
		super.nextWasPressed();
	}
	
	private void createAccount() throws Exception
	{
		String userNameValue = userNameField.getText();
		char[] passwordValue = passwordField.getText().toCharArray();
		
		StaticAccountCreationData.dispose();
		
		Task task = new CreateAccountTask(getApp(), userNameValue, passwordValue);
		MartusLocalization localization = getLocalization();
		String message = localization.getFieldLabel("CreatingAccount");
		showBusyDialog(message, task);
		getMainWindow().setCreatedNewAccount(true);
		
		String languageCodeUserStartedWith = getMainWindow().getLocalization().getCurrentLanguageCode();
		getMainWindow().initalizeUiState(languageCodeUserStartedWith);
		getApp().doAfterSigninInitalization();
	}
	
	private void setupContactInforFields()
	{
		authorField.setText(getConfigInfo().getAuthor());
		organizationField.setText(getConfigInfo().getOrganization());
	}

	private void setupLoginFields()
	{
		WizardNavigationButtonsInterface wizardNavigationHandler = getWizardNavigationHandler();
		wizardNavigationHandler.getNextButton().setDisable(true);
		
		userNameField.textProperty().addListener(new LoginChangeHandler());
		passwordField.textProperty().addListener(new LoginChangeHandler());
		
		errorLabel.setTooltip(new Tooltip(getLocalization().getFieldLabel("PasswordTipGeneral")));
	}
	
	private void setupConfirmationFields()
	{
		getWizardNavigationHandler().getNextButton().setDisable(true);
		confirmPasswordField.setDisable(false);
		
		confirmPasswordField.textProperty().addListener(new ConfirmLoginChangeHandler());
	}
	
	protected void confirmationLoginDataChanged()
	{
		try
		{
			getErrorLabel().setText("");
			String passwordValue = confirmPasswordField.getText();
			boolean passwordMatches = passwordValue.equals(getPasswordField().getText());
			boolean canContinue = passwordMatches;
			getWizardNavigationHandler().getNextButton().setDisable(!canContinue);

			String statusMessage = "";
			MartusLocalization localization = getLocalization();
			String styleTag = "errorText";
			if (!passwordMatches)
			{
				statusMessage = localization.getFieldLabel("notifypasswordsdontmatchcause");
			}
			else
			{
				styleTag = "hintText";
				statusMessage = localization.getFieldLabel("PasswordsMatch");
			}
			
			ObservableList<String> styleClassForInformationMessage = getErrorLabel().getStyleClass();
			styleClassForInformationMessage.clear();
			styleClassForInformationMessage.add(styleTag);
			getErrorLabel().setText(statusMessage);

		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
		}
	}
	
	protected void loginDataChanged()
	{
		clearConfirmationFields();
		
		boolean canContinue = false;
		String errorMessage = "";
		
		boolean hasUserName;
		boolean isPasswordLongEnough;
		boolean doesAccountExist;
		boolean usernameSameAsPassword;
		try
		{
			String candidateUserName = getUserName().getText();
			hasUserName = candidateUserName.length() > 0;
			
			char[] candidatePassword = getPasswordField().getText().toCharArray();
			isPasswordLongEnough = (candidatePassword.length >= MartusUserNameAndPassword.BASIC_PASSWORD_LENGTH);

			doesAccountExist = getApp().doesAccountExist(candidateUserName, candidatePassword);
			usernameSameAsPassword = areSame(candidateUserName, candidatePassword);
			
			MartusLocalization localization = getLocalization();
			if (!hasUserName)
				errorMessage = localization.getFieldLabel("notifyUserNameBlankcause");
			else if(!isPasswordLongEnough)
				errorMessage = localization.getFieldLabel("notifyPasswordInvalidcause");
			else if(usernameSameAsPassword)
				errorMessage = localization.getFieldLabel("notifyPasswordMatchesUserNamecause");
			else if(doesAccountExist)
				errorMessage = localization.getFieldLabel("notifyUserAlreadyExistscause");

			canContinue = hasUserName && isPasswordLongEnough && !doesAccountExist && !usernameSameAsPassword;
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			errorMessage = "Unexpected error";
		}
		
		getErrorLabel().setText(errorMessage);		
		enableConfirmationFields(canContinue);
	}

	private void enableConfirmationFields(boolean canContinue)
	{
		getConfirmPasswordField().setDisable(!canContinue);
	}

	private void clearConfirmationFields()
	{
		getConfirmPasswordField().setText("");
		getErrorLabel().setText("");
	}
	
	private Label getErrorLabel()
	{
		return errorLabel;
	}

	private boolean areSame(String candidateUserName, char[] candidatePassword)
	{
		char[] username = candidateUserName.toCharArray();
		return Arrays.equals(username, candidatePassword);
	}
	
	private PasswordField getPasswordField()
	{
		return passwordField;
	}

	private TextField getUserName()
	{
		return userNameField;
	}
	
	private PasswordField getConfirmPasswordField()
	{
		return confirmPasswordField;
	}
	
	private UiFontEncodingHelper getFontHelper()
	{
		return fontHelper;
	}

	private ConfigInfo getConfigInfo()
	{
		return info;
	}

	@Override
	public String getFxmlLocation()
	{
		return "setupwizard/step1/SetupUserAccount.fxml";
	}
	
	@Override
	public String getSidebarFxmlLocation()
	{
		return "setupwizard/step1/SetupUsernamePasswordSidebar.fxml";
	}
	
	protected class ConfirmLoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			confirmationLoginDataChanged();
		}
	}
	
	protected class LoginChangeHandler implements ChangeListener<String>
	{
		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
		{
			loginDataChanged();
		}
	}
		
	@FXML
	private TextField userNameField;
	
	@FXML
	private PasswordField passwordField;
	
	@FXML
	private PasswordField confirmPasswordField;
	
	@FXML
	private Label errorLabel;
	
	@FXML
	private TextField authorField;
	
	@FXML
	private TextField organizationField;
	
	private ConfigInfo info;
	private UiFontEncodingHelper fontHelper;
}
