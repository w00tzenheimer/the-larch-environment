//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.DocPresent.Interactor;

import BritefuryJ.Controls.PopupMenu;
import BritefuryJ.DocPresent.Input.PointerInputElement;

public interface ContextMenuElementInteractor extends AbstractElementInteractor
{
	boolean contextMenu(PointerInputElement element, PopupMenu menu);
}