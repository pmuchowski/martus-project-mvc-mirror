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
package org.martus.client.bulletinstore;

import java.util.Set;

import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.FieldTypeMultiline;
import org.martus.common.fieldspec.FormTemplate;

import javafx.collections.ObservableSet;

public class BulletinsToTemplateMatcher
{
	public static String findMatchingFormTemplateTitle(ClientBulletinStore store, Bulletin bulletin) throws Exception
	{
		Set bulletinTopSectionFieldSpecs = bulletin.getTopSectionFieldSpecs().asSet();
		ObservableSet<String> existingTemplateTitles = store.getAvailableTemplates();

		for (String templateTitle : existingTemplateTitles)
		{
			String matchingTemplateTitle = getMatchingTemplateTitle(store, bulletinTopSectionFieldSpecs, templateTitle);

			if (matchingTemplateTitle != null)
				return matchingTemplateTitle;
		}

		return null;
	}

	private static String getMatchingTemplateTitle(ClientBulletinStore store, Set bulletinTopSectionFieldSpecs, String templateTitle) throws Exception
	{
		FormTemplate formTemplate = store.getFormTemplate(templateTitle);
		Set formTemplateFieldSpecs = formTemplate.getTopFields().asSet();
		boolean topFieldSpecsAreEqual = areFieldSpecsEqual(bulletinTopSectionFieldSpecs, formTemplateFieldSpecs);
		if (topFieldSpecsAreEqual)
			return templateTitle;

		return null;
	}

	private static boolean areFieldSpecsEqual(Set bulletinTopSectionFieldSpecs, Set formTemplateFieldSpecs)
	{
		formTemplateFieldSpecs.remove(FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()));
		bulletinTopSectionFieldSpecs.remove(FieldSpec.createStandardField(BulletinConstants.TAGPRIVATEINFO, new FieldTypeMultiline()));

		return bulletinTopSectionFieldSpecs.equals(formTemplateFieldSpecs);
	}
}
