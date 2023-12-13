namespace com.sakisk.absence_checker.types

use alloy#uuidFormat
use smithy4s.meta#typeclass

@trait
@typeclass(targetType: "cats.Show", interpreter: "smithy4s.interopcats.SchemaVisitorShow")
structure show {}

@uuidFormat
string AbsenceId

string AbsenceName

timestamp AbsenceStartTime

timestamp AbsenceEndTime

@show
structure Absence {
  @required
  id: AbsenceId

  @required
  @timestampFormat("date-time")
  start: AbsenceStartTime

  @required
  @timestampFormat("date-time")
  end: AbsenceEndTime

  @required
  name: AbsenceName
}
