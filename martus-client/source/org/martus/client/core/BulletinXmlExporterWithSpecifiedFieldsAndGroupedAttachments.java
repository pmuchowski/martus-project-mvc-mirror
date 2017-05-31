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
package org.martus.client.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.martus.common.FieldSpecCollection;
import org.martus.common.MiniLocalization;
import org.martus.common.ProgressMeterInterface;
import org.martus.common.bulletin.Bulletin;
import org.martus.common.fieldspec.FieldSpec;
import org.martus.common.fieldspec.GridFieldSpec;
import org.martus.common.fieldspec.MiniFieldSpec;

public class BulletinXmlExporterWithSpecifiedFieldsAndGroupedAttachments extends AbstractBulletinXmlExporter
{
	private MiniFieldSpec[] fieldsToExport;

	public BulletinXmlExporterWithSpecifiedFieldsAndGroupedAttachments(MartusApp appToUse, MiniLocalization localizationToUse,
																																		 ProgressMeterInterface progressMeterToUse, MiniFieldSpec[] fieldsToExportToUse)
	{
		super(appToUse, localizationToUse, progressMeterToUse);
		fieldsToExport = fieldsToExportToUse;
	}

	private MiniFieldSpec[] getFieldsToExport()
	{
		return fieldsToExport;
	}

	@Override
	public boolean shouldGroupAttachments()
	{
		return true;
	}

	@Override
	protected FieldSpec[] getTopFieldsToExport(Bulletin bulletin) throws Exception
	{
		return findExportableFieldSpecsInFieldSpecCollection(bulletin.getTopSectionFieldSpecs());
	}

	@Override
	protected FieldSpec[] getBottomFieldsToExport(Bulletin bulletin) throws Exception
	{
		return findExportableFieldSpecsInFieldSpecCollection(bulletin.getBottomSectionFieldSpecs());
	}

	private FieldSpec[] findExportableFieldSpecsInFieldSpecCollection(FieldSpecCollection fieldSpecs) throws Exception
	{
		List<FieldSpec> fieldSpecToExportList = new ArrayList<>();
		Map<String, Vector<FieldSpec>> colsSpecMap = new HashMap<>();

		for (MiniFieldSpec miniFieldSpecToExport : getFieldsToExport())
		{
			String[] tagParts = miniFieldSpecToExport.getTag().split("\\.");
			String tag = tagParts[0];
			FieldSpec fieldSpecToExport = fieldSpecs.findBytag(tag);

			if (fieldSpecToExport == null)
			{
				continue;
			}

			if (fieldSpecToExport.getType().isGrid())
			{
				GridFieldSpec gridSpec = (GridFieldSpec) fieldSpecToExport;
				removeNonExportableGridColumns(colsSpecMap, gridSpec, tagParts);
			}

			fieldSpecToExportList.add(fieldSpecToExport);
		}

		return fieldSpecToExportList.toArray(new FieldSpec[fieldSpecToExportList.size()]);
	}

	private void removeNonExportableGridColumns(Map<String, Vector<FieldSpec>> colsSpecMap, GridFieldSpec gridSpec, String[] tagParts) throws Exception
	{
		String tag = tagParts[0];

		if (tagParts.length == 1)
			throw new Exception("Grid field spec tag does not contain column label: " + tag);

		String colLabel = tagParts[1];
		Vector<FieldSpec> colsSpecs = colsSpecMap.get(tag);

		if (colsSpecs == null)
		{
			colsSpecs = gridSpec.getColumns();
			colsSpecMap.put(tag, colsSpecs);
			gridSpec.setColumns(new Vector());
		}

		FieldSpec colSpec = findColumnSpecByLabel(colsSpecs, colLabel);

		gridSpec.addColumn(colSpec);
	}

	private FieldSpec findColumnSpecByLabel(Vector<FieldSpec> columns, String gridColumnLabel)
	{
		for (FieldSpec columnSpec : columns)
		{
			if (columnSpec.getLabel().equals(gridColumnLabel))
				return columnSpec;
		}

		return null;
	}
}
