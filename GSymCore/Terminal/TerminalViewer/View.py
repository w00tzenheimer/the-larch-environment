##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from java.awt.event import KeyEvent
from java.util.regex import Pattern

from javax.swing import JPopupMenu

from Britefury.Dispatch.ObjectMethodDispatch import ObjectDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectDispatch
from Britefury.gSym.gSymDocument import GSymDocument

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *


from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.Input import ObjectDndHandler

from BritefuryJ.GSym import GSymPerspective, GSymSubject
from BritefuryJ.GSym.View import GSymFragmentViewContext, PyGSymViewFragmentFunction

from GSymCore.Languages.Python25 import Python25

from GSymCore.Terminal import TerminalSchema as Schema
from GSymCore.Terminal.TerminalViewer.TerminalViewerStyleSheet import TerminalViewerStyleSheet



class CurrentModuleInteractor (ElementInteractor):
	def __init__(self, terminal):
		self._terminal = terminal
		
		
	def onKeyTyped(self, element, event):
		return False
		
		
	def onKeyPress(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_ENTER:
			if event.getModifiers() & KeyEvent.CTRL_MASK  !=  0:
				bEvaluate = event.getModifiers() & KeyEvent.SHIFT_MASK  ==  0
				self._terminal.execute( bEvaluate )
				return True
		elif event.getKeyCode() == KeyEvent.VK_UP:
			if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
				self._terminal.backwards()
				return True
		elif event.getKeyCode() == KeyEvent.VK_DOWN:
			if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
				self._terminal.forwards()
				return True
		return False
	
	def onKeyRelease(self, element, event):
		return False




class TerminalView (GSymViewObjectDispatch):
	@ObjectDispatchMethod( Schema.Terminal )
	def Terminal(self, ctx, styleSheet, state, node):
		blockViews = ctx.mapPresentFragment( node.getBlocks(), styleSheet )
		currentModuleView = ctx.presentFragmentWithPerspectiveAndStyleSheet( node.getCurrentPythonModule(), Python25.python25EditorPerspective, styleSheet['pythonStyle'] )
	
		def _onDrop(element, pos, data):
			def _onAccept(entry, text):
				node.setGlobalVar( text, data.getDocNode() )
				_finish( entry )
			
			def _onCancel(entry, text):
				_finish( entry )
				
			def _finish(entry):
				caret.moveTo( marker )
				dropPromptInsertionPoint.setChildren( [] )
			
			dropPrompt, textEntry = styleSheet.dropPrompt( _onAccept, _onCancel )
			rootElement = element.getRootElement()
			caret = rootElement.getCaret()
			marker = caret.getMarker().copy()
			dropPromptInsertionPoint.setChildren( [ dropPrompt ] )
			textEntry.grabCaret()
			textEntry.selectAll()
			rootElement.grabFocus()
			
			return True
			
		
		
		dropDest = ObjectDndHandler.DropDest( GSymFragmentViewContext.FragmentDocNode, _onDrop )
		terminalView, dropPromptInsertionPoint = styleSheet.terminal( blockViews, currentModuleView, CurrentModuleInteractor( node ), dropDest )
		return terminalView



	@ObjectDispatchMethod( Schema.TerminalBlock )
	def TerminalBlock(self, ctx, styleSheet, state, node):
		pythonModule = node.getPythonModule()
		caughtException = node.getCaughtException()
		result = node.getResult()
		
		moduleView = ctx.presentFragmentWithPerspectiveAndStyleSheet( pythonModule, Python25.python25EditorPerspective, styleSheet.staticPythonStyle() )
		caughtExceptionView = ctx.presentFragmentWithDefaultPerspective( caughtException )   if caughtException is not None   else None
		if result is not None:
			resultView = ctx.presentFragmentWithDefaultPerspective( result[0]   if result[0] is not None   else   str( None ) )
		else:
			resultView = None
		
		return styleSheet.terminalBlock( moduleView, node.getStdOut(), node.getStdErr(), caughtExceptionView, resultView )



	
	
_docNameRegex = Pattern.compile( '[a-zA-Z_][a-zA-Z0-9_]*', 0 )

class TerminalViewerPerspective (GSymPerspective):
	def __init__(self):
		self._viewFn = PyGSymViewFragmentFunction( TerminalView() )
		
	
	
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject
		else:
			return None
	
	
	def getFragmentViewFunction(self):
		return self._viewFn
	
	def getStyleSheet(self):
		return TerminalViewerStyleSheet.instance
	
	def getInitialInheritedState(self):
		return AttributeTable.instance
	
	def getEditHandler(self):
		return None
	
