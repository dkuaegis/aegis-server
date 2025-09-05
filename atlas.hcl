data "external_schema" "hibernate" {
    program = [
        "./gradlew",
        "-q",
        "schema",
        "--properties", "schema-export.properties"
    ]
}

env "hibernate" {
  src = data.external_schema.hibernate.url
  dev = "docker://postgres/17-alpine/dev?search_path=public"
  migration {
    dir = "file://migrations"
  }
  format {
    migrate {
      diff = "{{ sql . \"  \" }}"
    }
  }
  exclude = ["atlas_schema_revisions"]
}
