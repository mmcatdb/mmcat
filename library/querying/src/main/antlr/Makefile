.PHONY: antlr-generate

# Generate an up-to-date parset, lexer and visitor from the Querycat.g4 grammar
antlr-generate:
	antlr4 -Dlanguage=Java -visitor -Xexact-output-dir -Werror \
	-o ../java/cz/matfyz/querying/parser/antlr4generated \
	-package cz.matfyz.querying.parser.antlr4generated Querycat.g4
	