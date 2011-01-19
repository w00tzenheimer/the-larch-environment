//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import java.util.List;

import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.TreeEventListener;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class StructuralItem extends Pres
{
	private TreeEventListener editListeners[];
	private Object value;
	private Pres child;
	
	
	public StructuralItem(Object value, Object child)
	{
		this.editListeners = null;
		this.value = value;
		this.child = coerceNonNull( child );
	}

	public StructuralItem(TreeEventListener editListener, Object value, Object child)
	{
		this.editListeners = new TreeEventListener[] { editListener };
		this.child = coerceNonNull( child );
		this.value = value;
	}

	public StructuralItem(List<TreeEventListener> editListeners, Object value, Object child)
	{
		this.editListeners = editListeners.toArray( new TreeEventListener[] {} );
		this.value = value;
		this.child = coerceNonNull( child );
	}


	@Override
	public DPElement present(PresentationContext ctx, StyleValues style)
	{
		DPElement element = child.present( ctx, style );
		element.setFixedValue( value );
		if ( editListeners != null )
		{
			for (TreeEventListener listener: editListeners)
			{
				element.addTreeEventListener( listener );
			}
		}
		return element;
	}
}
