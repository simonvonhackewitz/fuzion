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
 * Source of class Intrinsics
 *
 *---------------------------------------------------------------------*/

package dev.flang.be.jvm.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import dev.flang.be.jvm.runtime.Runtime.SystemErrNo;
import dev.flang.util.ANY;


/**
 * Intrinsics provides implementations of Fuzion's intrinsic features for use by
 * code generated for the JVM backend.
 *
 * @author Fridtjof Siebert (siebert@tokiwa.software)
 */
public class Intrinsics extends ANY
{


  /*-------------------------  static methods  --------------------------*/


  public static long    fuzion_sys_stdin_stdin0 () { return Runtime._stdin;  }
  public static long    fuzion_sys_out_stdout   () { return Runtime._stdout; }
  public static long    fuzion_sys_err_stderr   () { return Runtime._stderr; }

  public static long    fuzion_sys_misc_unique_id() { return Runtime.unique_id(); }

  public static int     i8_as_i32                   (byte   a          ) { return                 (      a); }
  public static byte    i8_cast_to_u8               (byte   a          ) { return                 (      a); }
  public static byte    i8_prefix_minusDEGREE       (byte   a          ) { return (byte)          (  -   a); }
  public static byte    i8_infix_plusDEGREE         (byte   a, byte   b) { return (byte)          (a +   b); }
  public static byte    i8_infix_minusDEGREE        (byte   a, byte   b) { return (byte)          (a -   b); }
  public static byte    i8_infix_timesDEGREE        (byte   a, byte   b) { return (byte)          (a *   b); }
  public static byte    i8_div                      (byte   a, byte   b) { return (byte)          (a /   b); }
  public static byte    i8_mod                      (byte   a, byte   b) { return (byte)          (a %   b); }
  public static byte    i8_infix_AND                (byte   a, byte   b) { return (byte)          (a &   b); }
  public static byte    i8_infix_OR                 (byte   a, byte   b) { return (byte)          (a |   b); }
  public static byte    i8_infix_caret              (byte   a, byte   b) { return (byte)          (a ^   b); }
  public static byte    i8_infix_gtgt               (byte   a, byte   b) { return (byte)          (a >>  b); }
  public static byte    i8_infix_ltlt               (byte   a, byte   b) { return (byte)          (a <<  b); }
  public static boolean i8_type_equality             (byte   a, byte   b) { return                 ( a == b); }
  public static boolean i8_type_lteq                 (byte   a, byte   b) { return                 ( a <= b); }
  public static int     i16_as_i32                  (short  a          ) { return                 (      a); }
  public static char    i16_cast_to_u16             (short  a          ) { return (char)          (      a); }
  public static short   i16_prefix_minusDEGREE      (short  a          ) { return (short)         (  -   a); }
  public static short   i16_infix_plusDEGREE        (short  a, short  b) { return (short)         (a +   b); }
  public static short   i16_infix_minusDEGREE       (short  a, short  b) { return (short)         (a -   b); }
  public static short   i16_infix_timesDEGREE       (short  a, short  b) { return (short)         (a *   b); }
  public static short   i16_div                     (short  a, short  b) { return (short)         (a /   b); }
  public static short   i16_mod                     (short  a, short  b) { return (short)         (a %   b); }
  public static short   i16_infix_AND               (short  a, short  b) { return (short)         (a &   b); }
  public static short   i16_infix_OR                (short  a, short  b) { return (short)         (a |   b); }
  public static short   i16_infix_caret             (short  a, short  b) { return (short)         (a ^   b); }
  public static short   i16_infix_gtgt              (short  a, short  b) { return (short)         (a >>  b); }
  public static short   i16_infix_ltlt              (short  a, short  b) { return (short)         (a <<  b); }
  public static boolean i16_type_equality           (short  a, short  b) { return                 (a ==  b); }
  public static boolean i16_type_lteq               (short  a, short  b) { return                 (a <=  b); }
  public static long    i32_as_i64                  (int    a          ) { return (long)          (      a); }
  public static int     i32_cast_to_u32             (int    a          ) { return                 (      a); }
  public static double  i32_as_f64                  (int    a          ) { return (double)        (      a); }
  public static int     i32_prefix_minusDEGREE      (int    a          ) { return                 (  -   a); }
  public static int     i32_infix_plusDEGREE        (int    a, int    b) { return                 (a +   b); }
  public static int     i32_infix_minusDEGREE       (int    a, int    b) { return                 (a -   b); }
  public static int     i32_infix_timesDEGREE       (int    a, int    b) { return                 (a *   b); }
  public static int     i32_div                     (int    a, int    b) { return                 (a /   b); }
  public static int     i32_mod                     (int    a, int    b) { return                 (a %   b); }
  public static int     i32_infix_AND               (int    a, int    b) { return                 (a &   b); }
  public static int     i32_infix_OR                (int    a, int    b) { return                 (a |   b); }
  public static int     i32_infix_caret             (int    a, int    b) { return                 (a ^   b); }
  public static int     i32_infix_gtgt              (int    a, int    b) { return                 (a >>  b); }
  public static int     i32_infix_ltlt              (int    a, int    b) { return                 (a <<  b); }
  public static boolean i32_type_equality           (int    a, int    b) { return                 (a ==  b); }
  public static boolean i32_type_lteq               (int    a, int    b) { return                 (a <=  b); }
  public static long    i64_cast_to_u64             (long   a          ) { return                 (      a); }
  public static double  i64_as_f64                  (long   a          ) { return (double)        (      a); }
  public static long    i64_prefix_minusDEGREE      (long   a          ) { return                 (  -   a); }
  public static long    i64_infix_plusDEGREE        (long   a, long   b) { return                 (a +   b); }
  public static long    i64_infix_minusDEGREE       (long   a, long   b) { return                 (a -   b); }
  public static long    i64_infix_timesDEGREE       (long   a, long   b) { return                 (a *   b); }
  public static long    i64_div                     (long   a, long   b) { return                 (a /   b); }
  public static long    i64_mod                     (long   a, long   b) { return                 (a %   b); }
  public static long    i64_infix_AND               (long   a, long   b) { return                 (a &   b); }
  public static long    i64_infix_OR                (long   a, long   b) { return                 (a |   b); }
  public static long    i64_infix_caret             (long   a, long   b) { return                 (a ^   b); }
  public static long    i64_infix_gtgt              (long   a, long   b) { return                 (a >>  b); }
  public static long    i64_infix_ltlt              (long   a, long   b) { return                 (a <<  b); }
  public static boolean i64_type_equality           (long   a, long   b) { return                 (a ==  b); }
  public static boolean i64_type_lteq               (long   a, long   b) { return                 (a <=  b); }
  public static int     u8_as_i32                   (byte a            ) { return                  0xff & (      a) ; }
  public static byte    u8_cast_to_i8               (byte   a          ) { return (byte) ((int) (byte)    (      a)); }
  public static byte    u8_prefix_minusDEGREE       (byte   a          ) { return (byte)          (  -   (0xff & a)); }
  public static byte    u8_infix_plusDEGREE         (byte   a, byte   b) { return (byte)          ((0xff & a) +   (0xff & b)); }
  public static byte    u8_infix_minusDEGREE        (byte   a, byte   b) { return (byte)          ((0xff & a) -   (0xff & b)); }
  public static byte    u8_infix_timesDEGREE        (byte   a, byte   b) { return (byte)          ((0xff & a) *   (0xff & b)); }
  public static byte    u8_div                      (byte   a, byte   b) { return (byte) Integer.divideUnsigned   ((0xff & a), (0xff & b)); }
  public static byte    u8_mod                      (byte   a, byte   b) { return (byte) Integer.remainderUnsigned((0xff & a), (0xff & b)); }
  public static byte    u8_infix_AND                (byte   a, byte   b) { return (byte)          ((0xff & a) &   (0xff & b)); }
  public static byte    u8_infix_OR                 (byte   a, byte   b) { return (byte)          ((0xff & a) |   (0xff & b)); }
  public static byte    u8_infix_caret              (byte   a, byte   b) { return (byte)          ((0xff & a) ^   (0xff & b)); }
  public static byte    u8_infix_gtgt               (byte   a, byte   b) { return (byte)          ((0xff & a) >>> (0xff & b)); }
  public static byte    u8_infix_ltlt               (byte   a, byte   b) { return (byte)          ((0xff & a) <<  (0xff & b)); }
  public static boolean u8_type_equality             (byte   a, byte   b) { return                 (a ==  b) ; }
  public static boolean u8_type_lteq                 (byte   a, byte   b) { return Integer.compareUnsigned (a,    b) <= 0; }
  public static int     u16_as_i32                  (char   a          ) { return                 (      a); }
  public static byte    u16_low8bits                (char   a          ) { return (byte)          (      a); }
  public static short   u16_cast_to_i16             (char   a          ) { return (short)         (      a); }
  public static char    u16_prefix_minusDEGREE      (char   a, char   b) { return (char)          (  -   a); }
  public static char    u16_infix_plusDEGREE        (char   a, char   b) { return (char)          (a +   b); }
  public static char    u16_infix_minusDEGREE       (char   a, char   b) { return (char)          (a -   b); }
  public static char    u16_infix_timesDEGREE       (char   a, char   b) { return (char)          (a *   b); }
  public static char    u16_div                     (char   a, char   b) { return (char) Integer.divideUnsigned   (a, b); }
  public static char    u16_mod                     (char   a, char   b) { return (char) Integer.remainderUnsigned(a, b); }
  public static char    u16_infix_AND               (char   a, char   b) { return (char)          (a &   b); }
  public static char    u16_infix_OR                (char   a, char   b) { return (char)          (a |   b); }
  public static char    u16_infix_caret             (char   a, char   b) { return (char)          (a ^   b); }
  public static char    u16_infix_gtgt              (char   a, char   b) { return (char)          (a >>> b); }
  public static char    u16_infix_ltlt              (char   a, char   b) { return (char)          (a <<  b); }
  public static boolean u16_type_equality           (char   a, char   b) { return                 (a ==  b); }
  public static boolean u16_type_lteq               (char   a, char   b) { return Integer.compareUnsigned(a, b) <= 0; }
  public static long    u32_as_i64                  (int    a          ) { return Integer.toUnsignedLong(a); }
  public static byte    u32_low8bits                (int    a          ) { return (byte)          (      a); }
  public static char    u32_low16bits               (int    a          ) { return (char)          (      a); }
  public static int     u32_cast_to_i32             (int    a          ) { return                 (      a); }
  public static double  u32_as_f64                  (int    a          ) { return (double) Integer.toUnsignedLong(a); }
  public static float   u32_cast_to_f32             (int    a          ) { return Float.intBitsToFloat(       a); }
  public static int     u32_prefix_minusDEGREE      (int    a          ) { return                 (  -   a); }
  public static int     u32_infix_plusDEGREE        (int    a, int    b) { return                 (a +   b); }
  public static int     u32_infix_minusDEGREE       (int    a, int    b) { return                 (a -   b); }
  public static int     u32_infix_timesDEGREE       (int    a, int    b) { return                 (a *   b); }
  public static int     u32_div                     (int    a, int    b) { return Integer.divideUnsigned   (a, b); }
  public static int     u32_mod                     (int    a, int    b) { return Integer.remainderUnsigned(a, b); }
  public static int     u32_infix_AND               (int    a, int    b) { return                 (a &   b); }
  public static int     u32_infix_OR                (int    a, int    b) { return                 (a |   b); }
  public static int     u32_infix_caret             (int    a, int    b) { return                 (a ^   b); }
  public static int     u32_infix_gtgt              (int    a, int    b) { return                 (a >>> b); }
  public static int     u32_infix_ltlt              (int    a, int    b) { return                 (a <<  b); }
  public static boolean u32_type_equality           (int    a, int    b) { return                 (a ==  b); }
  public static boolean u32_type_lteq               (int    a, int    b) { return Integer.compareUnsigned(a, b) <= 0; }
  public static byte    u64_low8bits                (long   a          ) { return (byte)          (      a) ; }
  public static char    u64_low16bits               (long   a          ) { return (char)          (      a) ; }
  public static int     u64_low32bits               (long   a          ) { return (int)           (      a) ; }
  public static long    u64_cast_to_i64             (long   a          ) { return                 (      a) ; }
  public static double  u64_as_f64                  (long   a          ) { return Double.parseDouble(Long.toUnsignedString(a)); }  // NYI: why so complex?
  public static double  u64_cast_to_f64             (long   a          ) { return Double.longBitsToDouble(    a); }
  public static long    u64_prefix_minusDEGREE      (long   a          ) { return                 (  -   a); }
  public static long    u64_infix_plusDEGREE        (long   a, long   b) { return                 (a +   b); }
  public static long    u64_infix_minusDEGREE       (long   a, long   b) { return                 (a -   b); }
  public static long    u64_infix_timesDEGREE       (long   a, long   b) { return                 (a *   b); }
  public static long    u64_div                     (long   a, long   b) { return Long.divideUnsigned   (a, b); }
  public static long    u64_mod                     (long   a, long   b) { return Long.remainderUnsigned(a, b); }
  public static long    u64_infix_AND               (long   a, long   b) { return                 (a &   b); }
  public static long    u64_infix_OR                (long   a, long   b) { return                 (a |   b); }
  public static long    u64_infix_caret             (long   a, long   b) { return                 (a ^   b); }
  public static long    u64_infix_gtgt              (long   a, long   b) { return                 (a >>> b); }
  public static long    u64_infix_ltlt              (long   a, long   b) { return                 (a <<  b); }
  public static boolean u64_type_equality           (long   a, long   b) { return                 (a ==  b); }
  public static boolean u64_type_lteq               (long   a, long   b) { return Long.compareUnsigned(a,b) <= 0; }
  public static float   f32_prefix_minus            (float  a          ) { return                 (  -   a); }
  public static float   f32_infix_plus              (float  a, float  b) { return                 (a +   b); }
  public static float   f32_infix_minus             (float  a, float  b) { return                 (a -   b); }
  public static float   f32_infix_times             (float  a, float  b) { return                 (a *   b); }
  public static float   f32_infix_divide            (float  a, float  b) { return                 (a /   b); }
  public static float   f32_infix_PERCENT           (float  a, float  b) { return                 (a %   b); }
  public static float   f32_infix_timestimes        (float  a, float  b) { return (float) Math.pow(a,    b); }
  public static boolean f32_infix_eq                (float  a, float  b) { return                 (a ==  b); }
  public static boolean f32_infix_lteq              (float  a, float  b) { return                 (a <=  b); }
  public static boolean f32_infix_gteq              (float  a, float  b) { return                 (a >=  b); }
  public static boolean f32_infix_lt                (float  a, float  b) { return                 (a <   b); }
  public static boolean f32_infix_gt                (float  a, float  b) { return                 (a >   b); }
  public static double  f32_as_f64                  (float  a          ) { return (double)        (      a); }
  public static int     f32_cast_to_u32             (float  a          ) { return Float.floatToIntBits(  a); }
  public static double  f64_prefix_minus            (double a          ) { return                 (  -   a); }
  public static double  f64_infix_plus              (double a, double b) { return                 (a +   b); }
  public static double  f64_infix_minus             (double a, double b) { return                 (a -   b); }
  public static double  f64_infix_times             (double a, double b) { return                 (a *   b); }
  public static double  f64_infix_divide            (double a, double b) { return                 (a /   b); }
  public static double  f64_infix_PERCENT           (double a, double b) { return                 (a %   b); }
  public static double  f64_infix_timestimes        (double a, double b) { return         Math.pow(a,    b); }
  public static boolean f64_infix_eq                (double a, double b) { return                 (a ==  b); }
  public static boolean f64_infix_lteq              (double a, double b) { return                 (a <=  b); }
  public static boolean f64_infix_gteq              (double a, double b) { return                 (a >=  b); }
  public static boolean f64_infix_lt                (double a, double b) { return                 (a <   b); }
  public static boolean f64_infix_gt                (double a, double b) { return                 (a >   b); }
  public static long    f64_as_i64_lax              (double a          ) { return (long)          (      a); }
  public static float   f64_as_f32                  (double a          ) { return (float)         (      a); }
  public static long    f64_cast_to_u64             (double a          ) { return Double.doubleToLongBits(a); }
  public static boolean f32_type_is_NaN             (float  a          ) { return Float.isNaN       (         a); }
  public static boolean f64_type_is_NaN             (double a          ) { return Double.isNaN      (         a); }
  public static float   f32_type_acos               (float  a          ) { return (float) Math.acos (         a); }
  public static float   f32_type_asin               (float  a          ) { return (float) Math.asin (         a); }
  public static float   f32_type_atan               (float  a          ) { return (float) Math.atan (         a); }
  public static float   f32_type_cos                (float  a          ) { return (float) Math.cos  (         a); }
  public static float   f32_type_cosh               (float  a          ) { return (float) Math.cosh (         a); }
  public static float   f32_type_epsilon            (                  ) { return (float) Math.ulp  ((float)  1); }
  public static float   f32_type_exp                (float  a          ) { return (float) Math.exp  (         a); }
  public static float   f32_type_log                (float  a          ) { return (float) Math.log  (         a); }
  public static float   f32_type_max                (                  ) { return Float.MAX_VALUE;                }
  public static int     f32_type_max_exp            (                  ) { return Float.MAX_EXPONENT;             }
  public static float   f32_type_min_positive       (                  ) { return Float.MIN_NORMAL;               }
  public static int     f32_type_min_exp            (                  ) { return Float.MIN_EXPONENT;             }
  public static float   f32_type_sin                (float  a          ) { return (float) Math.sin  (         a); }
  public static float   f32_type_sinh               (float  a          ) { return (float) Math.sinh (         a); }
  public static float   f32_type_square_root        (float  a          ) { return (float) Math.sqrt ((double) a); }
  public static float   f32_type_tan                (float  a          ) { return (float) Math.tan  (         a); }
  public static float   f32_type_tanh               (float  a          ) { return (float) Math.tanh (         a); }
  public static double  f64_type_acos               (double a          ) { return         Math.acos (         a); }
  public static double  f64_type_asin               (double a          ) { return         Math.asin (         a); }
  public static double  f64_type_atan               (double a          ) { return         Math.atan (         a); }
  public static double  f64_type_cos                (double a          ) { return         Math.cos  (         a); }
  public static double  f64_type_cosh               (double a          ) { return         Math.cosh (         a); }
  public static double  f64_type_epsilon            (                  ) { return         Math.ulp  ((double) 1); }
  public static double  f64_type_exp                (double a          ) { return         Math.exp  (         a); }
  public static double  f64_type_log                (double a          ) { return         Math.log  (         a); }
  public static double  f64_type_max                (                  ) { return Double.MAX_VALUE;               }
  public static int     f64_type_max_exp            (                  ) { return Double.MAX_EXPONENT;            }
  public static double  f64_type_min_positive       (                  ) { return Double.MIN_NORMAL;              }
  public static int     f64_type_min_exp            (                  ) { return Double.MIN_EXPONENT;            }
  public static double  f64_type_sin                (double a          ) { return         Math.sin  (         a); }
  public static double  f64_type_sinh               (double a          ) { return         Math.sinh (         a); }
  public static double  f64_type_square_root        (double a          ) { return         Math.sqrt (         a); }
  public static double  f64_type_tan                (double a          ) { return         Math.tan  (         a); }
  public static double  f64_type_tanh               (double a          ) { return         Math.tanh (         a); }

