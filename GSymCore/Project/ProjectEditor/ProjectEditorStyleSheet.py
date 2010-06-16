##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color, BasicStroke

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location

from Britefury.AttributeTableUtils.DerivedAttributeMethod import DerivedAttributeMethod

from GSymCore.Utils.TabbedBox import TabbedBoxStyleSheet


class ProjectEditorStyleSheet (StyleSheet):
	def __init__(self):
		super( ProjectEditorStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'richTextStyle', RichTextStyleSheet.instance )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'tabbedBoxStyle', TabbedBoxStyleSheet.instance )
		
		self.initAttr( 'projectControlsAttrs', AttributeValues( border=SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None ), hBoxSpacing=30.0 ) )
		self.initAttr( 'packageNameAttrs', AttributeValues( foreground=Color( 0.0, 0.0, 0.5 ), fontBold=True, fontSize=14 ) )
		self.initAttr( 'itemHoverHighlightAttrs', AttributeValues( hoverBackground=FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )
		
		self.initAttr( 'packageContentsIndentation', 20.0 )
	
		
	def newInstance(self):
		return ProjectEditorStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withRichTextStyleSheet(self, richTextStyle):
		return self.withAttrs( richTextStyle=richTextStyle )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withTabbedBoxStyleSheet(self, tabbedBoxStyle):
		return self.withAttrs( tabbedBoxStyle=tabbedBoxStyle )
	
	
	def withProjectControlsAttrs(self, projectControlsAttrs):
		return self.withAttrs( projectControlsAttrs=projectControlsAttrs )
	
	def withPackageNameAttrs(self, packageNameAttrs):
		return self.withAttrs( packageNameAttrs=packageNameAttrs )
	
	def withItemHoverHighlightAttrs(self, itemHoverHighlightAttrs):
		return self.withAttrs( itemHoverHighlightAttrs=itemHoverHighlightAttrs )
	
	def withPackageContentsIndentation(self, packageContentsIndentation):
		return self.withAttrs( packageContentsIndentation=packageContentsIndentation )
	
	
	
	@DerivedAttributeMethod
	def projectControlsStyle(self):
		return self['primitiveStyle'].withAttrValues( self['projectControlsAttrs'] )
	
	@DerivedAttributeMethod
	def packageNameStyle(self):
		return self['primitiveStyle'].withAttrValues( self['packageNameAttrs'] )
	
	@DerivedAttributeMethod
	def itemHoverHighlightStyle(self):
		return self['primitiveStyle'].withAttrValues( self['itemHoverHighlightAttrs'] )
	
	
	
	def project(self, projectName, rootPackage, onSave, onSaveAs):
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
		controlsStyle = self['controlsStyle']
		projectControlsStyle = self.projectControlsStyle()

		
		homeLink = controlsStyle.link( 'HOME PAGE', Location( '' ) ).getElement()
		linkHeader = richTextStyle.linkHeaderBar( [ homeLink ] )
		
		title = richTextStyle.titleBarWithSubtitle( 'DOCUMENT', projectName )
		
		
		saveLink = controlsStyle.link( 'SAVE', onSave ).getElement()
		saveAsLink = controlsStyle.link( 'SAVE AS', onSaveAs ).getElement()
		controlsBox = projectControlsStyle.hbox( [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = projectControlsStyle.border( controlsBox )
		
		indexBox = self['tabbedBoxStyle'].tabbedBox( 'Project Index', rootPackage.alignHExpand() )
		
		contentBox = primitiveStyle.vbox( [ linkHeader, title, controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), indexBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()


	def renameEntry(self, name, renameEntryListener):
		controlsStyle = self['controlsStyle']
		return controlsStyle.textEntry( name, renameEntryListener )
	

	def package(self, packageName, packageLocation, items, packageContextMenuFactory):
		packageNameStyle = self.packageNameStyle()
		itemHoverHighlightStyle = self.itemHoverHighlightStyle()
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		
		icon = primitiveStyle.image( 'GSymCore/Project/icons/Package.png' )
		nameElement = packageNameStyle.staticText( packageName )
		nameBox = itemHoverHighlightStyle.hbox( [ icon.padX( 5.0 ).alignVCentre(), nameElement.alignVCentre() ] )
		nameBox.addContextMenuFactory( packageContextMenuFactory )
		
		itemsBox = primitiveStyle.vbox( items )
		
		return primitiveStyle.vbox( [ nameBox, itemsBox.padX( self['packageContentsIndentation'], 0.0 ).alignHExpand() ] ), nameBox, nameElement

	
	
	def page(self, pageName, pageLocation, pageContextMenuFactory):
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		itemHoverHighlightStyle = self.itemHoverHighlightStyle()

		link = controlsStyle.link( pageName, pageLocation ).getElement()
		link.addContextMenuFactory( pageContextMenuFactory )
		box = itemHoverHighlightStyle.hbox( [ link ] )

		return box, box, link
	
	

ProjectEditorStyleSheet.instance = ProjectEditorStyleSheet()
	
	
	