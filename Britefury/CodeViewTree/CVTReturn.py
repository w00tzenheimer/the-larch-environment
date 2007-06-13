##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGReturn import CGReturn

from Britefury.CodeViewTree.CVTStatement import CVTStatement



class CVTReturn (CVTStatement):
	graphNodeClass = CGReturn


	graphNode = SheetRefField( CGReturn )


	@FunctionRefField
	def _valueNode(self):
		return self._tree.buildNode( graphNode.value[0].node )

