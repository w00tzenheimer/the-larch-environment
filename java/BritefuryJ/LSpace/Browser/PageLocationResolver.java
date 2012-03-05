//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.LSpace.Browser;

import BritefuryJ.LSpace.PersistentState.PersistentStateStore;


public interface PageLocationResolver
{
	public BrowserPage resolveLocationAsPage(Location location, PersistentStateStore persistentState);
}