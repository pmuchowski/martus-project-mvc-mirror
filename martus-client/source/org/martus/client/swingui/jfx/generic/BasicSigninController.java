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
package org.martus.client.swingui.jfx.generic;

import java.net.URL;
import java.util.ResourceBundle;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.landing.bulletins.FxViewFieldCreator;
import org.martus.client.swingui.jfx.setupwizard.step6.FxSelectLanguageController;
import org.martus.clientside.UiBasicSigninDlg;
import org.martus.common.fieldspec.ChoiceItem;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SingleSelectionModel;

public class BasicSigninController extends SigninController implements SigninInterface
{
	public BasicSigninController(UiMainWindow mainWindowToUse, int modeToUse, String username, char[] password)
	{
		super(mainWindowToUse);
		mode = modeToUse;
		initialUsername = username;
		initialPassword = password;
	}

	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);

		if(mode == UiBasicSigninDlg.SECURITY_VALIDATE)
		{
			addMessagePaneText("securityServerConfigValidate");
			hideLanguagesDropdown();
		}
		else if(mode == UiBasicSigninDlg.RETYPE_USERNAME_PASSWORD)
		{
			addMessagePaneText("RetypeUserNameAndPassword");
		}
		else if(mode == UiBasicSigninDlg.CREATE_NEW)
		{
			addMessagePaneText("CreateNewUserNamePassword");
			addMessagePaneText("HelpOnCreatingNewPassword");
		}

		if (mode != UiBasicSigninDlg.SECURITY_VALIDATE)
		{
			ObservableList<ChoiceItem> availableLanguages = FxSelectLanguageController.getAvailableLanguages(getLocalization());
			languagesDropdown.setItems(availableLanguages);
			ChoiceItem currentLanguageChoiceItem = FxSelectLanguageController.findCurrentLanguageChoiceItem(getLocalization());
			languagesDropdown.getSelectionModel().select(currentLanguageChoiceItem);

			SingleSelectionModel<ChoiceItem> selectionModel = languagesDropdown.selectionModelProperty().getValue();
			ReadOnlyObjectProperty<ChoiceItem> selectedLanguageProperty = selectionModel.selectedItemProperty();
			selectedLanguageProperty.addListener((property, oldValue, newValue) -> languageChangedTo(newValue));
		}

		if (initialPassword != null && initialPassword.length > 0)
			getPasswordField().setText(String.valueOf(initialPassword));

		if (initialUsername != null && !initialUsername.isEmpty())
		{
			getUserNameField().setText(initialUsername);
			Platform.runLater(() -> getPasswordField().requestFocus());
		}
	}

	private void addMessagePaneText(String text)
	{
		FxViewFieldCreator textField = new FxViewFieldCreator(getLocalization());
		String message = getLocalization().getFieldLabel(text);
		Node textFieldNode = textField.createResponsiveMessage(message, getPreferredDimension().getWidth() - PADDING);
		textFieldNode.getStyleClass().add(MESSAGE_STYLE);
		signInMessagePane.getChildren().add(textFieldNode);
	}

	private void languageChangedTo(ChoiceItem newValue)
	{
		getMainWindow().updateUIStateForLanguageChosen(newValue.getCode());
		closeDialog(SigninResult.CHANGE_LANGUAGE);
	}

	private void hideLanguagesDropdown()
	{
		languagesDropdown.setVisible(false);
		languagesDropdownLabel.setVisible(false);
	}

	@FXML
	private void onOk()
	{
		closeDialog(SigninResult.SIGNIN);
	}

	@Override
	public String getFxmlLocation()
	{
		return "generic/BasicSignin.fxml";
	}

	@Override
	public boolean isSignin()
	{
		return SigninResult.SIGNIN.equals(getResult());
	}

	@Override
	public boolean isLanguageChanged()
	{
		return SigninResult.CHANGE_LANGUAGE.equals(getResult());
	}

	@FXML
	private ChoiceBox<ChoiceItem> languagesDropdown;

	@FXML
	private Label languagesDropdownLabel;

	private static final double PADDING = 100.0;
	private static final String MESSAGE_STYLE = "singIn-text";

	private int mode;
	private String initialUsername;
	private char[] initialPassword;
}
