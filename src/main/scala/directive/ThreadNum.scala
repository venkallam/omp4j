package org.omp4j.directive

import org.antlr.v4.runtime.{ParserRuleContext, TokenStreamRewriter, Token}
import org.omp4j.Config
import org.omp4j.directive.DirectiveSchedule._
import org.omp4j.exception.SyntaxErrorException
import org.omp4j.grammar.Java8Parser
import org.omp4j.preprocessor.DirectiveVisitor
import org.omp4j.tree.{OMPClass, OMPVariable, OMPFile}

/** Thread number attribute */
class ThreadNum(override val parent: Directive)(implicit ctx: ParserRuleContext, cmt: Token, line: Int, conf: Config) extends Directive(parent, List(), List())(DirectiveSchedule.Dynamic, null, ctx, cmt, line, conf) {

	// inherit all
	override lazy val threadCount = parent.threadCount
	override lazy val contextVar = parent.contextVar
	override lazy val executor = parent.executor
	override lazy val contextClass = parent.contextClass
	override lazy val threadArr = parent.threadArr
	override lazy val iter = parent.iter
	override lazy val iter2 = parent.iter2
	override lazy val secondIter = parent.secondIter
	override lazy val exceptionName = parent.exceptionName
	override val executorClass = parent.executorClass

	override def validate(directives: DirectiveVisitor.DirectiveMap) = parent match {
		case null => throw new SyntaxErrorException(s"Error in directive before line $line: 'threadNum' must by located directly in some other OMP block.")
		case _    => ;
	}

	override def translate(implicit rewriter: TokenStreamRewriter, ompFile: OMPFile, directives: DirectiveVisitor.DirectiveMap) = {
		throw new RuntimeException(s"Internal error in directive before line $line: translate can't be run on ThreadNum!")
	}

	override protected def preTranslate(implicit rewriter: TokenStreamRewriter, ompFile: OMPFile) = {
		throw new RuntimeException(s"Internal eError in directive before line $line: preTranslate can't be run on ThreadNum!")
	}

	override protected def postTranslate(captured: Set[OMPVariable], capturedThis: Boolean, directiveClass: OMPClass)(implicit rewriter: TokenStreamRewriter) = {
		throw new RuntimeException(s"Internal error in directive before line $line: postTranslate can't be run on ThreadNum!")
	}

	def postTranslate(implicit rewriter: TokenStreamRewriter) = {
		rewriter.replace(ctx.start, ctx.stop, s"$executor.getThreadNum()")
		// do not delete cmt as it is replaced the line before
	}

}
