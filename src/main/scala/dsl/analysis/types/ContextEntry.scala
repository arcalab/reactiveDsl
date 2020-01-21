package dsl.analysis.types

import dsl.analysis.syntax.SymbolType._
import dsl.backend.PType

/**
  * Created by guillecledou on 2019-08-01
  */

sealed trait ContextEntry {
  /* type expression associated to the entry */
  val tExp:TExp
}

case class FunEntry(/*tParams:List[TExp] , dps:List[TExp],*/ tExp:TFun, funCtx:Context) extends ContextEntry

case class TypeEntry(tExp:TBase, constructors:List[ConstEntry]) extends ContextEntry

case class ConstEntry(name:String, params:List[TExp], tExp:TExp) extends ContextEntry {
  def getConst():List[TExp] = if (params.isEmpty) List(tExp) else params
}


case class PortEntry(tExp:TExp, pType:PType) extends ContextEntry {}