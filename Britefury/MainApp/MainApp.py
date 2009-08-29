##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys
import os

from datetime import datetime

from java.lang import Runnable
from javax.swing import AbstractAction, Action, TransferHandler, KeyStroke, BoxLayout, BorderFactory
from javax.swing import JComponent, JFrame, JMenuItem, JMenu, JMenuBar, JMenuItem, JPopupMenu, JOptionPane, JFileChooser, JOptionPane, JTextField, JLabel, JPanel
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Dimension, Font, Color, KeyboardFocusManager
from java.awt.event import WindowListener, ActionListener, ActionEvent, KeyEvent
from java.beans import PropertyChangeListener


from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from BritefuryJ.Cell import CellInterface
from BritefuryJ.Utils.Profile import ProfileTimer

from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMNode

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Browser import *
from BritefuryJ.DocPresent.StyleSheets import *

from BritefuryJ.DocView import DocView


from Britefury.Kernel.Abstract import abstractmethod


from Britefury.Event.QueuedEvent import queueEvent


from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymDocument import GSymDocument

from Britefury.Plugin import InitPlugins

from GSymCore.GSymApp import GSymApp
from GSymCore.Project import Project



_bProfile = True





class GSymScriptEnvironment (object):
	def __init__(self, app):
		self._app = app


	def _p_getApp(self):
		return self._app

	app = property( _p_getApp, doc=_( 'The gSym app (a Britefury.MainApp.MainApp)' ) )

	__doc__ = _( """gSym Scripting Environment
	GSymScriptEnvironment(app) -> scripting environment with @app as the app""" )


	
	
	
class MainAppPluginInterface (object):
	def __init__(self, app):
		self._app = app
		
		
	def registerNewPageFactory(self, menuLabel, newDocFn):
		self._app.registerNewPageFactory( menuLabel, newDocFn )
		
	def registerImporter(self, menuLabel, fileType, filePattern, importFn):
		self._app.registerImporter( menuLabel, fileType, filePattern, importFn )
		

		
		
def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )
	
		
		

		
# Transfer action listener
class _GSymTransferActionListener (ActionListener):
	def __init__(self):
		pass
		
	def actionPerformed(self, e):
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		focusOwner = manager.getPermanentFocusOwner()
		if focusOwner is not None:
			action = e.getActionCommand()
			a = focusOwner.getActionMap().get( action )
			if a is not None:
				a.actionPerformed( ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, None ) )
				
				
class _AppLocationResolver (LocationResolver):
	def __init__(self, app):
		self._app = app
		
		
	def resolveLocation(self, location):
		document = self._app._document
		if document is not None:
			return document.viewDocLocationAsPage( location, self._app )
		else:
			return None
				

class _AppLocationResolverLISP (LocationResolver):
	def __init__(self, app):
		self._app = app
		
		
	def resolveLocation(self, location):
		document = self._app._document
		if document is not None:
			return document.viewDocLocationAsLispPage( location, self._app )
		else:
			return None
				

				
