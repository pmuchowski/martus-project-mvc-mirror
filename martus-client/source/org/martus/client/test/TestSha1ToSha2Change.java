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
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
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

	public void testLoadTemplateManagerStateSignedWithSha1UsingSha2Verifier() throws Exception
	{
		File tempDirectory = createTempDirectory();
		File templateDirectory = new File(tempDirectory, "templates");
		try
		{
			selectDefaultFormTemplate(templateDirectory, securitySha1);

			FormTemplateManager manager = FormTemplateManager.createOrOpen(securitySha2, templateDirectory);
			assertEquals("", manager.getCurrentFormTemplate().getTitle());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	public void testLoadTemplateManagerStateSignedWithSha2UsingSha1Verifier() throws Exception
	{
		File tempDirectory = createTempDirectory();
		File templateDirectory = new File(tempDirectory, "templates");
		try
		{
			selectDefaultFormTemplate(templateDirectory, securitySha2);

			FormTemplateManager.createOrOpen(securitySha1, templateDirectory);
			fail("Should have thrown UnableToLoadCurrentTemplateException");
		}
		catch (FormTemplateManager.UnableToLoadCurrentTemplateException ignoreExpectedException)
		{
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(tempDirectory);
		}
	}

	private void selectDefaultFormTemplate(File templateDirectory, MartusCrypto security) throws Exception
	{
		FormTemplateManager manager = FormTemplateManager.createOrOpen(security, templateDirectory);
		manager.setCurrentFormTemplate("");
	}

	public void testReadBulletinSignedWithSha1UsingSha2Verifier() throws Exception
	{
		MockBulletinStore testStore = new MockBulletinStore(securitySha1);
		assertEquals(0, testStore.getBulletinCount());

		final String initialSummary = "New bulletin";

		Bulletin bulletin = testStore.createEmptyBulletin();
		bulletin.set(Bulletin.TAGSUMMARY, initialSummary);

		testStore.saveBulletin(bulletin);
		assertEquals(1, testStore.getBulletinCount());

		testStore = changeBulletinStoreSecurity(testStore, securitySha2);
		UniversalId uId = bulletin.getUniversalId();

		bulletin = testStore.getBulletinRevision(uId);
		assertEquals("not saved initially?", initialSummary, bulletin.get(Bulletin.TAGSUMMARY));
	}

	public void testReadBulletinSignedWithSha2UsingSha1Verifier() throws Exception
	{
		MockBulletinStore testStore = new MockBulletinStore(securitySha2);
		assertEquals(0, testStore.getBulletinCount());

		Bulletin bulletin = testStore.createEmptyBulletin();

		testStore.saveBulletin(bulletin);
		assertEquals(1, testStore.getBulletinCount());

		testStore = changeBulletinStoreSecurity(testStore, securitySha1);
		UniversalId uId = bulletin.getUniversalId();
		assertNull("should not find bulletin", testStore.getBulletinRevision(uId));
	}

	public void testEditBulletinSignedWithSha1UsingSha2Verifier() throws Exception
	{
		MockBulletinStore testStore = new MockBulletinStore(securitySha1);
		assertEquals(0, testStore.getBulletinCount());

		final String initialSummary = "New bulletin";

		Bulletin bulletin = testStore.createEmptyBulletin();
		bulletin.set(Bulletin.TAGSUMMARY, initialSummary);

		testStore.saveBulletin(bulletin);
		assertEquals(1, testStore.getBulletinCount());

		testStore = changeBulletinStoreSecurity(testStore, securitySha2);

		UniversalId uId = bulletin.getUniversalId();

		bulletin = testStore.getBulletinRevision(uId);

		assertEquals("not saved initially?", initialSummary, bulletin.get(Bulletin.TAGSUMMARY));

		final String changedSummary = "Changed bulletin";

		bulletin.set(Bulletin.TAGSUMMARY, changedSummary);
		testStore.saveBulletin(bulletin);

		bulletin = testStore.getBulletinRevision(uId);
		assertEquals("not saved initially?", changedSummary, bulletin.get(Bulletin.TAGSUMMARY));
	}

	private MockBulletinStore changeBulletinStoreSecurity(MockBulletinStore oldStore, MartusCrypto newSecurity) throws Exception
	{
		return new MockBulletinStore(oldStore.getStoreRootDir(), oldStore.getWriteableDatabase(), newSecurity);
	}

	private static MartusCrypto securitySha1;
	private static MartusCrypto securitySha2;
}
