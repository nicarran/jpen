// based on http://stackoverflow.com/questions/1597383/cgeventtimestamp-to-nsdate/1601620#1601620

#import <Foundation/Foundation.h>
@interface NSDate (NSDate_Additions)

+(NSTimeInterval) timeIntervalSinceSystemStartup;
-(NSTimeInterval) timeIntervalSinceSystemStartup;
+(NSDate *) dateOfSystemStartup;
-(NSDate *) initWithDateOfSystemStartup;

@end

