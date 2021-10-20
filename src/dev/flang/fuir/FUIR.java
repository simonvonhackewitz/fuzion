/*

This file is part of the Fuzion language implementation.

The Fuzion language implementation is free software: you can redistribute it
and/or modify it under the terms of the GNU General Public License as published
by the Free Software Foundation, version 3 of the License.

The Fuzion language implementation is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public
License for more details.

You should have received a copy of the GNU General Public License along with The
Fuzion language implementation.  If not, see <https://www.gnu.org/licenses/>.

*/

/*-----------------------------------------------------------------------
 *
 * Tokiwa Software GmbH, Germany
 *
 * Source of class FUIR
 *
 *---------------------------------------------------------------------*/

package dev.flang.fuir;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

import java.util.TreeMap;
import java.util.TreeSet;

import dev.flang.ast.Assign; // NYI: remove dependency
import dev.flang.ast.Block; // NYI: remove dependency
import dev.flang.ast.BoolConst; // NYI: remove dependency
import dev.flang.ast.Box; // NYI: remove dependency
import dev.flang.ast.Call; // NYI: remove dependency
import dev.flang.ast.Check; // NYI: remove dependency
import dev.flang.ast.Current; // NYI: remove dependency
import dev.flang.ast.Expr; // NYI: remove dependency
import dev.flang.ast.Feature; // NYI: remove dependency
import dev.flang.ast.If; // NYI: remove dependency
import dev.flang.ast.Impl; // NYI: remove dependency
import dev.flang.ast.InlineArray; // NYI: remove dependency
import dev.flang.ast.NumLiteral; // NYI: remove dependency
import dev.flang.ast.Match; // NYI: remove dependency
import dev.flang.ast.Nop; // NYI: remove dependency
import dev.flang.ast.Stmnt; // NYI: remove dependency
import dev.flang.ast.StrConst; // NYI: remove dependency
import dev.flang.ast.Tag; // NYI: remove dependency
import dev.flang.ast.Types; // NYI: remove dependency
import dev.flang.ast.Unbox; // NYI: remove dependency
import dev.flang.ast.Universe; // NYI: remove dependency

import dev.flang.ir.Clazz;
import dev.flang.ir.Clazzes;

import dev.flang.util.ANY;
import dev.flang.util.Errors;
import dev.flang.util.List;
import dev.flang.util.Map2Int;
import dev.flang.util.MapComparable2Int;
import dev.flang.util.SourcePosition;


/**
 * The FUIR contains the intermediate representation of fuzion applications.
 *
 * @author Fridtjof Siebert (siebert@tokiwa.software)
 */
public class FUIR extends ANY
{


  /*----------------------------  constants  ----------------------------*/


  private static final int CLAZZ_BASE   = 0x10000000;

  private static final int CODE_BASE = 0x30000000;

  public enum ClazzKind
  {
    Routine,
    Field,
    Intrinsic,
    Abstract,
    Choice
  }

  public enum ExprKind
  {
    AdrOf,
    Assign,
    Box,
    Unbox,
    Call,
    Current,
    Comment,
    Const,
    Dup,
    Match,
    Outer,
    Tag,
    Pop,
  }

  public enum ContractKind
  {
    Pre,
    Post;

    /**
     * String representation for debugging.
     */
    public String toString()
    {
      switch (this)
        {
        case Pre : return "pre-condition";
        case Post: return "post-condition";
        default: throw new Error("unhandled switch case");
        }
    }
  }


  /**
   * Map used by getSpecialId() to quickly find the SpecialClazz corresponding
   * to a Clazz.
   */
  private static TreeMap<Clazz, SpecialClazzes> SPECIAL_ID = new TreeMap<>();


  /**
   * Enum of clazzes that require special handling in the backend
   */
  public enum SpecialClazzes
  {
    c_i8          { Clazz getIfCreated() { return Clazzes.i8         .getIfCreated(); } },
    c_i16         { Clazz getIfCreated() { return Clazzes.i16        .getIfCreated(); } },
    c_i32         { Clazz getIfCreated() { return Clazzes.i32        .getIfCreated(); } },
    c_i64         { Clazz getIfCreated() { return Clazzes.i64        .getIfCreated(); } },
    c_u8          { Clazz getIfCreated() { return Clazzes.u8         .getIfCreated(); } },
    c_u16         { Clazz getIfCreated() { return Clazzes.u16        .getIfCreated(); } },
    c_u32         { Clazz getIfCreated() { return Clazzes.u32        .getIfCreated(); } },
    c_u64         { Clazz getIfCreated() { return Clazzes.u64        .getIfCreated(); } },
    c_bool        { Clazz getIfCreated() { return Clazzes.bool       .getIfCreated(); } },
    c_TRUE        { Clazz getIfCreated() { return Clazzes.c_TRUE     .getIfCreated(); } },
    c_FALSE       { Clazz getIfCreated() { return Clazzes.c_FALSE    .getIfCreated(); } },
    c_conststring { Clazz getIfCreated() { return Clazzes.conststring.getIfCreated(); } },

    // dummy entry to report failure of getSpecialId()
    c_NOT_FOUND   { Clazz getIfCreated() { return null;                               } };

    abstract Clazz getIfCreated();

    SpecialClazzes()
    {
      var c = getIfCreated();
      if (c != null)
        {
          SPECIAL_ID.put(c, this);
        }
    }
  }


  /*----------------------------  variables  ----------------------------*/


  /**
   * The main clazz.
   */
  final Clazz _main;


  final Map2Int<Clazz> _clazzIds = new MapComparable2Int(CLAZZ_BASE);
  final Map2Int<List<Object>> _codeIds = new Map2Int(CODE_BASE);


  /*--------------------------  constructors  ---------------------------*/


