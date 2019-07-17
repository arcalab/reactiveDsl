package dsl

import dsl.common.{ParsingException, TypeException}
import dsl.analysis.semantics.Context
import dsl.analysis.semantics._
import dsl.analysis.syntax.ast.Program
import dsl.analysis.syntax.{AST, Parser,NewParser}
import dsl.backend.Prettify
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

  def newParse(code:String):Program = NewParser.parseProgram(code) match {
    case NewParser.Success(result, _) => result
    case f:NewParser.NoSuccess => throw new ParsingException("Parser failed: "+f.msg)
  }

  def unify(cons:Set[TCons]):Map[TVar,TypeExpr] = Unify(cons)

  def infer(ast:AST):(Context,Map[String,TypeConn],TypeExpr,Set[TCons]) = TypeInference.infer(ast)

  def typeCheck(ast: AST):Map[String,TypeExpr] = {
    // mk type constraints
    val (ctx,tconns,t,cons) = infer(ast)
    // try to unify them
    val substitutions:Map[TVar,TypeExpr] = Substitute(unify(cons))
    // mk type of connector
    val connTypes = tconns.map(c=> c._1->c._2.getType)
    // for each typed identifier, get its typed and prettify it in case it has free type variables
    val rawIdTypes = ctx.get.map(e => e._1 -> substitutions(e._2))
    var idTypes = Map[String,TypeExpr]()
    for((id,t) <- rawIdTypes) {
      Prettify.reset()
      idTypes += id -> Prettify(t)
    }
    // return the type for each identifier and for each connector definition
    idTypes++connTypes
  }

  def unsafeCoreConnector(c:Connector):CoreConnector =
    Eval.unsafeInstantiate(c) match {
      case Some(reduc) => Eval.unsafeReduce(reduc)
      case _ => // Failed to simplify
        throw new TypeException("Failed to reduce connector: " + Show(Simplify.unsafe(c)))
  }

}
