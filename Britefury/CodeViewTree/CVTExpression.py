##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGExpression import CGExpression
from Britefury.CodeGraph.CGSendMessage import CGSendMessage

from Britefury.CodeViewTree.CVTStatement import CVTStatement



class CVTExpression (CVTStatement):
	graphNodeClass = CGExpression


	graphNode = SheetRefField( CGExpression )


	def wrapInSendMessage(self):
		parentCGSink = self.graphNode.parent[0]
		sendCG = CGSendMessage()
		parentCGSink.splitLinkWithNode( self.graphNode.parent, sendCG.targetObject, sendCG.parent )
		return self._tree.buildNode( sendCG )

