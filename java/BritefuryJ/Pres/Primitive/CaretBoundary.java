//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Pres.Primitive;

import BritefuryJ.LSpace.LSCaretBoundary;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.LSHiddenText;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class CaretBoundary extends Pres
{
	public CaretBoundary()
	{
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		return new LSCaretBoundary();
	}
}