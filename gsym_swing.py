##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys
import os

from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from Britefury.I18n import i18n
i18n.initialise()



from Britefury.DocModel.DMIO import readSX
from Britefury.DocModel.DMList import DMList

from Britefury.gSym.gSymEnvironment import initGSymEnvironment, shutdownGSymEnvironment

#from Britefury.MainApp.MainApp import MainApp
from Britefury.MainApp.MainAppSwing import MainApp


def main():
	initGSymEnvironment()
	

	if len( sys.argv ) == 2:
		filename = sys.argv[1]
		try:
			documentRoot = readSX( file( filename, 'r' ) )
			documentRoot = DMList( documentRoot )
			bEvaluate = True
		except IOError:
			pass

	app = MainApp( None )

	app.run()
	
	shutdownGSymEnvironment()


if __name__ == '__main__':
	main()