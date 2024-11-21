pg_dump postgresql://postgres:postgres@localhost:6432/datastore -a -t case_data  | psql 'postgresql://postgres:postgres@localhost:6432/nfd'
