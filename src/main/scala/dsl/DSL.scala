package dsl

import dsl.common.{ParsingException, TypeException}
import dsl.analysis.semantics.Context
import dsl.analysis.semantics._
import dsl.analysis.syntax.{AST, Parser}
import preo.DSL
import preo.ast.{Connector, CoreConnector}
import preo.frontend.{Eval, Show, Simplify}

/**
  * Created by guillecledou on 2019-06-04
  */


object DSL {

  def parse(code:String):AST = Parser.parseProgram(code) match {
    case Parser.Success(result, next) => result
    case f:Parser.NoSuccess => throw new ParsingException("Parser failed: "+f.msg)
  }

  def unify(cons:Set[TCons]):Map[TVar,TypeExpr] = Unify(cons)

  def infer(ast:AST):(Context,Map[String,TypeConn],TypeExpr,Set[TCons]) = TypeInference.infer(ast)

  def typeCheck(ast: AST):Map[String,TypeExpr] = {
    // mk type constraints
    val (ctx,tconns,t,cons) = infer(ast)
    // try to unify them
    val substitutions:Map[TVar,TypeExpr] = Substitute(unify(cons))
    // mk type of connector
    var connTypes = tconns.map(c=> c._1->c._2.getType)
    // return the type for each identifier
    ctx.get.map(e => e._1 -> substitutions(e._2))++connTypes//tconns
  }

  def unsafeCoreConnector(c:Connector):CoreConnector =
    Eval.unsafeInstantiate(c) match {
      case Some(reduc) => Eval.unsafeReduce(reduc)
      case _ => // Failed to simplify
        throw new TypeException("Failed to reduce connector: " + Show(Simplify.unsafe(c)))
  }

}
