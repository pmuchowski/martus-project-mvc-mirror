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
package org.martus.meta;

import java.util.Vector;

import org.martus.client.core.MartusApp;
import org.martus.client.network.BackgroundUploader;
import org.martus.client.network.Retriever;
import org.martus.client.test.MockMartusApp;
import org.martus.client.test.NullProgressMeter;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.common.HeadquartersKey;
import org.martus.common.HeadquartersKeys;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.server.forclients.AbstractMockMartusServer;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.MockMartusServerSha1;
import org.martus.server.forclients.ServerSideNetworkHandler;
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
		TRACE_BEGIN("setUp");

		if(appSecuritySha1 == null)
			appSecuritySha1 = MockMartusSecuritySha1.createClient();

		if(appSecuritySha2 == null)
			appSecuritySha2 = MockMartusSecuritySha2.createClient();

		if(hqAppSecuritySha1 == null)
			hqAppSecuritySha1 = MockMartusSecuritySha1.createHQ();

		if(hqAppSecuritySha2 == null)
			hqAppSecuritySha2 = MockMartusSecuritySha2.createHQ();

		serverSha2 = new MockMartusServerSha1(this);
		serverSha2.serverForClients.loadBannedClients();
		serverSha2.verifyAndLoadConfigurationFiles();

		appSha1 = setUpMockMartusApp(appSecuritySha1, serverSha2);
		appSha2 = setUpMockMartusApp(appSecuritySha2, serverSha2);

		hqAppSha1 = setUpMockMartusApp(hqAppSecuritySha1, serverSha2);
		hqAppSha2 = setUpMockMartusApp(hqAppSecuritySha2, serverSha2);

		sha1AppUploader = new BackgroundUploader(appSha1, new NullProgressMeter());
		sha2AppUploader = new BackgroundUploader(appSha2, new NullProgressMeter());

		serverSha2.deleteAllData();

		serverSha2.allowUploads(appSha1.getAccountId());
		serverSha2.allowUploads(appSha2.getAccountId());

		TRACE_END();
	}

	private MockMartusApp setUpMockMartusApp(MartusCrypto security, AbstractMockMartusServer server) throws Exception
	{
		MockMartusApp app = MockMartusApp.create(security, getName());
		ClientSideNetworkHandlerUsingXmlRpc.addAllowedServer(MOCK_SERVER_NAME);
		app.setServerInfo(MOCK_SERVER_NAME, server.getAccountId(), "");

		ServerSideNetworkHandler serverSideNetworkHandler = new ServerSideNetworkHandler(server.serverForClients);
		app.setSSLNetworkInterfaceHandlerForTesting(serverSideNetworkHandler);

		return app;
	}

	public void tearDown() throws Exception
	{
		serverSha2.deleteAllFiles();

		appSha1.deleteAllFiles();
		appSha2.deleteAllFiles();

		hqAppSha1.deleteAllFiles();
		hqAppSha2.deleteAllFiles();

		super.tearDown();
	}

	public void testSha2ClientUploadBulletinToSha1Server() throws Exception
	{
		TRACE_BEGIN("testSha2ClientUploadBulletinToSha1Server");

		MockMartusServer server = new MockMartusServer(this);
		server.serverForClients.loadBannedClients();
		server.verifyAndLoadConfigurationFiles();
		server.deleteAllData();

		server.allowUploads(appSha2.getAccountId());

		ServerSideNetworkHandler serverSideNetworkHandler = new ServerSideNetworkHandler(server.serverForClients);
		appSha2.setSSLNetworkInterfaceHandlerForTesting(serverSideNetworkHandler);

		Bulletin b1 = createTestBulletin(appSha2);

		uploadBulletinAndVerifyResult(sha2AppUploader, b1, NetworkInterfaceConstants.SIG_ERROR);

		server.deleteAllFiles();

		TRACE_END();
	}

	public void testSha1ClientUploadBulletinToSha2Server() throws Exception
	{
		TRACE_BEGIN("testSha1ClientUploadBulletinToSha2Server");

		Bulletin b1 = createTestBulletin(appSha1);

		uploadBulletinAndVerifyResult(sha1AppUploader, b1, NetworkInterfaceConstants.OK);

		TRACE_END();
	}

	public void testSha1ClientShareBulletinWithSha1Contact() throws Exception
	{
		TRACE_BEGIN("testSha1ClientShareBulletinWithSha1Contact");

		addHqForClient(appSha1, hqAppSha1.getAccountId());

		Bulletin b1 = createTestBulletin(appSha1);
		Bulletin b2 = createTestBulletin(appSha1);

		uploadBulletinAndVerifyResult(sha1AppUploader, b1, NetworkInterfaceConstants.OK);
		uploadBulletinAndVerifyResult(sha1AppUploader, b2, NetworkInterfaceConstants.OK);

		retrieveBulletinsAndVerifyResult(NetworkInterfaceConstants.OK, hqAppSha1, b1, b2);

		TRACE_END();
	}

	public void testSha2ClientShareBulletinWithSha1Contact() throws Exception
	{
		TRACE_BEGIN("testSha2ClientShareBulletinWithSha1Contact");

		addHqForClient(appSha2, hqAppSha1.getAccountId());

		Bulletin b1 = createTestBulletin(appSha2);
		Bulletin b2 = createTestBulletin(appSha2);

		uploadBulletinAndVerifyResult(sha2AppUploader, b1, NetworkInterfaceConstants.OK);
		uploadBulletinAndVerifyResult(sha2AppUploader, b2, NetworkInterfaceConstants.OK);

		retrieveBulletinsAndVerifyResult(NetworkInterfaceConstants.INCOMPLETE, hqAppSha1, b1, b2);

		TRACE_END();
	}

	public void testSha1ClientShareBulletinWithSha2Contact() throws Exception
	{
		TRACE_BEGIN("testSha1ClientShareBulletinWithSha1Contact");

		addHqForClient(appSha1, hqAppSha2.getAccountId());

		Bulletin b1 = createTestBulletin(appSha1);
		Bulletin b2 = createTestBulletin(appSha1);

		uploadBulletinAndVerifyResult(sha1AppUploader, b1, NetworkInterfaceConstants.OK);
		uploadBulletinAndVerifyResult(sha1AppUploader, b2, NetworkInterfaceConstants.OK);

		retrieveBulletinsAndVerifyResult(NetworkInterfaceConstants.OK, hqAppSha2, b1, b2);

		TRACE_END();
	}

	public void testSha2ClientShareBulletinWithSha2Contact() throws Exception
	{
		TRACE_BEGIN("testSha2ClientShareBulletinWithSha2Contact");

		addHqForClient(appSha2, hqAppSha2.getAccountId());

		Bulletin b1 = createTestBulletin(appSha2);
		Bulletin b2 = createTestBulletin(appSha2);

		uploadBulletinAndVerifyResult(sha2AppUploader, b1, NetworkInterfaceConstants.OK);
		uploadBulletinAndVerifyResult(sha2AppUploader, b2, NetworkInterfaceConstants.OK);

		retrieveBulletinsAndVerifyResult(NetworkInterfaceConstants.OK, hqAppSha2, b1, b2);

		TRACE_END();
	}

	private void uploadBulletinAndVerifyResult(BackgroundUploader uploader, Bulletin bulletin, String expectedResult) throws Exception
	{
		assertEquals("failed upload?", expectedResult, uploader.uploadBulletin(bulletin));
	}

	private void retrieveBulletinsAndVerifyResult(String expectedResult, MartusApp app, Bulletin... bulletins)
	{
		Vector uidList = new Vector();

		for (Bulletin b : bulletins)
			uidList.add(b.getUniversalId());

		Retriever retriever = new Retriever(app, null);
		retriever.retrieveBulletins(uidList, app.createFolderRetrievedFieldOffice());
		assertEquals("retrieve field office bulletins failed?", expectedResult, retriever.getResult());
	}

	private Bulletin createTestBulletin(MartusApp martusApp) throws Exception
	{
		String sampleSummary1 = "this is a basic summary";

		Bulletin b1 = martusApp.createBulletin();
		b1.setAllPrivate(true);
		b1.set(Bulletin.TAGTITLE, sampleSummary1);
		b1.setImmutable();
		martusApp.setDefaultHQKeysInBulletin(b1);
		martusApp.getStore().saveBulletin(b1);

		return b1;
	}

	private void addHqForClient(MockMartusApp app, String hqAccountId) throws Exception
	{
		HeadquartersKeys keys = new HeadquartersKeys();
		HeadquartersKey key = new HeadquartersKey(hqAccountId);
		keys.add(key);
		app.setAndSaveHQKeys(keys, keys);
	}

	private static final String MOCK_SERVER_NAME = "mock";

	private MockMartusApp appSha1;
	private MockMartusApp appSha2;

	private MockMartusApp hqAppSha1;
	private MockMartusApp hqAppSha2;

	private AbstractMockMartusServer serverSha2;

	private static MartusCrypto appSecuritySha1;
	private static MartusCrypto appSecuritySha2;

	private static MartusCrypto hqAppSecuritySha1;
	private static MartusCrypto hqAppSecuritySha2;

	private BackgroundUploader sha1AppUploader;
	private BackgroundUploader sha2AppUploader;
}
