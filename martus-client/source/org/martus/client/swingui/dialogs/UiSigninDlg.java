/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
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

package org.martus.client.swingui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFrame;

import org.martus.client.core.FontSetter;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.fields.UiChoiceEditor;
import org.martus.client.swingui.jfx.generic.SigninInterface;
import org.martus.clientside.MtfAwareLocalization;
import org.martus.clientside.UiBasicSigninDlg;
import org.martus.swing.UiLabel;

public class UiSigninDlg extends UiBasicSigninDlg implements SigninInterface
{
	public UiSigninDlg(UiMainWindow mainWindowToUse, int mode, String username, char[] password)
	{
		super(mainWindowToUse.getLocalization(), mainWindowToUse.getCurrentUiState(), mode, username, password);
		mainWindow = mainWindowToUse;
	}
	
	public UiSigninDlg(UiMainWindow mainWindowToUse, JFrame owner, int mode, String username, char[] password)
	{
		super(mainWindowToUse.getLocalization(), mainWindowToUse.getCurrentUiState(), owner, mode, username, password);
		mainWindow = mainWindowToUse;
	}
	
	protected JComponent getLanguageComponent()
	{
		if(currentMode == TIMED_OUT || currentMode == SECURITY_VALIDATE)
			return new UiLabel();
		
		languageDropdown = new UiChoiceEditor(localization);
		languageDropdown.setChoices(localization.getUiLanguages());
		languageDropdown.setText(localization.getCurrentLanguageCode());
		languageDropdown.addActionListener(new LanguageChangedHandler());
		return languageDropdown.getComponent();
	}

	@Override
	public String getUserName()
	{
		return getNameText();
	}

	@Override
	public char[] getUserPassword()
	{
		return getPassword();
	}

	@Override
	public boolean isSignin()
	{
		return getUserChoice() == SIGN_IN;
	}

	@Override
	public boolean isLanguageChanged()
	{
		return getUserChoice() == LANGUAGE_CHANGED;
	}

	class LanguageChangedHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			String languageCode = languageDropdown.getText();
			FontSetter.setDefaultFont(languageCode.equals(MtfAwareLocalization.BURMESE));
			mainWindow.displayPossibleUnofficialIncompatibleTranslationWarnings(owner, localization, languageCode);
			changeLanguagesAndRestartSignin(languageCode);
			dispose();
		}
	}
	
	void changeLanguagesAndRestartSignin(String languageCode)
	{
		localization.setCurrentLanguageCode(languageCode);
		uiState.setCurrentLanguage(languageCode);
		uiState.save();
		usersChoice = LANGUAGE_CHANGED;
	}

	private UiChoiceEditor languageDropdown;
	private UiMainWindow mainWindow;
}

