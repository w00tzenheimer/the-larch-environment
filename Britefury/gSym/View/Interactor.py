##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************

import gtk

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.GLisp.GLispUtil import isGLispList
from Britefury.GLisp.GLispCompiler import GLispCompilerInvalidFormType, GLispCompilerVariableNameMustStartWithAt, GLispCompilerCouldNotCompileSpecial, compileExpressionListToPyTreeStatements
from Britefury.GLisp.PyCodeGen import pyt_compare, pyt_coerce, PyCodeGenError, PyVar, PyLiteral, PyLiteralValue, PyListLiteral, PyListComprehension, PyGetAttr, PyGetItem, PyGetSlice, PyUnOp, PyBinOp, PyCall, PyMethodCall, PyIsInstance, PyReturn, PyRaise, PyTry, PyIf, PySimpleIf, PyDef, PyAssign_SideEffects, PyDel_SideEffects

from Britefury.gSym.gMeta.GMetaComponent import GMetaComponent

from Britefury.gSym.View.InteractorEvent import InteractorEvent, InteractorEventKey, InteractorEventTokenList




class InvalidKeySpecification (PyCodeGenError):
	pass

class InvalidKeySymbol (PyCodeGenError):
	pass

class InvalidKeyModifier (PyCodeGenError):
	pass

class InvalidTokenSpecifier (PyCodeGenError):
	pass

class InvalidEventType (PyCodeGenError):
	pass



class NoEventMatch (Exception):
	pass


class _EventSpec (object):
	def __init__(self, xs):
		super( _EventSpec, self ).__init__()
		self.srcXs = xs
		
	
	def _p_conditionWrap(self, outerTreeFactory, py_condition):
		return lambda innerTrees: outerTreeFactory( [ py_condition.ifTrue( innerTrees ).debug( self.srcXs ) ] )

	def _p_prependWrap(self, outerTreeFactory, py_xs):
		return lambda innerTrees: outerTreeFactory( py_xs + innerTrees )


	@abstractmethod
	def compileToPyTree(self, py_eventExpr, py_actionStatements, bindings):
		pass

	@abstractmethod
	def compileResultEvent(self, py_eventExpr):
		pass



class _KeyEventSpec (_EventSpec):
	_modTable = { 'control' : gtk.gdk.CONTROL_MASK, 
		      'ctrl' : gtk.gdk.CONTROL_MASK, 
		      'shift' : gtk.gdk.SHIFT_MASK, 
		      'alt' : gtk.gdk.MOD1_MASK, 
		      'mod1' : gtk.gdk.MOD1_MASK }
	
	def __init__(self, xs):
		"""
		( $key <key_value> <mods...> )
		"""
		super( _KeyEventSpec, self ).__init__( xs )
		assert isGLispList( xs )
		
		if len( xs ) < 2:
			raise InvalidKeySpecification( xs )
		
		try:
			self.keyValue = getattr( gtk.keysyms, xs[1] )
		except AttributeError:
			raise InvalidKeySymbol( xs )
		
		mods = [ self._modTable.get( mod )   for mod in xs[2:] ]
		mods = [ mod   for mod in mods   if mod is not None ]
		self.mods = reduce( lambda x, y: x | y,  mods,  0 )
		
		
	def compileToPyTreeFactory(self, py_eventExpr, outerTreeFactory, bindings):
		treeFac = outerTreeFactory

		# Check the event type
		treeFac = self._p_conditionWrap( treeFac, py_eventExpr.isinstance_( PyVar( '_InteractorEventKey' ) ) )
		
		# Check the key value and modifiers
		treeFac = self._p_conditionWrap( treeFac, ( py_eventExpr.attr( 'keyValue' )  ==  self.keyValue ).and_( py_eventExpr.attr( 'mods' )  ==  self.mods ) )
		
		return treeFac
	
	def compileResultEvent(self, py_eventExpr):
		return pyt_coerce( None )
		

		
		
