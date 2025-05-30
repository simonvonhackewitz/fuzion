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
 * Source of class i32Value
 *
 *---------------------------------------------------------------------*/

package dev.flang.be.interpreter;

import dev.flang.fuir.SpecialClazzes;

/**
 * i32Value is a value of type i32
 *
 * @author Fridtjof Siebert (siebert@tokiwa.software)
 */
public class i32Value extends Value
{


  /*----------------------------  variables  ----------------------------*/


  /**
   *
   */
  private int _val;


  /*--------------------------  constructors  ---------------------------*/


  /**
   * Constructor
   */
  public i32Value(int val)
  {
    _val = val;
  }


  /*-----------------------------  methods  -----------------------------*/


  /**
   * toString
   *
   * @return
   */
  public String toString()
  {
    return Integer.toString(_val);
  }


  /**
   * For a value of type i32, return the value.
   *
   * @return the i32 value
   */
  public int i32Value()
  {
    return _val;
  }


  /**
   * Store this value in a field
   *
   * @param slot the slot that addresses the field this should be stored in.
   *
   * @param size the size of the data to be stored
   */
  void storeNonRef(LValue slot, int size)
  {
    if (PRECONDITIONS) require
      (size == 1);

    slot.container.nonrefs[slot.offset] = _val;
  }


  /**
   * Debugging only: Check that this value is valid as the current instance for
   * a feature with given static clazz.
   *
   * @param expected the static clazz of the feature this value is called on.
   *
   * @throws Error in case this does not match the expected clazz
   */
  void checkStaticClazz(int expected)
  {
    if (expected != fuir().clazz(SpecialClazzes.c_i32))
      {
        throw new Error("i32 value not allowed for clazz " + expected);
      }
  }


  @Override
  protected Object toNative()
  {
    return this.i32Value();
  }

}

/* end of file */
