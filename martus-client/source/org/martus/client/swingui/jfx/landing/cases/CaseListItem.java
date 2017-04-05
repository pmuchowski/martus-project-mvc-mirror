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
package org.martus.client.swingui.jfx.landing.cases;

import org.martus.client.bulletinstore.BulletinFolder;
import org.martus.client.swingui.MartusLocalization;
import org.martus.common.MartusLogger;
import org.martus.util.TokenReplacement;
import org.martus.util.TokenReplacement.TokenInvalidException;

public class CaseListItem
{
	CaseListItem(BulletinFolder folder, MartusLocalization localizationToUse)
	{
		caseFolder = folder;
		localization = localizationToUse;
	}
	
	public String getNameLocalized()
	{
		String localizedFolderName = caseFolder.getLocalizedName(localization);

		return getLocalizedNameWithCount(localizedFolderName, caseFolder.getBulletinCount());
	}
	
	protected String getLocalizedNameWithCount(String localizedName, int recordCount)
	{
		try
		{
			localizedName = localizedName + " (" + RECORD_COUNT_TOKEN + ")";
			return TokenReplacement.replaceToken(localizedName, RECORD_COUNT_TOKEN, Integer.toString(recordCount));
		} 
		catch (TokenInvalidException e)
		{
			MartusLogger.logException(e);
			
			return localizedName + "(Error)";
		}
	}
	
	public String getName()
	{
		return caseFolder.getName();
	}
	
	public BulletinFolder getFolder()
	{
		return caseFolder;
	}
	
	@Override
	public String toString()
	{
		return getNameLocalized();
	}

	final BulletinFolder caseFolder;
	final MartusLocalization localization;
	private static final String RECORD_COUNT_TOKEN = "#recordCount#";
}