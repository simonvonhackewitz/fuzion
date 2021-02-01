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
 * Tokiwa GmbH, Berlin
 *
 * Source of class Fusion
 *
 *---------------------------------------------------------------------*/

package dev.flang.tools;

import java.util.TreeMap;
import java.util.TreeSet;

import dev.flang.be.c.C;

import dev.flang.be.interpreter.Intrinsics;
import dev.flang.be.interpreter.Interpreter;

import dev.flang.fe.FrontEnd;
import dev.flang.fe.FrontEndOptions;

import dev.flang.me.MiddleEnd;

import dev.flang.opt.Optimizer;

import dev.flang.util.ANY;
import dev.flang.util.Errors;


/**
 * Fusion is the main class of the Fusion interpreter and compiler.
 *
 * @author Fridtjof Siebert (siebert@tokiwa.eu)
 */
class Fusion extends ANY
{

  /*----------------------------  constants  ----------------------------*/


  /**
   * Fusion Backends:
   */
  static enum Backend
  {
    interpreter("-interpreter"),
    c          ("-c"),
    java       ("-java"),
    classes    ("-classes"),
    llvm       ("-llvm"),
    undefined;

    /**
     * the command line argument corresponding to this backend
     */
    private final String _arg;

    /**
     * Construct undefined backend
     */
    Backend()
    {
      _arg = null;
    }

    /**
     * Construct normal Backend option
     *
     * @param arg the command line arg to enable this backend
     */
    Backend(String arg)
    {
      if (PRECONDITIONS) require
        (arg != null && arg.startsWith("-"));

      _arg = arg;
      _allBackendArgs_.append(_allBackendArgs_.length() == 0 ? "" : "|").append(arg);
      _allBackends_.put(arg, this);
    }
  }

  static StringBuilder _allBackendArgs_ = new StringBuilder();
  static TreeMap<String, Backend> _allBackends_ = new TreeMap<>();


  static final String CMD = System.getProperty("fuzion.command", "fz");

  static final String USAGE =
    "Usage: " + CMD + " [-h|--help] [" + _allBackendArgs_ + "] (<main>|-)\n";


  /*----------------------------  variables  ----------------------------*/


  /**
   * Level of verbosity of output
   */
  final int VERBOSE = Integer.getInteger("fuzion.verbose", 0);


  /**
   * Flag to enable intrinsic functions such as fuzion.java.callVirtual. These are
   * not allowed if run in a web playground.
   */
  final boolean ENABLE_UNSAFE_INTRINSICS = Boolean.getBoolean("fuzion.enableUnsafeIntrinsics");
  {
    Intrinsics.ENABLE_UNSAFE_INTRINSICS = ENABLE_UNSAFE_INTRINSICS;  // NYI: Add to Fusion IR or BE Config
  }


  /**
   * Default result of debugLevel:
   */
  final int FUSION_DEBUG_LEVEL = Integer.getInteger("fuzion.debugLevel", 1);
  {
    Intrinsics.FUSION_DEBUG_LEVEL = FUSION_DEBUG_LEVEL;  // NYI: Add to Fusion IR or BE Config
  }


  /**
   * Default result of safety:
   */
  final boolean FUSION_SAFETY = new Boolean(System.getProperty("fuzion.safety", "true"));
  {
    Intrinsics.FUSION_SAFETY = FUSION_SAFETY;  // NYI: Add to Fusion IR or BE Config
  }


  /**
   * Read input from stdin instead of file?
   */
  boolean _readStdin = false;


  /**
   * name of main features .
   */
  String  _main = null;


  /**
   * Desired backend.
   */
  Backend _backend = Backend.undefined;


  /*--------------------------  static methods  -------------------------*/


  /**
   * main the main method
   *
   * @param args the command line arguments.  One argument is
   * currently supported: the main feature name.
   */
  public static void main(String[] args)
  {
    new Fusion(args);
  }


  /*--------------------------  constructors  ---------------------------*/


  /**
   * Constructor for the fusion class
   *
   * @param args the command line arguments.  One argument is
   * currently supported: the main feature name.
   */
  private Fusion(String[] args)
  {
    try
      {
        parseArgs(args);
        var options = new FrontEndOptions(VERBOSE,
                                          FUSION_SAFETY,
                                          FUSION_DEBUG_LEVEL,
                                          _readStdin,
                                          _main);
        if (_backend == Backend.c)
          {
            options.setTailRec();
          }
        var mir = new FrontEnd(options).createMIR();
        var air = new MiddleEnd(options, mir).air();
        var fuir = new Optimizer(options, air).fuir();
        var interp = new Interpreter(fuir);
        switch (_backend)
          {
          case interpreter: interp.run(); break;
          case c          : new C(options, fuir).compile(); break;
          default         : Errors.fatal("*** backend '" + _backend + "' not supported yet"); break;
          }
      }
    catch(Throwable e)
      {
        e.printStackTrace();
        System.exit(1);
      }
  }


  /*-----------------------------  methods  -----------------------------*/


  /**
   * Parse the given command line args and store the result in _readStdIn,
   * _main, _backend.  System.exit() in case of error or -help.
   *
   * @param args the command line arguments
   *
   * @return NYI: Should better return an instance of FuionOptions
   */
  private void parseArgs(String[] args)
  {
    var duplicates = new TreeSet<String>();
    for (var a : args)
      {
        if (duplicates.contains(a))
          {
            Errors.fatal("*** duplicate argument: '" + a + "'\n" + USAGE);
          }
        duplicates.add(a);
        if (a.equals("-"))
          {
            _readStdin = true;
          }
        else if (_allBackends_.containsKey(a))
        {
          if (_backend != Backend.undefined)
            {
              Errors.fatal("*** arguments must specify at most one backend, found '" + _backend._arg + "' and '" + a + "'\n" + USAGE);
            }
          _backend = _allBackends_.get(a);
        }
        else if (a.equals("-h"    ) ||
                 a.equals("-help" ) ||
                 a.equals("--help")    )
          {
            System.out.println(USAGE);
            System.exit(0);
          }
        else if (a.startsWith("-"))
          {
            Errors.fatal("*** unknown argument: '" + a + "'\n" + USAGE);
          }
        else if (_main != null)
          {
            Errors.fatal("*** several main feature names provided: '" + _main + "', '" + a + "'\n" + USAGE);
          }
        else
          {
            _main = a;
          }
      }
    if (_main == null && !_readStdin)
      {
        Errors.fatal("*** missing main feature name in command line args\n" + USAGE);
      }
    if (_main != null && _readStdin)
      {
        Errors.fatal("*** cannot process main feature name together with stdin input\n" + USAGE);
      }
    if (_backend == Backend.undefined)
      {
        _backend = Backend.interpreter;
      }
  }

}

/* end of file */
