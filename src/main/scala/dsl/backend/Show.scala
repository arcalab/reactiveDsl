package dsl.backend

//import dsl.analysis.semantics._
import dsl.analysis.syntax._
import dsl.analysis.types._


/**
  * Created by guillecledou on 2019-06-07
  */

object Show {

  def apply(te:TExp):String = te match {
    case TVar(n) => s"$n"
    case TFun(ins,outs) => apply(ins) + " -> " + apply(outs)
    case TTensor(t1,t2) => apply(t1) + " x " + apply(t2)
    case TBase(n, ps) => n + (if (ps.isEmpty) "" else ps.map(apply).mkString("<",",",">"))
    case TUnit => "()"
    case TDestr(t1) => s"destr(${apply(t1)})"
  }


  def apply(tname:TypeName):String = tname match {
    case AbsTypeName(n) => n
    case ConTypeName(n,ps) => n + (if (ps.isEmpty) "" else  ps.map(apply).mkString("<",",",">"))
  }

  def apply(variant:Constructor):String =
    variant.name + (if (variant.param.nonEmpty) variant.param.map(apply).mkString("(",",",")") else "")

  def apply(tcons:TCons):String =
    Show(tcons.l) + " = " + Show(tcons.r)

  //////////////////
  def apply(p:Program): String =
    p.types.map(apply).mkString("\n") +
      (if (p.types.nonEmpty) "\n\n" else "") +
      p.block.map(apply).mkString("\n")

  def apply(td: TypeDecl): String =
    "data " + apply(td.name) + " = " + td.constructors.map(apply).mkString(" | ")

  def apply(s: Statement)(implicit ind:Int = 0): String = fwd(ind) + (s match {
    case Assignment(variables, expr) =>
      variables.mkString(",")+" := "+apply(expr)(0)
    case FunDef(name, params, typ, block) =>
      "def "+name+"("+params.map(apply).mkString(",")+")"+
        (if (typ.isDefined) " : "+apply(typ.get) else "")+
        " = {\n"+block.map(s=>apply(s)(ind+1)+"\n").mkString+
        fwd(ind)+"}"
    case expr: StreamExpr => expr match {
      case FunctionApp(sfun, args) => apply(sfun)+"("+args.map(s=>apply(s)(0)).mkString(",")+")"
      case term: GroundTerm => term match {
        case Const(q, args) => q+(if (args.nonEmpty) "("+args.map(s=>apply(s)(0)).mkString(",")+")" else "")
        case Port(x) => x
      }
    }
  })

  def apply(tv:TypedVar): String =
    tv.name+(tv.typ match {
      case Some(t) => ":"+apply(t)
      case None => ""
    })
  def apply(fun: StreamFun): String = fun match {
    case FunName(f) => f
    case Build => "build"
    case Match => "match"
  }


  private def fwd(i: Int): String = "  "*i
}
