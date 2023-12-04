namespace com.sakisk.absense_checker.types

use alloy#uuidFormat


@uuidFormat
string TripId

@length(min: 1)
string TripName

timestamp TripStartTime

timestamp TripEndTime

structure Trip {
  @required
  id: TripId

  @required
  @timestampFormat("date-time")
  start: TripStartTime

  @required
  @timestampFormat("date-time")
  end: TripEndTime

  name: TripName
}