  public static void fuzion_std_exit (int code) { System.exit(code); }

  public static void fuzion_std_date_time(Object data)
  {
    int[] arg0 = (int[])data;
    var date = new Date();
    var calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
    calendar.setTime(date);
    arg0[0] = calendar.get(Calendar.YEAR);
    arg0[1] = calendar.get(Calendar.DAY_OF_YEAR);
    arg0[2] = calendar.get(Calendar.HOUR_OF_DAY);
    arg0[3] = calendar.get(Calendar.MINUTE);
    arg0[4] = calendar.get(Calendar.SECOND);
    arg0[5] = calendar.get(Calendar.MILLISECOND) * 1000;
  }

  public static int fuzion_sys_net_bind0(int family, int socketType, int protocol, Object host0, Object port0, Object res)
  {
    long[] result = (long[]) res;
    var host = Runtime.utf8ByteArrayDataToString((byte[]) host0);
    var port = Runtime.utf8ByteArrayDataToString((byte[]) port0);

    if (family != 2 && family != 10)
      {
        throw new RuntimeException("NYI");
      }
    try
      {
        return switch (protocol)
          {
            case 6 ->
              {
                var ss = ServerSocketChannel.open();
                ss.bind(new InetSocketAddress(host, Integer.parseInt(port)));
                result[0] = Runtime._openStreams_.add(ss);
                yield 0;
              }
            case 17 ->
              {
                var ss = DatagramChannel.open();
                ss.bind(new InetSocketAddress(host, Integer.parseInt(port)));
                result[0] = Runtime._openStreams_.add(ss);
                yield 0;
              }
            default -> throw new RuntimeException("NYI");
          };
      }
    catch(BindException e)
      {
        result[0] = SystemErrNo.EADDRINUSE.errno;
        return -1;
      }
    catch(IOException e)
      {
        result[0] = -1;
        return -1;
      }
  }

