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
package org.martus.common.crypto;

public class MockMartusSecuritySha2 extends AbstractMockMartusSecurity
{
	public static MockMartusSecuritySha2 createClient() throws Exception
	{
		MockMartusSecuritySha2 security = new MockMartusSecuritySha2();
		security.loadSampleAccount();
		return security;
	}

	public static MockMartusSecuritySha2 createOtherClient() throws Exception
	{
		MockMartusSecuritySha2 security = new MockMartusSecuritySha2();
		security.createKeyPairForOtherClient();
		return security;
	}

	public static MockMartusSecuritySha2 createHQ() throws Exception
	{
		MockMartusSecuritySha2 security = new MockMartusSecuritySha2();
		security.createKeyPairForHQ();
		return security;
	}

	public static MockMartusSecuritySha2 createServer() throws Exception
	{
		MockMartusSecuritySha2 security = new MockMartusSecuritySha2();
		security.createKeyPairForServer();
		return security;
	}

	public static MockMartusSecuritySha2 createOtherServer() throws Exception
	{
		MockMartusSecuritySha2 security = new MockMartusSecuritySha2();
		security.createKeyPairForOtherServer();
		return security;
	}

	public static MockMartusSecuritySha2 createAmplifier() throws Exception
	{
		MockMartusSecuritySha2 security = new MockMartusSecuritySha2();
		security.createKeyPairForAmplifier();
		return security;
	}

	public MockMartusSecuritySha2() throws Exception
	{
	}

	public SignatureEngine createSignatureVerifier(String signedByPublicKey)
			throws Exception
	{
		if(fakeSigVerifyFailure)
			return null;

		return SignatureEngineSha2.createVerifier(signedByPublicKey);
	}

	public SignatureEngine createSigner() throws Exception
	{
		return SignatureEngineSha2.createSigner(getKeyPair());
	}
}