  public FUIR(Clazz main)
  {
    _main = main;
    Clazzes.findAllClasses(main());
  }


  /*-----------------------------  methods  -----------------------------*/


  public Clazz main()
  {
    return _main;
  }


  /*------------------------  accessing classes  ------------------------*/


  private void addClasses()
  {
    if (_clazzIds.size() == 0)
      {
        for (var cl : Clazzes.all())
          {
            if (cl._type != Types.t_ADDRESS     // NYI: would be better to not create this dummy clazz in the first place
                )
              {
                _clazzIds.add(cl);
              }
          }
      }
  }

  public int firstClazz()
  {
    addClasses();
    return CLAZZ_BASE;
  }

  public int lastClazz()
  {
    addClasses();
    return CLAZZ_BASE + _clazzIds.size() - 1;
  }

  public int mainClazzId()
  {
    addClasses();
    return _clazzIds.get(_main);
  }

  public int clazzNumFields(int cl)
  {
    return _clazzIds.get(cl).fields().length;
  }


  /**
   * Return the field #i in the given clazz
   *
   * @param cl a clazz id
   *
   * @param i the field number
   *
   * @return the clazz id of the field
   */
  public int clazzField(int cl, int i)
  {
    if (PRECONDITIONS)
      require
        (i >= 0 && i < clazzNumFields(cl));

    var cc = _clazzIds.get(cl);
    var fc = cc.fields()[i];
    return _clazzIds.get(fc);
  }


  /**
   * For a choice type, the number of entries to choose from.
   *
   * @param cl a clazz id
   *
   * @return -1 if cl is not a choice clazz, the number of choice entries
   * otherwise.  May be 0 for the void choice.
   */
  public int clazzNumChoices(int cl)
  {
    var cc = _clazzIds.get(cl);
    return cc.isChoice() ? cc.choiceGenerics().size() : -1;
  }


  /**
   * Return the choice #i in the given choice clazz
   *
   * @param cl a clazz id
   *
   * @param i the choice number
   *
   * @return the clazz id of the choice type, or -1 if the clazz is never
   * instantiated and hence does not need to be taken care for.
   *
   * NYI: Instead of returning -1 for non-instantiated value clazzes, it would
   * be much nicer if those clazzes would be removed completely from the IR or
   * replaced by someting obvious like 'void'.
   */
  public int clazzChoice(int cl, int i)
  {
    if (PRECONDITIONS)
      require
        (i >= 0 && i < clazzNumChoices(cl));

    var cc = _clazzIds.get(cl);
    var cg = cc.choiceGenerics().get(i);
    return cg.isRef() || cg.isInstantiated() ? _clazzIds.get(cg) : -1;
  }


  /**
   * Get all heirs of given clazz that are instantiated.
   *
   * @param cl a clazz id
   *
   * @return an array of the clazz id's of all heirs for cl that are
   * instantiated, including cl itself, provided that cl is instantiated.
   */
  public int[] clazzInstantiatedHeirs(int cl)
  {
    var cc = _clazzIds.get(cl);
    var result = new List<Clazz>();
    for (var h : cc.heirs())
      {
        if (h.isInstantiated())
          {
            result.add(h);
          }
      }
    var res = new int[result.size()];
    for (var i = 0; i < result.size(); i++)
      {
        res[i] = _clazzIds.get(result.get(i));
        check(res[i] != -1);
      }
    return res;
  }


  /**
   * Is this a choice type with some elements of ref type?
   *
   * @param cl a clazz id
   *
   * @return true iff cl is a choice with at least one ref element
   */
  public boolean clazzIsChoiceWithRefs(int cl)
  {
    var cc = _clazzIds.get(cl);
    return cc.isChoiceWithRefs();
  }


  /**
   * Is this a choice type with all elements of ref type?
   *
   * @param cl a clazz id
   *
   * @return true iff cl is a choice with only ref or unit/void elements
   */
  public boolean clazzIsChoiceOfOnlyRefs(int cl)
  {
    var cc = _clazzIds.get(cl);
    return cc.isChoiceOfOnlyRefs();
  }


  /**
   * Check if field does not store the value directly, but a pointer to the value.
   *
   * @param fcl a clazz id of the field
   *
   * @return true iff the field is an outer ref field that holds an address of
   * an outer value, false for normal fields our outer ref fields that store the
   * outer ref or value directly.
   */
  public boolean clazzFieldIsAdrOfValue(int fcl)
  {
    var fc = _clazzIds.get(fcl);
    var f = fc.feature();
    return f.isOuterRef() &&
      !fc.resultClazz().isRef() &&
      !fc.resultClazz().feature().isBuiltInPrimitive();
  }


  /**
   * Get the clazz of the result of calling a clazz
   *
   * @param cl a clazz id
   *
   * @return clazz id of cl's result or -1 if the result is a stateless value
   */
  public int clazzResultClazz(int cl)
  {
    var cc = _clazzIds.get(cl);
    return _clazzIds.get(cc.resultClazz());
  }


  public ClazzKind clazzKind(int cl)
  {
    return clazzKind(_clazzIds.get(cl));
  }


  ClazzKind clazzKind(Clazz cc)
  {
    if (cc.isChoice())
      {
        return ClazzKind.Choice;
      }
    else
      {
        var ff = cc.feature();
        switch (ff.impl.kind_)
          {
          case Routine    :
          case RoutineDef : return ClazzKind.Routine;
          case Field      :
          case FieldDef   :
          case FieldActual:
          case FieldInit  : return ClazzKind.Field;
          case Intrinsic  : return ClazzKind.Intrinsic;
          case Abstract   : return ClazzKind.Abstract;
          default: throw new Error ("Unexpected feature impl kind: "+ff.impl.kind_);
          }
      }
  }

