//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.LSpace.Interactor;

import BritefuryJ.LSpace.Event.AbstractPointerButtonEvent;
import BritefuryJ.LSpace.Event.PointerButtonClickedEvent;
import BritefuryJ.LSpace.Input.PointerInputElement;

public interface ClickElementInteractor extends AbstractElementInteractor
{
	public boolean testClickEvent(PointerInputElement element, AbstractPointerButtonEvent event);
	public boolean buttonClicked(PointerInputElement element, PointerButtonClickedEvent event);
}