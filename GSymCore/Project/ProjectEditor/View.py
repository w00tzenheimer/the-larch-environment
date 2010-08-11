##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
import sys
import imp


from datetime import datetime

from java.lang import Object

from java.awt import Color
from java.awt import BasicStroke

from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *

from BritefuryJ.Cell import LiteralCell

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent import ElementInteractor
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.StyleSheet import StyleSheet
from BritefuryJ.DocPresent.Input import ObjectDndHandler
from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.GSym.PresCom import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject


from GSymCore.GSymApp import DocumentManagement

from GSymCore.Project import Schema
#from GSymCore.Project.ProjectEditor.ProjectEditorStyleSheet import ProjectEditorStyleSheet



# handleNewPageFn(unit)
def _newPageMenu(world, handleNewPageFn):
	def _make_newPage(newPageFn):
		def newPage(menuItem):
			unit = newPageFn()
			handleNewPageFn( unit )
		return newPage
	items = []
	for newPageFactory in world.newPageFactories:
		items.append( MenuItem.menuItemWithLabel( newPageFactory.menuLabelText, _make_newPage( newPageFactory.newPageFn ) ) )
	return VPopupMenu( items )
	
	
	
# handleImportedPageFn(name, unit)
def _importPageMenu(world, component, handleImportedPageFn):
	def _make_importPage(fileType, filePattern, importUnitFn):
		def _import(actionEvent):
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( fileType, [ filePattern ] ) )
			response = openDialog.showDialog( component, 'Import' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						t1 = datetime.now()
						unit = importUnitFn( filename )
						t2 = datetime.now()
						if unit is not None:
							unitName = os.path.splitext( filename )[0]
							unitName = os.path.split( unitName )[1]
							print 'ProjectEditor.View: IMPORT TIME = %s'  %  ( t2 - t1, )
							handleImportedPageFn( unitName, unit )
		return _import

	items = []
	for pageImporter in world.pageImporters:
		items.append( MenuItem.menuItemWithLabel( pageImporter.menuLabelText, _make_importPage( pageImporter.fileType, pageImporter.filePattern, pageImporter.importFn ) ) )
	return VPopupMenu( items )




def _ProjectViewState(location):
	return ( location, )




def _joinLocation(*xs):
	s = '.'.join( [ str( x )   for x in xs ] )
	return Location( s )



class ProjectDrag (Object):
	def __init__(self, source):
		self.source = source
		

def _dragSourceCreateSourceData(element, aspect):
	return ProjectDrag( element.getFragmentContext().getModel() )


_dragSource = ObjectDndHandler.DragSource( ProjectDrag, ObjectDndHandler.ASPECT_NORMAL, _dragSourceCreateSourceData )



def _isChildOf(node, package):
	if node is package:
		return True
	
	if package.isInstanceOf( Schema.Package ):
		for child in package['contents']:
			if _isChildOf( node, child ):
				return True
	return False


def _pageCanDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		model = element.getFragmentContext().getModel()
		return model is not data.source
	return False

def _pageDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPage = element.getFragmentContext().getModel()
		parent = destPage.getValidParents()[0]
		index = parent.indexOfById( destPage )
		if targetPos.y > ( element.getHeight() * 0.5 ):
			index += 1
		
		source = data.source.deepCopy()   if action == ObjectDndHandler.COPY   else data.source
		
		if action == ObjectDndHandler.MOVE:
			sourceParent = data.source.getValidParents()[0]
			indexOfSource = sourceParent.indexOfById( data.source )
			del sourceParent[indexOfSource]
			if parent is sourceParent  and  index > indexOfSource:
				index -= 1

		parent.insert( index, source )
		return True
	return False
			


def _getDestPackageAndIndex(element, targetPos):
	targetPackage = element.getFragmentContext().getModel()
	if targetPos.x > ( element.getWidth() * 0.5 ):
		return targetPackage, len( targetPackage['contents'] )
	else:
		parent1 = targetPackage.getValidParents()[0]
		parent2 = parent1.getValidParents()[0]
		index = parent1.indexOfById( targetPackage )
		if targetPos.y > ( element.getHeight() * 0.5 ):
			index += 1
		return parent2, index
	


def _packageCanDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.COPY  or  not _isChildOf( data.source, destPackage ):
			return True
	return False

def _packageDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		destPackage, index = _getDestPackageAndIndex( element, targetPos )
		if action == ObjectDndHandler.MOVE  and _isChildOf( data.source, destPackage ):
			return False
		
		
		
		#targetPackage = element.getFragmentContext().getModel()
		#if targetPos.x > ( element.getWidth() * 0.5 ):
			#pass
		#else:
			#parent = targetPackage.getValidParents()[0]
			#index = parent.indexOfById( targetPackage )
			#if targetPos.y > ( element.getHeight() * 0.5 ):
				#index += 1
			
			#source = data.source.deepCopy()   if action == ObjectDndHandler.COPY   else data.source
			
			#if action == ObjectDndHandler.MOVE:
				#sourceParent = data.source.getValidParents()[0]
				#indexOfSource = sourceParent.indexOfById( data.source )
				#del sourceParent[indexOfSource]
				#if parent is sourceParent  and  index > indexOfSource:
					#index -= 1

		#parent.insert( index, source )
		#return True
	return False


def _projectIndexDrop(element, targetPos, data, action):
	if action & ObjectDndHandler.COPY_OR_MOVE  !=  0:
		return False
		
		#targetPackage = element.getFragmentContext().getModel()
		#if targetPos.x > ( element.getWidth() * 0.5 ):
			#pass
		#else:
			#parent = targetPackage.getValidParents()[0]
			#index = parent.indexOfById( targetPackage )
			#if targetPos.y > ( element.getHeight() * 0.5 ):
				#index += 1
			
			#source = data.source.deepCopy()   if action == ObjectDndHandler.COPY   else data.source
			
			#if action == ObjectDndHandler.MOVE:
				#sourceParent = data.source.getValidParents()[0]
				#indexOfSource = sourceParent.indexOfById( data.source )
				#del sourceParent[indexOfSource]
				#if parent is sourceParent  and  index > indexOfSource:
					#index -= 1

		#parent.insert( index, source )
		#return True
	return False


_pageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _pageCanDrop, _pageDrop )
_packageDropDest = ObjectDndHandler.DropDest( ProjectDrag, _packageCanDrop, _packageDrop )
_projectIndexDropDest = ObjectDndHandler.DropDest( ProjectDrag, _projectIndexDrop )




