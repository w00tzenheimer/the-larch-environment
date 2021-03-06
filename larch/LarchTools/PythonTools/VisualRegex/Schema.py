##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.DocModel import DMSchema, DMObjectClass, DMNode




schema = DMSchema( 'VisualRegex', 'vre', 'LarchTools.PythonTools.VisualRegex', 1 )


Node = schema.newClass( 'Node', [] )


PythonRegEx = schema.newClass( 'PythonRegEx', Node, [ 'expr' ] )


UNPARSED = schema.newClass( 'UNPARSED', Node, [ 'value' ] )



EscapedChar = schema.newClass( 'EscapedChar', Node, [ 'char' ] )
PythonEscapedChar = schema.newClass( 'PythonEscapedChar', Node, [ 'char' ] )
LiteralChar = schema.newClass( 'LiteralChar', Node, [ 'char' ] )


AnyChar = schema.newClass( 'AnyChar', Node, [] )
StartOfLine = schema.newClass( 'StartOfLine', Node, [] )
EndOfLine = schema.newClass( 'EndOfLine', Node, [] )


CharClass = schema.newClass( 'CharClass', Node, [ 'cls' ] )


CharSet = schema.newClass( 'CharSet', Node, [ 'invert', 'items' ] )
CharSetChar = schema.newClass( 'CharSetChar', Node, [ 'char' ] )
CharSetRange = schema.newClass( 'CharSetRange', Node, [ 'min', 'max' ] )


Group = schema.newClass( 'Group', Node, [ 'subexp', 'capturing' ] )
DefineNamedGroup = schema.newClass( 'DefineNamedGroup', Node, [ 'subexp', 'name' ] )
MatchNumberedGroup = schema.newClass( 'MatchNumberedGroup', Node, [ 'number' ] )
MatchNamedGroup = schema.newClass( 'MatchNamedGroup', Node, [ 'name' ] )

Look = schema.newClass( 'Look', Node, [ 'subexp', 'positive' ] )
Lookahead = schema.newClass( 'Lookahead', Look, [] )
Lookbehind = schema.newClass( 'Lookbehind', Look, [] )

SetFlags = schema.newClass( 'SetFlags', Node, [ 'flags' ] )
Comment = schema.newClass( 'Comment', Node, [ 'text' ] )


Repetition = schema.newClass( 'Repetition', Node, [ 'subexp' ] )
RepetitionGreedyOption = schema.newClass( 'RepetitionGreedyOption', Repetition, [ 'greedy' ] )

Repeat = schema.newClass( 'Repeat', Repetition, [ 'repetitions' ] )
ZeroOrMore = schema.newClass( 'ZeroOrMore', RepetitionGreedyOption, [] )
OneOrMore = schema.newClass( 'OneOrMore', RepetitionGreedyOption, [] )
Optional = schema.newClass( 'Optional', RepetitionGreedyOption, [] )
RepeatRange = schema.newClass( 'RepeatRange', RepetitionGreedyOption, [ 'min', 'max' ] )


Sequence = schema.newClass( 'Sequence', Node, [ 'subexps' ] )
Choice = schema.newClass( 'Choice', Node, [ 'subexps' ] )

