##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os
import sys

import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.extlibs.pyconsole.pyconsole import Console

from Britefury.Math.Math import Colour3f

from Britefury.Event.QueuedEvent import queueEvent

from Britefury.UI.ConfirmDialog import *
from Britefury.UI.ConfirmOverwriteFileDialog import confirmOverwriteFileDialog

from Britefury.FileIO.IOXml import *

from Britefury.CommandHistory.CommandHistory import CommandHistory

from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel

from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMIO import readSX, writeSX

from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymEnvironment import GSymEnvironment
from Britefury.gSym.gSymDocument import loadDocument, newDocument, GSymDocumentViewContentHandler, GSymDocumentLISPViewContentHandler

from Britefury.DocView.DocView import DocView

from Britefury.Plugin import InitPlugins

#from Britefury.PyImport import PythonImporter




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
		
		
	def registerNewDocumentFactory(self, menuLabel, newDocFn):
		self._app.registerNewDocumentFactory( menuLabel, newDocFn )
		
	def registerImporter(self, menuLabel, fileType, filePattern, importFn):
		self._app.registerImporter( menuLabel, fileType, filePattern, importFn )


		

class MainApp (object):
	class _Output (object):
		def __init__(self, textBuffer, tagName, backupOut):
			self._textBuffer = textBuffer
			self._tagName = tagName
			self._backupOut = backupOut

		def write(self, text):
			pos = self._textBuffer.get_iter_at_mark( self._textBuffer.get_insert() )
			self._textBuffer.insert_with_tags_by_name( pos, text, self._tagName )


	def __init__(self, documentRoot, bEvaluate):
		self._documentRoot = None
		self._view = None
		self._commandHistory = None
		self._bUnsavedData = False
		
		self._world = GSymWorld()

		self._doc = DTDocument()
		self._doc.undoSignal.connect( self._p_onUndo )
		self._doc.redoSignal.connect( self._p_onRedo )
		self._doc.getGtkWidget().show()


		resetButton = gtk.Button( 'Reset' )
		resetButton.show()
		resetButton.connect( 'clicked', self._p_onReset )

		oneToOneButton = gtk.Button( '1:1' )
		oneToOneButton.show()
		oneToOneButton.connect( 'clicked', self._p_onOneToOne )


		buttonBox = gtk.HBox( spacing=20 )
		buttonBox.pack_end( oneToOneButton, False, False, 0 )
		buttonBox.pack_end( resetButton, False, False, 0 )
		buttonBox.show()
		
		
		
		
		# FILE -> NEW MENU
		
		newEmptyItem = gtk.MenuItem( 'Empty' )
		newEmptyItem.connect( 'activate', self._p_onNewEmpty )

		self._newMenu = gtk.Menu()
		self._newMenu.append( newEmptyItem )
		
		
		
		
		# FILE -> IMPORT MENU
		
		self._importMenu = gtk.Menu()


		
		
		# FILE MENU
		
		newItem = gtk.MenuItem( 'New' )
		newItem.set_submenu( self._newMenu )

		openItem = gtk.MenuItem( 'Open' )
		openItem.connect( 'activate', self._p_onOpen )

		saveItem = gtk.MenuItem( 'Save' )
		saveItem.connect( 'activate', self._p_onSave )

		importItem = gtk.MenuItem( 'Import' )
		importItem.set_submenu( self._importMenu )

		exportTeXItem = gtk.MenuItem( 'Export TeX document' )
		exportTeXItem.connect( 'activate', self._p_onExportTeX )
		exportTeXItem.set_sensitive( False )


		fileMenu = gtk.Menu()
		fileMenu.append( newItem )
		fileMenu.append( openItem )
		fileMenu.append( saveItem )
		fileMenu.append( importItem )
		fileMenu.append( exportTeXItem )


		

		# EDIT MENU
		
		undoItem = gtk.MenuItem( 'Undo' )
		undoItem.connect( 'activate', self._p_onUndo )

		redoItem = gtk.MenuItem( 'Redo' )
		redoItem.connect( 'activate', self._p_onRedo )

		editMenu = gtk.Menu()
		editMenu.append( undoItem )
		editMenu.append( redoItem )


		
		
		# EXECUTE MENU
		
		executeItem = gtk.MenuItem( 'Execute' )
		executeItem.connect( 'activate', self._p_onExecute )
		executeItem.set_sensitive( False )

		showCodeItem = gtk.MenuItem( 'Show code' )
		showCodeItem.connect( 'activate', self._p_onShowCode )
		showCodeItem.set_sensitive( False )

		runMenu = gtk.Menu()
		runMenu.append( executeItem )
		runMenu.append( showCodeItem )



		
		# ACTIONS MENU
		
		self._actionsMenu = gtk.Menu()



		
		# VIEW MENU
		
		viewLispItem = gtk.MenuItem( 'Show LISP window' )
		viewLispItem.connect( 'activate', self._onShowLisp )
		
		viewMenu = gtk.Menu()
		viewMenu.append( viewLispItem )
		
		
		
		
		# SCRIPT MENU
		
		scriptWindowItem = gtk.MenuItem( _( 'Script window' ) )
		scriptWindowItem.connect( 'activate', self._p_onScriptWindowMenuItem )


		scriptMenu = gtk.Menu()
		scriptMenu.append( scriptWindowItem )



		fileMenuItem = gtk.MenuItem( 'File' )
		fileMenuItem.set_submenu( fileMenu )

		editMenuItem = gtk.MenuItem( 'Edit' )
		editMenuItem.set_submenu( editMenu )

		runMenuItem = gtk.MenuItem( 'Run' )
		runMenuItem.set_submenu( runMenu )
		
		self._actionsMenuItem = gtk.MenuItem( 'Actions' )
		self._actionsMenuItem.set_submenu( self._actionsMenu )
		
		viewMenuItem = gtk.MenuItem( 'View' )
		viewMenuItem.set_submenu( viewMenu )

		scriptMenuItem = gtk.MenuItem( _( 'Script' ) )
		scriptMenuItem.set_submenu( scriptMenu )


		menuBar = gtk.MenuBar()
		menuBar.append( fileMenuItem )
		menuBar.append( editMenuItem )
		menuBar.append( runMenuItem )
		menuBar.append( self._actionsMenuItem )
		menuBar.append( viewMenuItem )
		menuBar.append( scriptMenuItem )
		menuBar.show_all()




		box = gtk.VBox()
		box.pack_start( menuBar, False, False )
		box.pack_start( self._doc.getGtkWidget() )
		box.pack_start( gtk.HSeparator(), False, False, 10 )
		box.pack_start( buttonBox, False, False, 10 )
		box.show_all()
		
		
		self._p_initialise()


		self._window = gtk.Window( gtk.WINDOW_TOPLEVEL );
		self._window.connect( 'delete-event', self._p_onDeleteEvent )
		self._window.connect( 'destroy', self._p_onDestroy )
		self._window.set_border_width( 10 )
		self._window.set_size_request( 640, 480 )
		self._window.add( box )
		self._window.show()



		#
		# LISP window
		#
		self._lispWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
		self._lispDoc =DTDocument()
		self._lispDoc.undoSignal.connect( self._p_onUndo )
		self._lispDoc.redoSignal.connect( self._p_onRedo )
		self._lispDoc.getGtkWidget().show()
		self._lispWindow.set_transient_for( self._window )
		self._lispWindow.connect( 'delete-event', self._p_onLispWindowDelete )
		self._lispWindow.set_title( _( 'LISP View Window' ) )
		self._lispWindow.set_size_request( 640, 480 )
		self._lispWindow.add( self._lispDoc.getGtkWidget() )
		self._bLispWindowVisible = False
		self._lispView = None

		
		# Set the document
		self.setDocument( documentRoot, bEvaluate )


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
		self._scriptConsole = Console( locals = { 'gsym' : self._scriptEnv }, banner=scriptBanner, use_rlcompleter=False )
		self._scriptConsole.connect( 'command', self._p_onScriptPreCommand )
		self._scriptConsole.connect_after( 'command', self._p_onScriptPostCommand )
		self._scriptConsole.show()

		self._scriptScrolledWindow = gtk.ScrolledWindow()
		self._scriptScrolledWindow.set_policy( gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC )
		self._scriptScrolledWindow.add( self._scriptConsole )
		self._scriptScrolledWindow.set_size_request( 640, 480 )
		self._scriptScrolledWindow.show()

		self._scriptWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
		self._scriptWindow.set_transient_for( self._window )
		self._scriptWindow.add( self._scriptScrolledWindow )
		self._scriptWindow.connect( 'delete-event', self._p_onScriptWindowDelete )
		self._scriptWindow.set_title( _( 'gSym Script Window' ) )
		self._bScriptWindowVisible = False
		
		
		
		
		





	def _p_initialise(self):
		pass
		


	def setDocument(self, documentRoot, bEvaluate):
		self._documentRoot = documentRoot

		self._commandHistory = CommandHistory()
		self._commandHistory.track( self._documentRoot )
		self._commandHistory.changedSignal.connect( self._p_onCommandHistoryChanged )
		self._bUnsavedData = False
		
	
		self._actionsMenu = gtk.Menu()
		self._actionsMenuItem.set_submenu( self._actionsMenu )
		
		if bEvaluate:
			contentHandler = GSymDocumentViewContentHandler( self._commandHistory )
			self._view = loadDocument( self._world, documentRoot, contentHandler )
			self._view.refreshCell.changedSignal.connect( self._p_queueRefresh )
			self._view.refresh()
			self._doc.child = self._view.rootView.widget
			self._view.setDocument( self._doc )
			
			lispContentHandler = GSymDocumentLISPViewContentHandler( self._commandHistory )
			self._lispView = loadDocument( self._world, documentRoot, lispContentHandler )
			self._lispView.refreshCell.changedSignal.connect( self._p_queueLispRefresh )
			self._lispView.refresh()
			self._lispDoc.child = self._lispView.rootView.widget
			self._lispView.setDocument( self._lispDoc )
		else:
			self._view = None
			self._doc.child = DTLabel( '<empty>', font='Sans 11 bold', colour=Colour3f( 0.0, 0.0, 0.5 ) )
			self._lispDoc.child = DTLabel( '<empty>', font='Sans 11 bold', colour=Colour3f( 0.0, 0.0, 0.5 ) )
	





	def _p_refreshView(self):
		if self._view is not None:
			self._view.refresh()

	def _p_refreshLispView(self):
		if self._view is not None:
			self._lispView.refresh()


	def _p_queueRefresh(self):
		queueEvent( self._p_refreshView )

	def _p_queueLispRefresh(self):
		queueEvent( self._p_refreshLispView )



	def _p_executeCode(self, source):
		textView = gtk.TextView()
		textBuffer = textView.get_buffer()
		textView.set_wrap_mode( gtk.WRAP_WORD )
		textView.set_editable( False )
		textView.set_cursor_visible( True )
		textView.show()

		stdoutTag = textBuffer.create_tag( 'stdout', foreground="#006000")
		stderrTag = textBuffer.create_tag( 'stderr', foreground="#006000")

		scrolledWindow = gtk.ScrolledWindow()
		scrolledWindow.set_policy( gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC )
		scrolledWindow.add( textView )
		scrolledWindow.set_size_request( 640, 480 )
		scrolledWindow.show()

		textViewWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
		textViewWindow.set_transient_for( self._window )
		textViewWindow.add( scrolledWindow )
		textViewWindow.set_title( 'Output' )

		textViewWindow.show()

		savedStdout, savedStderr = sys.stdout, sys.stderr
		sys.stdout = self._Output( textBuffer, 'stdout', savedStdout )
		sys.stderr = self._Output( textBuffer, 'stderr', savedStderr )
		exec source in {}
		sys.stdout, sys.stderr = savedStdout, savedStderr




	def _p_makeSourceWindow(self, text):
		textView = gtk.TextView()
		textView.get_buffer().set_text( text )
		textView.set_wrap_mode( gtk.WRAP_WORD )
		textView.set_editable( False )
		textView.set_cursor_visible( False )
		textView.show()

		scrolledWindow = gtk.ScrolledWindow()
		scrolledWindow.set_policy( gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC )
		scrolledWindow.add( textView )
		scrolledWindow.set_size_request( 640, 480 )
		scrolledWindow.show()

		textViewWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
		textViewWindow.set_transient_for( self._window )
		textViewWindow.add( scrolledWindow )
		textViewWindow.set_title( 'Python source' )

		textViewWindow.show()




	def _p_onReset(self, widget):
		self._doc.reset()

	def _p_onOneToOne(self, widget):
		self._doc.oneToOne()





	def _p_onCommandHistoryChanged(self, commandHistory):
		self._bUnsavedData = True




	def _p_onNewEmpty(self, widget):
		bProceed = True
		if self._bUnsavedData:
			bProceed = confirmDialog( _( 'New project' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
		if bProceed:
			documentRoot = self.makeEmptyDocument()
			self.setDocument( documentRoot, False )


	def registerNewDocumentFactory(self, menuLabel, newDocFn):
		def _onNew(widget):
			bProceed = True
			if self._bUnsavedData:
				bProceed = confirmDialog( _( 'New' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
			if bProceed:
				content = newDocFn()
				if content is not None:
					documentRoot = newDocument( content )
					self.setDocument( documentRoot, True )
		menuItem = gtk.MenuItem( menuLabel )
		menuItem.connect( 'activate', _onNew )
		menuItem.show()
		self._newMenu.append( menuItem )
		

	def _p_onOpen(self, widget):
		bProceed = True
		if self._bUnsavedData:
			bProceed = confirmDialog( _( 'New project' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
		if bProceed:
			gsymFilter = gtk.FileFilter()
			gsymFilter.set_name( _( 'gSym project (*.gsym)' ) )
			gsymFilter.add_pattern( '*.gsym' )

			openDialog = gtk.FileChooserDialog( _( 'Open' ), self._window, gtk.FILE_CHOOSER_ACTION_OPEN,
											( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
			openDialog.add_filter( gsymFilter )
			openDialog.show()
			response = openDialog.run()
			filename = openDialog.get_filename()
			openDialog.destroy()
			if response == gtk.RESPONSE_OK:
				if filename is not None:
					f = open( filename, 'r' )
					if f is not None:
						try:
							documentRoot = readSX( f )
							self.setDocument( documentRoot, True )
						except IOError:
							pass


	def _p_onSave(self, widget):
		filename = None
		bFinished = False
		while not bFinished:
			gsymFilter = gtk.FileFilter()
			gsymFilter.set_name( _( 'gSym project (*.gsym)' ) )
			gsymFilter.add_pattern( '*.gsym' )

			saveAsDialog = gtk.FileChooserDialog( _( 'Save As' ), self._window, gtk.FILE_CHOOSER_ACTION_SAVE,
										( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
			saveAsDialog.add_filter( gsymFilter )
			saveAsDialog.show()
			response = saveAsDialog.run()
			filenameFromDialog = saveAsDialog.get_filename()
			saveAsDialog.destroy()
			if response == gtk.RESPONSE_OK:
				if filenameFromDialog is not None:
					if os.path.exists( filenameFromDialog ):
						if confirmOverwriteFileDialog( filenameFromDialog, self._window ):
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

		if filename is not None:
			self._p_writeFile( filename )
			return True
		else:
			return False


	def registerImporter(self, menuLabel, fileType, filePattern, importFn):
		def _onImport(widget):
			bProceed = True
			if self._bUnsavedData:
				bProceed = confirmDialog( _( 'Import' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
			if bProceed:
				filter = gtk.FileFilter()
				filter.set_name( fileType )
				filter.add_pattern( filePattern )
	
				openDialog = gtk.FileChooserDialog( _( 'Import' ), self._window, gtk.FILE_CHOOSER_ACTION_OPEN,
												( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
				openDialog.add_filter( filter )
				openDialog.show()
				response = openDialog.run()
				filename = openDialog.get_filename()
				openDialog.destroy()
				if response == gtk.RESPONSE_OK:
					if filename is not None:
						content = importFn( filename )
						if content is not None:
							documentRoot = newDocument( content )
							self.setDocument( documentRoot, True )
		menuItem = gtk.MenuItem( menuLabel )
		menuItem.connect( 'activate', _onImport )
		menuItem.show()
		self._importMenu.append( menuItem )
		

	def _p_onImportPy(self, widget):
		pass
		#bProceed = True
		#if self._bUnsavedData:
			#bProceed = confirmDialog( _( 'New project' ), _( 'You have not saved your work. Proceed?' ), gtk.STOCK_NEW, gtk.STOCK_CANCEL, 'y', 'n', True, self._window )
		#if bProceed:
			#pyFilter = gtk.FileFilter()
			#pyFilter.set_name( _( 'Python source (*.py)' ) )
			#pyFilter.add_pattern( '*.py' )

			#openDialog = gtk.FileChooserDialog( _( 'Import' ), self._window, gtk.FILE_CHOOSER_ACTION_OPEN,
											#( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
			#openDialog.add_filter( pyFilter )
			#openDialog.show()
			#response = openDialog.run()
			#filename = openDialog.get_filename()
			#openDialog.destroy()
			#if response == gtk.RESPONSE_OK:
				#if filename is not None:
					#f = open( filename, 'r' )
					#if f is not None:
						#graph, root = self.importPythonSource( f.read() )
						#if graph is not None  and  root is not None:
							#self.setGraph( graph, root )



	def _p_onExportTeX(self, widget):
		pass
		#filename = None
		#bFinished = False
		#while not bFinished:
			#texFilter = gtk.FileFilter()
			#texFilter.set_name( _( 'TeX document (*.tex)' ) )
			#texFilter.add_pattern( '*.tex' )

			#saveAsDialog = gtk.FileChooserDialog( _( 'Export TeX' ), self._window, gtk.FILE_CHOOSER_ACTION_SAVE,
										#( gtk.STOCK_CANCEL, gtk.RESPONSE_CANCEL, gtk.STOCK_OK, gtk.RESPONSE_OK ) )
			#saveAsDialog.add_filter( texFilter )
			#saveAsDialog.show()
			#response = saveAsDialog.run()
			#filenameFromDialog = saveAsDialog.get_filename()
			#saveAsDialog.destroy()
			#if response == gtk.RESPONSE_OK:
				#if filenameFromDialog is not None:
					#if os.path.exists( filenameFromDialog ):
						#if confirmOverwriteFileDialog( filenameFromDialog, self._window ):
							#filename = filenameFromDialog
							#bFinished = True
						#else:
							#bFinished = False
					#else:
						#filename = filenameFromDialog
						#bFinished = True
				#else:
					#bFinished = True
			#else:
				#bFinished = True

		#if filename is not None:
			#self._p_exportTeX( filename )
			#return True
		#else:
			#return False


	def _p_writeFile(self, filename):
		f = open( filename, 'w' )
		if f is not None:
			writeSX( f, self._documentRoot )
			f.close()
			self._bUnsavedData = False


	#def _p_exportTeX(self, filename):
		#open( filename, 'w' ).write( self._graphRoot.generateTex().asText() )


	def _p_onUndo(self, sender):
		if self._commandHistory.canUndo():
			self._commandHistory.undo()

	def _p_onRedo(self, sender):
		if self._commandHistory.canRedo():
			self._commandHistory.redo()



	def _p_onExecute(self, widget):
		pyCodeBlock = self._graphRoot.generatePyCodeBlock()
		text = pyCodeBlock.asText()
		self._p_executeCode( text )


	def _p_onShowCode(self, widget):
		pyCodeBlock = self._graphRoot.generatePyCodeBlock()
		text = pyCodeBlock.asText()
		self._p_makeSourceWindow( text )




	def _p_onDeleteEvent(self, widget, event, data=None):
		return False

	def _p_onDestroy(self, widget, data=None):
		gtk.main_quit()

		
		
		
	def _onShowLisp(self, widget):
		self._bLispWindowVisible = not self._bLispWindowVisible
		if self._bLispWindowVisible:
			self._lispWindow.show()
		else:
			self._lispWindow.hide()



	def _p_onLispWindowDelete(self, window, event):
		window.hide()
		self._bLispWindowVisible = False
		return True


	
	def _p_onScriptWindowDelete(self, window, event):
		window.hide()
		self._bScriptWindowVisible = False
		return True

	def _p_onScriptWindowMenuItem(self, widget):
		self._bScriptWindowVisible = not self._bScriptWindowVisible
		if self._bScriptWindowVisible:
			self._scriptWindow.show()
		else:
			self._scriptWindow.hide()


	def _p_onScriptPreCommand(self, console, code):
		self._commandHistory.freeze()

	def _p_onScriptPostCommand(self, console, code):
		self._commandHistory.thaw()




	
	
	@staticmethod
	def makeEmptyDocument():
		return DMList()


	@staticmethod
	def importPythonSource(source):
		return PythonImporter.importPythonSource( source )