  public String clazzBaseName(int cl)
  {
    var cc = _clazzIds.get(cl);
    var res = cc.feature().featureName().baseName();
    if (!cc._type._generics.isEmpty())
      {
        res = res + cc._type._generics.toString("<",",",">");
      }
    return res;
  }


  /**
   * The intrinsic names is the original qualified name of the intrinsic feature
   * ignoring any inheritance into new clazzes.
   *
   * @param cl an intrinsic
   *
   * @return its intrinsic name, e.g. 'Array.getel' instead of
   * 'conststring.getel'
   */
  public String clazzIntrinsicName(int cl)
  {
    if (PRECONDITIONS) require
      (clazzKind(cl) == ClazzKind.Intrinsic);

    var cc = _clazzIds.get(cl);
    return cc.feature().qualifiedName();
  }

  public boolean clazzIsRef(int cl)
  {
    return _clazzIds.get(cl).isRef();
  }


  /**
   * For a reference clazz, obtain the corresponding value clazz.
   *
   * @param cl a clazz id
   *
   * @return clazz id  of corresponding value clazz.
   */
  public int clazzAsValue(int cl)
  {
    var cc = _clazzIds.get(cl);
    return _clazzIds.get(cc.asValue());
  }


  /**
   * Get the number of arguments required for a call to this clazz.
   *
   * @param cl clazz id
   *
   * @return number of arguments expected by cl, 0 if none or if clazz cl can
   * not be called (is a choice type)
   */
  public int clazzArgCount(int cl)
  {
    var cc = _clazzIds.get(cl);
    return cc.argumentFields().length;
  }


  /**
   * Get the clazz id of the type of the given argument of clazz cl
   *
   * @param cl clazz id
   *
   * @parem arg argument number 0, 1, .. clazzArgCount(cl)-1
   *
   * @return clazz id of the argument or -1 if no such feature exists (the
   * argument is unused).
   */
  public int clazzArgClazz(int cl, int arg)
  {
    if (PRECONDITIONS) require
      (arg >= 0,
       arg < clazzArgCount(cl));

    var c = _clazzIds.get(cl);
    var rc = c.argumentFields()[arg].resultClazz(); // or .fieldClazz()?
    return _clazzIds.get(rc);
  }


  /**
   * Get the clazz id of the given argument of clazz cl
   *
   * @param cl clazz id
   *
   * @parem arg argument number 0, 1, .. clazzArgCount(cl)-1
   *
   * @return clazz id of the argument or -1 if no such argument exists (the
   * argument is unused).
   */
  public int clazzArg(int cl, int arg)
  {
    var cc = _clazzIds.get(cl);
    var a = cc.argumentFields()[arg];
    return a == null ? -1 : _clazzIds.get(a);
  }


  /**
   * is the given clazz a choice clazz
   *
   * @param cl a clazz id
   */
  public boolean clazzIsChoice(int cl)
  {
    return _clazzIds.get(cl).isChoice();
  }


  /**
   * Get the choice tag field of a choice clazz
   *
   * @param cl a clazz id
   *
   * @return clazz id of cl's choice tag or -1 if cl is not a choice or does not
   * need a choice tag.
   */
  public int clazzChoiceTag(int cl)
  {
    var cc = _clazzIds.get(cl);
    var ct = cc.choiceTag();
    return ct == null ? -1 : _clazzIds.get(ct);
  }


  /**
   * Get the choice tag id for a given value clazz in a choice clazz
   *
   * @param cl a clazz id of a choice clazz
   *
   * @param valuecl a clazz id of a static clazz of a value that is stored in an
   * instance of cl.
   *
   * @return id of the valuecl, correspods to the value to be stored in the tag.
   */
  public int clazzChoiceTag(int cl, int valuecl)
  {
    var cc = _clazzIds.get(cl);
    var vc = _clazzIds.get(valuecl);
    return cc.getChoiceTag(vc._type);
  }


  /**
   * Get the outer clazz of the given clazz.
   *
   * @param cl a clazz id
   *
   * @return clazz id of cl's outer clazz, -1 if cl is universe or a value-less
   * type.
   */
  public int clazzOuterClazz(int cl)
  {
    var o = _clazzIds.get(cl)._outer;
    return o == null ? -1 : _clazzIds.get(o);
  }


  /**
   * If a clazz's instance contains an outer ref field, return this field.
   *
   * @param cl a clazz id
   *
   * @return clazz id of cl's outer ref field or -1 if no such field exists.
   */
  public int clazzOuterRef(int cl)
  {
    var cc = _clazzIds.get(cl);
    var or = cc.outerRef();
    var cco = cc._outer;
    return
      or == null ||
      (cco.isUnitType()) ? -1
                         : _clazzIds.get(or);
  }


  /**
   * Get the id of clazz Object.
   *
   * @return clazz id of clazz Object
   */
  public int clazzObject()
  {
    return _clazzIds.get(Clazzes.object.get());
  }


  /**
   * Get the id of clazz universe.
   *
   * @return clazz id of clazz universe
   */
  public int clazzUniverse()
  {
    return _clazzIds.get(Clazzes.universe.get());
  }



  /**
   * Obtain SpecialClazzes id from a given clazz.
   *
   * @param cl a clazz id
   *
   * @return the corresponding SpecialClazzes id or c_NOT_FOUND if cl is not a
   * special clazz.
   */
  public SpecialClazzes getSpecialId(int cl)
  {
    var cc = _clazzIds.get(cl);
    var result = SPECIAL_ID.get(cc);
    return result == null ? SpecialClazzes.c_NOT_FOUND : result;
  }


  /**
   * Check if a clazz is the special clazz c.
   *
   * @param cl a clazz id
   *
   * @param One of the constants SpecialClazzes.c_i8,...
   *
   * @return true iff cl is the specified special clazz c
   */
  public boolean clazzIs(int cl, SpecialClazzes c)
  {
    var cc = _clazzIds.get(cl);
    return cc == c.getIfCreated();
  }


