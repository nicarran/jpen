/* [{
Copyright 2008 2009 Max Berger <maxberger at users.sourceforge.net>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.test;

import javax.swing.JComponent;
import javax.swing.JPanel;
import jpen.PenManager;
import jpen.PenProvider;
import junit.framework.TestCase;

public class SmokeTest extends TestCase {

	public void testPenManager() throws Exception {
		JComponent component = new JPanel();
		PenManager penManager = new PenManager(component);
		System.out.println("Providers:");
		int count=0;
		for(PenProvider.Constructor constructor: penManager.getProviderConstructors()){
			System.out.println("Constructor: "+constructor.getName());
			count++;
			PenProvider.ConstructionException ex=constructor.getConstructionException();
			if(ex!=null){
				ex.printStackTrace();
				fail(ex.getMessage());
			}
		}
		assertTrue(count>=2);
	}

}
