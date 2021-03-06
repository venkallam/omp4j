package org.omp4j.tree

import org.antlr.v4.runtime._
import org.omp4j.Config
import org.omp4j.grammar._

/** The first-level class representation */
case class TopClass(ec: Java8Parser.ClassDeclarationContext, parent: OMPClass, parser: Java8Parser)(implicit conf: Config, ompFile: OMPFile) extends OMPClass(Left(ec), parent, parser) with Reflectable {
	
	/** Recursion bottom */
	override lazy val cunit: Java8Parser.CompilationUnitContext = cunit()

	/** Iterate recursively until CompilationUnitContext is found
	 *
	 * @param pt from whence to start
	 * @return Compilation unit context
	 */
	protected def cunit(pt: ParserRuleContext = ctx): Java8Parser.CompilationUnitContext = {
		try {
			val cunit = pt.asInstanceOf[Java8Parser.CompilationUnitContext]
			cunit
		} catch {
			case e: Exception => cunit(pt.getParent)
		}
	}
}
