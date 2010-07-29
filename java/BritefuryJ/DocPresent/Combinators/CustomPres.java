//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Combinators;

import BritefuryJ.DocPresent.DPElement;

public class CustomPres extends Pres
{
	public static interface PresFn
	{
		public DPElement present(PresentationContext ctx);
	}
	
	
	private PresFn presFn;
	
	
	public CustomPres(PresFn presFn)
	{
		super();
		this.presFn = presFn;
	}
	
	
	@Override
	public DPElement present(PresentationContext ctx)
	{
		return presFn.present( ctx );
	}
}