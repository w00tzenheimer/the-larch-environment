##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGIntLiteral import CGIntLiteral
from Britefury.CodeGraph.CGFloatLiteral import CGFloatLiteral

from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CodeViewTree import *



class CVTIntLiteral (CVTExpression):
	graphNode = SheetRefField( CGIntLiteral )

	strValue = FieldProxy( graphNode.strValue )



	def convertToFloat(self):
		floatCG = CGFloatLiteral()
		self.graph.nodes.append( floatCG )

		floatCG.strValue = self.strValue

		self.graphNode.parent[0].replace( self.graphNode.parent, floatCG.parent )

		self.graphNode.destroySubtree()

		return self._tree.buildNode( floatCG )



class CVTRuleIntLiteral (CVTRuleSimple):
	graphNodeClass = CGIntLiteral
	cvtNodeClass = CVTIntLiteral

CVTRuleIntLiteral.register()

