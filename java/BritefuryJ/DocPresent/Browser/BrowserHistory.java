//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package BritefuryJ.DocPresent.Browser;

import java.util.Stack;

class BrowserHistory
{
	private Stack<BrowserContext> past, future;
	private BrowserContext currentContext;
	
	
	
	public BrowserHistory(String location)
	{
		past = new Stack<BrowserContext>();
		future = new Stack<BrowserContext>();
		currentContext = new BrowserContext( location );
	}
	
	
	public BrowserContext getCurrentContext()
	{
		return currentContext;
	}
	
	
	public void visit(String location)
	{
		past.push( currentContext );
		currentContext = new BrowserContext( location );
		future.clear();
	}
	
	
	public boolean canGoBack()
	{
		return past.size() > 0;
	}
	
	public void back()
	{
		if ( past.size() > 0 )
		{
			future.push( currentContext );
			currentContext = past.pop();
		}
	}


	public boolean canGoForward()
	{
		return future.size() > 0;
	}
	
	public void forward()
	{
		if ( future.size() > 0 )
		{
			past.push( currentContext );
			currentContext = future.pop();
		}
	}
	
	
	public void clear()
	{
		past.clear();
		future.clear();
	}
}
