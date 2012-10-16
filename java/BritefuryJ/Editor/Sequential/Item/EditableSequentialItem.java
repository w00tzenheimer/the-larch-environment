//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Editor.Sequential.Item;

import java.util.List;

import BritefuryJ.Editor.Sequential.SequentialController;
import BritefuryJ.LSpace.LSElement;
import BritefuryJ.LSpace.TreeEventListener;
import BritefuryJ.Pres.Pres;
import BritefuryJ.Pres.PresentationContext;
import BritefuryJ.StyleSheet.StyleValues;

public class EditableSequentialItem extends Pres
{
	private SequentialController editor;
	private TreeEventListener editListeners[];
	private Pres child;
	
	
	public EditableSequentialItem(SequentialController editor, TreeEventListener editListener, Object child)
	{
		this.editor = editor;
		this.editListeners = new TreeEventListener[] { editListener };
		this.child = coerceNonNull( child );
	}

	public EditableSequentialItem(SequentialController editor, List<TreeEventListener> editListeners, Object child)
	{
		this.editor = editor;
		this.editListeners = editListeners.toArray( new TreeEventListener[editListeners.size()] );
		this.child = coerceNonNull( child );
	}


	@Override
	public LSElement present(PresentationContext ctx, StyleValues style)
	{
		LSElement element = child.present( ctx, style );
		if ( editor.isClearNeighbouringStructuresEnabled() )
		{
			element.addTreeEventListener( SequentialController.getClearNeighbouringStructuralValueListener() );
		}
		for (TreeEventListener listener: editListeners)
		{
			element.addTreeEventListener( listener );
		}
		return element;
	}
}
