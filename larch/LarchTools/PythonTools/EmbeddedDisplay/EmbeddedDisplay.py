##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from copy import deepcopy

from BritefuryJ.Command import Command

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import MenuItem

from BritefuryJ.Pres.Primitive import Column, Paragraph
from BritefuryJ.Pres.ObjectPres import ObjectBox

from LarchCore.Languages.Python2.PythonCommands import pythonCommandSet, EmbeddedExpressionAtCaretAction, WrapSelectionInEmbeddedExpressionAction, chainActions
from LarchCore.Languages.Python2.Embedded import EmbeddedPython2Expr



class EmbeddedDisplay (object):
	def __init__(self):
		self._expr = EmbeddedPython2Expr()
		self._code = None
		self._values = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		
	def __getstate__(self):
		return { 'expr' : self._expr }
	
	def __setstate__(self, state):
		self._expr = state['expr']
		self._code = None
		self._values = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
	
	
	def __get_trackable_contents__(self):
		return [ self._expr ]
		
		
	def __py_compile_visit__(self, codeGen):
		self._code = codeGen.compileForEvaluation( self._expr.model )
	
	def __py_eval__(self, _globals, _locals, codeGen):
		value = eval( self._code, _globals, _locals )
		self._values.append( value )
		self._incr.onChanged()
		return value
	
	def __py_replacement__(self):
		return deepcopy( self._expr.model['expr'] )
		
	
	def __present__(self, fragment, inheritedState):
		def _embeddedDisplayMenu(element, menu):
			def _onClear(item):
				del self._values[:]
				self._incr.onChanged()
			
			menu.add( MenuItem.menuItemWithLabel( 'Clear collected values', _onClear ) )
			
			return False
				
		
		
		self._incr.onAccess()
		#exprPres = pyPerspective.applyTo( self._expr )
		exprPres = self._expr
		
		valuesPres = ObjectBox( 'Values', Column( [ Paragraph( [ value ] )   for value in self._values ] ) )
		
		contents = Column( [ exprPres, valuesPres ] )
		return ObjectBox( 'Embedded display', contents ).withContextMenuInteractor( _embeddedDisplayMenu )



	
@EmbeddedExpressionAtCaretAction
def _newEmbeddedDisplayAtCaret(caret):
	return EmbeddedDisplay()

@WrapSelectionInEmbeddedExpressionAction
def _newEmbeddedDisplayAtSelection(expr, selection):
	d = EmbeddedDisplay()
	d._expr.model['expr'] = deepcopy( expr )
	return d


_edCommand = Command( '&Embedded &Display', chainActions( _newEmbeddedDisplayAtSelection, _newEmbeddedDisplayAtCaret ) )

pythonCommandSet( 'LarchTools.PythonTools.EmbeddedDisplay', [ _edCommand ] )
