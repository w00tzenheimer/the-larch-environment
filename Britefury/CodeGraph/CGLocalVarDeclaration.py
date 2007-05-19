##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGStatement import CGStatement
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *
from Britefury.LowLevelCodeTree.LLCTBindExp import LLCTBindExp



class CGLocalVarDeclaration (CGStatement):
	value = SheetGraphSinkSingleField( 'Value', 'Value' )
	variable = SheetGraphSinkSingleField( 'Variable', 'The variable' )


	def generateLLCT(self, tree):
		assert len( self.variable ) > 0
		if len( self.value ) > 0:
			return LLCTBindExp( self.variable[0].node.generateLLCT( tree ), self.value[0].node.generateLLCT( tree ) )
		else:
			return LLCTBindExp( self.variable[0].node.generateLLCT( tree ), None )


	def buildReferenceableNodeTable(self, nodeTable):
		nodeTable[self.variable[0].node.name] = self.variable[0].node