_controlsStyle = StyleSheet.instance.withAttr( Controls.bClosePopupOnActivate, True )
_projectControlsStyle = StyleSheet.instance.withAttr( Primitive.border, SolidBorder( 2.0, 2.0, Color( 131, 149, 172 ), None ) ).withAttr( Primitive.hboxSpacing, 30.0 )
_projectIndexNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.25, 0.5 ) ).withAttr( Primitive.fontBold, True ).withAttr( Primitive.fontSize, 14 )
_packageNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) ).withAttr( Primitive.fontBold, True ).withAttr( Primitive.fontSize, 14 )
_itemHoverHighlightStyle = StyleSheet.instance.withAttr( Primitive.hoverBackground, FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) )
_pythonPackageNameStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.0, 0.0, 0.5 ) )
_pythonPackageNameNotSetStyle = StyleSheet.instance.withAttr( Primitive.foreground, Color( 0.5, 0.0, 0.0 ) )

_packageContentsIndentation = 20.0


_nameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*' )
_pythonPackageNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*' )





class ProjectView (GSymViewObjectNodeDispatch):
	@DMObjectNodeDispatchMethod( Schema.Project )
	def Project(self, ctx, state, node, pythonPackageName, contents):
		# Save and Save As
		def _onSave(link, buttonEvent):
			if document._filename is None:
				def handleSaveDocumentAsFn(filename):
					document.saveAs( filename )
				
				DocumentManagement.promptSaveDocumentAs( ctx.getSubjectContext()['world'], link.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )
			else:
				document.save()
				
		
		def _onSaveAs(link, buttonEvent):
			def handleSaveDocumentAsFn(filename):
				document.saveAs( filename )
			
			DocumentManagement.promptSaveDocumentAs( ctx.getSubjectContext()['world'], link.getElement().getRootElement().getComponent(), handleSaveDocumentAsFn )
		
		
			
			
			

		# Python package name
		class _PythonPackageNameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				node['pythonPackageName'] = text
				
			def onCancel(self, textEntry, originalText):
				pythonPackageCell.setLiteralValue( pythonPackageNameLabel )
		

		class _PythonPackageNameInteractor (ElementInteractor):
			def onButtonClicked(self, element, event):
				if event.getButton() == 1:
					n = pythonPackageName   if pythonPackageName is not None   else 'Untitled'
					textEntry = TextEntry( n, _PythonPackageNameListener(), _pythonPackageNameRegex, 'Please enter a valid dotted identifier' )
					textEntry.grabCaretOnRealise()
					pythonPackageCell.setLiteralValue( textEntry )
					return True
				else:
					return False
			
			
		# Project index
		def _addPackage(menuItem):
			contents.append( Schema.Package( name='NewPackage', contents=[] ) )

		def _addPage(pageUnit):
			p = Schema.Page( name='NewPage', unit=pageUnit )
			contents.append( p )
		
		def _importPage(name, pageUnit):
			contents.append( Schema.Page( name=name, unit=pageUnit ) )

		def _projectIndexContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'New package', _addPackage ) )
			newPageMenu = _newPageMenu( world, _addPage )
			importPageMenu = _importPageMenu( world, element.getRootElement().getComponent(), _importPage )
			menu.add( MenuItem.menuItemWithLabel( 'New page', newPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( MenuItem.menuItemWithLabel( 'Import page', importPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			return True

		
		
		# Get some initial variables
		subjectContext = ctx.getSubjectContext()
		document = subjectContext['document']
		location = subjectContext['location']
		
		name = document.getDocumentName()
		
		# Set location attribute of state
		state = state.withAttrs( location=location )

		# Link to home page, in link header bar
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		linkHeader = LinkHeaderBar( [ homeLink ] )
		
		# Title
		title = TitleBarWithSubtitle( 'DOCUMENT', name )
		
		
		# Controls for 'save' and 'save as'
		saveLink = Hyperlink( 'SAVE', _onSave )
		saveAsLink = Hyperlink( 'SAVE AS', _onSaveAs )
		controlsBox = HBox( [ saveLink.padX( 10.0 ), saveAsLink.padX( 10.0 ) ] )
		controlsBorder = _projectControlsStyle.applyTo( Border( controlsBox ) )
		
		
		
		# Python package name
		pythonPackageNamePrompt = Label( 'Root Python package name: ' )
		if pythonPackageName is None:
			pythonPackageNameLabel = _itemHoverHighlightStyle.applyTo( _pythonPackageNameNotSetStyle.applyTo( Label( '<not set>' ) ) )
		else:
			pythonPackageNameLabel = _itemHoverHighlightStyle.applyTo( _pythonPackageNameStyle.applyTo( Label( pythonPackageName ) ) )
		pythonPackageNameLabel = pythonPackageNameLabel.withInteractor( _PythonPackageNameInteractor() )
		pythonPackageCell = LiteralCell( pythonPackageNameLabel )
		pythonPackageNameBox = HBox( [ pythonPackageNamePrompt, pythonPackageCell.genericPerspectiveValuePresInFragment() ] )
		
		
		# Project index
		indexHeader = Heading3( 'Project Index' )
		
		
		# Project contents
		items = InnerFragment.map( contents, state )
		
		world = ctx.getSubjectContext()['world']
		
		nameElement = _projectIndexNameStyle.applyTo( StaticText( 'Project' ) )
		nameBox = _itemHoverHighlightStyle.applyTo( nameElement.alignVCentre() )
		nameBox = nameBox.withContextMenuFactory( _projectIndexContextMenuFactory )
		nameBox = nameBox.withDropDest( _projectIndexDropDest )
		
		itemsBox = VBox( items )
		
		contentsView = VBox( [ nameBox, itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )
		
		
		# Project index box
		projectIndex = VBox( [ indexHeader, contentsView.alignHExpand() ] )
		
		
		# The page
		head = Head( [ linkHeader, title ] )
		body = Body( [ controlsBorder.pad( 5.0, 10.0 ).alignHLeft(), pythonPackageNameBox, projectIndex ] )
		
		return StyleSheet.instance.withAttr( Primitive.editable, False ).applyTo( Page( [ head, body ] ) )


	@DMObjectNodeDispatchMethod( Schema.Package )
	def Package(self, ctx, state, node, name, contents):
		def _addPackage(menuItem):
			contents.append( Schema.Package( name='NewPackage', contents=[] ) )

		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				node['name'] = text
				
			def onCancel(self, textEntry, originalText):
				nameCell.setLiteralValue( nameBox )
		
		def _onRename(menuItem):
			textEntry = TextEntry( name, _RenameListener(), _nameRegex, 'Please enter a valid identifier' )
			textEntry.grabCaretOnRealise()
			nameCell.setLiteralValue( textEntry )
			
		def _addPage(pageUnit):
			p = Schema.Page( name='NewPage', unit=pageUnit )
			contents.append( p )
		
		def _importPage(name, pageUnit):
			contents.append( Schema.Page( name=name, unit=pageUnit ) )

		def _packageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'New package', _addPackage ) )
			newPageMenu = _newPageMenu( world, _addPage )
			importPageMenu = _importPageMenu( world, element.getRootElement().getComponent(), _importPage )
			menu.add( MenuItem.menuItemWithLabel( 'New page', newPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( MenuItem.menuItemWithLabel( 'Import page', importPageMenu, MenuItem.SubmenuPopupDirection.RIGHT ) )
			menu.add( HSeparator() )
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			return True

		location = state['location']
		packageLocation = _joinLocation( location, name )
		
		items = InnerFragment.map( contents, state.withAttrs( location=packageLocation ) )
		
		world = ctx.getSubjectContext()['world']
		
		icon = Image( 'GSymCore/Project/icons/Package.png' )
		nameElement = _packageNameStyle.applyTo( StaticText( name ) )
		nameBox = _itemHoverHighlightStyle.applyTo( HBox( [ icon.padX( 5.0 ).alignVCentre(), nameElement.alignVCentre() ]  ) )
		nameBox = nameBox.withContextMenuFactory( _packageContextMenuFactory )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _packageDropDest )
		
		nameCell = LiteralCell( nameBox )
		
		itemsBox = VBox( items )
		
		return VBox( [ nameCell.genericPerspectiveValuePresInFragment(), itemsBox.padX( _packageContentsIndentation, 0.0 ).alignHExpand() ] )
		
		

	@DMObjectNodeDispatchMethod( Schema.Page )
	def Page(self, ctx, state, node, name, unit):
		class _RenameListener (TextEntry.TextEntryListener):
			def onAccept(self, textEntry, text):
				node['name'] = text
				
			def onCancel(self, textEntry, originalText):
				nameCell.setLiteralValue( nameBox )

		def _onRename(menuItem):
			textEntry = TextEntry( name, _RenameListener(), _nameRegex, 'Please enter a valid identifier' )
			textEntry.grabCaretOnRealise()
			nameCell.setLiteralValue( textEntry )
			
		def _pageContextMenuFactory(element, menu):
			menu.add( MenuItem.menuItemWithLabel( 'Rename', _onRename ) )
			return True
			
		
		location = state['location']
		pageLocation = _joinLocation( location, name )

		link = Hyperlink( name, pageLocation )
		link = link.withContextMenuFactory( _pageContextMenuFactory )
		nameBox = _itemHoverHighlightStyle.applyTo( HBox( [ link ] ) )
		nameBox = nameBox.withDragSource( _dragSource )
		nameBox = nameBox.withDropDest( _pageDropDest )
		
		nameCell = LiteralCell( nameBox )
		
		return nameCell.genericPerspectiveValuePresInFragment()
	


	
	

perspective = GSymPerspective( ProjectView(), None )


class PackageSubject (object):
	def __init__(self, projectSubject, model, location):
		self._projectSubject = projectSubject
		self._model = model
		self._location = location
		
		
	def __getattr__(self, name):
		for item in self._model['contents']:
			if name == item['name']:
				itemLocation = self._location + '.' + name
				if item.isInstanceOf( Schema.Package ):
					return PackageSubject( self._projectSubject, item, itemLocation )
				elif item.isInstanceOf( Schema.Page ):
					return self._projectSubject._document.newUnitSubject( item['unit'], self._projectSubject, itemLocation )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )
			
	                            


class _ModuleFinder (object):
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject
	
	def find_module(self, fullname, namesuffix, path):
		suffix = namesuffix
		model = self._projectSubject._model
		modelLocation = self._projectSubject._location
		while suffix != '':
			prefix, dot, suffix = suffix.partition( '.' )
			for item in model['contents']:
				if prefix == item['name']:
					modelLocation = modelLocation + '.' + prefix
					model = item
					
					if model.isInstanceOf( Schema.Page ):
						if suffix == '':
							# We have found a page: load it
							pageSubject = self._projectSubject._document.newUnitSubject( model['unit'], self._projectSubject, modelLocation )
							try:
								l = pageSubject.load_module
							except AttributeError:
								return None
							else:
								return pageSubject
						else:
							# Still path to consume; loop over
							break
					elif model.isInstanceOf( Schema.Package ):
						# Loop over
						break
					else:
						raise TypeError, 'unreckognised model type'
			return None
		
		# Ran out of name to comsume, load as a package
		return self
		
	def load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		return mod


class _RootFinder (object):
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject
	
	def find_module(self, fullname, path):
		pythonPackageName = self._projectSubject._model['pythonPackageName']
		if pythonPackageName is not None:
			pythonPackageName = pythonPackageName.split( '.' )
			suffix = fullname
			index = 0
			while suffix != '':
				prefix, dot, suffix = suffix.partition( '.' )
				if prefix == pythonPackageName[index]:
					index += 1
					if index == len( pythonPackageName )  and  suffix != '':
						return self._projectSubject._moduleFinder.find_module( fullname, suffix, path )
				else:
					return None
		return self
		
	def load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		return mod

	

class ProjectSubject (GSymSubject):
	def __init__(self, document, model, enclosingSubject, location):
		self._document = document
		self._model = model
		self._enclosingSubject = enclosingSubject
		self._location = location
		self._moduleFinder = _ModuleFinder( self )
		self._rootFinder = _RootFinder( self )


	def getFocus(self):
		return self._model
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Project'
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( document=self._document, documentLocation=Location( self._location ), location=Location( self._location ) )
	
	def getCommandHistory(self):
		return self._document.getCommandHistory()
	
	
	
	def __getattr__(self, name):
		for item in self._model['contents']:
			if name == item['name']:
				itemLocation = self._location + '.' + name
				if item.isInstanceOf( Schema.Package ):
					return PackageSubject( self, item, itemLocation )
				elif item.isInstanceOf( Schema.Page ):
					return self._document.newUnitSubject( item['unit'], self, itemLocation )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )
	
	
	
	def find_module(self, fullname, path=None):
		return self._rootFinder.find_module( fullname, path )

	