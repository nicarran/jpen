/* [{
 Copyright 2009 Marcello Bastea-Forte <marcello at cellosoft.com>
 
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


#import "WacomAccess.h"

// the following code is word-for-word from here:
//  http://www.wacomeng.com/devsupport/downloads/mac/macosx/EN0056-NxtGenImpGuideX.pdf (page 25)
//  on http://www.wacomeng.com/devsupport/mac.html

//////////////////////////////////////////////////////////////////////////////
// ResendLastTabletEventofType
// parameters:
//            DescType tabletEventType - eEventProximity, eEventPointer
// returns: noErr on success, else an AE error code
//////////////////////////////////////////////////////////////////////////////
OSErr	ResendLastTabletEventofType(DescType tabletEventType)
{
	OSType			tdSig = kWacomDriverSig;
	AEDesc			driverTarget;
	AppleEvent		aeSend;
	OSErr			err;
	
	// Create the Target this Apple Event is to be sent to (The Tablet Driver)
	AEInitializeDesc(&driverTarget);
	err = AECreateDesc(typeApplSignature,
					   (Ptr) &tdSig,
					   sizeof(tdSig),
					   &driverTarget);
	if(err)
	{
		AEDisposeDesc(&driverTarget);
		return err;
	}
	
	err = AECreateAppleEvent(kAEWacomSuite,		// Create a special Wacom Event
							 eSendTabletEvent,   // Send Last Tablet Event
							 &driverTarget,
							 kAutoGenerateReturnID,
							 kAnyTransactionID,
							 &aeSend);
	if(err)
	{
		AEDisposeDesc(&driverTarget);
		return err;
	}
	
	err = AEPutParamPtr ( &aeSend, keyAEData,
						 typeEnumeration,
						 &tabletEventType,
						 sizeof(tabletEventType)); // Add what type of event to send.
	
	// Finally send the event
	err = AESend(&aeSend,	// The complete AE we created above
				 NULL,
				 kAEWaitReply,
				 kAEHighPriority,
				 kDefaultTimeOut,
				 NULL,
				 NULL);
	
	AEDisposeDesc(&aeSend);
	
	return err;
}


