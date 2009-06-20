//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent;

import java.awt.event.KeyEvent;

public interface ElementKeyboardListener
{
	public boolean onKeyPress(DPWidget element, KeyEvent event);
	public boolean onKeyRelease(DPWidget element, KeyEvent event);
	public boolean onKeyTyped(DPWidget element, KeyEvent event);
}