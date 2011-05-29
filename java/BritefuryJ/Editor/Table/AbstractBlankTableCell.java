//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Table;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Cell.AbstractBlankCell;
import BritefuryJ.IncrementalView.FragmentView;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.Primitive.Bin;
import BritefuryJ.Pres.Primitive.Segment;

public abstract class AbstractBlankTableCell extends AbstractBlankCell
{
	public AbstractBlankTableCell(Pres blankPres)
	{
		super( blankPres );
	}

	
	@Override
	public Pres present(FragmentView fragment, SimpleAttributeTable inheritedState)
	{
		Pres cellContents = presentCell( fragment, inheritedState );
		return new Bin( new Segment( cellContents.alignHPack().alignVRefY() ) ).alignHPack().alignVRefY();
	}
}