schema "public" {}

table "trips" {
  schema = schema.public
  column "id" {
    type = uuid
    null = false
  }
  column "start" {
    type = timestamptz
    null = false
  }
  column "end" {
    type = timestamptz
    null = false
  }
  column "name" {
    type = text
    null = false
  }

  primary_key {
    columns = [column.id]
  }

  index "end_time" {
    on {
      column = column.end
    }
  }
}