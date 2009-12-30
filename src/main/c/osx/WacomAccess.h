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

#import <jni.h>
#import <Cocoa/Cocoa.h>
#import <objc/runtime.h>

#define kWacomDriverSig		'WaCM'
#define cContext			'CTxt'
#define pContextTypeBlank	'Blnk'

#define cTabletEvent		'TblE'

#define kAEWacomSuite		'Wacm'
#define eSendTabletEvent	'WSnd'
#define eEventProximity		'WePx'
#define eEventPointer		'WePt'
#define kDefaultTimeOut		15  //Timeout value in ticks Approx 1/4 second

OSErr ResendLastTabletEventofType(DescType tabletEventType);