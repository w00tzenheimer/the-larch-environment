##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Math.Math import Colour3f

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTStringLiteral import CVTStringLiteral

from Britefury.CodeView.CVExpression import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection
from Britefury.DocView.CellEdit.DVCStringCellEditEntryLabel import DVCStringCellEditEntryLabel



class CVStringLiteral (CVExpression):
	treeNodeClass = CVTStringLiteral


	treeNode = SheetRefField( CVTStringLiteral )


	@FunctionRefField
	def stringValueWidget(self):
		entry = DVCStringCellEditEntryLabel()
		entry.entry.textColour=Colour3f( 0.0, 0.0, 0.75 )
		entry.keyHandler = self
		entry.attachCell( self.treeNode.cells.stringValue )
		return entry.entry





	@FunctionField
	def refreshCell(self):
		self._box[1] = self.stringValueWidget




	@CVCharInputHandlerMethod( '\'' )
	def _finishEditingString(self, receivingNodePath, entry, event):
		self.stringValueWidget.finishEditing()
		self.widget.grabFocus()
		return True


	def __init__(self, treeNode, view):
		super( CVStringLiteral, self ).__init__( treeNode, view )
		self._box = DTBox()
		self._box.append( DTLabel( '\'' ) )
		self._box.append( DTLabel( 'nil', colour=Colour3f( 0.0, 0.0, 0.5 ) ) )
		self._box.append( DTLabel( '\'' ) )
		self.widget.child = self._box
