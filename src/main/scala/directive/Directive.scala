package org.omp4j.directive

import org.omp4j.Config

import scala.collection.mutable.{HashSet, SynchronizedSet}
import org.antlr.v4.runtime.{ParserRuleContext, Token}
import org.omp4j.directive.DirectiveSchedule._
import org.omp4j.exception._
import org.omp4j.grammar._

import scala.collection.JavaConverters._
import scala.util.Random
/** Abstract omp directive class; implemented by several case classes */
abstract class Directive(val parent: Directive, val publicVars: List[String], val privateVars: List[String])(implicit val schedule: DirectiveSchedule, val ctx: Java8Parser.StatementContext, val cmt: Token, val line: Int, conf: Config) {
	validate()

	/** Context variable name */
	lazy val contextVar     = uniqueName("ompContext")

	/** Context class name */
	lazy val contextClass   = uniqueName("OMPContext")

	/** Thread array name*/
	lazy val threadArr      = uniqueName("ompThreads")

	/** 1. iterator name */
	lazy val iter           = uniqueName("ompI")

	/** 2. iterator name */
	lazy val iter2          = uniqueName("ompJ")

	/** exception name */
	lazy val exceptionName  = uniqueName("ompE")

	/** Closest omp-parallel directive or null if none exists */
	val parentOmpParallel: Directive = parent match {
		case null => null
		case _    => parent.parentOmpParallel
	}

	// TODO: use some trait together with omptree
	private def cunit(t: ParserRuleContext = ctx): Java8Parser.CompilationUnitContext = {
		t.isInstanceOf[Java8Parser.CompilationUnitContext] match {
			case true  => t.asInstanceOf[Java8Parser.CompilationUnitContext]
			case false => cunit(t.getParent)
		}
	}

	/** Validation of parent */
	def validate(): Unit = parent match {
		case _: Sections => throw new SyntaxErrorException("In block 'omp sections' only 'omp section' blocks are allowed.")
		case _ => ;
	}

	override def toString =	s"Before line $line: ${cmt.getText}"

	/** Generate unique name (different from every token used and every loadable class) */
	def uniqueName(baseName: String): String = {

//		if (conf == null) throw new Error("null conf")

		val rand = new Random

		/** Static name generator*/
		def getName(prefix: String): String = {
//			"foo"

			if (Directive.JAVA_KEYWORDS contains prefix) {
				val suff = rand.alphanumeric.take(3).mkString("")
				getName(s"${prefix}_${suff}")
			} else {
				conf.tokenSet.testAndSet(prefix) match {
					case true => prefix
					case false =>
						val suff = rand.alphanumeric.take(3).mkString("")
						getName(s"${prefix}_${suff}")
				}
			}
		}

		/** Dynamic name generator */
		val name = getName(baseName)
//		name
		try {
			conf.loader.load(name, cunit())
			val suff = rand.alphanumeric.take(3).mkString("")
			uniqueName(s"${baseName}_${suff}")
		} catch {
			case _: ClassNotFoundException => name
		}
	}
}

/** Static directive procedures */
object Directive {

	val JAVA_KEYWORDS = List("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while")

	/** Directive constructor */
	def apply(parent: Directive, ompCtx: OMPParser.OmpUnitContext, cmt: Token, ctx: Java8Parser.StatementContext)(implicit conf: Config): Directive = {
		if (ompCtx == null) throw new SyntaxErrorException("null OMP context")

		val parallel = ompCtx.ompParallel
		val parallelFor = ompCtx.ompParallelFor
		val nonParFor = ompCtx.ompFor
		val sections = ompCtx.ompSections
		val section = ompCtx.ompSection
		val barrier = ompCtx.ompBarrier

		if (parallel != null) {
			val (pub, pri) = separate(parallel.ompModifier.asScala.toList)
			new Parallel(parent, pub, pri)(DirectiveSchedule(parallel.ompSchedule), ctx, cmt, getLine(ctx), conf)
		} else if (parallelFor != null) {
			val (pub, pri) = separate(parallelFor.ompModifier.asScala.toList)
			new ParallelFor(parent, pub, pri)(DirectiveSchedule(parallelFor.ompSchedule), ctx, cmt, getLine(ctx), conf)
		} else if (nonParFor != null) {
			val (pub, pri) = separate(nonParFor.ompModifier.asScala.toList)
			new For(parent, pub, pri)(ctx, cmt, getLine(ctx), conf)
		} else if (sections != null) {
			new Sections(parent)(DirectiveSchedule(sections.ompSchedule), ctx, cmt, getLine(ctx), conf)
		} else if (section != null) {
			new Section(parent)(ctx, cmt, getLine(ctx), conf)
		} else if (barrier != null) {
			new Barrier(parent)(ctx, cmt, getLine(ctx), conf)
		} else {
			throw new SyntaxErrorException("Invalid directive")
		}
	}

	/** Get approximate line number */
	private def getLine(ctx: ParserRuleContext) = {
		if (ctx == null || ctx.start == null) -1
		else ctx.start.getLine
	}

	/**
	  * Separate public and private variables
	  * @return tuple of (Public, Private)
	  */
	private def separate(list: List[OMPParser.OmpModifierContext]): (List[String], List[String]) = {

		/** Extracts variables from public/private statement */
		def getVars(vars: OMPParser.OmpVarsContext): List[String] = {
			if (vars == null) List()
			else {
				val v = vars.ompVar
				if (v == null) List()
				else v.asScala.map(_.VAR.getText).toList
			}

		}

		if (list == null || list.size == 0) (List(), List())
		else {
			val head = list.head
			val (resPub, resPri) = separate(list.tail)

			if (head.PUBLIC != null) (getVars(head.ompVars) ++ resPub, resPri)
			else if (head.PRIVATE != null) (resPub, getVars(head.ompVars) ++ resPri)
			else throw new ParseException("Unexpected variable modifier")
		}
	}
}