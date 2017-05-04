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
package org.martus.client.swingui.jfx.generic;

import java.net.URL;
import java.util.ResourceBundle;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.setupwizard.step6.FxSelectLanguageController;
import org.martus.common.fieldspec.ChoiceItem;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SingleSelectionModel;

public class InitialSigninController extends SigninController
{
	@Override
	public void initialize(URL location, ResourceBundle bundle)
	{
		super.initialize(location, bundle);

		ObservableList<ChoiceItem> availableLanguages = FxSelectLanguageController.getAvailableLanguages(getLocalization());
		languagesDropdown.setItems(availableLanguages);
		ChoiceItem currentLanguageChoiceItem = FxSelectLanguageController.findCurrentLanguageChoiceItem(getLocalization());
		languagesDropdown.getSelectionModel().select(currentLanguageChoiceItem);

		SingleSelectionModel<ChoiceItem> selectionModel = languagesDropdown.selectionModelProperty().getValue();
		ReadOnlyObjectProperty<ChoiceItem> selectedLanguageProperty = selectionModel.selectedItemProperty();
		selectedLanguageProperty.addListener((property, oldValue, newValue) -> languageChangedTo(newValue));

		updateCreateNewAccountBehavior();
	}

	public void updateCreateNewAccountBehavior()
	{
		boolean doesAnyAccountExist = getApp().doesAnyAccountExist();
		setSignInRowsVisible(doesAnyAccountExist);
	}

	public String getSelectedLanguageCode()
	{
		return languagesDropdown.getSelectionModel().getSelectedItem().getCode();
	}

	private void languageChangedTo(ChoiceItem newValue)
	{
		closeDialog(SigninResult.CHANGE_LANGUAGE);
	}

	public InitialSigninController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public String getFxmlLocation()
	{
		return "generic/InitialSignin.fxml";
	}
	
	@FXML
	private Button newAccountButton;

	@FXML
	private ChoiceBox<ChoiceItem> languagesDropdown;
}
