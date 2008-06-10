/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

// A sample program showing how to obtain tablet event information in Java on Max OS X

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TabletTest {

    public TabletTest() {
    }
    
    // A main program for testing
    public static void main (String args[]) {
        TabletWrapper tabletWrapper = new TabletWrapper();

        TabletListener listener = new TabletListener() {
            public void tabletEvent( TabletEvent e ) {
                System.out.println( e );
            }
            
            public void tabletProximity( TabletProximityEvent e ) {
            }
        };

        tabletWrapper.addTabletListener( listener );
        Frame f = new Frame();
        Canvas c = new Canvas();
        c.setBackground( Color.red );
        f.add( c );
        f.setSize( 300, 300 );

        f.show();
    }

}
/*
public class TabletTest extends Frame {

    public TabletTest() {
        addWindowListener(
            new WindowAdapter() {
                public void windowOpened( WindowEvent e ) {
                    TabletWrapper tabletWrapper = new TabletWrapper();

                    TabletListener listener = new TabletListener() {
                        public void tabletEvent( TabletEvent e ) {
                            System.out.println( e );
                        }
                        
                        public void tabletProximity( TabletProximityEvent e ) {
                        }
                    };

                    tabletWrapper.addTabletListener( listener );
                }
            }
        );
    }

    // A main program for testing
    public static void main (String args[]) {
        TabletTest f = new TabletTest();
        Canvas c = new Canvas();
        c.setBackground( Color.red );
        f.add( c );
        f.setSize( 300, 300 );

        f.show();
    }

}
*/
