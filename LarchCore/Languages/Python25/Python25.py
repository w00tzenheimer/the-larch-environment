##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from LarchCore.Languages.Python25 import Schema
from LarchCore.Languages.Python25.Python25Importer import importPy25File
from LarchCore.Languages.Python25.PythonEditor.View import perspective as python25EditorPerspective
from LarchCore.Languages.Python25.PythonEditor.Subject import Python25Subject
from LarchCore.Languages.Python25.PythonEditor.Parser import Python25Grammar

from LarchCore.Project.PageData import PageData, registerPageFactory, registerPageImporter



def py25NewModule():
	return Schema.PythonModule( suite=[] )

def py25NewSuite():
	return Schema.PythonSuite( suite=[] )

def py25NewExpr():
	return Schema.PythonExpression( expr=Schema.UNPARSED( value=[ '' ] ) )

def py25NewTarget():
	return Schema.PythonTarget( target=Schema.UNPARSED( value=[ '' ] ) )



class EmbeddedPython25 (object):
	def __init__(self, model):
		self.model = model
		self.__change_history__ = None
	
	
	def __getstate__(self):
		return { 'model' : self.model }
	
	def __setstate__(self, state):
		self.model = state['model']
	
	
	def __get_trackable_contents__(self):
		return [ self.model ]
	
	
	def __present__(self, fragment, inheritedState):
		return python25EditorPerspective( self.model )


	@staticmethod
	def module():
		return EmbeddedPython25( py25NewModule() )

	@staticmethod
	def suite():
		return EmbeddedPython25( py25NewSuite() )

	@staticmethod
	def expression():
		return EmbeddedPython25( py25NewExpr() )

	@staticmethod
	def expressionFromText(text):
		parseResult = _grammar.tupleOrExpressionOrYieldExpression().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25( Schema.PythonExpression( expr=parseResult.getValue() ) )
		else:
			return EmbeddedPython25( Schema.PythonExpression( expr=Schema.UNPARSED( value = [ text ] ) ) )

	@staticmethod
	def target():
		return EmbeddedPython25( py25NewTarget() )
	
	@staticmethod
	def targetFromText(text):
		parseResult = _grammar.targetListOrTargetItem().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25( Schema.PythonTarget( target=parseResult.getValue() ) )
		else:
			return EmbeddedPython25( Schema.PythonTarget( target=Schema.UNPARSED( value = [ text ] ) ) )



_grammar = Python25Grammar()

class Python25PageData (PageData):
	def makeEmptyContents(self):
		return py25NewModule()
	
	def __new_subject__(self, document, enclosingSubject, location, title):
		return Python25Subject( document, self.contents, enclosingSubject, location, title )
	
def _py25ImportPage(filename):
	content = importPy25File( filename )
	return Python25PageData( content )	
	

registerPageFactory( 'Python 2.5', Python25PageData, 'Python' )
registerPageImporter( 'Python 2.5', 'Python 2.5 source (*.py)', 'py', _py25ImportPage )

