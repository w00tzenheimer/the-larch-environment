//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

public abstract class TextEditEvent extends EditEvent
{
	protected DPContentLeaf leaf;
	
	
	public TextEditEvent(DPContentLeaf leaf)
	{
		this.leaf = leaf;
	}
	
	
	public DPContentLeaf getLeaf()
	{
		return leaf;
	}
	
	
	public abstract boolean revert();
}
