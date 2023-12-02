metadata smithy4sErrorsAsScala3Unions = true

namespace com.sakisk.absense_checker

use alloy#simpleRestJson
use com.sakisk.absense_checker.types#TripId
use com.sakisk.absense_checker.types#Trip

@simpleRestJson
service AbsenseCheckerService {
  version: "0.1.0"
  operations: [InsertTrip, ListTrips, GetTrip]
}

@http(method: "PUT", uri: "/trips", code: 201)
@idempotent
operation InsertTrip {
  input: Trip
  errors: [TripNotValid]
}

@http(method: "GET", uri: "/trips", code: 200)
operation ListTrips {
  output: ListTripsOutput
}


@http(method: "GET", uri: "/trips/{id}", code: 200)
operation GetTrip {
  input: TripIdFilter
  output: Trip
  errors: [TripNotFound]
}

@http(method: "DELETE", uri: "/trips/{id}", code: 204)
@idempotent
operation DeleteTrip {
  input: TripIdFilter
  errors: [TripNotFound]
}

structure TripIdFilter {
  @required
  @httpLabel
  id: TripId
}

set TripSet {
  member: Trip
}

structure ListTripsOutput {
  @required
  trips: TripSet
}

@error("client")
@httpError(404)
structure TripNotFound {}

@error("client")
@httpError(422)
structure TripNotValid {
  @required
  reason: String
}
