//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPGridRow;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.Combinators.SequentialPres;

public class GridRow extends SequentialPres
{
	public GridRow(Object children[])
	{
		super( children );
	}
	
	public GridRow(List<Object> children)
	{
		super( children );
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPGridRow element = new DPGridRow( Primitive.gridRowParams.get( ctx.getStyle() ) );
		element.setChildren( mapPresent( ctx.withStyle( Primitive.useGridRowParams( ctx.getStyle() ) ), children ) );
		return element;
	}
}