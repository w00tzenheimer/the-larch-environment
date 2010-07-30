//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPViewport;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;
import BritefuryJ.DocPresent.PersistentState.PersistentState;
import BritefuryJ.DocPresent.Util.Range;

public class Viewport extends Pres
{
	private Pres child;
	private Range xRange = null, yRange = null;
	private PersistentState persistentState;
	
	
	public Viewport(Object child, PersistentState persistentState)
	{
		this.child = coerce( child );
		this.persistentState = persistentState;
	}
	
	public Viewport(Object child, Range xRange, Range yRange, PersistentState persistentState)
	{
		this.child = coerce( child );
		this.xRange = xRange;
		this.yRange = yRange;
		this.persistentState = persistentState;
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPViewport element = new DPViewport( Primitive.containerParams.get( ctx.getStyle() ), xRange, yRange, persistentState );
		element.setChild( child.present( ctx.withStyle( Primitive.useContainerParams.get( ctx.getStyle() ) ) ).layoutWrap() );
		return element;
	}
}