class _TokenListEventSpec (_EventSpec):
	class TokenEventSpec (object):
		def __init__(self, xs):
			super( _TokenListEventSpec.TokenEventSpec, self ).__init__()
			
			self.srcXs = xs

			if isGLispList( xs ):
				if xs[0] != ':':
					raise InvalidTokenSpecifier( token )
				if xs[1][0] != '@':
					raise GLispCompilerVariableNameMustStartWithAt( token )
				varName = xs[1][1:]
				self.bindName = varName
				self.tokenClass = xs[2]
			else:
				self.bindName = None
				self.tokenClass = xs
				
	
			
		def _p_conditionWrap(self, outerTreeFactory, py_condition):
			return lambda innerTrees: outerTreeFactory( [ py_condition.ifTrue( innerTrees ).debug( self.srcXs ) ] )

		def compileToPyTreeFactory(self, py_eventTokenExpr, outerTreeFactory, bindings):
			if self.bindName is not None:
				bindings[self.bindName] = py_eventTokenExpr.attr( 'value' )
			return self._p_conditionWrap( outerTreeFactory, py_eventTokenExpr.attr( 'tokenClass' ) == PyLiteralValue( self.tokenClass ) )


		
	def __init__(self, xs):
		super( _TokenListEventSpec, self ).__init__( xs )
		
		self.tokenSpecs = [ self.TokenEventSpec( x )   for x in xs[1:] ]
				

	def compileToPyTreeFactory(self, py_eventExpr, outerTreeFactory, bindings):
		treeFac = outerTreeFactory
		
		# Check the event type
		treeFac = self._p_conditionWrap( treeFac, py_eventExpr.isinstance_( PyVar( '_InteractorEventTokenList' ) ) )
		
		# Check the event size
		treeFac = self._p_conditionWrap( treeFac, py_eventExpr.attr( 'tokens' ).len_()  >=  len( self.tokenSpecs ) )
		
		# Check the tokens
		for i, tokenSpec in enumerate( self.tokenSpecs ):
			treeFac = tokenSpec.compileToPyTreeFactory( py_eventExpr.attr( 'tokens' )[i], treeFac, bindings )
			
		return treeFac
		
	def compileResultEvent(self, py_eventExpr):
		return py_eventExpr.methodCall( 'tailEvent', len( self.tokenSpecs ) )
		
		

	

class Interactor (object):
	def __init__(self, onEventFunction):
		super( Interactor, self ).__init__()
		self._onEventFunction = onEventFunction
		
	
	def handleEvent(self, event):
		try:
			return self._onEventFunction( event )
		except NoEventMatch:
			return event
	



def _compileInteractorMatch(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree, py_eventExpr):
	eventSpecXs = srcXs[0]
	actionXs = srcXs[1:]
	bindings = {}
	

	# Compile the event conditions
	
	# Build the spec
	if not isGLispList( eventSpecXs ):
		raise GLispCompilerInvalidFormType( eventSpecXs )
	
	if eventSpecXs[0] == '$key':
		spec = _KeyEventSpec( eventSpecXs )
	elif eventSpecXs[0] == '$tokens':
		spec = _TokenListEventSpec( eventSpecXs )
	else:
		raise InvalidEventType( eventSpecXs )
	
	matchTreeFac = spec.compileToPyTreeFactory( py_eventExpr, lambda innerTrees: innerTrees, bindings )
		
	
	
	# Compile the action statements
	actionContext = context.innerContext()

	# Action function name
	actionFnName = actionContext.temps.allocateTempName( 'interactor_fn' )

	# Build the action tree
	actionFnContext = context.innerContext()
	# Bind variables (in alphabetical order)
	bindingPairs = bindings.items()
	bindingPairs.sort( lambda x, y: cmp( x[0], y[0] ) )
		
	# Action expression code
	py_actionStmts, py_actionResultStore = compileExpressionListToPyTreeStatements( actionXs, actionFnContext, True, compileSpecial, lambda t, xs: t.return_().debug( xs ) )
	actionFnContext.body.extend( py_actionStmts )
	
	# Make a function define
	py_actionFn = PyDef( actionFnName, [ pair[0]   for pair in bindingPairs ], actionFnContext.body, dbgSrc=srcXs )
	py_actionFnCall = PyVar( actionFnName, dbgSrc=srcXs )( *[ pair[1].debug( srcXs )   for pair in bindingPairs ] ).debug( srcXs )
	py_resultEvent = PyReturn( spec.compileResultEvent( py_eventExpr ) ).debug( srcXs )
	
	py_action = [ py_actionFn,  py_actionFnCall, py_resultEvent ]
	
	
	py_match = matchTreeFac( py_action )
	
	return py_match




