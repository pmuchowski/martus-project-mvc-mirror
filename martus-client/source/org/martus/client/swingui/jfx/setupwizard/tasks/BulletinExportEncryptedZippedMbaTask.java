/*

Martus(TM) is a trademark of Beneficent Technology, Inc. 
This software is (c) Copyright 2001-2015, Beneficent Technology, Inc.

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
package org.martus.client.swingui.jfx.setupwizard.tasks;

import java.io.File;
import java.util.Vector;

import org.martus.client.bulletinstore.ExportEncryptedBulletinsInsideZip;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.packet.UniversalId;

public class BulletinExportEncryptedZippedMbaTask extends AbstractExportTask
{
	public BulletinExportEncryptedZippedMbaTask(UiMainWindow mainWindowToUse, UniversalId[] bulletinIdsToUse, File destinationToUse)
	{
		super(mainWindowToUse);
		
		bulletinIdsToExport = bulletinIdsToUse;
		destination = destinationToUse;
		mainWindow = mainWindowToUse;
	}

	@Override
	protected Void call() throws Exception
	{
		Vector<Bulletin> bulletinsToExport = mainWindow.getBulletins(bulletinIdsToExport);
		for (Bulletin bulletin : bulletinsToExport)
		{
			bulletin.getBulletinHeaderPacket().enableNonAuthorUpload();
			mainWindow.getStore().saveBulletin(bulletin);
		}
		
		exporter = new ExportEncryptedBulletinsInsideZip(mainWindow, progress, destination);
		exporter.doExport(destination.getParentFile(), bulletinsToExport);
		
		return null;
	}
	
	private UiMainWindow mainWindow;
	private UniversalId[] bulletinIdsToExport;
	private File destination;
}
