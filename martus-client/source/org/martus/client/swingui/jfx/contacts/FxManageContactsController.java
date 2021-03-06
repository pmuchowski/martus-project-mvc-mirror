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
package org.martus.client.swingui.jfx.contacts;

import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.actions.ActionMenuExportMyPublicKey;
import org.martus.common.MartusAccountAccessToken;
import org.martus.common.MartusLogger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;

public class FxManageContactsController extends FxWizardAddContactsController
{

	public FxManageContactsController(UiMainWindow mainWindowToUse)
	{
		super(mainWindowToUse);
	}

	@Override
	public void initializeMainContentPane()
	{
		super.initializeMainContentPane();
		
		//TODO remove this and figure out a better solution in FXML
		contactsVbox.setMaxWidth(MAX_WIDTH_CONTACTS_TABLE);

		sendToByDefaultColumn.setVisible(true);
		showOldPublicCodeDuringVerification();

		accountAccessTokenLabel.setText(getAccountAccessToken());
	}

	@FXML
	public void importContactFromFile(ActionEvent event)
	{
		doAction(new ImportContactAction(this));
	}

	@FXML
	public void updateButtonStatus(ActionEvent event)
	{
		if (addContactAccessTokenButton.isSelected())
		{
			accessTokenField.setDisable(false);
			importContactButton.setDisable(true);
			updateAddContactButtonState();
			addContactLabel.setText(getLocalization().getFieldLabel("AddContactStep1AccessToken"));
		}
		else
		{
			accessTokenField.setDisable(true);
			addContactButton.setDisable(true);
			importContactButton.setDisable(false);
			addContactLabel.setText(getLocalization().getFieldLabel("AddContactStep1PublicKey"));
		}
	}

	@FXML
	private void onExportPublicKey(ActionEvent event)
	{
		doAction(new ActionMenuExportMyPublicKey(getMainWindow()));
	}

	private String getAccountAccessToken()
	{
		try {
			MartusAccountAccessToken accountToken = getApp().getConfigInfo().getCurrentMartusAccountAccessToken();

			return accountToken.getToken();
		}
		catch (MartusAccountAccessToken.TokenInvalidException e)
		{
			MartusLogger.logException(e);
		}

		return getLocalization().getFieldLabel("TokenNotAvailable");
	}

	@Override
	public String getFxmlLocation()
	{
		return "contacts/ManageContacts.fxml";
	}

	@FXML
	protected RadioButton addContactAccessTokenButton;

	@FXML
	protected Button importContactButton;

	@FXML
	protected Label addContactLabel;

	@FXML
	protected Label accountAccessTokenLabel;

	private static final int MAX_WIDTH_CONTACTS_TABLE = 960;
}
