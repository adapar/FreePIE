using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices;
using FreePIE.Core.Contracts;
using FreePIE.Core.Plugins.Strategies;
using SlimDX.DirectInput;
using SlimDX.RawInput;

namespace FreePIE.Core.Plugins
{

    [GlobalType(Type = typeof(CursorGlobal))]
    public class CursorPlugin : Plugin
    {
        // Cursor position state variables
        private int posXOut;
        private int posYOut;

        public override object CreateGlobal()
        {
            return new CursorGlobal(this);
        }

        public override Action Start()
        {
            IntPtr handle = Process.GetCurrentProcess().MainWindowHandle;

            CursorPosIO.ClipCursor();

            OnStarted(this, new EventArgs());
            return null;
        }

        public override void Stop()
        {

        }
        
        public override string FriendlyName
        {
            get { return "Cursor"; }
        }

        public override void DoBeforeNextExecute()
        {
            // If a cursor command was given in the script, issue it all at once right here
            if ((int)posXOut != 0 || (int)posYOut != 0)
            {
                CursorPosIO.SetCursorPos(posXOut, posYOut);

                // Reset the cursor values
                if (posXOut != 0)
                {
                    posXOut = 0;
                }
                if (posYOut != 0)
                {
                    posYOut = 0;
                }
            }
        }

        public int PosX
        {
            set
            {
                posXOut = posXOut + value;
            }

            get {
                return (int)CursorPosition.x;
            }
        }

        public int PosY
        {
            set
            {
                posYOut = posYOut + value;
            }

            get
            {
                return (int)CursorPosition.y;
            }
        }

        private CursorPosIO.POINT CursorPosition
        {
            get
            {
                CursorPosIO.POINT cursorPos;
                CursorPosIO.GetCursorPos(out cursorPos);
                return cursorPos;
            }
        }
    }

    [Global(Name = "cursor")]
    public class CursorGlobal
    {
        private readonly CursorPlugin plugin;

        public CursorGlobal(CursorPlugin plugin)
        {
            this.plugin = plugin;
        } 

        public int posX
        {
            get { return plugin.PosX; }
            set { plugin.PosX = value; }
        }

        public int posY
        {
            get { return plugin.PosY; }
            set { plugin.PosY = value; }
        }
    }
}
