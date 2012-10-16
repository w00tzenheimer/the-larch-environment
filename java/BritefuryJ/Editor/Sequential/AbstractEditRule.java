//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential;

import BritefuryJ.AttributeTable.SimpleAttributeTable;
import BritefuryJ.Pres.Pres;

public abstract class AbstractEditRule
{
	protected SequentialController editor;
	
	
	public AbstractEditRule(SequentialController editor)
	{
		this.editor = editor;
	}
	

	public abstract Pres applyToFragment(Pres view, Object model, SimpleAttributeTable inheritedState);
}
