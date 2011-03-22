/* [{
Copyright 2007, 2008 Nicolas Carranza <nicarran at gmail.com>

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
package jpen.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import jpen.PenProvider;
import jpen.PenDevice;
import jpen.PenManager;

public abstract class AbstractPenProvider
	implements PenProvider {
	private final Constructor constructor;
	protected final List<PenDevice> devices=new ArrayList<PenDevice>();
	private final List<PenDevice> devicesA=Collections.unmodifiableList(devices);

	protected AbstractPenProvider(Constructor constructor) {
		this.constructor=constructor;
	}

	//@Override
	public Collection<PenDevice> getDevices() {
		return devicesA;
	}

	public final PenManager getPenManager() {
		return getConstructor().getPenManager();
	}

	//@Override
	public final Constructor getConstructor() {
		return constructor;
	}
	
	//@Override
	public boolean getUseRelativeLocationFilter(){
		return false;
	}

	//@Override
	public String toString() {
		return "[PenProvider: constructor.name="+getConstructor().getName()+"]";
	}
	
	public static abstract class AbstractConstructor
		implements PenProvider.Constructor{
		private PenManager penManager;
		private PenProvider constructed;
		private ConstructionException constructionException;

		//@Override
		public PenManager getPenManager(){
			return penManager;
		}

		//@Override
		public ConstructionException getConstructionException(){
			return constructionException;
		}

		//@Override
		public PenProvider getConstructed(){
			return constructed;
		}

		//@Override
		public final boolean construct(PenManager penManager){
			if(this.penManager!=null)
				throw new IllegalStateException("constructor already used by PenManager");
			this.penManager=penManager;
			try{
				checkExpectedNativeBuild();
				this.constructed=constructProvider();
			}catch(Throwable t){
				this.constructionException=new ConstructionException(t);
				return false;
			}
			return true;
		}
		
		protected abstract PenProvider constructProvider() throws Throwable;
		
		private void checkExpectedNativeBuild() throws IllegalStateException{
			if(getExpectedNativeBuild()>getNativeBuild())
				throw new IllegalStateException("expectedNativeBuild number ("+getExpectedNativeBuild()+") is greater than library's nativeBuild number ("+getNativeBuild()+")");
		}
		
		//@Override
		public int getNativeVersion(){
			return -1;
		}
		//@Override
		public int getNativeBuild(){
			return -1;
		}
		//@Override
		public int getExpectedNativeBuild(){
			return -1;
		}
	}
}
