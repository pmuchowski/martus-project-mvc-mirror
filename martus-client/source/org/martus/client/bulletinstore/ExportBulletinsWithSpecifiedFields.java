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
package org.martus.client.bulletinstore;

import java.io.File;
import java.util.Vector;

import org.martus.client.bulletinstore.converters.BulletinsAsXmlToCsvConverter;
import org.martus.client.bulletinstore.converters.BulletinsAsXmlToCsvConverterWithoutMetadataFields;
import org.martus.client.core.BulletinXmlExporterWithSpecifiedFieldsAndGroupedAttachments;
import org.martus.client.swingui.UiMainWindow;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.MiniFieldSpec;
import org.xml.sax.InputSource;

public class ExportBulletinsWithSpecifiedFields extends ExportBulletins
{
	private MiniFieldSpec[] fieldsToExport;

	public ExportBulletinsWithSpecifiedFields(UiMainWindow mainWindowToUse, MiniFieldSpec[] fieldsToExport)
	{
		super(mainWindowToUse, null);
		this.fieldsToExport = fieldsToExport;
	}

	@Override
	public void doExport(File destFile, Vector bulletinsToUse)
	{
		destinationFile = destFile;
		bulletinsToExport = bulletinsToUse;
		ExporterThread exporterThread = new ExporterThreadWithSpecifiedFields(getProgressDlg());
		exporterThread.start();
	}

	class ExporterThreadWithSpecifiedFields extends ExporterThread
	{
		public ExporterThreadWithSpecifiedFields(ProgressMeterInterface progressRetrieveDlgToUse)
		{
			super(progressRetrieveDlgToUse);
		}

		@Override
		protected String exportBulletinsWithGroupedAttachments(Vector<Bulletin> bulletinsToExportForTemplate, File destinationDir) throws Exception
		{
			exporter = new BulletinXmlExporterWithSpecifiedFieldsAndGroupedAttachments(getMainWindow().getApp(), getMainWindow().getLocalization(), progressMeter, fieldsToExport);

			return exportBulletins(bulletinsToExportForTemplate, destinationDir);
		}

		@Override
		protected void exportAsCsvs() throws Exception
		{
			exportBulletins("", bulletinsToExport);
		}

		@Override
		protected BulletinsAsXmlToCsvConverter createXmlToCsvConverter(InputSource inputSourceToUse, String anOutFileName)
		{
			return new BulletinsAsXmlToCsvConverterWithoutMetadataFields(inputSourceToUse, anOutFileName);
		}
	}
}
