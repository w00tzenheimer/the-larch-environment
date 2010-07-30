//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;

public class Bin extends Pres
{
	private Pres child;
	
	
	public Bin(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPBin element = new DPBin( Primitive.containerParams.get( ctx.getStyle() ) );
		element.setChild( child.present( ctx.withStyle( Primitive.useContainerParams.get( ctx.getStyle() ) ) ).layoutWrap() );
		return element;
	}
}
