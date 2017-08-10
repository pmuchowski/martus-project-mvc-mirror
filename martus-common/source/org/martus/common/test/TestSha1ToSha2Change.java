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
package org.martus.common.test;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.database.ClientFileDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.fieldspec.CustomFieldError;
import org.martus.common.fieldspec.FormTemplate;
import org.martus.common.fieldspec.StandardFieldSpecs;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.util.DirectoryUtils;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.FileInputStreamWithSeek;
import org.martus.util.inputstreamwithseek.InputStreamWithSeek;

public class TestSha1ToSha2Change extends TestCaseEnhanced
{
	public TestSha1ToSha2Change(String name)
	{
		super(name);
	}

	public void setUp() throws Exception
	{
		super.setUp();
		if(securitySha1 == null)
		{
			securitySha1 = MockMartusSecuritySha1.createClient();
		}
		if(securitySha2 == null)
		{
			securitySha2 = MockMartusSecuritySha2.createClient();
		}
	}

	public void testVerifyPacketSignedWithSha1UsingSha2Verifier() throws Exception
	{
		ByteArrayInputStreamWithSeek in0 = createTestPacketAndWriteToXml(securitySha1);

		try
		{
			Packet.verifyPacketSignature(in0, securitySha2);
		}
		catch (Exception e)
		{
			fail("verifyPacketSignature should succeed without any exceptions");
		}
	}

	public void testVerifyPacketSignedWithSha2UsingSha1Verifier() throws Exception
	{
		ByteArrayInputStreamWithSeek in0 = createTestPacketAndWriteToXml(securitySha2);

		try
		{
			Packet.verifyPacketSignature(in0, securitySha1);
			fail("verifyPacketSignature Should have thrown SignatureVerificationException");
		}
		catch(Packet.SignatureVerificationException ignoreExpectedException)
		{
		}
	}

	private ByteArrayInputStreamWithSeek createTestPacketAndWriteToXml(MartusCrypto security) throws Exception
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(security);
		bhp.setPrivateFieldDataPacketId("Jos"+UnicodeConstants.ACCENT_E_LOWER+"e");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		bhp.writeXml(out, security);

