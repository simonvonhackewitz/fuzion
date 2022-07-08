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
 * Source of class Instance
 *
 *---------------------------------------------------------------------*/

package dev.flang.fuir.analysis;

import java.util.TreeMap;

import dev.flang.fuir.FUIR;

import dev.flang.util.ANY;
import dev.flang.util.Errors;


/**
 * Instance represents an abstract instance of a feature handled by the DFA
 * Analysis. An Abstract instance may consist of abstract values as well as
 * context information, taint information, etc.
 *
 * @author Fridtjof Siebert (siebert@tokiwa.software)
 */
public class Instance extends Value implements Comparable<Instance>
{


  /*-----------------------------  classes  -----------------------------*/


  /*----------------------------  constants  ----------------------------*/


  /*----------------------------  variables  ----------------------------*/


  /**
   * The clazz this is an instance of.
   */
  int _clazz;


  /**
   * The DFA instance we are working with.
   */
  DFA _dfa;


  /**
   * Map from fields to the values that have been assigned to the fields.
   */
  TreeMap<Integer, Value> _fields = new TreeMap<>();


  /*---------------------------  consructors  ---------------------------*/


  /**
   * Create Instance of given clazz
   *
   * @param clazz the clazz this is an instance of.
   */
  public Instance(DFA dfa, int clazz)
  {
    _clazz = clazz;
    _dfa = dfa;
  }


  /*-----------------------------  methods  -----------------------------*/


  /**
   * Compare this to another instance.
   */
  public int compareTo(Instance other)
  {
    return
      _clazz < other._clazz ? -1 :
      _clazz > other._clazz ? +1 : 0;
  }


  /**
   * Add v to the set of values of given field within this instance.
   */
  public void setField(int field, Value v)
  {
    var oldv = _fields.get(field);
    if (oldv != null)
      {
        v = oldv.join(v);
      }
    _fields.put(field, v);
  }


  /**
   * Get set of values of given field within this instance.
   */
  public Value readField(int target, int field)
  {
    if (PRECONDITIONS) require
      (_clazz == target);

    var v = _fields.get(field);
    if (v == null)
      {
        if (_dfa._reportResults)
          {
            System.err.println("*** reading uninitialized field " + _dfa._fuir.clazzAsString(field));
          }
        v = Value.UNIT;
      }
    return v;
  }


  /**
   * Create human-readable string from this instance.
   */
  public String toString()
  {
    return _dfa._fuir.clazzAsString(_clazz);
  }

}

/* end of file */