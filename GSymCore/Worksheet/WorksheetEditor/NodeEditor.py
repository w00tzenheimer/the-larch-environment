##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from weakref import WeakValueDictionary

from java.util import List
from java.awt.event import KeyEvent

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface


from BritefuryJ.DocPresent import *


from BritefuryJ.Logging import LogEntry


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations


from GSymCore.Languages.Python25 import Python25

from GSymCore.Worksheet import Schema, ViewSchema



class _ListenerTable (object):
	def __init__(self, createFn):
		self._table = WeakValueDictionary()
		self._createFn = createFn
	
		
	def get(self, *args):
		key = args
		try:
			return self._table[key]
		except KeyError:
			listener = self._createFn( *args )
			self._table[key] = listener
			return listener
		
	
	
class TitleTreeEventListener (TreeEventListener):
	def __init__(self):
		pass

	def onTreeEvent(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		node.setTitle( value )
		return True
		
	
	
titleTreeEventListener = TitleTreeEventListener()
	

class EmptyTreeEventListener (TreeEventListener):
	def __init__(self):
		pass

	def onTreeEvent(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		if '\n' not in value:
			node.appendContentsNode( Schema.Paragraph( text=value, style='normal' ) )
			return True
		else:
			return False

		
emptyTreeEventListener = EmptyTreeEventListener()




class TextTreeEventListener (TreeEventListener):
	def __init__(self):
		pass

	def onTreeEvent(self, element, sourceElement, event):
		value = element.getTextRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		if '\n' not in value:
			node.setText( value )
			return True
		else:
			return False
		
		
textTreeEventListener = TextTreeEventListener()



class OperationTreeEventListener (TreeEventListener):
	def __init__(self):
		pass

	def onTreeEvent(self, element, sourceElement, event):
		return event.apply( element.getFragmentContext().getDocNode() )
	
operationTreeEventListener = OperationTreeEventListener()
	


class InsertPythonCodeEvent (object):
	def __init__(self, node):
		super( InsertPythonCodeEvent, self ).__init__()
		self._node = node
		
class OperationEvent (object):
	def apply(self, node):
		return False

class TextOperationEvent (OperationEvent):
	pass

class ParagraphStyleEvent(TextOperationEvent):
	def __init__(self, style):
		self._style = style
		
	def apply(self, node):
		node.setStyle( self._style )
		return True

	
class TextInteractor (ElementInteractor):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		return False
		
		
	def onKeyPress(self, element, event):
		if event.getModifiers() & KeyEvent.ALT_MASK  !=  0:
			ctx = element.getFragmentContext()
			node = ctx.getDocNode()

			if event.getKeyCode() == KeyEvent.VK_N:
				node.setStyle( 'normal' )
			elif event.getKeyCode() == KeyEvent.VK_1:
				node.setStyle( 'h1' )
			elif event.getKeyCode() == KeyEvent.VK_2:
				node.setStyle( 'h2' )
			elif event.getKeyCode() == KeyEvent.VK_3:
				node.setStyle( 'h3' )
			elif event.getKeyCode() == KeyEvent.VK_4:
				node.setStyle( 'h4' )
			elif event.getKeyCode() == KeyEvent.VK_5:
				node.setStyle( 'h5' )
			elif event.getKeyCode() == KeyEvent.VK_6:
				node.setStyle( 'h6' )
			elif event.getKeyCode() == KeyEvent.VK_C:
				self._insertPythonCode( ctx, element, node )
				return True
			else:
				return False
			
			return True
			
		return False
	
	def onKeyRelease(self, element, event):
		return False



	def _insertPythonCode(self, ctx, element, node):
		return element.postTreeEvent( InsertPythonCodeEvent( node.getModel() ) )
		
		
class WorksheetTreeEventListener (TreeEventListener):
	def __init__(self):
		pass

	def onTreeEvent(self, element, sourceElement, event):
		if isinstance( event, InsertPythonCodeEvent ):
			ctx = element.getFragmentContext()
			node = ctx.getDocNode().getModel()
			index = node['contents'].indexOf( event._node )
			
			if index != -1:
				pythonCode = Schema.PythonCode( showCode='True', codeEditable='False', showResult='True', code=Python25.py25NewModule() )
				node['contents'].insert( index+1, pythonCode )
				return True
		return False
	
	
worksheetTreeEventListener = WorksheetTreeEventListener()


class WorksheetInteractor (ElementInteractor):
	def __init__(self):
		pass
		
		
	def onKeyPress(self, element, event):
		if event.getKeyCode() == KeyEvent.VK_F5:
			ctx = element.getFragmentContext()
			node = ctx.getDocNode()
			node.refreshResults()
			return True
		
		
