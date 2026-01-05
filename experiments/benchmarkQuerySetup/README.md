# How to use LLMs to generate queries for tests

Concatenate the contents of `./mmql-explanation.txt` (which explains the language and shows a schema for cal.com) with one of the following phrases:

> Generate a simple MMQL query for benchmarking.

> Generate an MMQL query for performance benchmarking.

> Generate an interesting and elaborate MMQL query for performance benchmarking.

> Generate an interesting and elaborate MMQL query for performance benchmarking, potentially including multiple filters, and a complex (but not nested) result structure with many patterns and/or arrays of scalar values.

Then also append:

> Do not explain the query, only output one MMQL query. Use the above described schema category.

(To save on tokens, you can tell it to generate more queries at once).

---
Other stuff (not yet refined)

> Generate 10 simple MMQL queries for benchmarking. Include a filter. Use only arrays of scalar values, and do not use nested records.

> Generate 10 interesting and elaborate MMQL queries for benchmarking, potentially including large pattern matches. Include a filter. Use only arrays of scalar values, and do not use nested records.

> Generate 10 interesting and elaborate MMQL queries for benchmarking, potentially including a complex result with many pattern matches. Include a filter. Use only arrays of scalar values, and do not use nested records.

> Generate 10 interesting and elaborate MMQL queries for benchmarking. Include multiple filters, and potentially also filters with inequality operators (e.g. >=, <) on some of the datetime objexes, which are availabilityStart, availabilityEnd, outOfOfficeStart, outOfOfficeEnd, and bookingTime. Use only arrays of scalar values, and do not use nested records.
