env "local" {
  src = "file://db/local/schema.hcl"

  url = "postgres://absense_checker:pass@localhost:5432/absense_checker?search_path=public&sslmode=disable"

  dev = "docker://postgres/15/dev?search_path=public"
}