  public static int fuzion_sys_net_listen(long sockfd, int backlog)
  {
    return 0;
  }

  public static boolean fuzion_sys_net_accept(long sockfd, Object res)
  {
    long[] result = (long[]) res;
    try
      {
        var asc = Runtime._openStreams_.get(sockfd);
        if(asc instanceof ServerSocketChannel ssc)
          {
            var socket = ssc.accept();
            result[0] = Runtime._openStreams_.add(socket);
            return true;
          }
        else if(asc instanceof DatagramChannel dc)
          {
            result[0] = sockfd;
            return true;
          }
        throw new RuntimeException("NYI");
      }
    catch(IOException e)
      {
        return false;
      }
  }

  public static int fuzion_sys_net_connect0(int family, int socketType, int protocol, Object host0, Object port0, Object res)
  {
    long[] result = (long[]) res;
    var host = Runtime.utf8ByteArrayDataToString((byte[]) host0);
    var port = Runtime.utf8ByteArrayDataToString((byte[]) port0);
    if (family != 2 && family != 10)
      {
        throw new RuntimeException("NYI");
      }
    try
      {
        return switch (protocol)
          {
            case 6 ->
              {
                var socket = SocketChannel.open();
                socket.connect(new InetSocketAddress(host, Integer.parseInt(port)));
                result[0] = Runtime._openStreams_.add(socket);
                yield 0;
              }
            case 17 ->
              {
                var ss = DatagramChannel.open();
                ss.connect(new InetSocketAddress(host, Integer.parseInt(port)));
                result[0] = Runtime._openStreams_.add(ss);
                yield 0;
              }
            default -> throw new RuntimeException("NYI");
          };
      }
    catch(IOException e)
      {
        result[0] = SystemErrNo.ECONNREFUSED.errno;
        return -1;
      }
  }