class MainApp (object):
	def __init__(self, world, unit):
		self._world = world
		
		if unit is None:
			#unit = GSymApp.newAppState()
			unit = Project.newProject()
		document = GSymDocument( self._world, unit )
		
		self._document = None
		
		self._resolver = _AppLocationResolver( self )
		self._lispResolver = _AppLocationResolverLISP( self )
		
		self._browser = TabbedBrowser( self._resolver, '' )
		self._browser.getComponent().setPreferredSize( Dimension( 800, 600 ) )

		
		class _CommandHistoryListener (CommandHistoryListener):
			def onCommandHistoryChanged(_self, history):
				self._onCommandHistoryChanged( history )
		
		self._browser.setCommandHistoryListener( _CommandHistoryListener() )
		
		
		# NEW PAGE POPUP MENU
		self._newPageFactories = []
		self._pageImporters = []
		
		
		# FILE MENU
		
		fileMenu = JMenu( 'File' )
		fileMenu.add( _action( 'New project', self._onNewProject ) )
		fileMenu.add( _action( 'Open', self._onOpen ) )
		fileMenu.add( _action( 'Save', self._onSave ) )


		
		# EDIT MENU
		
		transferActionListener = _GSymTransferActionListener()
		
		editMenu = JMenu( 'Edit' )
		
		editUndoItem = JMenuItem( 'Undo' )
		undoAction = _action( 'undo', self._onUndo )
		editUndoItem.setActionCommand( undoAction.getValue( Action.NAME ) )
		editUndoItem.addActionListener( undoAction )
		editUndoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK ) )
		editUndoItem.setMnemonic( KeyEvent.VK_U )
		editMenu.add( editUndoItem )

		editRedoItem = JMenuItem( 'Redo' )
		redoAction = _action( 'redo', self._onRedo )
		editRedoItem.setActionCommand( redoAction.getValue( Action.NAME ) )
		editRedoItem.addActionListener( redoAction )
		editRedoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK ) )
		editRedoItem.setMnemonic( KeyEvent.VK_R )
		editMenu.add( editRedoItem )

		
		editMenu.addSeparator()
		
		editCutItem = JMenuItem( 'Cut' )
		editCutItem.setActionCommand( TransferHandler.getCutAction().getValue( Action.NAME ) )
		editCutItem.addActionListener( transferActionListener )
		editCutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) )
		editCutItem.setMnemonic( KeyEvent.VK_T )
		editMenu.add( editCutItem )
		
		editCopyItem = JMenuItem( 'Copy' )
		editCopyItem.setActionCommand( TransferHandler.getCopyAction().getValue( Action.NAME ) )
		editCopyItem.addActionListener( transferActionListener )
		editCopyItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) )
		editCopyItem.setMnemonic( KeyEvent.VK_C )
		editMenu.add( editCopyItem )
		
		editPasteItem = JMenuItem( 'Paste' )
		editPasteItem.setActionCommand( TransferHandler.getPasteAction().getValue( Action.NAME ) )
		editPasteItem.addActionListener( transferActionListener )
		editPasteItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) )
		editPasteItem.setMnemonic( KeyEvent.VK_P )
		editMenu.add( editPasteItem )
		

		
		
		# ACTIONS MENU
		
		self._actionsMenu = JMenu( 'Actions' )
		self._actionsMenu.add( _action( 'Transform...', self._onTransform ) )



		
		# VIEW MENU
		
		viewMenu = JMenu( 'View' )
		viewMenu.add( _action( 'Show LISP window', self._onShowLisp ) )
		viewMenu.add( _action( 'Show element tree explorer', self._onShowElementTreeExplorer ) )
		viewMenu.add( _action( 'Reset', self._onReset ) )
		viewMenu.add( _action( '1:1', self._onOneToOne ) )
		
		
		
		# SCRIPT MENU
		
		scriptMenu = JMenu( 'Script' )
		scriptMenu.add( _action( _( 'Script window' ), self._onScriptWindowMenuItem ) )
		
		
		menuBar = JMenuBar();
		menuBar.add( fileMenu )
		menuBar.add( editMenu )
		menuBar.add( self._actionsMenu )
		menuBar.add( viewMenu )
		menuBar.add( scriptMenu )

		
		
		self._initialise()
		
		
		
		# WINDOW
		
		windowPanel = JPanel()
		windowPanel.setLayout( BoxLayout( windowPanel, BoxLayout.Y_AXIS ) )
		windowPanel.add( self._browser.getComponent() )
		
		
		

		self._frame = JFrame( 'gSym' )
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		self._frame.setJMenuBar( menuBar )
		
		self._frame.add( windowPanel )
		
		self._frame.pack()
		
		
		
		
		
		#
		# LISP window
		#
		self._lispBrowser = None
		self._lispFrame = None
		self._bLispWindowVisible = False

		
		# Set the document
		self.setDocument( document )
		
		

		#
		# Plugins
		#
		self._pluginInterface = MainAppPluginInterface( self )
		InitPlugins.initPlugins( self._pluginInterface )

		
		
		#
		# Script window
		#
		scriptBanner = _( "gSym scripting console (uses pyconsole by Yevgen Muntyan)\nPython %s\nType help(object) for help on an object\nThe gSym scripting environment is available via the local variable 'gsym'\n" ) % ( sys.version, )
		self._scriptEnv = GSymScriptEnvironment( self )
		#self._scriptConsole = Console( locals = { 'gsym' : self._scriptEnv }, banner=scriptBanner, use_rlcompleter=False )
		#self._scriptConsole.connect( 'command', self._p_onScriptPreCommand )
		#self._scriptConsole.connect_after( 'command', self._p_onScriptPostCommand )
		#self._scriptConsole.show()

		#self._scriptScrolledWindow = gtk.ScrolledWindow()
		#self._scriptScrolledWindow.set_policy( gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC )
		#self._scriptScrolledWindow.add( self._scriptConsole )
		#self._scriptScrolledWindow.set_size_request( 640, 480 )
		#self._scriptScrolledWindow.show()

		#self._scriptWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
		#self._scriptWindow.set_transient_for( self._window )
		#self._scriptWindow.add( self._scriptScrolledWindow )
		#self._scriptWindow.connect( 'delete-event', self._p_onScriptWindowDelete )
		#self._scriptWindow.set_title( _( 'gSym Script Window' ) )
		self._bScriptWindowVisible = False

		

	
	def run(self):
		self._frame.setVisible( True )

		
		
		
		
	def _initialise(self):
		pass
	
	


	def setDocument(self, document):
		self._document = document
		
		#self._actionsMenu.removeAll()

		self._browser.reset( '' )
		
		self._setLispDocument()
			


	def _setLispDocument(self):
		if self._lispBrowser is not None:
			self._lispBrowser.reset( '' )

			
			
			
			
	def _onCommandHistoryChanged(self, commandHistory):
		print 'Not implemented; update date of undo and redo menu entries'

		
		
	def _onNewProject(self):
		bProceed = True
		if self._bUnsavedData:
			response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'New Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'New', 'Cancel' ], 'Cancel' )
			bProceed = response == JOptionPane.YES_OPTION
		if bProceed:
			self.setDocument( GSymDocument( Project.newProject() ) )


	def registerNewPageFactory(self, menuLabel, newUnitFn):
		self._newPageFactories.append( ( menuLabel, newUnitFn ) )

		
	def promptNewPage(self, unitReceiverFn):
		def _make_newPage(newUnitFn):
			def newPage():
				unit = newUnitFn()
				unitReceiverFn( unit )
			return newPage
		newPageMenu = JPopupMenu( 'New page' )
		for menuLabel, newUnitFn in self._newPageFactories:
			newPageMenu.add( _action( menuLabel, _make_newPage( newUnitFn ) ) )
		pos = self._frame.getMousePosition( True )
		newPageMenu.show( self._frame, pos.x, pos.y )
		
		
		
	def _onOpen(self):
		bProceed = True
		if self._bUnsavedData:
			response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'Open Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Open', 'Cancel' ], 'Cancel' )
			bProceed = response == JOptionPane.YES_OPTION
		if bProceed:
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
			response = openDialog.showOpenDialog( self._frame )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						document = GSymDocument.readFile( self._world, filename )
						if document is not None:
							self.setDocument( document )


	def _onSave(self):
		filename = None
		bFinished = False
		while not bFinished:
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
			response = openDialog.showSaveDialog( self._frame )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filenameFromDialog = sf.getPath()
					if filenameFromDialog is not None:
						if os.path.exists( filenameFromDialog ):
							response = JOptionPane.showOptionDialog( self._frame, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
							if response == JFileChooser.APPROVE_OPTION:
								filename = filenameFromDialog
								bFinished = True
							else:
								bFinished = False
						else:
							filename = filenameFromDialog
							bFinished = True
					else:
						bFinished = True
				else:
					bFinished = True
			else:
				bFinished = True

		if filename is not None:
			if self._document is not None:
				self._document.saveAs( filename )
			return True
		else:
			return False


	def promptImportPage(self, unitReceiverFn):
		def _make_importPage(fileType, filePattern, importUnitFn):
			def _import():
				openDialog = JFileChooser()
				openDialog.setFileFilter( FileNameExtensionFilter( fileType, [ filePattern ] ) )
				response = openDialog.showDialog( self._frame, 'Import' )
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
								print 'MainApp: IMPORT TIME = %s'  %  ( t2 - t1, )
								unitReceiverFn( unitName, unit )
			return _import

		importPageMenu = JPopupMenu( 'Import page' )
		for menuLabel, fileType, filePattern, importUnitFn in self._pageImporters:
			importPageMenu.add( _action( menuLabel, _make_importPage( fileType, filePattern, importUnitFn ) ) )

		pos = self._frame.getMousePosition( True )
		importPageMenu.show( self._frame, pos.x, pos.y )
			
			
	def registerImporter(self, menuLabel, fileType, filePattern, importFn):
		self._pageImporters.append( ( menuLabel, fileType, filePattern, importFn ) )

		
		
		
		
	def _onUndo(self):
		commandHistoryController = self._browser.getCommandHistoryController()
		if commandHistoryController.canUndo():
			commandHistoryController.undo()

	def _onRedo(self):
		commandHistoryController = self._browser.getCommandHistoryController()
		if commandHistoryController.canRedo():
			commandHistoryController.redo()


		

	def _onTransform(self):
		openDialog = JFileChooser()
		openDialog.setFileFilter( FileNameExtensionFilter( 'Python source (*.py)', [ 'py' ] ) )
		response = openDialog.showOpenDialog( self._frame )
		if response == JFileChooser.APPROVE_OPTION:
			sf = openDialog.getSelectedFile()
			if sf is not None:
				filename = sf.getPath()
				if filename is not None:
					env = {}
					execfile( filename, env )
					xFn = env['xform']
					
					if self._document is not None:
						transformUnit( self._document.unit, self._world, xFn )
							

					
	def _onShowLisp(self):
		self._bLispWindowVisible = not self._bLispWindowVisible
		if self._bLispWindowVisible:
			class _Listener (WindowListener):
				def windowActivated(listener, event):
					pass
				
				def windowClosed(listener, event):
					self._lispDocView = None
					self._lispFrame = None
					self._bLispWindowVisible = False
				
				def windowClosing(listener, event):
					pass
				
				def windowDeactivated(listener, event):
					pass
				
				def windowDeiconified(listener, event):
					pass
				
				def windowIconified(listener, event):
					pass
				
				def windowOpened(listener, event):
					pass
			
			
			self._lispBrowser = TabbedBrowser( self._lispResolver, '' )
			self._lispBrowser.getComponent().setPreferredSize( Dimension( 800, 600 ) )
			self._lispFrame = JFrame( 'LISP View Window' )
			self._lispFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			self._lispFrame.add( self._lispBrowser.getComponent() )
			self._lispFrame.pack()
			self._lispFrame.setVisible( True )
			self._setLispDocument()
		else:
			self._lispBrowser = None
			self._lispFrame.dispose()
			self._lispFrame = None
	
	
	def _onShowElementTreeExplorer(self):
		self._browser.createTreeExplorer()


	def _onScriptWindowMenuItem(self):
		self._bScriptWindowVisible = not self._bScriptWindowVisible
		if self._bScriptWindowVisible:
			self._scriptWindow.show()
		else:
			self._scriptWindow.hide()


	def _onReset(self):
		self._browser.viewportReset()
		if self._lispBrowser is not None:
			self._lispBrowser.viewportReset()

	def _onOneToOne(self):
		self._browser.viewportOneToOne()
		if self._lispBrowser is not None:
			self._lispBrowser.viewportOneToOne()
			
			
			

		

		
	#def _p_onScriptPreCommand(self, console, code):
		#self._document._commandHistory.freeze()

	#def _p_onScriptPostCommand(self, console, code):
		#self._document._commandHistory.thaw()




	


