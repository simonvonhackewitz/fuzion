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
 * Source of class LibraryType
 *
 *---------------------------------------------------------------------*/

package dev.flang.fe;

import java.util.Set;

import dev.flang.ast.AbstractFeature;
import dev.flang.ast.AbstractType;
import dev.flang.ast.Expr;
import dev.flang.ast.Feature;
import dev.flang.ast.Generic;
import dev.flang.ast.Type;

import dev.flang.util.List;

import dev.flang.util.SourcePosition;


/**
 * A LibraryType represents a Fuzion type loaded from a precompiled Fuzion
 * module file .fum.
 *
 * @author Fridtjof Siebert (siebert@tokiwa.software)
 */
public class LibraryType extends AbstractType
{


  /*----------------------------  variables  ----------------------------*/


  /**
   * The library this come from.
   */
  public final LibraryModule _libModule;


  /**
   * Position in _libModule that declares this type. Maybe -1 for
   * _feature.thisType().
   */
  public final int _at;

  /**
   * The soucecode position of this type, used for error messages.
   */
  public final SourcePosition _pos;


  /**
   * For a type that is not a generic argument, this is the feature the type is
   * based on.
   */
  AbstractFeature _feature;


  /**
   * For a type that is not a generic argument, this is the list of actual
   * generics.
   */
  List<AbstractType> _generics;


  Generic _generic;

  /**
   * NYI: For now, this is just a wrapper around an AST type. This should be
   * removed once all data is obtained from _libModule;
   */
  private final Type _from;


  /*--------------------------  constructors  ---------------------------*/


  /**
   * Constructor for a plain Type from a given feature that does not have any
   * actual generics.
   */
  LibraryType(LibraryModule mod, SourcePosition pos, AbstractFeature feature, AbstractType from)
  {
    this._libModule = mod;
    this._at = -1;
    this._pos = pos;
    this._feature = feature;
    this._generics = Type.NONE;
    this._from = from.astType();
  }


  /**
   * Constructor for a plain Type from a given feature that does not have any
   * actual generics.
   */
  LibraryType(LibraryModule mod, SourcePosition pos, int at, AbstractType from)
  {
    this._libModule = mod;
    this._at = at;
    this._pos = pos;
    var k = mod.typeKind(at);
    AbstractFeature feature;
    List<AbstractType> generics;
    Generic generic;
    if (k < 0)
      {
        // generic argument
        feature = null;
        generics = null;
        generic = mod.genericArgument(mod.typeGeneric(at));
      }
    else
      {
        generic = null;
        feature = mod.libraryFeature(mod.typeFeature(at), (Feature) from.featureOfType().astFeature());
        if (k > 0)
          {
            var i = mod.typeActualGenericsPos(at);
            generics = new List<AbstractType>();
            var gi = 0;
            while (gi < k)
              {
                generics.add(new LibraryType(mod, pos, i, from.generics().get(gi)));
                i = mod.typeNextPos(i);
                gi++;
              }
          }
        else
          {
            generics = Type.NONE;
          }
      }
    this._feature = feature;
    this._generics = generics;
    this._generic = generic;
    this._from = from.astType();
  }

  /*-----------------------------  methods  -----------------------------*/


  public SourcePosition pos()
  {
    return _pos;
  }

  public AbstractFeature featureOfType()
  {
    return _feature;
  }

  public boolean isGenericArgument()
  {
    return _feature == null;
  }

  public List<AbstractType> generics()
  {
    return _generics;
  }


  /**
   * genericArgument gives the Generic instance of a type defined by a generic
   * argument.
   *
   * @return the Generic instance, never null.
   */
  public Generic genericArgument()
  {
    if (PRECONDITIONS) require
      (isGenericArgument());

    Generic result = _generic;

    if (POSTCONDITIONS) ensure
      (result != null);

    return result;
  }


  public AbstractType asRef() { return _from.asRef(); }
  public AbstractType asValue() { return _from.asValue(); }
  public boolean isRef() { return _from.isRef(); }
  public boolean isAssignableFrom(AbstractType actual, Set<String> assignableTo) { return _from.isAssignableFrom(actual, assignableTo); }
  public int compareToIgnoreOuter(Type other) { return _from.compareToIgnoreOuter(other); }
  public AbstractType outer() { return _from.outer(); }

  public Type astType() { return _from; }

}

/* end of file */
