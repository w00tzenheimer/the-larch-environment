//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace;

import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.Projection.ProjectiveBrowserContext;
import BritefuryJ.StyleSheet.StyleValues;

public interface FragmentContext
{
	public PresentationContext createPresentationContext();
	public StyleValues getStyleValues();
	public ProjectiveBrowserContext getBrowserContext();
}