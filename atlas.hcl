env "local" {
  src = "file://db/schema.hcl"

  url = "postgres://absense_checker:pass@localhost:5342/absense_checker?search_path=public&sslmode=disable"

  test = "postgres://absense_checker:pass@localhost:5342/absense_checker_test?search_path=public&sslmode=disable" 
}
