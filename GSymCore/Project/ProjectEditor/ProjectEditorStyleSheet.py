##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color, BasicStroke

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *

from GSymCore.Utils.LinkHeader import LinkHeaderStyleSheet
from GSymCore.Utils.Title import TitleBarStyleSheet
from GSymCore.Utils.TabbedBox import TabbedBoxStyleSheet



class ProjectEditorStyleSheet (StyleSheet):
	def __init__(self):
		super( ProjectEditorStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'linkHeaderStyle', LinkHeaderStyleSheet.instance )
		self.initAttr( 'titleBarStyle', TitleBarStyleSheet.instance )
		self.initAttr( 'tabbedBoxStyle', TabbedBoxStyleSheet.instance )
		
		self.initAttr( 'projectControlsAttrs', AttributeSet( border=SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None ), hBoxSpacing=30.0 ) )
		self.initAttr( 'packageNameAttrs', AttributeSet( foreground=Color( 0.0, 0.0, 0.5 ), font=Font( 'Sans serif', Font.BOLD, 14 ) ) )
		self.initAttr( 'itemHoverHighlightAttrs', AttributeSet( hoverBackground=FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )
		
		self.initAttr( 'packageContentsIndentation', 20.0 )
	
		
	def newInstance(self):
		return ProjectEditorStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withLinkheaderStyleSheet(self, linkHeaderStyle):
		return self.withAttrs( linkHeaderStyle=linkHeaderStyle )
	
	def withTitlebarStyleSheet(self, titleBarStyle):
		return self.withAttrs( titleBarStyle=titleBarStyle )
	
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
	
	
	
	@StyleSheetDerivedPyAttrFn
	def projectControlsStyle(self):
		return self['primitiveStyle'].withAttrSet( self['projectControlsAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def packageNameStyle(self):
		return self['primitiveStyle'].withAttrSet( self['packageNameAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def itemHoverHighlightStyle(self):
		return self['primitiveStyle'].withAttrSet( self['itemHoverHighlightAttrs'] )
	
	
	
	def project(self, projectName, rootPackage, onSave, onSaveAs):
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		projectControlsStyle = self.projectControlsStyle()

		
		homeLink = controlsStyle.link( 'HOME PAGE', '' ).getElement()
		linkHeader = self['linkHeaderStyle'].linkHeaderBar( [ homeLink ] )
		
		title = self['titleBarStyle'].titleBarWithHeader( 'DOCUMENT', projectName )
		
		
		saveLink = controlsStyle.link( 'SAVE', onSave ).getElement()
		saveAsLink = controlsStyle.link( 'SAVE AS', onSaveAs ).getElement()
		controlsBox = projectControlsStyle.hbox( [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = projectControlsStyle.border( controlsBox )
		
		indexBox = self['tabbedBoxStyle'].tabbedBox( 'Project Index', rootPackage.alignHExpand() )
		
		contentBox = primitiveStyle.vbox( [ linkHeader, title, controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), indexBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()




	def package(self, packageName, packageLocation, items, packageRenameFn, app, onPageAdd, onPageImport, onPackageAdd):
		def _onRenameAccept(textEntry, text):
			packageRenameFn( text )
			
		def _onRenameCancel(textEntry, originalText):
			nameBox.setChild( nameElement )
		
		def _onRename(actionEvent):
			textEntry = controlsStyle.textEntry( packageName, _onRenameAccept, _onRenameCancel )
			nameBox.setChild( textEntry.getElement() )
			textEntry.grabCaret()
			
		def _onNewPackage(actionEvent):
			onPackageAdd()
		
		def _packageContextMenuFactory(menu):
			menu.addItem( 'New package', _onNewPackage )
			app.populateNewPageMenu( menu.addSubMenu( 'New page' ), onPageAdd )
			app.populateImportPageMenu( menu.addSubMenu( 'Import page' ), onPageImport )
			menu.addSeparator()
			menu.addItem( 'Rename', _onRename )
			return True
		
		packageNameStyle = self.packageNameStyle()
		itemHoverHighlightStyle = self.itemHoverHighlightStyle()
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		
		icon = primitiveStyle.image( 'GSymCore/Project/icons/Package.png' )
		nameElement = packageNameStyle.staticText( packageName )
		nameBox = itemHoverHighlightStyle.hbox( [ icon.padX( 5.0 ).alignVCentre(), nameElement.alignVCentre() ] )
		nameBox.addContextMenuFactory( _packageContextMenuFactory )
		
		itemsBox = primitiveStyle.vbox( items )
		
		return primitiveStyle.vbox( [ nameBox, itemsBox.padX( self['packageContentsIndentation'], 0.0 ).alignHExpand() ] )

	
	
	def page(self, pageName, pageLocation, pageRenameFn):
		def _onRenameAccept(textEntry, text):
			pageRenameFn( text )
			
		def _onRenameCancel(textEntry, originalText):
			box.setChild( link )
		
		def _onRename(actionEvent):
			textEntry = controlsStyle.textEntry( pageName, _onRenameAccept, _onRenameCancel )
			box.setChild( textEntry.getElement() )
			textEntry.grabCaret()
		
		def _pageContextMenuFactory(menu):
			menu.addItem( 'Rename', _onRename )
			return True
		
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		itemHoverHighlightStyle = self.itemHoverHighlightStyle()

		link = controlsStyle.link( pageName, pageLocation ).getElement()
		link.addContextMenuFactory( _pageContextMenuFactory )
		box = itemHoverHighlightStyle.hbox( [ link ] )

		return box
	
	

ProjectEditorStyleSheet.instance = ProjectEditorStyleSheet()
	
	
	