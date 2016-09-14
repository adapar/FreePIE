// The CursorPosIO class encapsulates the Win32 functions ClipCursor(),
// SetCursorPos()

using System;
using System.Runtime.InteropServices;

namespace FreePIE.Core.Plugins
{
    class CursorPosIO
    {
        [StructLayout(LayoutKind.Sequential)]
        public struct RECT
        {
            public long left;
            public long top;
            public long right;
            public long bottom;
        }

        [StructLayout(LayoutKind.Sequential)]
        public struct POINT
        {
            public long x;
            public long y;
        }

        [DllImport("user32.dll", SetLastError = true)]
        public static extern uint ClipCursor();

        [DllImport("user32.dll", SetLastError = true)]
        public static extern bool SetCursorPos(int X, int Y);

        [DllImport("user32.dll", SetLastError = true)]
        public static extern bool GetCursorPos(out POINT lpPoint);
    }
}