  public static int fuzion_sys_net_get_peer_address(long sockfd, Object res)
  {
    byte[] data = (byte[])res;
    try
      {
        if (Runtime._openStreams_.get(sockfd) instanceof SocketChannel sc)
          {
            byte[] address = ((InetSocketAddress)sc.getRemoteAddress()).getAddress().getAddress();
            System.arraycopy(address, 0, data, 0, address.length);
            return address.length;
          }
        return -1;
      }
    catch (IOException e)
      {
        return -1;
      }
  }

  public static char fuzion_sys_net_get_peer_port(long sockfd)
  {
    try
      {
        if (Runtime._openStreams_.get(sockfd) instanceof SocketChannel sc)
          {
            return (char) ((InetSocketAddress)sc.getRemoteAddress()).getPort();
          }
        return 0;
      }
    catch (IOException e)
      {
        return 0;
      }
  }

  public static boolean fuzion_sys_net_read(long sockfd, Object b, int length, Object res)
  {
    byte[] buff = (byte[])b;
    long[] result = (long[])res;

    try
      {
        var desc = Runtime._openStreams_.get(sockfd);
        // NYI blocking / none blocking read
        long bytesRead;
        if (desc instanceof DatagramChannel dc)
          {
            bytesRead = dc.receive(ByteBuffer.wrap(buff)) == null
              ? 0
              // NYI how to determine datagram length?
              : buff.length;
          }
        else if (desc instanceof ByteChannel sc)
          {
            bytesRead = sc.read(ByteBuffer.wrap(buff));
          }
        else
          {
            throw new RuntimeException("NYI");
          }
        result[0] = bytesRead;
        return bytesRead != -1;
      }
    catch(IOException e) //SocketTimeoutException and others
      {
        // unspecified error
        result[0] = -1;
        return false;
      }
  }

