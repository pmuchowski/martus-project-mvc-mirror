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

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecuritySha1;
import org.martus.common.crypto.MockMartusSecuritySha2;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.Packet;
import org.martus.util.TestCaseEnhanced;
import org.martus.util.inputstreamwithseek.ByteArrayInputStreamWithSeek;

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

	private static MartusCrypto securitySha1;
	private static MartusCrypto securitySha2;
}
