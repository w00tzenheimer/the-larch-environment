##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************

from BritefuryJ.LSpace.LSSpaceBin import SizeConstraint

from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank
from BritefuryJ.Pres.UI import Section, SectionHeading1, SectionHeading2, SectionHeading3, ControlsRow

from BritefuryJ.Controls import ScrolledViewport, ResizeableBin

from LarchTools.PythonTools.GUIEditor.ComponentPalette import createPalette


#
#
#Control editor:
def _createTree():
	heading = SectionHeading1('Tree')
	return Section(heading, Blank())

def _createBottomPane():
	return _createTree()

#
#
#Side panel:

def _createSidePanel():
	palette = createPalette()
	bottomPane = _createBottomPane()
	palette = ScrolledViewport(palette.alignVTop().alignHPack(), 150.0, 150.0, SizeConstraint.LARGER, None).alignHExpand().alignVExpand()
	bottomPane = ScrolledViewport(bottomPane.alignVTop().alignHPack(), 150.0, 150.0, SizeConstraint.LARGER, None).alignHExpand().alignVExpand()
	bottomPane = ResizeableBin(bottomPane).resizeTop(350.0)
	return Column([palette.alignHExpand().alignVExpand(), bottomPane.alignHExpand().alignVExpand()])

def showSidePanel(sourceElement):
	pm = sourceElement.rootElement.paneManager
	pm.rightEdgePane.setContent(_createSidePanel(), None, 250.0)

def hideSidePanel(sourceElement):
	pm = sourceElement.rootElement.paneManager
	pm.rightEdgePane.clearContent()