  public static int fuzion_sys_net_write(long sockfd, Object fileContent, int l)
  {
    try
      {
        var sc = (ByteChannel)Runtime._openStreams_.get(sockfd);
        sc.write(ByteBuffer.wrap((byte[])fileContent));
        return 0;
      }
    catch(IOException e)
      {
        return -1;
      }
  }

  public static int fuzion_sys_net_close0(long sockfd)
  {
    return Runtime._openStreams_.remove(sockfd)
      ? 0
      : -1;
  }

  public static int fuzion_sys_net_set_blocking0(long sockfd, int blocking)
  {
    var asc = (AbstractSelectableChannel)Runtime._openStreams_.get(sockfd);
    try
      {
        asc.configureBlocking(blocking == 1);
        return 0;
      }
    catch(IOException e)
      {
        return -1;
      }
  }

  public static int fuzion_sys_fileio_flush(long fd)
  {
    var s = Runtime._openStreams_.get(fd);
    if (s instanceof PrintStream ps)
      {
        ps.flush();
      }
    return 0;
  }

  public static int fuzion_sys_fileio_read(long fd, Object d, int l)
  {
    byte[] byteArr = (byte[])d;
    try
      {
        var s = Runtime._openStreams_.get(fd);
        int bytesRead = 0;
        if (s instanceof RandomAccessFile raf)
          {
            bytesRead = raf.read(byteArr);
          }
        else
          {
            bytesRead = ((InputStream) s).read(byteArr);
          }

        return bytesRead;
      }
    catch (Exception e)
      {
        return -2;
      }
  }

