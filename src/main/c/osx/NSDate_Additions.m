// based on code from http://stackoverflow.com/questions/1597383/cgeventtimestamp-to-nsdate/1768504#1768504

#import "NSDate_Additions.h"
#include <assert.h>
#include <CoreServices/CoreServices.h>
// these two fail when compiling with 10.6, for some reason
#include <mach/mach.h>
#include <mach/mach_time.h>
#include <unistd.h>

#if MAC_OS_X_VERSION_MAX_ALLOWED == MAC_OS_X_VERSION_10_5
@interface NSProcessInfo (SnowLeopard)
- (NSTimeInterval)systemUptime;
@end

@interface NSDate (SnowLeopard)
- (id)dateByAddingTimeInterval:(NSTimeInterval)seconds;
@end
#endif


// Boosted from Apple sample code
uint64_t UpTimeInNanoseconds(void)
{
    uint64_t        time;
    uint64_t        timeNano;
    static mach_timebase_info_data_t    sTimebaseInfo;
	
    time = mach_absolute_time();
	
    // Convert to nanoseconds.
	
    // If this is the first time we've run, get the timebase.
    // We can use denom == 0 to indicate that sTimebaseInfo is
    // uninitialised because it makes no sense to have a zero
    // denominator is a fraction.
	
    if ( sTimebaseInfo.denom == 0 ) {
        (void) mach_timebase_info(&sTimebaseInfo);
    }
	
    // Do the maths.  We hope that the multiplication doesn't
    // overflow; the price you pay for working in fixed point.
	
    timeNano = time * sTimebaseInfo.numer / sTimebaseInfo.denom;
	
    return timeNano;
}


@implementation NSDate (NSDate_Additions)

+(NSTimeInterval) timeIntervalSinceSystemStartup
{
    NSTimeInterval interval;
    SInt32 sysVersion;
	
    Gestalt( gestaltSystemVersion, &sysVersion );
    if( sysVersion >= 0x1060 )
        interval = [[NSProcessInfo processInfo] systemUptime];
    else
        interval = UpTimeInNanoseconds() / 1000000000.0;
	
    return( interval );
}

-(NSTimeInterval) timeIntervalSinceSystemStartup
{
    return( [self timeIntervalSinceDate:[NSDate dateOfSystemStartup]] );
}

-(NSDate *) initWithDateOfSystemStartup
{
	
    NSTimeInterval interval;
    SInt32 sysVersion;
	
    Gestalt( gestaltSystemVersion, &sysVersion );
    if( sysVersion >= 0x1060 ) {
		NSProcessInfo *info = [NSProcessInfo processInfo];
		return [self initWithTimeIntervalSinceNow:-[info systemUptime]];
    }
	return [self initWithTimeIntervalSinceNow:UpTimeInNanoseconds() * -1e-9];
}

+(NSDate *) dateOfSystemStartup
{
    return( [NSDate dateWithTimeIntervalSinceNow:-([NSDate timeIntervalSinceSystemStartup])] );
}

@end
