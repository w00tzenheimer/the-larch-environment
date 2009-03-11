##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Dispatch.Dispatch import DispatchError
from Britefury.Dispatch.MethodDispatch import methodDispatch, methodDispatchAndGetName

from Britefury.gSym.View.gSymStyles import viewError_textStyle


			
		
		
class GSymView (object):
	def __call__(self, xs, ctx, state):
		element = None
		try:
			element, name = methodDispatchAndGetName( self, xs, ctx, state )
			element.setDebugName( name )
		except DispatchError:
			element = ctx.text( viewError_textStyle, '<<ERROR>>' )
		return element
	
		

	

