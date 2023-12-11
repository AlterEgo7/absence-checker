namespace com.sakisk.absense_checker.types

use alloy#uuidFormat
use smithy4s.meta#typeclass

@trait
@typeclass(targetType: "cats.Show", interpreter: "smithy4s.interopcats.SchemaVisitorShow")
structure show {}

@uuidFormat
string TripId

string TripName

timestamp TripStartTime

timestamp TripEndTime

@show
structure Trip {
  @required
  id: TripId

  @required
  @timestampFormat("date-time")
  start: TripStartTime

  @required
  @timestampFormat("date-time")
  end: TripEndTime

  @required
  name: TripName
}