  // String representation of clazz, for debugging only
  public String clazzAsString(int cl)
  {
    return cl == -1
      ? "-- no clazz --"
      : _clazzIds.get(cl).toString();
  }


  /**
   * Are values of this clazz all the same, so they are essentially C/Java void
   * values?
   */
  public boolean clazzIsUnitType(int cl)
  {
    var cc = _clazzIds.get(cl);
    return cc.isUnitType();
  }


  /**
   * Is this a void type, i.e., values of this clazz do not exist.
   */
  public boolean clazzIsVoidType(int cl)
  {
    var cc = _clazzIds.get(cl);
    return cc.isVoidType();
  }


  /**
   * Get the id of the result field of a given clazz.
   *
   * @param cl a clazz id
   *
   * @return id of cl's result field or -1 if f has no result field (NYI: or a
   * result field that contains no data)
   */
  public int clazzResultField(int cl)
  {
    var cc = _clazzIds.get(cl);
    var rf = cc.resultField();
    return rf == null ? -1 : _clazzIds.get(rf);
  }


  /**
   * Get the id of an actual generic parameter of a given clazz.
   *
   * @param cl a clazz id
   *
   * @param gix indec of the generic parameter
   *
   * @return id of cl's actual generic parameter #gix
   */
  public int clazzActualGeneric(int cl, int gix)
  {
    var cc = _clazzIds.get(cl);
    return _clazzIds.get(cc.actualGenerics()[gix]);
  }


  /**
   * add the code of feature ff to code.  In case ff has inherits calls, also
   * include the code of the inherited features.
   *
   * @param code a list that code should be added to.
   *
   * @param ff a routine or constructor feature.
   */
  private void addCode(Clazz cc, List<Object> code, Feature ff)
  {
    for (Call p: ff.inherits)
      {
        /*
NYI: Any side-effects in p.target or p._actuals will be executed twice, once for
     the precondition and once for the inlinded call! See this example:

hw25 is
  A (a i32)
    pre
      a < 100
  is
    say "in A: $a"

  B : A x is

  count := 0

  x =>
    set count := count + 1
    count

  B; B; B
  if (count == 3) say "PASS" else say "FAIL"
        */

        var pf = p.calledFeature();
        var or = cc._inner.get(pf.outerRef_);
        toStack(code, p.target);
        if (or != null && !or.resultClazz().isUnitType())
          {
            if (!or.resultClazz().isRef() &&
                !or.resultClazz().feature().isBuiltInPrimitive())
              {
                code.add(ExprKind.AdrOf);
              }
            code.add(ExprKind.Dup);
            code.add(ExprKind.Current);
            code.add(or);  // field clazz means assignment to field
          }
        check
          (p._actuals.size() == p.calledFeature().arguments.size());
        for (var i = 0; i < p._actuals.size(); i++)
          {
            var a = p._actuals.get(i);
            var f = pf.arguments.get(i);
            toStack(code, a);
            code.add(ExprKind.Current);
            // Field clazz means assign value to that field
            code.add((Clazz) cc.getRuntimeData(p.parentCallArgFieldIds_ + i));
          }
        addCode(cc, code, p.calledFeature());
      }
    toStack(code, ff.impl._code);
  }


  /**
   * Code for a routine or precondition prolog.
   *
   * This addss code to initialize outer reference, must be done at the
   * beginning of every routine and precondition.
   *
   * @param code the code to add the initialization to.
   *
   * @param cc the routine we are creating code for.
   */
  private List<Object> prolog(Clazz cc)
  {
    List<Object> code = new List<>();
    var vcc = cc.asValue();
    var or = vcc.outerRef();
    var cco = cc._outer;
    if (or != null && !cco.isUnitType())
      {
        code.add(ExprKind.Outer);
        code.add(ExprKind.Current);
        code.add(or);
      }
    return code;
  }



  /**
   * Get access to the code of a clazz of kind Routine
   *
   * @param cl a clazz id
   *
   * @return a code id referring to cl's code
   */
  public int clazzCode(int cl)
  {
    if (PRECONDITIONS) require
      (clazzKind(cl) == ClazzKind.Routine);

    var cc = _clazzIds.get(cl);
    var ff = cc.feature();
    var code = prolog(cc);
    addCode(cc, code, ff);
    return _codeIds.add(code);
  }


  /**
   * Get access to the code of the contract of a clazz of kind Routine,
   * Intrinsic, Abstract or Field.
   *
   * @param cl a clazz id
   *
   * @param ck the part of the contract to be accessed, ContractKind.Pre or
   * ContractKind.Post for pre- and post-conditions, respectively.
   *
   * @param ix the index of the pre- or post-condition, 0 for the first
   * condition
   *
   * @return a code id referring to cl's pre- or post-condition, -1 if cl does
   * not have a pre- or post-condition with the given index
   */
  public int clazzContract(int cl, ContractKind ck, int ix)
  {
    if (PRECONDITIONS) require
      (clazzKind(cl) == ClazzKind.Routine   ||
       clazzKind(cl) == ClazzKind.Field     ||
       clazzKind(cl) == ClazzKind.Intrinsic ||
       clazzKind(cl) == ClazzKind.Abstract     ,
       ix >= 0);

    var cc = _clazzIds.get(cl);
    var ff = cc.feature();
    var ccontract = ff.contract;
    var cond = (ccontract != null && ck == ContractKind.Pre  ? ccontract.req :
                ccontract != null && ck == ContractKind.Post ? ccontract.ens : null);
    // NYI: PERFORMANCE: Always iterating the conditions results in performance
    // quadratic in the number of conditions.  This could be improved by
    // filtering BoolConst.TRUE once and reusing the resulting cond.
    var i = 0;
    while (cond != null && i < cond.size() &&
           (cond.get(i).cond == BoolConst.TRUE || ix > 0))
      {
        if (cond.get(i).cond != BoolConst.TRUE)
          {
            ix--;
          }
        i++;
      }
    if (cond != null && i < cond.size())
      {
        var code = prolog(cc);
        toStack(code, cond.get(i).cond);
        return _codeIds.add(code);
      }
    else
      {
        return -1;
      }
  }


