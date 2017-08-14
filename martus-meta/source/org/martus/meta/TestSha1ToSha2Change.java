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

import org.martus.client.core.MartusApp;
import org.martus.client.network.BackgroundUploader;
import org.martus.client.test.MockMartusApp;
import org.martus.client.test.NullProgressMeter;
import org.martus.clientside.ClientSideNetworkHandlerUsingXmlRpc;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.server.forclients.AbstractMockMartusServer;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.MockMartusServerSha2;
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

		serverSha2 = new MockMartusServerSha2(this);
		serverSha2.serverForClients.loadBannedClients();
		serverSha2.verifyAndLoadConfigurationFiles();

		appSha1 = setUpMockMartusApp(appSecuritySha1, serverSha2);
		appSha2 = setUpMockMartusApp(appSecuritySha2, serverSha2);

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

		super.tearDown();
	}

	public void testSha2ClientUploadBulletinToSha1Server() throws Exception
	{
		TRACE_BEGIN("testSha2ClientUploadBulletinToSha1Server");

		MockMartusServer server = new MockMartusServer(this);
		server.serverForClients.loadBannedClients();
		server.verifyAndLoadConfigurationFiles();
		server.deleteAllData();

		ServerSideNetworkHandler serverSideNetworkHandler = new ServerSideNetworkHandler(server.serverForClients);
		appSha2.setSSLNetworkInterfaceHandlerForTesting(serverSideNetworkHandler);

		Bulletin b1 = createTestBulletin(appSha2);

		server.allowUploads(appSha2.getAccountId());
		assertEquals(NetworkInterfaceConstants.SIG_ERROR, sha2AppUploader.uploadBulletin(b1));

		server.deleteAllFiles();

		TRACE_END();
	}

	public void testSha1ClientUploadBulletinToSha2Server() throws Exception
	{
		TRACE_BEGIN("testSha1ClientUploadBulletinToSha2Server");

		Bulletin b1 = createTestBulletin(appSha1);

		serverSha2.allowUploads(appSha1.getAccountId());
		assertEquals(NetworkInterfaceConstants.OK, sha1AppUploader.uploadBulletin(b1));

		TRACE_END();
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

	private static final String MOCK_SERVER_NAME = "mock";

	private MockMartusApp appSha1;
	private MockMartusApp appSha2;

	private AbstractMockMartusServer serverSha2;

	private static MartusCrypto appSecuritySha1;
	private static MartusCrypto appSecuritySha2;

	private BackgroundUploader sha1AppUploader;
	private BackgroundUploader sha2AppUploader;
}