  public static int fuzion_sys_fileio_write(long f, Object data, int l)
  {
    try
      {
        byte[] fileContent = (byte[])data;
        var s = Runtime._openStreams_.get(f);
        if (s instanceof RandomAccessFile raf)
          {
            /* NYI:
            if (!ENABLE_UNSAFE_INTRINSICS)
              {
                Errors.fatal("*** error: unsafe feature "+innerClazz+" disabled");
              }
            */
            raf.write(fileContent);
          }
        else if (s instanceof OutputStream os)
          {
            os.write(fileContent);
          }
        return 0;
      }
    catch (IOException e)
      {
        return -1;
      }
  }

  public static boolean fuzion_sys_fileio_delete(Object s)
  {
    Path path = Path.of(Runtime.utf8ByteArrayDataToString((byte[])s));
    try
      {
        return Files.deleteIfExists(path);
      }
    catch (Exception e)
      {
        return false;
      }
  }

  public static boolean fuzion_sys_fileio_move(Object o, Object n)
  {
    Path oldPath = Path.of(Runtime.utf8ByteArrayDataToString((byte[])o));
    Path newPath = Path.of(Runtime.utf8ByteArrayDataToString((byte[])n));
    try
      {
        Files.move(oldPath, newPath);
        return true;
      }
    catch (Exception e)
      {
        return false;
      }
  }

