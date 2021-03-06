//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace.LayoutTree;

import BritefuryJ.LSpace.LSBlank;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.ElementFilter;
import BritefuryJ.Math.Point2;

public class LayoutNodeBlank extends LeafLayoutNode
{
	public LayoutNodeBlank(LSBlank element)
	{
		super( element );
	}



	protected void updateRequisitionX()
	{
		layoutReqBox.setRequisitionX( 0.0, 0.0 );
	}

	protected void updateRequisitionY()
	{
		layoutReqBox.setRequisitionY( 0.0, 0.0 );
	}
	



	public LSElement getLeafClosestToLocalPoint(Point2 localPos, ElementFilter filter)
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
