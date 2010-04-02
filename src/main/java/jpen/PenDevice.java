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
package jpen;

import java.util.Collection;

public interface PenDevice {

	PenProvider getProvider();

	int getKindTypeNumber();

	void setKindTypeNumber(int kindTypeNumber);

	void setEnabled(boolean enabled);

	boolean getEnabled();

	String getName();
	/**
	@return A unique and constant id. This id is assigned at runtime by the {@link PenManager} and can change between restarts.
	
	@see #getPhysicalId()
	*/
	byte getId();
	
	/**
	Don't call this method. It is only for use by the {@link PenManager}. This method is called when the device is being constructed to set a meaningful {@code id}.
	
	@see PLevelEvent#getDeviceId()
	*/
	void penManagerSetId(byte id);
	
	/**
	@return A unique and constant id. This {@code physicalId} is always the same for the given device, even if the program restarts. Each PenDevice has a different {@code physicalId}.  
	*/
	String getPhysicalId();
	
	/**
	@return {@code true} if this device uses fractional (floating point precision) movement levels.
	*/
	boolean getUseFractionalMovements();
	
	/**
	Don't call this method. It is only for use by the {@link PenManager}. This method is called when the provider {@link PenProvider#getUseRelativeLocationFilter()} is {@code true} and this {@code PenDevice} must change its {@code useFractionalMovement} mode.
	
	@see #getUseFractionalMovements()
	*/
	void penManagerSetUseFractionalMovements(boolean useFractionalMovements);
	
}