  public static boolean fuzion_sys_fileio_create_dir(Object s)
  {
    Path path = Path.of(Runtime.utf8ByteArrayDataToString((byte[])s));
    try
      {
        Files.createDirectory(path);
        return true;
      }
    catch (Exception e)
      {
        return false;
      }
  }

  public static void fuzion_sys_fileio_open(Object s, Object res, byte mode)
  {
    var path = Runtime.utf8ByteArrayDataToString((byte[])s);
    long[] open_results = (long[])res;
    try
      {
        switch (mode)
          {
          case 0 :
            RandomAccessFile fis = new RandomAccessFile(path, "r");
            open_results[0] = Runtime._openStreams_.add(fis);
            break;
          case 1 :
            RandomAccessFile fos = new RandomAccessFile(path, "rw");
            open_results[0] = Runtime._openStreams_.add(fos);
            break;
          case 2 :
            RandomAccessFile fas = new RandomAccessFile(path, "rw");
            fas.seek(fas.length());
            open_results[0] = Runtime._openStreams_.add(fas);
            break;
          default:
            open_results[1] = -1;
            System.err.println("*** Unsupported open flag. Please use: 0 for READ, 1 for WRITE, 2 for APPEND. ***");
            System.exit(1);
          }
      }
    catch (Exception e)
      {
        open_results[1] = -1;
      }
    return;
  }

