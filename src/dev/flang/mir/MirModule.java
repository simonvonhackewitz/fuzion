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
 * Source of interface MirModule
 *
 *---------------------------------------------------------------------*/

package dev.flang.mir;

import java.nio.ByteBuffer;

import dev.flang.ast.AbstractModule; // NYI: Remove dependency!


/**
 * MirModule provides callbacks from the MIR to data structures in the current
 * module, in particular to sets of features.
 *
 * @author Fridtjof Siebert (siebert@tokiwa.software)
 */
public interface MirModule extends AbstractModule
{

  /**
   * The binary data from this module's .mir file.
   */
  ByteBuffer data(String name);

}

/* end of file */
