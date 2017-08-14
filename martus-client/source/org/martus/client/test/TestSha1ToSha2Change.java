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
package org.martus.client.test;

import java.io.File;

import org.martus.client.core.templates.FormTemplateManager;
import org.martus.common.FieldSpecCollection;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.Packet;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;

public class TestSha1ToSha2Change extends TestCaseEnhanced
{
	public TestSha1ToSha2Change(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();

		securitySha1 = MockMartusSecuritySha1.createClient();
		securitySha2 = MockMartusSecuritySha2.createClient();
	}

	public void testLoadTemplateSignedWithSha1UsingSha2Verifier() throws Exception
	{
		File tempDirectory = createTempDirectory();
		File templateDirectory = new File(tempDirectory, "templates");
		try
		{
			String templateTitle = "t1";
			FormTemplate template = createAndSaveTemplate(templateDirectory, templateTitle, securitySha1);

			FormTemplateManager manager = FormTemplateManager.createOrOpen(securitySha2, templateDirectory);
			assertEquals(2, manager.getAvailableTemplateNames().size());
			FormTemplate got = manager.getTemplate(templateTitle);
			assertEquals(template.getDescription(), got.getDescription());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	public void testLoadTemplateSignedWithSha2UsingSha1Verifier() throws Exception
	{
		File tempDirectory = createTempDirectory();
		File templateDirectory = new File(tempDirectory, "templates");
		try
		{
			String templateTitle = "t1";
			createAndSaveTemplate(templateDirectory, templateTitle, securitySha2);

			FormTemplateManager manager = FormTemplateManager.createOrOpen(securitySha1, templateDirectory);
			manager.getTemplate(templateTitle);
			fail("Should have thrown SignatureVerificationException");
		}
		catch (Packet.SignatureVerificationException ignoreExpectedException)
		{
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	private FormTemplate createAndSaveTemplate(File templateDirectory, String templateTitle, MartusCrypto security) throws Exception
	{
		FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);

		assertEquals(1, manager.getAvailableTemplateNames().size());

		FormTemplate template = createFormTemplate(templateTitle, "d1");
		manager.putTemplate(template);
		assertEquals(2, manager.getAvailableTemplateNames().size());

		return template;
	}

	private FormTemplate createFormTemplate(String title, String description) throws Exception
	{
		FieldSpecCollection top = StandardFieldSpecs.getDefaultTopSectionFieldSpecs();
		FieldSpecCollection bottom = StandardFieldSpecs.getDefaultBottomSectionFieldSpecs();
		FormTemplate template = new FormTemplate(title, description, top, bottom);
		return template;
	}


	private static MartusCrypto securitySha1;
	private static MartusCrypto securitySha2;
}