  public static byte fuzion_sys_fileio_close(long fd)
  {
    return (byte) (Runtime._openStreams_.remove(fd)
                                    ? 0
                                    : -1);
  }

  public static boolean fuzion_sys_fileio_lstats(Object s, Object stats) // NYI : should be altered in the future to not resolve symbolic links
  {
    return fuzion_sys_fileio_stats(s, stats);
  }

  public static boolean fuzion_sys_fileio_stats(Object s, Object res)
  {
    Path path = Path.of(Runtime.utf8ByteArrayDataToString((byte[])s));
    long[] stats = (long[]) res;
    var err = SystemErrNo.UNSPECIFIED;
    try
      {
        BasicFileAttributes metadata = Files.readAttributes(path, BasicFileAttributes.class);
        stats[0] = metadata.size();
        stats[1] = metadata.lastModifiedTime().to(TimeUnit.SECONDS);
        stats[2] = metadata.isRegularFile() ? 1: 0;
        stats[3] = metadata.isDirectory() ? 1: 0;
        return true;
      }
    catch (UnsupportedOperationException e)
      {
        err = SystemErrNo.ENOTSUP;
      }
    catch (IOException e)
      {
        err = SystemErrNo.EIO;
      }
    catch (SecurityException e)
      {
        err = SystemErrNo.EACCES;
      }

    stats[0] = err.errno;
    stats[1] = 0;
    stats[2] = 0;
    stats[3] = 0;
    return false;
  }

  public static void fuzion_sys_fileio_seek(long fd, short s, Object res)
  {
    long[] seekResults = (long[]) res;
    try
      {
        var raf = (RandomAccessFile) Runtime._openStreams_.get(fd);
        raf.seek(s);
        seekResults[0] = raf.getFilePointer();
        return;
      }
    catch (Exception e)
      {
        seekResults[1] = -1;
        return;
      }
  }

  public static void fuzion_sys_fileio_file_position(long fd, Object res)
  {
    long[] arr = (long[])res;
    try
      {
        arr[0] = ((RandomAccessFile) Runtime._openStreams_.get(fd)).getFilePointer();
        return;
      }
    catch (Exception e)
      {
        arr[1] = -1;
        return;
      }
  }

  public static byte[] fuzion_sys_fileio_mmap(long fd, long offset, long size, Object res)
  {
    int[] result = (int[])res;
    try
      {
        var raf = (RandomAccessFile) Runtime._openStreams_.get(fd);

        // offset+size must not exceed file size, to match semantics of
        // c-backend.
        if (raf.length() < (offset + size))
          {
            result[0] = -1;
            return new byte[0];
          }

        var mmap = raf.getChannel().map(MapMode.READ_WRITE, offset, size);

        // success
        result[0] = 0;
        return new byte[0];
        /* NYI
                  @Override
                  void set(
                    int x,
                    Value v,
                    AbstractType elementType)
                  {
                    checkIndex(x);
                    mmap.put(x, (byte)v.u8Value());
                  }

                  @Override
                  Value get(
                    int x,
                    AbstractType elementType)
                  {
                    checkIndex(x);
                    return new u8Value(mmap.get(x));
                  }

                  @Override
                  int length(){
                    return (int)size;
                  }
        */
      }
    catch (IOException e)
      {
        result[0] = -1;
        return new byte[0];
      }
  }

  public static int fuzion_sys_fileio_munmap()
  {
    return 0;
  }

  public static void fuzion_std_nano_sleep(long d)
  {
    try
      {
        TimeUnit.NANOSECONDS.sleep(d < 0 ? Long.MAX_VALUE: d);
      }
    catch (InterruptedException ie)
      {
        throw new Error("unexpected interrupt", ie);
      }
  };

  public static boolean fuzion_sys_env_vars_has0(Object s)
  {
    return System.getenv(Runtime.utf8ByteArrayDataToString((byte[])s)) != null;
  }

  public static String fuzion_sys_env_vars_get0(Object s)
  {
    return System.getenv(Runtime.utf8ByteArrayDataToString((byte[])s));
  }

}

/* end of file */
