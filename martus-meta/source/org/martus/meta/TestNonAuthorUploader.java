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

import java.io.File;

import org.martus.client.test.MockBulletinStore;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.bulletin.BulletinForTesting;
import org.martus.common.bulletin.BulletinLoader;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.database.DatabaseKey;
import org.martus.common.database.MockClientDatabase;
import org.martus.common.network.NetworkInterfaceConstants;
import org.martus.server.forclients.MockMartusServer;
import org.martus.server.forclients.ServerForClients;
import org.martus.util.StreamableBase64;
import org.martus.util.TestCaseEnhanced;

public class TestNonAuthorUploader extends TestCaseEnhanced
{
	public TestNonAuthorUploader(String name)
	{
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	
		setupClient();
		setupUploaderClient();
		setupServer();
	}

	private void setupClient() throws Exception 
	{
		if(authorSecurity == null)
		{
			authorSecurity = MockMartusSecuritySha2.createClient();
		}
		
		if(authorBulletinStore == null)
		{
			authorBulletinStore = new MockBulletinStore(authorSecurity);
			
			bulletinCreatedByAuthor = new Bulletin(authorSecurity);
			bulletinCreatedByAuthor.set(Bulletin.TAGTITLE, "Some Title");
			bulletinCreatedByAuthor.set(Bulletin.TAGPUBLICINFO, "Details1");
			bulletinCreatedByAuthor.set(Bulletin.TAGPRIVATEINFO, "PrivateDetails1");
			bulletinCreatedByAuthor.setImmutable();
			authorBulletinStore.saveEncryptedBulletinForTesting(bulletinCreatedByAuthor);
			bulletinCreatedByAuthor = BulletinLoader.loadFromDatabase(getClientDatabase(), DatabaseKey.createImmutableKey(bulletinCreatedByAuthor.getUniversalId()), authorSecurity);
			
			assertEquals("Incorrect bulletin title?", "Some Title", bulletinCreatedByAuthor.get(Bulletin.TAGTITLE));
		}
	}

	private void setupUploaderClient() throws Exception 
	{
		if (uploaderSecurity == null)
		{
			uploaderSecurity = MockMartusSecuritySha2.createOtherClient();
			uploaderAccountId = uploaderSecurity.getPublicKeyString();
		}
		
		if (uploaderBulletinStore == null)
		{
			uploaderBulletinStore = new MockBulletinStore(uploaderSecurity);
		}
	}
	
	private void setupServer() throws Exception 
	{
		if(serverSecurity == null)
		{
			serverSecurity = MockMartusSecuritySha2.createServer();
		}

		if (mockServer == null)
		{
			mockServer = new MockMartusServer(this);
			mockServer.setClientListenerEnabled(true);
			mockServer.verifyAndLoadConfigurationFiles();
			mockServer.setSecurity(serverSecurity);
			
			serverForClients = mockServer.serverForClients;
		}
	}
		
	@Override
	protected void tearDown() throws Exception
	{
		mockServer.deleteAllFiles();
		
		super.tearDown();
	}

	public void testUploader() throws Exception
	{
		File authorBulletinTempFile = exportAuthorCreatedBulletinToFile();
		Bulletin bulletinInUploader = moveBulletinToUploaderStore(authorBulletinTempFile);		
		uploadBulletinToServer(authorBulletinTempFile, bulletinInUploader);		
	}
	
	private Bulletin moveBulletinToUploaderStore(File authorBulletinTempFile) throws Exception 
	{
		assertEquals("Incorrect bulletin count for uploader client store?", 0, uploaderBulletinStore.getBulletinCount());
		
		Bulletin bulletinInUploader = uploaderBulletinStore.createEmptyBulletin();
        bulletinInUploader.set(Bulletin.TAGTITLE, bulletinCreatedByAuthor.get(Bulletin.TAGTITLE));
        bulletinInUploader.setMutable();
        bulletinInUploader.setAllPrivate(true);
        bulletinInUploader.getBulletinHeaderPacket().enableNonAuthorUpload();
        uploaderBulletinStore.saveBulletin(bulletinInUploader);
		assertEquals("Incorrect bulletin count for uploader client store?", 1, uploaderBulletinStore.getBulletinCount());		
		assertEquals("Incorrect bulletin title?","Some Title", bulletinInUploader.get(Bulletin.TAGTITLE));
		bulletinInUploader = BulletinLoader.loadFromDatabase(getUploaderDatabase(), DatabaseKey.createImmutableKey(bulletinInUploader.getUniversalId()), uploaderSecurity);
		assertTrue("Should allow non author upload?", bulletinInUploader.getBulletinHeaderPacket().allowNonAuthorUpload());

		return bulletinInUploader;
	}
	
	private void uploadBulletinToServer(File tempFileForBulletin, Bulletin bulletinInUploader) throws Exception 
	{
		String draft1ZipString = BulletinForTesting.saveToZipString(getUploaderDatabase(), bulletinInUploader, uploaderSecurity);
		byte[] draft1ZipBytes = StreamableBase64.decode(draft1ZipString);
		
		mockServer.setSecurity(serverSecurity);
		mockServer.serverForClients.clearCanUploadList();
		
		String bulletinLocalId = bulletinInUploader.getLocalId();		
		String result = serverForClients.putBulletinChunk(uploaderAccountId, uploaderAccountId, bulletinLocalId, draft1ZipBytes.length, 0, draft1ZipBytes.length, draft1ZipString);
		assertEquals(NetworkInterfaceConstants.OK, result);
	}
	
	private File exportAuthorCreatedBulletinToFile() throws Exception
	{
		File tempFileForBulletin = createTempFile();
		BulletinForTesting.saveToFile(getClientDatabase(), bulletinCreatedByAuthor, tempFileForBulletin, authorSecurity);
		assertEquals("Incorrect bulletin count for client store?", 1, authorBulletinStore.getBulletinCount());
		
		return tempFileForBulletin;
	}
	
	private static MockClientDatabase getClientDatabase()
	{
		return (MockClientDatabase) authorBulletinStore.getDatabase();
	}
	
	private static MockClientDatabase getUploaderDatabase()
	{
		return (MockClientDatabase) uploaderBulletinStore.getDatabase();
	}

	private static MartusCrypto authorSecurity;
	private static MockBulletinStore authorBulletinStore;
	private Bulletin bulletinCreatedByAuthor;
	
	private MartusCrypto uploaderSecurity;
	private static MockBulletinStore uploaderBulletinStore;
	private String uploaderAccountId;
	
	private static MartusCrypto serverSecurity;	
	private static MockMartusServer mockServer; 
	private ServerForClients serverForClients;
}