  /**
   * Does the backend need to generate code for this clazz since it might be
   * called at runtime.  This is true for all features that are called directly
   * or dynamically in a 'normal' call, i.e., not in an inheritance call.
   *
   * An inheritance call is inlined since it works on a different instance, the
   * instance of the heir class.  Consequently, a clazz resulting from an
   * inheritance call does not need code for itself.
   */
  public boolean clazzNeedsCode(int cl)
  {
    return clazzNeedsCode(_clazzIds.get(cl));
  }


  /**
   * Does the backend need to generate code for this clazz since it might be
   * called at runtime.  This is true for all features that are called directly
   * or dynamically in a 'normal' call, i.e., not in an inheritance call.
   *
   * An inheritance call is inlined since it works on a different instance, the
   * instance of the heir class.  Consequently, a clazz resulting from an
   * inheritance call does not need code for itself.
   */
  boolean clazzNeedsCode(Clazz cc)
  {
    switch (clazzKind(cc))
      {
      case Abstract : return false;
      case Choice   : return false;
      case Intrinsic: return !cc.isAbsurd();
      case Routine  :
      case Field    :
        return (cc.isInstantiated() || cc.feature().isOuterRef()) && cc != Clazzes.conststring.getIfCreated() && !cc.isAbsurd();
      default: throw new Error("unhandled case: " + clazzKind(cc));
      }
  }


  /**
   * Is the given field clazz a reference to an outer feature?
   *
   * @param cl a clazz id of kind Field
   *
   * @return true for automatically generated references to outer instance
   */
  public boolean clazzIsOuterRef(int cl)
  {
    return _clazzIds.get(cl).feature().isOuterRef();
  }


  /**
   * Get the id of clazz consstring
   *
   * @param the id of connststring or -1 if that clazz was not created.
   */
  public int clazz_conststring()
  {
    var cc = Clazzes.conststring.getIfCreated();
    return cc == null ? -1 : _clazzIds.get(cc);
  }


  /*--------------------------  stack handling  -------------------------*/


  List<Object> toStack(Stmnt s)
  {
    List<Object> result = new List<>();
    toStack(result, s);
    return result;
  }
  void toStack(List<Object> l, Stmnt s) { toStack(l, s, false); }
  void toStack(List<Object> l, Stmnt s, boolean dumpResult)
  {
    if (PRECONDITIONS) require
      (l != null,
       s != null);

    if (s instanceof Assign a)
      {
        toStack(l, a._value);
        toStack(l, a._target);
        l.add(a);
      }
    else if (s instanceof Unbox u)
      {
        toStack(l, u.adr_);
        if (u._needed)
          {
            l.add(u);
          }
      }
    else if (s instanceof Box b)
      {
        toStack(l, b._value);
        l.add(b);
      }
    else if (s instanceof Block b)
      {
        // for (var st : b.statements_)
        for (int i=0; i<b.statements_.size(); i++)
          {
            var st = b.statements_.get(i);
            toStack(l, st, dumpResult || i < b.statements_.size()-1);
          }
      }
    else if (s instanceof BoolConst)
      {
        l.add(s);
      }
    else if (s instanceof Current)
      {
        l.add(ExprKind.Current);
      }
    else if (s instanceof If i)
      {
        // if is converted to If, blockId, elseBlockId
        toStack(l, i.cond);
        l.add(i);
        List<Object> block = toStack(i.block);
        l.add(new NumLiteral(_codeIds.add(block)));
        Stmnt elseBlock;
        if (i.elseBlock != null)
          {
            elseBlock = i.elseBlock;
          }
        else if (i.elseIf != null)
          {
            elseBlock = i.elseIf;
          }
        else
          {
            elseBlock = new Block(i.pos(), new List<>());
          }
        List<Object> elseBlockCode = toStack(elseBlock);
        l.add(new NumLiteral(_codeIds.add(elseBlockCode)));
      }
    else if (s instanceof NumLiteral)
      {
        l.add(s);
      }
    else if (s instanceof Call c)
      {
        toStack(l, c.target);
        for (var a : c._actuals)
          {
            toStack(l, a);
          }
        l.add(c);
        if (dumpResult)
          {
            l.add(ExprKind.Pop);
          }
      }
    else if (s instanceof Match m)
      {
        toStack(l, m.subject);
        l.add(m);
        for (var c : m.cases)
          {
            var caseCode = toStack(c.code);
            l.add(new NumLiteral(_codeIds.add(caseCode)));
          }
      }
    else if (s instanceof Tag t)
      {
        toStack(l, t._value);
        l.add(t);
      }
    else if (s instanceof Nop)
      {
      }
    else if (s instanceof Universe)
      {
        var un = (Universe) s;
      }
    else if (s instanceof StrConst)
      {
        l.add(s);
      }
    else if (s instanceof InlineArray)
      {
        l.add(s);
      }
    else if (s instanceof Check c)
      {
        // NYI: Check not supported yet
        //
        // l.add(s);
      }
    else
      {
        System.err.println("Missing handling of "+s.getClass()+" in FUIR.toStack");
      }
  }


  /*--------------------------  accessing code  -------------------------*/


  public boolean withinCode(int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0);

