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
package org.martus.client.swingui.jfx.setupwizard;

import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiMainWindow;
import org.martus.client.swingui.jfx.generic.FxContentController;
import org.martus.client.swingui.jfx.generic.PureFxWizardStage;
import org.martus.client.swingui.jfx.setupwizard.step1.FxSetupNewUserAccountController;
import org.martus.common.EnglishCommonStrings;

public class PureFxCreateNewAccountWizardStage extends PureFxWizardStage
{
	public PureFxCreateNewAccountWizardStage(UiMainWindow mainWindow) throws Exception
	{
		super(mainWindow, new FxSetupNewAccountWizardShellController(mainWindow));
	}

	@Override
	protected FxContentController getFirstController()
	{
		return new FxSetupNewUserAccountController(getMainWindow());
	}

	@Override
	protected boolean confirmExit()
	{
		if (!getApp().isSignedIn())
			return true;
		
		MartusLocalization localization = getMainWindow().getLocalization();
		String title = localization.getWindowTitle("ExitWizard");
		String message = localization.getFieldLabel("ExitWizard");
		String[] buttons = new String[] 
		{ 
			localization.getButtonLabel(EnglishCommonStrings.YES), 
			localization.getButtonLabel(EnglishCommonStrings.NO)
		};
		return getMainWindow().confirmDlg(title, new String[] {message}, buttons);
	}
}
