metadata smithy4sErrorsAsScala3Unions = true

namespace com.sakisk.absence_checker

use alloy#simpleRestJson
use com.sakisk.absence_checker.types#AbsenceId
use com.sakisk.absence_checker.types#Absence
use smithy4s.meta#packedInputs

@simpleRestJson
service AbsenceCheckerService {
  version: "0.1.0"
  operations: [InsertAbsence, ListAbsences, GetAbsence, DeleteAbsence]
}

@http(method: "PUT", uri: "/absences", code: 201)
@idempotent
@packedInputs
operation InsertAbsence {
  input: Absence
  errors: [AbsenceNotValid]
}

@http(method: "GET", uri: "/absences", code: 200)
operation ListAbsences {
  output: ListAbsencesOutput
}


@http(method: "GET", uri: "/absences/{id}", code: 200)
operation GetAbsence {
  input: AbsenceIdFilter
  output: Absence
  errors: [AbsenceNotFound]
}

@http(method: "DELETE", uri: "/absences/{id}", code: 204)
@idempotent
operation DeleteAbsence {
  input: AbsenceIdFilter
}

structure AbsenceIdFilter {
  @required
  @httpLabel
  id: AbsenceId
}

set AbsenceSet {
  member: Absence
}

structure ListAbsencesOutput {
  @required
  absences: AbsenceSet
}

@error("client")
@httpError(404)
structure AbsenceNotFound {}

@error("client")
@httpError(422)
structure AbsenceNotValid {
  @required
  reason: String
}
