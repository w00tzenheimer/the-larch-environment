//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators.Primitive;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPProxy;
import BritefuryJ.DocPresent.Combinators.Pres;
import BritefuryJ.DocPresent.Combinators.PresentationContext;

public class Proxy extends Pres
{
	private Pres child;
	
	
	public Proxy(Object child)
	{
		this.child = coerce( child );
	}
	

	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		DPElement childElement = child.present( ctx );
		DPProxy proxy = new DPProxy();
		proxy.setChild( childElement );
		proxy.copyAlignmentFlagsFrom( childElement );
		return proxy;
	}
}