##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os



#
#
#
# GSYM World
#
#
#

class GSymWorld (object):
	def __init__(self):
		super( GSymWorld, self ).__init__()
		
		
	def getModuleLanguage(self, moduleName):
		mod = __import__( moduleName )
		components = moduleName.split( '.' )
		for comp in components[1:]:
			mod = getattr( mod, comp )
		return getattr( mod, 'language' )
		
		

	