    var code = _codeIds.get(c);
    return ix < code.size();
  }

  public ExprKind codeAt(int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0, withinCode(c, ix));

    ExprKind result;
    var e = _codeIds.get(c).get(ix);
    if (e instanceof ExprKind ek)
      {
        result = ek;
      }
    else if (e instanceof String)
      {
        result = ExprKind.Comment;
      }
    else if (e instanceof Assign ||
             e instanceof Clazz    )  /* Clazz represents the field we assign a value to */
      {
        result = ExprKind.Assign;
      }
    else if (e instanceof Box)
      {
        result = ExprKind.Box;
      }
    else if (e instanceof Unbox)
      {
        result = ExprKind.Unbox;
      }
    else if (e instanceof Call)
      {
        result = ExprKind.Call;
      }
    else if (e instanceof If    ||
             e instanceof Match    )
      {
        result = ExprKind.Match;
      }
    else if (e instanceof Tag)
      {
        result = ExprKind.Tag;
      }
    else if (e instanceof BoolConst ||
             e instanceof NumLiteral  ||
             e instanceof StrConst  ||
             e instanceof InlineArray   )
      {
        result = ExprKind.Const;
      }
    else
      {
        Errors.fatal((e instanceof Stmnt s) ? s.pos() :
                     (e instanceof Clazz z) ? z._type.pos : null,
                     "Stmnt not supported in FUIR.codeAt", "Statement class: " + e.getClass());
        result = ExprKind.Current; // keep javac from complaining.
      }
    return result;
  }


  public int codeSizeAt(int c, int ix)
  {
    int result = 1;
    var s = codeAt(c, ix);
    if (s == FUIR.ExprKind.Match)
      {
        result = result + matchCaseCount(c, ix);
      }
    return result;
  }


  public int tagValueClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Tag);

    var outerClazz = _clazzIds.get(cl);
    var t = (Tag) _codeIds.get(c).get(ix);
    var vcl = (Clazz) outerClazz.getRuntimeData(t._valAndTaggedClazzId + 0);
    return vcl == null ? -1 : _clazzIds.get(vcl);
  }

  public int tagNewClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Tag);

    var outerClazz = _clazzIds.get(cl);
    var t = (Tag) _codeIds.get(c).get(ix);
    var ncl = (Clazz) outerClazz.getRuntimeData(t._valAndTaggedClazzId + 1);
    return ncl == null ? -1 : _clazzIds.get(ncl);
  }

  public int boxValueClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Box);

    var outerClazz = _clazzIds.get(cl);
    var b = (Box) _codeIds.get(c).get(ix);
    Clazz vc = (Clazz) outerClazz.getRuntimeData(b._valAndRefClazzId);
    return _clazzIds.get(vc);
  }

  public int boxResultClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Box);

    var outerClazz = _clazzIds.get(cl);
    var b = (Box) _codeIds.get(c).get(ix);
    Clazz rc = (Clazz) outerClazz.getRuntimeData(b._valAndRefClazzId+1);
    return _clazzIds.get(rc);
  }

  public int unboxOuterRefClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Unbox);

    var outerClazz = _clazzIds.get(cl);
    var u = (Unbox) _codeIds.get(c).get(ix);
    Clazz orc = (Clazz) outerClazz.getRuntimeData(u._refAndValClazzId);
    return _clazzIds.get(orc);
  }

  public int unboxResultClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Unbox);

    var outerClazz = _clazzIds.get(cl);
    var u = (Unbox) _codeIds.get(c).get(ix);
    Clazz vc = (Clazz) outerClazz.getRuntimeData(u._refAndValClazzId+1);
    return _clazzIds.get(vc);
  }


  /**
   * Get the code for a comment expression.  This is used for debugging.
   *
   * @param cl index of clazz containing the comment
   *
   * @param c code block containing the comment
   *
   * @param ix index of the comment
   */
  public String comment(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Comment);

    return (String) _codeIds.get(c).get(ix);
  }


  /**
   * Get the inner clazz for a non dynamic access or the static clazz of a dynamic
   * access.
   *
   * @param cl index of clazz containing the access
   *
   * @param c code block containing the access
   *
   * @param ix index of the access
   *
   * @return the clazz that has to be accessed or -1 if the access is an
   * assignment to a field that is unused, so the assignment is not needed.
   */
  public int accessedClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Call   ||
       codeAt(c, ix) == ExprKind.Assign    );

    var outerClazz = _clazzIds.get(cl);
    var s = _codeIds.get(c).get(ix);
    Clazz innerClazz =
      (s instanceof Call   call) ? (Clazz) outerClazz.getRuntimeData(call.sid_ + 0) :
      (s instanceof Assign a   ) ? (Clazz) outerClazz.getRuntimeData(a   .tid_ + 1) :
      (s instanceof Clazz  fld ) ? fld :
      (Clazz) (Object) new Object() { { if (true) throw new Error("acccessedClazz found unexpected Stmnt."); } } /* Java is ugly... */;

    return innerClazz == null ? -1 : _clazzIds.get(innerClazz);
  }


  /**
   * Get the possible inner clazzes for a dynamic call or assignment to a field
   *
   * @param cl index of clazz containing the access
   *
   * @param c code block containing the access
   *
   * @param ix index of the call
   *
   * @return an array with an even number of element pairs with accessed target
   * clazzes at even indices followed by the corresponding inner clazz of the
   * feature to be accessed for this target.
   */
  public int[] accessedClazzes(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Call   ||
       codeAt(c, ix) == ExprKind.Assign    ,
       accessIsDynamic(cl, c, ix));

    var outerClazz = _clazzIds.get(cl);
    var s =  _codeIds.get(c).get(ix);
    Clazz tclazz;
    Feature f;

    if (s instanceof Call call)
      {
        f = call.calledFeature();
        tclazz     = (Clazz) outerClazz.getRuntimeData(call.sid_ + 1);
      }
    else if (s instanceof Assign ass)
      {
        var assignedField = (Clazz) outerClazz.getRuntimeData(ass.tid_+ 1);
        tclazz = (Clazz) outerClazz.getRuntimeData(ass.tid_);  // NYI: This should be the same as assignedField._outer
        f = assignedField.feature();
      }
    else if (s instanceof Clazz fld)
      {
        tclazz = (Clazz) fld._outer;
        f = fld.feature();
      }
    else
      {
        throw new Error();
      }
    var innerClazzes = new List<Clazz>();
    for (var clz : tclazz.heirs())
      {
        var in = clz._inner.get(f);
        if (in != null && clazzNeedsCode(in))
          {
            innerClazzes.add(clz);
            innerClazzes.add(in);
          }
      }

    var innerClazzIds = new int[innerClazzes.size()];
    for (var i = 0; i < innerClazzes.size(); i++)
      {
        innerClazzIds[i] = _clazzIds.get(innerClazzes.get(i));
        check(innerClazzIds[i] != -1);
      }
    return innerClazzIds;
  }


  /**
   * Is an access to a feature (assignment, call) dynamic?
   *
   * @param cl index of clazz containing the access
   *
   * @param c code block containing the access
   *
   * @param ix index of the acces
   *
   * @return true iff the assignment or call requires dynamic binding depending
   * on the actual target type.
   */
  public boolean accessIsDynamic(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Assign ||
       codeAt(c, ix) == ExprKind.Call  );

    var outerClazz = _clazzIds.get(cl);
    var s = _codeIds.get(c).get(ix);
    var res =
      (s instanceof Assign ass ) ? ((Clazz) outerClazz.getRuntimeData(ass.tid_)).isRef() : // NYI: This should be the same as assignedField._outer
      (s instanceof Clazz  arg ) ? outerClazz.isRef() && !arg.feature().isOuterRef() : // assignment to arg field in inherits call (dynamic if outerlClazz is ref)
                                                                                       // or to outer ref field (not dynamic)
      (s instanceof Call   call) ? call.isDynamic() && ((Clazz) outerClazz.getRuntimeData(call.sid_ + 1)).isRef() :
      new Object() { { if (true) throw new Error("acccessIsDynamic found unexpected Stmnt."); } } == null /* Java is ugly... */;

    return res;
  }


  /**
   * Is this call only to check preconditions.
   *
   * This is a bit of a hack for calls to parent features in inherits clauses:
   * These calls are inlined, so the backend does not need to take care.  Only
   * the precondition must be executed explicitly, so there remains a call to
   * the parent feature with callPreconditionOnly() returning true.
   *
   * The result of a the call in this case is unit.
   *
   * @param cl index of clazz containing the call
   *
   * @param c code block containing the call
   *
   * @param ix index of the call
   *
   * @return true if only the precondition should be executed.
   */
  public boolean callPreconditionOnly(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Call);

    var outerClazz = _clazzIds.get(cl);
    var call = (Call) _codeIds.get(c).get(ix);
    return call.isInheritanceCall_;
  }


  /**
   * Get the target (outer) clazz of a feature access
   *
   * @param cl index of clazz containing the access
   *
   * @param c code block containing the access
   *
   * @param ix index of the access
   *
   * @return index of the static outer clazz of the accessed feature.
   */
  public int accessTargetClazz(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Assign ||
       codeAt(c, ix) == ExprKind.Call  );

    var outerClazz = _clazzIds.get(cl);
    var s = _codeIds.get(c).get(ix);
    var tclazz =
      (s instanceof Assign ass ) ? (Clazz) outerClazz.getRuntimeData(ass.tid_) : // NYI: This should be the same as assignedField._outer
      (s instanceof Clazz  arg ) ? outerClazz : // assignment to arg field in inherits call, so outer clazz is current instance
      (s instanceof Call   call) ? (Clazz) outerClazz.getRuntimeData(call.sid_ + 1) :
      (Clazz) (Object) new Object() { { if (true) throw new Error("acccessTargetClazz found unexpected Stmnt."); } } /* Java is ugly... */;

    return _clazzIds.get(tclazz);
  }


  public int fieldIndex(int field)
  {
    return _clazzIds.get(field).fieldIndex();
  }

  /**
   * For an intermediate command of type ExprKind.Const, return its clazz.
   *
   * Currently, the clazz is one of bool, i32, u32, i64, u64 of conststring.
   * This will be extended by other basic types (f64, etc.), value instances
   * without refs, choice instances with tag, arrays, etc.
   */
  public int constClazz(int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Const);

    Clazz clazz;
    var ic = _codeIds.get(c).get(ix);
    var t = ((Expr) ic).type();
    if      (t == Types.resolved.t_bool  ) { clazz = Clazzes.bool       .getIfCreated(); }
    else if (t == Types.resolved.t_i8    ) { clazz = Clazzes.i8         .getIfCreated(); }
    else if (t == Types.resolved.t_i16   ) { clazz = Clazzes.i16        .getIfCreated(); }
    else if (t == Types.resolved.t_i32   ) { clazz = Clazzes.i32        .getIfCreated(); }
    else if (t == Types.resolved.t_i64   ) { clazz = Clazzes.i64        .getIfCreated(); }
    else if (t == Types.resolved.t_u8    ) { clazz = Clazzes.u8         .getIfCreated(); }
    else if (t == Types.resolved.t_u16   ) { clazz = Clazzes.u16        .getIfCreated(); }
    else if (t == Types.resolved.t_u32   ) { clazz = Clazzes.u32        .getIfCreated(); }
    else if (t == Types.resolved.t_u64   ) { clazz = Clazzes.u64        .getIfCreated(); }
    else if (t == Types.resolved.t_f32   ) { clazz = Clazzes.f32        .getIfCreated(); }
    else if (t == Types.resolved.t_f64   ) { clazz = Clazzes.f64        .getIfCreated(); }
    else if (t == Types.resolved.t_string) { clazz = Clazzes.conststring.getIfCreated(); } // NYI: a slight inconsistency here, need to change AST
    else if (ic instanceof InlineArray)
      {
        throw new Error("NYI: FUIR support for InlineArray still missing");
      }
    else { throw new Error("Unexpected type for ExprKind.Const: " + t); }
    return _clazzIds.get(clazz);
  }


  /**
   * For an intermediate command of type ExprKind.Const, return the constant
   * data using little endian encoding, i.e, 0x12345678 -> { 0x78, 0x56, 0x34, 0x12 }.
   */
  public byte[] constData(int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Const);

    var ic = _codeIds.get(c).get(ix);
    var t = ((Expr) ic).type();
    if      (ic instanceof BoolConst     ) { return new byte[] { ((BoolConst) ic).b ? (byte) 1 : (byte) 0 }; }
    else if (ic instanceof NumLiteral    ) { return ((NumLiteral) ic).data(); }
    else if (t == Types.resolved.t_string) { return ((StrConst) ic).str.getBytes(StandardCharsets.UTF_8); }
    else if (ic instanceof InlineArray)
      {
        throw new Error("NYI: FUIR support for InlineArray still missing");
      }
    throw new Error("Unexpected constant type " + t + ", expected bool, i32, u32, i64, u64, or string");
  }


  /**
   * For a match statement, get the static clazz of the subject.
   *
   * @param cl index of clazz containing the match
   *
   * @param c code block containing the match
   *
   * @param ix index of the match
   *
   * @return clazz id of type of the subject
   */
  public int matchStaticSubject(int cl, int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Match);

    var cc = _clazzIds.get(cl);
    var s = _codeIds.get(c).get(ix);
    Clazz ss = s instanceof If
      ? cc.getRuntimeClazz(((If) s).runtimeClazzId_)
      : cc.getRuntimeClazz(((Match) s).runtimeClazzId_);
    return _clazzIds.get(ss);
  }


  /**
   * For a match statement, get the number of cases
   *
   * @param c code block containing the match
   *
   * @param ix index of the match
   *
   * @return the number of cases
   */
  public int matchCaseCount(int c, int ix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Match);

    var s = _codeIds.get(c).get(ix);
    int result = 2; // two cases for If
    if (s instanceof Match m)
      {
        result = m.cases.size();
      }
    return result;
  }


  /**
   * For a match statement, get the field of a given case
   *
   * @param cl index of clazz containing the match
   *
   * @param c code block containing the match
   *
   * @param ix index of the match
   *
   * @paramc cix index of the case in the match
   *
   * @return clazz id of field the value in this case is assigned to, -1 if this
   * case does not have a field or the field is unused.
   */
  public int matchCaseField(int cl, int c, int ix, int cix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Match,
       0 <= cix && cix <= matchCaseCount(c, ix));

    var cc = _clazzIds.get(cl);
    var s = _codeIds.get(c).get(ix);
    int result = -1; // no field for If
    if (s instanceof Match m)
      {
        var mc = m.cases.get(cix);
        var f = mc.field;
        var fc = f != null && Clazzes.isUsed(f, cc) ? cc.getRuntimeClazz(mc.runtimeClazzId_) : null;
        result = fc != null ? _clazzIds.get(fc) : -1;
      }
    return result;
  }


  /**
   * For a match statement, get the tags matched by a given case
   *
   * @param cl index of clazz containing the match
   *
   * @param c code block containing the match
   *
   * @param ix index of the match
   *
   * @paramc cix index of the case in the match
   *
   * @return array of tag numbers this case matches
   */
  public int[] matchCaseTags(int cl, int c, int ix, int cix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Match,
       0 <= cix && cix <= matchCaseCount(c, ix));

    var cc = _clazzIds.get(cl);
    var s = _codeIds.get(c).get(ix);
    int[] result;
    if (s instanceof If)
      {
        result = new int[] { cix == 0 ? 1 : 0 };
      }
    else
      {
        var match = (Match) s;
        var ss = cc.getRuntimeClazz(match.runtimeClazzId_);
        var mc = match.cases.get(cix);
        var f = mc.field;
        var fc = f != null && Clazzes.isUsed(f, cc) ? cc.getRuntimeClazz(mc.runtimeClazzId_) : null;
        int nt = f != null ? 1 : mc.types.size();
        var resultL = new List<Integer>();
        int tag = 0;
        for (var cg : ss.choiceGenerics())
          {
            for (int tix = 0; tix < nt; tix++)
              {
                var rc = fc != null ? fc.resultClazz() : cc.getRuntimeClazz(mc.runtimeClazzId_ + tix);
                if (rc.isAssignableFrom(cg))
                  {
                    resultL.add(tag);
                  }
              }
            tag++;
          }
        result = new int[resultL.size()];
        for (int i = 0; i < result.length; i++)
          {
            result[i] = resultL.get(i);
          }
      }
    return result;
  }


  /**
   * For a match statement, get the code associated with a given case
   *
   * @param c code block containing the match
   *
   * @param ix index of the match
   *
   * @paramc cix index of the case in the match
   *
   * @return code block for the case
   */
  public int matchCaseCode(int c, int ix, int cix)
  {
    if (PRECONDITIONS) require
      (ix >= 0,
       withinCode(c, ix),
       codeAt(c, ix) == ExprKind.Match,
       0 <= cix && cix <= matchCaseCount(c, ix));

    var s = _codeIds.get(c).get(ix+1+cix);
    return ((NumLiteral)s).intValue().intValueExact();
  }

}

/* end of file */
