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

> generate 3 more queries including filters with inequality operators (e.g. >=, <), filter some of the datetime objexes, which are availabilityStart, availabilityEnd, outOfOfficeStart, outOfOfficeEnd, and bookingTime

> generate a query which might return a large volume of data, either using many arrays, few/no filters, etc.

> use potentially many filters