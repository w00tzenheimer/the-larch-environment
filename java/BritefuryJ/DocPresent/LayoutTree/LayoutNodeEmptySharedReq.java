//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.LayoutTree;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPBlank;
import BritefuryJ.DocPresent.ElementFilter;
import BritefuryJ.DocPresent.Layout.LReqBox;
import BritefuryJ.Math.Point2;

public abstract class LayoutNodeEmptySharedReq extends LeafLayoutNodeSharedReq
{
	public LayoutNodeEmptySharedReq(DPBlank element, LReqBox reqBox)
	{
		super( element, reqBox );
	}



	protected void updateRequisitionX()
	{
	}

	protected void updateRequisitionY()
	{
	}
	



	public DPElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
	{
		if ( filter == null  ||  filter.testElement( element ) )
		{
			return element;
		}
		else
		{
			return null;
		}
	}
}
