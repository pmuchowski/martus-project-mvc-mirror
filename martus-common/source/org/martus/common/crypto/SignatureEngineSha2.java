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

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

import org.martus.common.MartusConstants;

public class SignatureEngineSha2 implements SignatureEngine
{
	public static SignatureEngine createSigner(MartusKeyPair keyPair) throws Exception
	{
		SignatureEngineSha2 engine = new SignatureEngineSha2();
		engine.prepareToSign(keyPair.getPrivateKey());
		return engine;
	}

	public static SignatureEngine createVerifier(String signedByPublicKey) throws Exception
	{
		SignatureEngineSha2 engine = new SignatureEngineSha2();
		engine.prepareToVerify(signedByPublicKey);
		return engine;
	}

	public void digest(byte b) throws Exception
	{
		engineSha2.update(b);
		engineSha1.update(b);
	}

	public void digest(byte[] bytes) throws Exception
	{
		engineSha2.update(bytes);
		engineSha1.update(bytes);
	}

	public void digest(byte[] buffer, int off, int len) throws Exception
	{
		engineSha2.update(buffer, off, len);
		engineSha1.update(buffer, off, len);
	}

	public void digest(InputStream in) throws Exception
	{
		int got;
		byte[] bytes = new byte[MartusConstants.streamBufferCopySize];
		while ((got = in.read(bytes)) >= 0)
		{
			engineSha2.update(bytes, 0, got);
			engineSha1.update(bytes, 0, got);
		}
	}

	public byte[] getSignature() throws Exception
	{
		return engineSha2.sign();
	}

	public boolean isValidSignature(byte[] sig) throws Exception
	{
		return engineSha2.verify(sig) || engineSha1.verify(sig);
	}




	private SignatureEngineSha2() throws Exception
	{
		engineSha2 = Signature.getInstance(SIGN_ALGORITHM_SHA2, "BC");
		engineSha1 = Signature.getInstance(SIGN_ALGORITHM_SHA1, "BC");
	}

	private void prepareToSign(PrivateKey key) throws Exception
	{
		engineSha2.initSign(key);
		engineSha1.initSign(key);
	}

	private void prepareToVerify(String signedByPublicKey) throws Exception
	{
		PublicKey key = MartusJceKeyPair.extractPublicKey(signedByPublicKey);
		engineSha2.initVerify(key);
		engineSha1.initVerify(key);
	}

	private Signature engineSha2;
	private Signature engineSha1;


	private static final String SIGN_ALGORITHM_SHA2 = "SHA512WithRSA";
	private static final String SIGN_ALGORITHM_SHA1 = "SHA1WithRSA";
}