		byte[] bytes = out.toByteArray();
		return new ByteArrayInputStreamWithSeek(bytes);
	}

	public void testImportTemplateSignedWithSha1UsingSha2Verifier() throws Exception
	{
		File exportFile = createAndExportTestTemplate(securitySha1);
		assertTrue(exportFile.exists());

		FormTemplate importedTemplate = new FormTemplate();
		assertTrue("Should import successfully", importTemplate(importedTemplate, exportFile, securitySha2));

		exportFile.delete();
	}

	public void testImportTemplateSignedWithSha2UsingSha1Verifier() throws Exception
	{
		File exportFile = createAndExportTestTemplate(securitySha2);
		assertTrue(exportFile.exists());

		FormTemplate importedTemplate = new FormTemplate();
		assertFalse("Import should fail", importTemplate(importedTemplate, exportFile, securitySha1));
		assertEquals(CustomFieldError.CODE_SIGNATURE_ERROR, ((CustomFieldError)importedTemplate.getErrors().get(0)).getCode());

		exportFile.delete();
	}

	private File createAndExportTestTemplate(MartusCrypto security) throws Exception
	{
		File exportFile = createTempFileFromName("$$$testExportXml");
		exportFile.deleteOnExit();
		String formTemplateTitle = "New Form Title";
		String formTemplateDescription = "New Form Description";
		FieldSpecCollection defaultFieldsTopSection = new FieldSpecCollection(StandardFieldSpecs.getDefaultTopSectionFieldSpecs().asArray());
		FieldSpecCollection defaultFieldsBottomSection = new FieldSpecCollection(StandardFieldSpecs.getDefaultBottomSectionFieldSpecs().asArray());
		FormTemplate template = new FormTemplate(formTemplateTitle, formTemplateDescription, defaultFieldsTopSection, defaultFieldsBottomSection);
		template.exportTemplate(security, exportFile);

		return exportFile;
	}

	private boolean importTemplate(FormTemplate template, File exportFile, MartusCrypto security) throws Exception
	{
		InputStreamWithSeek inputStreamWithSeek = new FileInputStreamWithSeek(exportFile);
		try
		{
			return template.importTemplate(security, inputStreamWithSeek);
		}
		finally
		{
			inputStreamWithSeek.close();
		}
	}

	public void testVerifySessionKeyCacheSignedWithSha1UsingSha2Verifier() throws Exception
	{
		byte[] original = securitySha1.getSessionKeyCache();

		try
		{
			securitySha2.setSessionKeyCache(original);
		}
		catch (Exception e)
		{
			fail("setSessionKeyCache should succeed without any exceptions");
		}
	}

	public void testVerifySessionKeyCacheSignedWithSha2UsingSha1Verifier() throws Exception
	{
		byte[] original = securitySha2.getSessionKeyCache();

		try
		{
			securitySha1.setSessionKeyCache(original);
			fail("setSessionKeyCache should have thrown MartusSignatureException");
		}
		catch(MartusCrypto.MartusSignatureException ignoreExpectedException)
		{
		}
	}

	public void testVerifyFileSignedWithSha1UsingSha2Verifier() throws Exception
	{
		String string1 = "The string to write into the file to sign.";
		File testFile = createTempFileWithData(string1);
		File signatureFile = MartusUtilities.createSignatureFileFromFile(testFile, securitySha1);

		try
		{
			MartusUtilities.verifyFileAndSignature(testFile, signatureFile, securitySha2, securitySha2.getPublicKeyString());
		}
		catch (Exception e)
		{
			fail("verifyFileAndSignature should succeed without any exceptions");
		}

		signatureFile.delete();
		testFile.delete();
	}

	public void testVerifyFileSignedWithSha2UsingSha1Verifier() throws Exception
	{
		String string1 = "The string to write into the file to sign.";
		File testFile = createTempFileWithData(string1);
		File signatureFile = MartusUtilities.createSignatureFileFromFile(testFile, securitySha2);

		try
		{
			MartusUtilities.verifyFileAndSignature(testFile, signatureFile, securitySha1, securitySha1.getPublicKeyString());
			fail("verifyFileAndSignature should have thrown FileVerificationException");
		}
		catch (MartusUtilities.FileVerificationException ignoreExpectedException)
		{
		}

		signatureFile.delete();
		testFile.delete();
	}

	public void testClientFileDbInitializerWhenAccountMapSignedWithSha1UsingSha2Verifier() throws Exception
	{
		File dir = createAccountMapAndSignature(securitySha1);

		try
		{
			ClientFileDatabase cfdb = new ClientFileDatabase(dir, securitySha2);
			cfdb.initialize();
		}
		catch (Exception e)
		{
			fail("initialize should succeed without any exceptions");
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(dir);
		}
	}

	public void testClientFileDbInitializerWhenAccountMapSignedWithSha2UsingSha1Verifier() throws Exception
	{
		File dir = createAccountMapAndSignature(securitySha2);

		try
		{
			ClientFileDatabase cfdb = new ClientFileDatabase(dir, securitySha1);
			cfdb.initialize();
			fail("initialize should have thrown FileVerificationException");
		}
		catch(MartusUtilities.FileVerificationException ignoreExpectedException)
		{
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(dir);
		}
	}

	private File createAccountMapAndSignature(MartusCrypto security) throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdir();

		File accountMap = new File(dir, "acctmap.txt");
		UnicodeWriter writer = new UnicodeWriter(accountMap);
		writer.writeln("user=dir");
		writer.close();

		MartusUtilities.createSignatureFileFromFile(accountMap, security);

		return dir;
	}

	public void testServerFileDbInitializerWhenAccountMapSignedWithSha1UsingSha2Verifier() throws Exception
	{
		File dir = createServerAccountMapAndSignature(securitySha1);

		try
		{
			ServerFileDatabase cfdb = new ServerFileDatabase(dir, securitySha2);
			cfdb.initialize();
		}
		catch (Exception e)
		{
			fail("initialize should succeed without any exceptions");
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(dir);
		}
	}

	public void testServerFileDbInitializerWhenAccountMapSignedWithSha2UsingSha1Verifier() throws Exception
	{
		File dir = createServerAccountMapAndSignature(securitySha2);

		try
		{
			ServerFileDatabase cfdb = new ServerFileDatabase(dir, securitySha1);
			cfdb.initialize();
			fail("initialize should have thrown FileVerificationException");
		}
		catch(MartusUtilities.FileVerificationException ignoreExpectedException)
		{
		}
		finally
		{
			DirectoryUtils.deleteEntireDirectoryTree(dir);
		}
	}

	private File createServerAccountMapAndSignature(MartusCrypto security) throws Exception
	{
		File dir = createTempFile();
		dir.delete();
		dir.mkdir();

		File accountMap = new File(dir, "acctmap.txt");
		UnicodeWriter writer = new UnicodeWriter(accountMap);
		writer.writeln("user=dir");
		writer.close();

		MartusServerUtilities.createSignatureFileFromFileOnServer(accountMap, security);

		return dir;
	}

	private static MartusCrypto securitySha1;
	private static MartusCrypto securitySha2;
}
