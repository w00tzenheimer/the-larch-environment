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

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTBreak import CVTBreak

from Britefury.CodeView.CVStatement import *

from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class CVBreak (CVStatement):
	treeNodeClass = CVTBreak


	treeNode = SheetRefField( CVTBreak )


	@FunctionField
	def refreshCell(self):
		pass



	def __init__(self, treeNode, view):
		super( CVBreak, self ).__init__( treeNode, view )
		self.widget.child = DTLabel( markup=_( 'B<span size="small">REAK</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) )



	def startEditing(self):
		self.makeCurrent()
