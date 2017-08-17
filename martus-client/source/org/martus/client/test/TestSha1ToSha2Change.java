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
import java.util.Vector;

import org.martus.client.core.MartusApp;
import org.martus.client.core.templates.FormTemplateManager;
import org.martus.client.network.RetrieveCommand;
import org.martus.client.swingui.MartusLocalization;
import org.martus.client.swingui.UiSession;
import org.martus.clientside.CurrentUiState;
import org.martus.clientside.test.ServerSideNetworkHandlerNotAvailable;
import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusLogger;
import org.martus.common.MartusUtilities;
import org.martus.common.MiniLocalization;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.Packet;
import org.martus.common.packet.UniversalId;
import org.martus.common.test.UniversalIdForTesting;
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

		appSha1 = MockMartusApp.create(securitySha1, getName());
		appSha1.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandlerNotAvailable());

		appSha2 = MockMartusApp.create(securitySha2, getName());
		appSha2.setSSLNetworkInterfaceHandlerForTesting(new ServerSideNetworkHandlerNotAvailable());

		testAppLocalization = new MartusLocalization(null, UiSession.getAllEnglishStrings());
		CurrentUiState currentUi = new CurrentUiState();
		currentUi.setCurrentLanguage(MiniLocalization.ENGLISH);
		currentUi.setCurrentDateFormat(MDY_SLASH);
		testAppLocalization.setLanguageSettingsProvider(currentUi);
	}

	public void tearDown() throws Exception
	{
		appSha1.deleteAllFiles();
		appSha2.deleteAllFiles();
		super.tearDown();
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

			disableLoggingDueToExpectedExceptionFromSha1Verifier();
			FormTemplateManager manager = FormTemplateManager.createOrOpen(securitySha1, templateDirectory);
			MartusLogger.reEnableLogging();
			
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

	private void disableLoggingDueToExpectedExceptionFromSha1Verifier()
	{
		MartusLogger.disableLogging();
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

		disableLoggingDueToExpectedExceptionFromSha1Verifier();
		Bulletin bulletin = testStore.createEmptyBulletin();
		MartusLogger.reEnableLogging();

		testStore.saveBulletin(bulletin);
		assertEquals(1, testStore.getBulletinCount());

		testStore = changeBulletinStoreSecurity(testStore, securitySha1);
		UniversalId uId = bulletin.getUniversalId();
		disableLoggingDueToExpectedExceptionFromSha1Verifier();
		assertNull("should not find bulletin", testStore.getBulletinRevision(uId));
		MartusLogger.reEnableLogging();
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

	public void testParseRetrieveCommandBundleSignedWithSha1UsingSha2Verifier() throws Exception
	{
		Vector uids = createTestIdsVector();
		RetrieveCommand rc = new RetrieveCommand("folder", uids);
		byte[] bundle = appSha1.createRetrieveCommandBundle(rc);

		RetrieveCommand got = appSha2.parseRetrieveCommandBundle(bundle);
		assertEquals("wrong folder?", rc.getFolderName(), got.getFolderName());
		assertEquals("wrong count?", rc.getRemainingToRetrieveCount(), got.getRemainingToRetrieveCount());
		assertEquals("wrong next uid?", rc.getNextToRetrieve(), got.getNextToRetrieve());
	}

	public void testParseRetrieveCommandBundleSignedWithSha2UsingSha1Verifier() throws Exception
	{
		Vector uids = createTestIdsVector();
		RetrieveCommand rc = new RetrieveCommand("folder", uids);
		byte[] bundle = appSha2.createRetrieveCommandBundle(rc);

		try
		{
			appSha1.parseRetrieveCommandBundle(bundle);
			fail("testParseRetrieveCommandBundleSignedWithSha2UsingSha1Verifier should have thrown MartusSignatureException");
		}
		catch (MartusCrypto.MartusSignatureException ignoreExpectedException)
		{
		}
	}

	private Vector createTestIdsVector()
	{
		Vector uids = new Vector();
		uids.add(UniversalIdForTesting.createDummyUniversalId());
		uids.add(UniversalIdForTesting.createDummyUniversalId());

		return uids;
	}

	public void testExtractPublicInfoSignedWithSha1UsingSha2Verifier() throws Exception
	{
		File temp = createTempFile();
		temp.delete();

		appSha1.exportPublicInfo(temp);

		String publicKey = appSha2.extractPublicInfo(temp);
		assertEquals("Public Key wrong?", appSha2.getSecurity().getPublicKeyString(), publicKey);

		temp.delete();
	}

	public void testExtractPublicInfoSignedWithSha2UsingSha1Verifier() throws Exception
	{
		File temp = createTempFile();
		temp.delete();

		appSha2.exportPublicInfo(temp);

		try
		{
			appSha1.extractPublicInfo(temp);
			fail("testExtractPublicInfoSignedWithSha2UsingSha1Verifier should have thrown PublicInformationInvalidException");
		}
		catch (MartusUtilities.PublicInformationInvalidException ignoreExpectedException)
		{
		}

		temp.delete();
	}

	public void testGetAccountDirectoryCreatedWithSha1UsingSha2() throws Exception
	{
		TRACE_BEGIN("testGetAccountDirectoryCreatedWithSha1UsingSha2");
		File rootDir = createTempDirectory();
		try
		{
			MartusApp app = new MartusApp(securitySha1, rootDir, testAppLocalization);

			String username = "name";
			String password = "pass";
			createAccount(app, username, password);
			String realAccountId1 = app.getAccountId();
			saveConfigInfo(app);

			app = new MartusApp(securitySha2, rootDir, testAppLocalization);
			File rootAccountDirectory = app.getAccountDirectory(realAccountId1);
			assertEquals(rootDir.getAbsolutePath(), rootAccountDirectory.getAbsolutePath());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(rootDir);
		}
		TRACE_END();
	}

	public void testGetAccountDirectoryCreatedWithSha2UsingSha1() throws Exception
	{
		TRACE_BEGIN("testGetAccountDirectoryCreatedWithSha2UsingSha1");
		File rootDir = createTempDirectory();
		try
		{
			MartusApp app = new MartusApp(securitySha2, rootDir, testAppLocalization);

			String username = "name";
			String password = "pass";
			createAccount(app, username, password);
			String realAccountId1 = app.getAccountId();
			saveConfigInfo(app);

			app = new MartusApp(securitySha1, rootDir, testAppLocalization);
			File rootAccountDirectory = app.getAccountDirectory(realAccountId1);
			assertNotEquals(rootDir.getAbsolutePath(), rootAccountDirectory.getAbsolutePath());
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(rootDir);
		}
		TRACE_END();
	}

	public void testCreateAccountWithSha2AttemptToSinginWithSha2() throws Exception
	{
		TRACE_BEGIN("testCreateAccountWithSha2AttemptToSinginWithSha2");
		File rootDir = createTempDirectory();
		try
		{
			MartusApp app = new MartusApp(securitySha2, rootDir, testAppLocalization);

			String username = "name";
			String password = "pass";
			createAccount(app, username, password);

			app = new MartusApp(securitySha2, rootDir, testAppLocalization);
			signin(app, username, password);
		}
		catch (Exception e)
		{
			fail("Should signin without and exceptions");
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(rootDir);
		}
		TRACE_END();
	}

	public void testCreateAccountWithSha1AttemptToSinginWithSha2() throws Exception
	{
		TRACE_BEGIN("testCreateAccountWithSha1AttemptToSinginWithSha2");
		File rootDir = createTempDirectory();
		try
		{
			MartusApp app = new MartusApp(securitySha1, rootDir, testAppLocalization);

			String username = "name";
			String password = "pass";
			createAccount(app, username, password);

			app = new MartusApp(securitySha2, rootDir, testAppLocalization);
			signin(app, username, password);
		}
		catch (Exception e)
		{
			fail("Should signin without and exceptions");
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(rootDir);
		}
		TRACE_END();
	}

	private void saveConfigInfo(MartusApp app) throws Exception
	{
		File configFile1 = app.getConfigInfoFile();
		app.saveConfigInfo();
		assertTrue("no config?", configFile1.exists());
		File sigFile1 = app.getConfigInfoSignatureFile();
		assertTrue("no config sig?", sigFile1.exists());
	}

	private void createAccount(MartusApp app, String username, String password) throws Exception
	{
		app.createAccount(username, password.toCharArray());
		app.doAfterSigninInitalization();
	}

	private void signin(MartusApp app, String username, String password) throws Exception
	{
		app.attemptSignIn(username, password.toCharArray());
		app.loadConfigInfo();
		app.doAfterSigninInitalization();
	}

	private static MartusCrypto securitySha1;
	private static MartusCrypto securitySha2;

	private MockMartusApp appSha1;
	private MockMartusApp appSha2;

	private MartusLocalization testAppLocalization;

	private static final String MDY_SLASH = "MM/dd/yyyy";
}