def compileInteractor(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
	#
	#( $interactor <interactor_specs...> )
	#
	# interactor_spec:
	#    ( <event_spec>  <actions...> )
	#
	# event_spec:
	# key:
	#    ( $key <key_value> <mods...> )    - key event
	# tokens:
	#    ( $tokens <token_specs...> )     - token list; consumes the tokens specified in the list
	#
	# token_spec:
	#    token_class        - the token class name
	# or:
	#    (: @var token_class)     - look for a token with the specified class, and bind the value to a variable called 'var'
	assert srcXs[0] == '$interactor'
	
	# A event respone function will be built
	onEventFnName = context.temps.allocateTempName( 'interactor_onEventFn' )
	eventName = context.temps.allocateTempName( 'interactor_event' )
	
	py_interactor = []

	
	for xs in srcXs[1:]:
		py_match = _compileInteractorMatch( xs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree, PyVar( eventName ).debug( srcXs ) )
		py_interactor.extend( py_match )
		
	py_interactor.append( PyRaise( PyVar( 'NoEventMatch' ) ).debug( srcXs ) )
	
	py_interactorOnEventFn = PyDef( onEventFnName, [ eventName ], py_interactor ).debug( srcXs )
	

	interactorFactoryFnName = context.temps.allocateTempName( 'interactor_factoryFn' )
	py_interactorFactoryFn = PyDef( interactorFactoryFnName, [], [ py_interactorOnEventFn, PyVar( '_Interactor' )( PyVar( onEventFnName ) ).return_().debug( srcXs ) ] ).debug( srcXs )
	
	context.body.append( py_interactorFactoryFn )
	
	if bNeedResult:
		return PyVar( interactorFactoryFnName )().debug( srcXs )
	else:
		return None
	
	

	

class GMetaComponentInteractor (GMetaComponent):
	def compileSpecial(self, srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree):
		name = srcXs[0]
		
		if name == '$interactor':
			"""
			($interactor ...)      (see compileInteractor)
			"""
			return compileInteractor(srcXs, context, bNeedResult, compileSpecial, compileGLispExprToPyTree )
	
		raise GLispCompilerCouldNotCompileSpecial( srcXs )


	def getConstants(self):
		return {
			'_InteractorEventKey' : InteractorEventKey,
			'_InteractorEventTokenList' : InteractorEventTokenList,
			'_Interactor' : Interactor
			}
	
	

	
	
import unittest
from Britefury.DocModel.DMIO import readSX
from Britefury.GLisp.GLispUtil import gLispSrcToString
from Britefury.GLisp.GLispCompiler import _CompilationContext

class TestCase_Interactor (unittest.TestCase):
	def _compilePyTreeTest(self, py_trees, expectedValue):
		srcLines = []
		for t in py_trees:
			srcLines.extend( t.compileAsStmt() )
		result = '\n'.join( srcLines )  +  '\n'

		if isinstance( expectedValue, list ):
			expectedValue = '\n'.join( expectedValue )  +  '\n'
		if isinstance( expectedValue, str ):
			if len( expectedValue ) == 0  or  expectedValue[-1] != '\n':
				expectedValue += '\n'
			if result != expectedValue:
				e = min( len( result ), len( expectedValue ) )
				for i in xrange( e, 0, -1 ):
					if result.startswith( expectedValue[:i] ):
						print ''
						print 'First %d characters match (result/expected)'  %  ( i, )
						print result[:i+1]
						print ''
						print expectedValue[:i+1]
						print ''
						break
				print 'FULL RESULT'
				print result
			self.assert_( result ==  expectedValue )
	
	
	def _compilePyTreeFactoryTest(self, py_treeFactory, expectedValue):
		py_trees = py_treeFactory( [] )
		return self._compilePyTreeTest( py_trees, expectedValue )

			
	def test_KeyEventSpec(self):
		spec = _KeyEventSpec( readSX( '($key a ctrl shift)' ) )
		
		self.assert_( spec.keyValue == gtk.keysyms.a )
		self.assert_( spec.mods == gtk.gdk.CONTROL_MASK | gtk.gdk.SHIFT_MASK )
		
		bindings = {}
		
		
		pysrc1 = [
			'if isinstance( event, _InteractorEventKey ):',
			'  if event.keyValue == %d and event.mods == %d:'  %  ( spec.keyValue, spec.mods ),
			'    pass',
		]
		
		self._compilePyTreeFactoryTest( spec.compileToPyTreeFactory( PyVar( 'event' ), lambda innerTrees: innerTrees, bindings ), pysrc1 )
		
		self.assert_( bindings == {} )


	def test_TokenEventSpec(self):
		spec1 = _TokenListEventSpec.TokenEventSpec( readSX( 'identifier' ) )
		spec2 = _TokenListEventSpec.TokenEventSpec( readSX( '(: @x literal)' ) )
		
		self.assert_( spec1.tokenClass == 'identifier' )
		self.assert_( spec1.bindName == None )

		self.assert_( spec2.tokenClass == 'literal' )
		self.assert_( spec2.bindName == 'x' )

		bindings1 = {}
		bindings2 = {}
		
		
		pysrc1 = [
			"if event.tokenClass == 'identifier':",
			"  pass",
		]
		
		pysrc2 = [
			"if event.tokenClass == 'literal':",
			"  pass",
		]
		
		self._compilePyTreeFactoryTest( spec1.compileToPyTreeFactory( PyVar( 'event' ), lambda innerTrees: innerTrees, bindings1 ), pysrc1 )
		self._compilePyTreeFactoryTest( spec2.compileToPyTreeFactory( PyVar( 'event' ), lambda innerTrees: innerTrees, bindings2 ), pysrc2 )

		self.assert_( bindings1 == {} )
		self.assert_( pyt_compare( bindings2['x'], PyVar( 'event' ).attr( 'value' ) ) )

		
		
	def test_TokenListEventSpec(self):
		spec = _TokenListEventSpec( readSX( '($tokens identifier (: @x literal) (: @y identifier))' ) )
		
		bindings = {}
		
		pysrc1 = [
			"if isinstance( event, _InteractorEventTokenList ):",
			"  if len( event.tokens ) >= 3:",
			"    if event.tokens[0].tokenClass == 'identifier':",
			"      if event.tokens[1].tokenClass == 'literal':",
			"        if event.tokens[2].tokenClass == 'identifier':",
			"          pass",
		]
		
		self._compilePyTreeFactoryTest( spec.compileToPyTreeFactory( PyVar( 'event' ), lambda innerTrees: innerTrees, bindings ), pysrc1 )

		
	def test_compileInteractor(self):
		xssrc1 = """
		($interactor
		  ( ($key a ctrl shift)
		    (@x y @z)
		  )
		  
		  ( ($tokens (: @x identifier) (: @y literal))
		    (@a b @c)
		  )
		)
		"""

		pysrc1 = [
			"def __gsym__interactor_factoryFn_0():",
			"  def __gsym__interactor_onEventFn_0(__gsym__interactor_event_0):",
			"    if isinstance( __gsym__interactor_event_0, _InteractorEventKey ):",
			"      if __gsym__interactor_event_0.keyValue == 97 and __gsym__interactor_event_0.mods == 5:",
			"        def __gsym__interactor_fn_0():",
			"          return x.y( z )",
			"        __gsym__interactor_fn_0()",
			"        return None",
			"    if isinstance( __gsym__interactor_event_0, _InteractorEventTokenList ):",
			"      if len( __gsym__interactor_event_0.tokens ) >= 2:",
			"        if __gsym__interactor_event_0.tokens[0].tokenClass == 'identifier':",
			"          if __gsym__interactor_event_0.tokens[1].tokenClass == 'literal':",
			"            def __gsym__interactor_fn_1(x, y):",
			"              return a.b( c )",
			"            __gsym__interactor_fn_1( __gsym__interactor_event_0.tokens[0].value, __gsym__interactor_event_0.tokens[1].value )",
			"            return __gsym__interactor_event_0.tailEvent( 2 )",
			"    raise NoEventMatch",
			"  return _Interactor( __gsym__interactor_onEventFn_0 )",
			"__gsym__interactor_factoryFn_0()",
		]

		context = _CompilationContext()
		py_interactor = compileInteractor( readSX( xssrc1 ), context, True, None, None )
		
		self._compilePyTreeTest( context.body + [ py_interactor ], pysrc1 )

